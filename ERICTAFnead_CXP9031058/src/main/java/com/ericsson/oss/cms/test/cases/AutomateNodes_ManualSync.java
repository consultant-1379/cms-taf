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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;

public class AutomateNodes_ManualSync extends TorTestCaseHelper implements TestCase {

    @Inject
    private SyncNodesCliOperator syncOperator;

    @Inject
    private NodeCliOperator nodeOperator;

    /**
     * @DESCRIPTION Automate the sync operation for NODES to reduce manual
     *              entry, by reading available nodes and iterating sync on
     *              them.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-33328_FUNC_1", title = "Perform Manual sync of node")
    @Context(context = { Context.CLI })
    @DataDriven(name = "syncnode")
    @Test(groups = { "CDB", "NEAD", "KGB", "GAT" })
    public void manualNodeSyncTest(@Input("neType") final String nodeType, @Input("percentage") final int percentage, @Input("nodeFdn") final String nodeFdn) {

        setTestStep("Get Node by Type and check if synced");
        final List<Fdn> listOfActiveNodes = nodeOperator.getPercentageOfSyncedNodes(nodeType, percentage, nodeFdn);
        assertThat("There are no synced nodes ", listOfActiveNodes, hasSize(greaterThan(0)));

        setTestStep("For each available node, sync with server");

        syncOperator.startSyncOnNodes(listOfActiveNodes);
        final List<Fdn> failedNodes = syncOperator.checkForFailedNodes(listOfActiveNodes, 300);
        assertThat("List of failed Nodes: " + failedNodes, failedNodes, hasSize(0));

        setTestStep("Check the status of the Node in CS");

        final List<Fdn> successNodes = syncOperator.checkForSuccessNodes(listOfActiveNodes, 300);
        assertThat("List of Sucess Nodes: " + successNodes, successNodes, hasSize(0));

        /*
         * setTestStep("Check Cif logs for synced nodes enteries");
         * final List<CIFLogItem> cifLogEntries = syncOperator.getSyncStatusCifLogEntries(listOfActiveNodes, startTime);
         * syncOperator.printCollection(cifLogEntries);
         * final int expected = syncOperator.getExpectedNumCifLogs(listOfActiveNodes.size());
         * assertThat("Expected number of CIF logs does not match retrieved logs ", cifLogEntries, hasSize(expected));
         */

    }
}
