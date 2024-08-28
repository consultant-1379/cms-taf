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

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.StartCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.StopCommand;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = { Context.CLI, Context.API })
public class DisconnectNodesCliOperator implements DisconnectNodesOperator {

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final NetSimCommandHandler netsimCommandHandler = NetSimCommandHandler.getInstance(HostGroup.getAllNetsims());

    private final Logger log = LoggerFactory.getLogger(DisconnectNodesCliOperator.class);

    @Override
    public NeGroup getNodesFromNetsim(final List<Fdn> listOfActiveNodes) {
        final NeGroup allNes = netsimCommandHandler.getAllStartedNEs();
        final NeGroup elementsUnderTest = new NeGroup();
        final List<NetworkElement> networkElements = allNes.getNetworkElements();
        for (final Fdn node : listOfActiveNodes) {
            for (final NetworkElement ne : networkElements) {
                final String nodeMeContext = node.getNameOfFdnPart(MECONTEXT).toLowerCase();
                final String neName = ne.getName().toLowerCase();

                if (nodeMeContext.equals(neName)) {
                    elementsUnderTest.add(ne);
                }
            }
        }
        log.info("Retrieved {} node(s) from Netsim", elementsUnderTest.size());
        return elementsUnderTest;
    }

    @Override
    public void stopNodes(final NeGroup nes) {
        final StopCommand stopCmd = NetSimCommands.stop();
        nes.exec(stopCmd);
    }

    @Override
    public List<Fdn> waitForNodesStop(final List<Fdn> nodes, final int nodeTimeout) {
        return waitForNodeCycle(nodes, nodeTimeout, 4);
    }

    @Override
    public void startDisconnectedNodes(final NeGroup nes) {
        final StartCommand startCmd = NetSimCommands.start();
        nes.exec(startCmd);
    }

    @Override
    public List<Fdn> waitForNodesStart(final List<Fdn> listOfActiveNodesStart, final int nodeTimeout) {
        return waitForNodeCycle(listOfActiveNodesStart, nodeTimeout, 3);
    }

    private List<Fdn> waitForNodeCycle(final List<Fdn> activeNodes, final int nodeTimeout, final int synchState) {
        final List<Fdn> failedNodes = new ArrayList<>();
        for (final Fdn fdn : activeNodes) {
            if (!checkNodesSyncStatus(fdn, synchState, nodeTimeout)) {
                failedNodes.add(fdn);
            }
        }
        return failedNodes;
    }

    public boolean checkNodesSyncStatus(final Fdn fdn, final int expectedStatus, final int timeOut) {
        final int timeoutInMillis = timeOut * 1000;
        final long estimatedEndTime = System.currentTimeMillis() + timeoutInMillis;

        while (System.currentTimeMillis() <= estimatedEndTime) {
            final String attribute = csHandler.getAttributeValue(fdn, "mirrorMIBsynchStatus");
            if (attribute != null) {
                final int attributeValue = Integer.parseInt(attribute);

                if (attributeValue == expectedStatus) {
                    log.info("Sync Status for Node {} reached expected value of {}", fdn.getFdn(), attributeValue);
                    return true;
                }
                sleep(5000);
            }
        }
        log.info("Node {} sync status did not reach {} within the timeout of {}", fdn.getFdn(), expectedStatus, timeOut);
        return false;
    }

    private void sleep(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
            log.warn("Sleep was interupted", e);
        }
    }
}