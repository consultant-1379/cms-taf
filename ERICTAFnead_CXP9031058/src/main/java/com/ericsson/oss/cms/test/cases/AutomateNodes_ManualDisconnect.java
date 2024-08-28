package com.ericsson.oss.cms.test.cases;

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

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.oss.cms.test.operators.DisconnectNodesOperator;
import com.ericsson.oss.cms.test.operators.NodeOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

public class AutomateNodes_ManualDisconnect extends TorTestCaseHelper implements TestCase {

    @Inject
    private OperatorRegistry<DisconnectNodesOperator> disconnectNodesOperatorRegistry;

    @Inject
    private OperatorRegistry<NodeOperator> nodeOperatorRegistry;

    private final SMHandler smHandler = new SmtoolHandler(HostGroup.getOssmaster());

    /**
     * @DESCRIPTION Automate the disconnect operation for NODES nodes to reduce
     *              manual entry, by reading available nodes and iterating
     *              disconnect on them.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-27907_Func_1", title = "Disconnect a node")
    @Context(context = { Context.CLI })
    @DataDriven(name = "disconnectnode")
    @Test(groups = { "CDB, NEAD, KGB, GAT" })
    public void manualNodeDisconnectTest(
            @Input("neType") final String nodeType,
            @Input("percentage") final int percentage,
            @Input("pollrateConnected") final String pollrateConnected,
            @Input("pollrateDisconnected") final String pollrateDisconnected) {

        final DisconnectNodesOperator disconnectNodesOperator = disconnectNodesOperatorRegistry.provide(DisconnectNodesOperator.class);
        final NodeOperator nodeOperator = nodeOperatorRegistry.provide(NodeOperator.class);

        setTestStep("Select connected and synchronished nodes");
        final List<Fdn> listOfActiveNodes = nodeOperator.getPercentageMimScopedSynchedNode(nodeType, percentage);
        assertThat("No sync'd node(s) found", listOfActiveNodes, is(not(empty())));
        final NeGroup elementsUnderTest = disconnectNodesOperator.getNodesFromNetsim(listOfActiveNodes);
        assertThat("Could not find selected node(s) in Netsim", elementsUnderTest, is(not(empty())));

        setTestStep("Read Pollrate for connected node(s)");
        final int stopTimeout = Integer.parseInt(smHandler.getConfigurationForMC(NEAD_MC, pollrateConnected));
        assertThat("Pollrate for connected node(s) is not configured", stopTimeout, is(greaterThan(0)));
        setTestInfo("Pollrate for connected %s node(s) is %d", nodeType, stopTimeout);

        setTestStep("Use Netsim to stop nodes ");
        disconnectNodesOperator.stopNodes(elementsUnderTest);
        final List<Fdn> failedNodes = disconnectNodesOperator.waitForNodesStop(listOfActiveNodes, stopTimeout);
        assertThat("These Nodes failed to stop: " + failedNodes, failedNodes, is(empty()));

        setTestStep("Read Pollrate for disconnected node");
        int startTimeout = Integer.parseInt(smHandler.getConfigurationForMC(NEAD_MC, pollrateDisconnected));
        startTimeout =  startTimeout+60;
        assertThat("Pollrate for disconnected node(s) is not configured", startTimeout, is(greaterThan(0)));
        setTestInfo("Pollrate for disconnected %s node(s) is %d", nodeType, startTimeout);

        setTestStep("Use Netsim to start nodes ");
        disconnectNodesOperator.startDisconnectedNodes(elementsUnderTest);
        final List<Fdn> failedNodesStart = disconnectNodesOperator.waitForNodesStart(listOfActiveNodes, startTimeout);
        assertThat("These nodes failed to start: " + failedNodesStart, failedNodesStart, is(empty()));
    }
}
