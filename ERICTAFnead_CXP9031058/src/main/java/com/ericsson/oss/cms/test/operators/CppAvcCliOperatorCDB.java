/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.NETSIM_CMD_EXEC_SUCCESS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.NE_TYPE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_SYNCHRONIZED;

import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SetmoattributeCommand;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.cms.test.util.Lists;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class CppAvcCliOperatorCDB {

    private CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private Logger logger = Logger.getLogger(this.getClass());

    private NetSimCommandHandler netSimCommandHandler = NetSimCommandHandler.getInstance();

    public NeGroup getNodes(final List<Fdn> listOfActiveNodes) {
        netSimCommandHandler = NetSimCommandHandler.getInstance(HostGroup.getAllNetsims());
        final NeGroup allNesGrp = netSimCommandHandler.getAllStartedNEs();
        final NeGroup elementsUnderTestGrpList = new NeGroup();
        final List<NetworkElement> networkElementsList = allNesGrp.getNetworkElements();
        for (final Fdn node : listOfActiveNodes) {
            for (final NetworkElement neEl : networkElementsList) {
                final String nodeMeContextName = node.getNameOfFdnPart(MECONTEXT).toLowerCase();
                final String neNameList = neEl.getName().toLowerCase();

                if (nodeMeContextName.equals(neNameList)) {
                    elementsUnderTestGrpList.add(neEl);
                }
            }
        }
        // logger = Logger.getLogger(this.getClass());
        // logger.info("Retrieved {} node(s) from Netsim", elementsUnderTestGrpList.size());
        return elementsUnderTestGrpList;
    }

    public String buildAttributeValuePair(final String attributeNames, final String attributeValues) {
        final StringBuilder attrValuePairBuilder = new StringBuilder();

        attrValuePairBuilder.append(attributeNames);
        attrValuePairBuilder.append("=");
        attrValuePairBuilder.append(attributeValues);

        return attrValuePairBuilder.toString();
    }

    public void setAttributeValues(final Fdn moFdn, final String attributeNames, final String attributeValues) {
        NetworkElement networkElement = getNetworkElementForFdn(moFdn);
        final String attributeValuePair = buildAttributeValuePair(attributeNames, attributeValues);
        logger = Logger.getLogger(this.getClass());
        logger.info("Setting attribute value pair(s) as follows: " + attributeValuePair.toString());

        final SetmoattributeCommand setAttributeCommand = NetSimCommands.setmoattribute(moFdn.getLdn(), attributeValuePair);
        networkElement = getNetworkElementForFdn(moFdn);
        final NetSimResult commandResult = networkElement.exec(setAttributeCommand);
        commandResult.getRawOutput().trim().endsWith(NETSIM_CMD_EXEC_SUCCESS);
    }

    public List<Fdn> findSynchedNodes(final String nodeType, final double percentage) {
        logger = Logger.getLogger(this.getClass());
        csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);
        final int neType = NeType.getNeTypeValue(nodeType);
        logger.info("Looking for Nodes with neType of: " + neType);

        final Filter filter = SimpleFilterBuilder.builder().attr(NE_TYPE).equalTo(neType).and().attr(SYNCH_STATUS).equalTo(SYNCH_STATUS_SYNCHRONIZED).build();
        final List<Fdn> listOfActiveNodes = csHandler.getByType(MECONTEXT, filter);
        return Lists.reduce(listOfActiveNodes, percentage);
    }

    public NetworkElement getNetworkElementForFdn(final Fdn moFdn) {
        final String meContextName = moFdn.getNameOfFdnPart(MECONTEXT);
        final NetworkElement networkElement = netSimCommandHandler.getAllStartedNEs().get(meContextName);
        return networkElement;
    }

}
