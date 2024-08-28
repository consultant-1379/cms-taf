package com.ericsson.oss.cms.test.operators;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import static com.ericsson.oss.cms.test.constants.CmsConstants.NETSIM_CMD_EXEC_SUCCESS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.FdnConstants.MANAGED_ELEMENT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.GENERATION_COUNTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.NE_TYPE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_SYNCHRONIZED;
import static com.ericsson.oss.taf.nodeOperator.nodeFiltering.AtLeastXChildMOsOfTypeFilter.atLeast;
import static com.ericsson.oss.taf.nodeOperator.nodeFiltering.ChildTypeFilterGroup.childTypeFiltering;
import static com.ericsson.oss.taf.nodeOperator.nodeFiltering.ExactlyXOfTypeFilter.exactly;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.model.attributes.NeDetails;
import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.cms.test.util.Lists;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.nodeOperator.NodeConstants;
import com.ericsson.oss.taf.nodeOperator.nodeFiltering.ChildTypeFilter;
import com.ericsson.oss.taf.nodeOperator.nodeFiltering.ChildTypeFilterGroup;
import com.ericsson.oss.taf.nodeOperator.nodeFiltering.NodeSelectionFilter;

@Operator(context = Context.CLI)
public class NodeCliOperator implements NodeOperator {

    @Inject
    private MoCliOperator moOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = Logger.getLogger(this.getClass());

    private final NetSimCommandHandler netsimCmdHandler = NetSimCommandHandler.getInstance();

