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

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS_DISCONNECTED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS_NEVERCONNECTED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.NE_TYPE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_UNSYNCHRONIZED;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = { Context.CLI, Context.API })
public class NodesInNetsimandCSCliOperator implements NodesInNetsimandCSOperator {

    final static Logger LOGGER = Logger.getLogger(NodesInNetsimandCSOperator.class);

    @Inject
    private DisconnectNodesCliOperator disconnectNodesCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public int getUnsyncNode(final String nodeType) {

        final int neType = NeType.getNeTypeValue(nodeType);
        LOGGER.info("Looking for Nodes with neType of: " + neType);

        final Filter filter = SimpleFilterBuilder.builder().attr(NE_TYPE).equalTo(neType).and().attr(SYNCH_STATUS).equalTo(SYNCH_STATUS_UNSYNCHRONIZED).build();

        final List<Fdn> listOfUnsyncNodes = csHandler.getByType(MECONTEXT, filter);
        LOGGER.info("Number of Unsync node in CS :::" + listOfUnsyncNodes.size());

        for (final Fdn node : listOfUnsyncNodes) {

            final String nodeMeContext = node.getNameOfFdnPart(MECONTEXT).toUpperCase();
            LOGGER.info("Unsync node name :" + nodeMeContext + "," + "Looks like application issue!");
        }

        return listOfUnsyncNodes.size();

    }

    @Override
    public int getDisConnetedNode(final String nodeType) {

        int disConnNode = 0;

        final int neType = NeType.getNeTypeValue(nodeType);
        LOGGER.info("Looking for Nodes with neType of: " + neType);

        final Filter filter = SimpleFilterBuilder.builder().attr(NE_TYPE).equalTo(nodeType).and().attr(CONN_STATUS).equalTo(CONN_STATUS_DISCONNECTED).build();

        final List<Fdn> listOfDisconNodes = csHandler.getByType(MECONTEXT, filter);
        LOGGER.info("Disconnected node  :" + listOfDisconNodes.size());
        if (listOfDisconNodes.size() > 0) {
            final NeGroup allNes = disconnectNodesCliOperator.getNodesFromNetsim(listOfDisconNodes);
            disConnNode = getStartedDisconNodes(allNes);
            return disConnNode;

        }
        return disConnNode;

    }

    @Override
    public int getNeverConnetedNode(final String nodeType) {

        int disConnNode = 0;

        final int neType = NeType.getNeTypeValue(nodeType);
        LOGGER.info("Looking for Nodes with neType of: " + neType);

        final Filter filter = SimpleFilterBuilder.builder().attr(NE_TYPE).equalTo(neType).and().attr(CONN_STATUS).equalTo(CONN_STATUS_NEVERCONNECTED).build();

        final List<Fdn> listOfNeverconNodes = csHandler.getByType(MECONTEXT, filter);
        LOGGER.info("Never connected node  :" + listOfNeverconNodes.size());
        if (listOfNeverconNodes.size() > 0) {
            final NeGroup allNes = disconnectNodesCliOperator.getNodesFromNetsim(listOfNeverconNodes);
            disConnNode = getStartedDisconNodes(allNes);
            return disConnNode;

        }
        return disConnNode;

    }

    @Override
    public int getStartedDisconNodes(final NeGroup allNes) {
        final List<NetworkElement> networkElements = allNes.getNetworkElements();

        for (final NetworkElement ne : networkElements) {
            final String neName = ne.getName().toLowerCase();
            LOGGER.info("Nodes started in Netsim :-connection Configuration issue :" + neName);
        }
        LOGGER.info("Total Number of problematic  Nodes :" + networkElements.size());
        return networkElements.size();
    }

}