    @Override
    public Fdn getSynchedNode(final String nodeType) {
        final int neType = NeType.getNeTypeValue(nodeType);
        logger.info("Looking for Nodes with neType of: " + neType);

        final Filter filter = SimpleFilterBuilder.builder().attr(NE_TYPE).equalTo(neType).and().attr(SYNCH_STATUS).equalTo(SYNCH_STATUS_SYNCHRONIZED).build();
        final List<Fdn> listOfActiveNodes = csHandler.getByType(MECONTEXT, filter);
        final List<Fdn> activeNodeFdns = Lists.reduce(listOfActiveNodes, 0);

        if (activeNodeFdns.size() > 0) {
            return activeNodeFdns.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Fdn getSyncedNode(final CSHandler csHandler, final String nodeType, final NodeSelectionFilter... childTypeFilterGroup) {
        for (final Fdn node : findSynchedNodes(csHandler, nodeType)) {
            boolean validNode = true;
            for (final NodeSelectionFilter selector : childTypeFilterGroup) {
                if (!selector.isPermitted(node, csHandler)) {
                    validNode = false;
                    break;
                }
            }
            if (validNode) {
                return node;
            }
        }
        logger.info("Failed to find Synched node and additional Filter mo of node type = " + nodeType);
        return null;
    }

    @Override
    public List<Fdn> getListOfSyncedNode(final CSHandler csHandler, final String nodeType, final NodeSelectionFilter... childTypeFilterGroup) {
        final List<Fdn> synchedNodes = new ArrayList<Fdn>();
        for (final Fdn node : findSynchedNodes(csHandler, nodeType)) {
            boolean validNode = true;
            for (final NodeSelectionFilter selector : childTypeFilterGroup) {
                if (!selector.isPermitted(node, csHandler)) {
                    validNode = false;
                    break;
                }
            }
            if (validNode) {
                synchedNodes.add(node);
            }
        }
        return synchedNodes;
    }

    @Override
    public List<Fdn> getPercentageSynchedNode(final List<Fdn> syncNode, final int percentage) {
        final List<Fdn> synchedNodes = selectRandomSynchedNodes(syncNode, percentage);
        return synchedNodes;
    }

    @Override
    public Fdn getMimScopedSynchedNode(final String nodeType) {
        final String functionType = NeDetails.getNeFunctionMo(nodeType);
        final String mimName = NeDetails.getNeMimName(nodeType);

        final List<Fdn> listNodes = csHandler.getMeContextByMim(functionType, mimName);

        for (int i = 0; i < listNodes.size(); i++) {
            final List<Fdn> activeNodeFdns = Lists.reduce(listNodes, 0);
            if (activeNodeFdns.size() > 0) {
                if (isNodeSynched(activeNodeFdns.get(0))) {
                    return activeNodeFdns.get(0);
                }
            }
        }
        return null;

    }

    @Override
    public List<Fdn> getPercentageMimScopedSynchedNode(final String nodeType, final int percentage) {

        final String functionType = NeDetails.getNeFunctionMo(nodeType);
        final String mimName = NeDetails.getNeMimName(nodeType);

        final List<Fdn> listNodes = csHandler.getMeContextByMim(functionType, mimName);
        final List<Fdn> synchedNodes = selectRandomSynchedNodes(listNodes, percentage);

        return synchedNodes;
    }

    @Override
    public Fdn getSpecifiedSynchedNode(final String nodeFdn) {
        final Fdn fdn = new Fdn(nodeFdn);
        if (isNodeSynched(fdn)) {
            return fdn;
        }
        return null;
    }

    @Override
    public List<Fdn> getPercentageOfSyncedNodes(final String nodeType, final int percentage, final String nodeFdn) {
        List<Fdn> list;

        if (nodeFdn == null || nodeFdn.trim().isEmpty()) {
            list = findSynchedNodes(nodeType, percentage);
        } else {
            logger.info("Skipping node discovery, using provided node Fdn instead");
            list = addNodeIfSynched(nodeFdn);
        }
        return list;
    }

    private List<Fdn> addNodeIfSynched(final String nodeFdn) {
        final List<Fdn> list = new ArrayList<>();
        final Fdn fdn = new Fdn(nodeFdn);
        if (isNodeSynched(fdn)) {
            list.add(fdn);
        }
        return list;
    }

    private boolean isNodeSynched(final Fdn fdn) {
        final int synchStatus = Integer.parseInt(csHandler.getAttributeValue(fdn, SYNCH_STATUS));
        logger.info("Node " + fdn + "synced status is: " + synchStatus);
        return synchStatus == SYNCH_STATUS_SYNCHRONIZED;
    }

    private List<Fdn> findSynchedNodes(final String nodeType, final double percentage) {
        final int neType = NeType.getNeTypeValue(nodeType);
        logger.info("Looking for Nodes with neType of: " + neType);

        final Filter filter = SimpleFilterBuilder.builder().attr(NE_TYPE).equalTo(neType).and().attr(SYNCH_STATUS).equalTo(SYNCH_STATUS_SYNCHRONIZED).build();
        final List<Fdn> listOfActiveNodes = csHandler.getByType(MECONTEXT, filter);
        return Lists.reduce(listOfActiveNodes, percentage);
    }

    private List<Fdn> findSynchedNodes(final CSHandler csHandler, final String nodeType) {
        logger.info("Looking for Nodes with neType of '" + nodeType + "' (" + nodeType + ")");
        final int neType = NeType.getNeTypeValue(nodeType);
        final int nodeSyncStatus = NodeConstants.MECONTEXT_SYNCH_STATUS_SYNCHRONIZED;
        final Filter filter = SimpleFilterBuilder
                .builder()
                .attr(NodeConstants.MECONTEXT_NE_TYPE)
                .equalTo(neType)
                .and()
                .attr(NodeConstants.MECONTEXT_SYNCH_STATUS)
                .equalTo(nodeSyncStatus)
                .build();
        final List<Fdn> listOfActiveNodes = csHandler.getByType(NodeConstants.MECONTEXT, filter);
        logger.info("Found " + listOfActiveNodes.size() + " nodes of type '" + nodeType + "' (" + nodeType + ")");
        logger.info("listOfActiveNodes = " + listOfActiveNodes.toString());
        Collections.shuffle(listOfActiveNodes);
        return listOfActiveNodes;
    }

    @Override
    public Fdn getChildMoFromSyncedNode(final String nodeType, final String moType) {

        final List<Fdn> moFdnList = getChildrenFromSyncedNode(nodeType, moType);

        if (moFdnList.size() > 0) {
            final Fdn moFdn = moFdnList.get(0);
            logger.info("Selected MO for this run: " + moFdn);
            return moFdn;
        }
        return null;
    }

    @Override
    public List<Fdn> getChildrenFromSyncedNode(final String nodeType, final String... moTypes) {

        final List<Fdn> listOfActiveNodes = findSynchedNodes(nodeType, 100);

        for (final Fdn nodeFdn : listOfActiveNodes) {
            final List<Fdn> moFdnList = getChildrenFromSameNode(nodeFdn, moTypes);
            if (moFdnList.size() > 0) {
                return moFdnList;
            }
        }

        logger.info("Provided moTypes not found on available nodes.");
        return emptyList();
    }

    @Override
    public List<Fdn> getChildrenFromSyncedNodeFiltering(final Fdn nodeFdn, final String... moTypes) {
        final List<Fdn> moFdnList = getChildrenFromSameNode(nodeFdn, moTypes);
        if (moFdnList.size() > 0) {
            return moFdnList;
        }
        logger.info("Provided moTypes not found on available nodes.");
        return emptyList();

    }

    public static ChildTypeFilterGroup buildNodeFiltering(final String additionalNodeChildrenFiltering, final int numCells, final String... filterMoType) {
        final List<ChildTypeFilter> childFilters = new ArrayList<>();
        for (final String moFilterTypeCount : filterMoType) {
            if (moFilterTypeCount != null) {
                childFilters.add(atLeast(numCells, moFilterTypeCount));
            }
        }
        for (final String typeCountPair : additionalNodeChildrenFiltering.split("&&")) {
            final String[] split = typeCountPair.trim().split("==");
            final String moType = split[0].trim();
            final int moCount = Integer.parseInt(split[1].trim());
            childFilters.add(exactly(moCount, moType));
        }
        return childTypeFiltering(childFilters.toArray(new ChildTypeFilter[childFilters.size()]));
    }

    @Override
    public Fdn getChildMoFromMimScopedSyncedNode(final String nodeType, final String moType) {

        final List<Fdn> moFdnList = getChildrenFromMimScopedSyncedNode(nodeType, moType);

        if (moFdnList.size() > 0) {
            final Fdn moFdn = moFdnList.get(0);
            logger.info("Selected MO for this run: " + moFdn);
            return moFdn;
        }
        return null;
    }

    @Override
    public List<Fdn> getChildrenFromMimScopedSyncedNode(final String nodeType, final String... moTypes) {

        final List<Fdn> listOfActiveNodes = getPercentageMimScopedSynchedNode(nodeType, 100);

        for (final Fdn nodeFdn : listOfActiveNodes) {
            final List<Fdn> moFdnList = getChildrenFromSameNode(nodeFdn, moTypes);
            if (moFdnList.size() > 0) {
                return moFdnList;
            }
        }

        logger.info("Provided moTypes not found on available nodes.");
        return emptyList();
    }

    private List<Fdn> getChildrenFromSameNode(final Fdn nodeFdn, final String... moTypes) {

        final List<Fdn> moFdnList = new ArrayList<>();

        for (final String moType : moTypes) {
            final Fdn moFdn = moOperator.getChildMoFdn(nodeFdn, moType);
            if (moFdn != null) {
                moFdnList.add(moFdn);
            } else {
                return emptyList();
            }
        }
        return moFdnList;
    }

    @Override
    public int getGenerationCounter(final Fdn moFdn) {
        final Fdn meContextFdn = moFdn.getMeContext();
        final String generationCounter = csHandler.getAttributeValue(meContextFdn, GENERATION_COUNTER);
        return Integer.parseInt(generationCounter);
    }

    @Override
    public int getIncreasedGenerationCounter(final Fdn moFdn, final int oldGenCounter, final int maxTime) {

        int gC = 0;
        int presentTime = 0;

        while (presentTime < maxTime) {
            gC = getGenerationCounter(moFdn);
            if (gC <= oldGenCounter) {
                sleep(5000);
            } else {
                break;
            }
            presentTime = presentTime + 5;
        }
        return gC;
    }

    private void sleep(final int milliSec) {
        try {
            Thread.sleep(milliSec);
        } catch (final InterruptedException e) {
            logger.info("InterruptedException occurred.");
        }
    }

    @Override
    public NetworkElement getNetworkElement(final String NodeFdnMeContext) {
        return netsimCmdHandler.getAllStartedNEs().get(NodeFdnMeContext);
    }

    @Override
    public boolean checkCmdResult(final NetSimResult cmdResult) {
        return cmdResult.getRawOutput().trim().endsWith(NETSIM_CMD_EXEC_SUCCESS);
    }

    @Override
    public Fdn getMoFromDifferentNode(final String nodeType, final String moType, final Fdn mofdnOnCurrentNode) {
        final String oldMeContext = mofdnOnCurrentNode.getNameOfFdnPart(MECONTEXT);

        Fdn mo = null;
        String newMeContext = "";
        int attempts = 1;
        do {
            mo = getChildMoFromSyncedNode(nodeType, moType);
            if (mo != null) {
                newMeContext = mo.getNameOfFdnPart(MECONTEXT);
                attempts++;
            }
        } while (mo != null && attempts < 500 && newMeContext.equals(oldMeContext));

        return mo;

    }

    @Override
    public List<Fdn> getMeContexts(final Fdn... fdns) {

        final List<Fdn> moFdnList = new ArrayList<>();
        for (final Fdn fdn : fdns) {
            final Fdn meContext = fdn.getMeContext();
            moFdnList.add(meContext);
        }
        return Collections.unmodifiableList(moFdnList);
    }

    private List<Fdn> selectRandomSynchedNodes(final List<Fdn> listNodes, final int percentage) {
        final List<Fdn> synchedNodes = new ArrayList<Fdn>();

        final Random randomGenerator = new Random();
        final int absoluteNumber = listNodes.size() * percentage / 100;

        while (listNodes.size() > 0) {
            if (synchedNodes.size() >= absoluteNumber) {
                break;
            }
            final int randomIndex = randomGenerator.nextInt(listNodes.size());
            final Fdn node = listNodes.remove(randomIndex);
            if (isNodeSynched(node)) {
                synchedNodes.add(node);
            }
        }
        return synchedNodes;
    }

    @Override
    public Fdn getManagedElementChild(final Fdn meContextFdn, final String childFdnName) {

        return meContextFdn.getMeContext().newChild(MANAGED_ELEMENT, "1").newChild(childFdnName, "1");
    }

}
