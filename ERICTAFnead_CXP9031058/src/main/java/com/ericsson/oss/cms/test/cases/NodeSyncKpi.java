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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.cms.test.util.KpiUtil;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author EEIEONL
 */

public class NodeSyncKpi extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeOperator;

    @Inject
    private SyncNodesCliOperator syncOperator;

    private Integer accumulatedSyncTime = 0;

    private List<CIFLogItem> cifSyncInfoLog;

    private static final int LOGS_PER_FULL_NODE_SYNC = 3;

    @BeforeMethod
    public void resetVariables() {
        accumulatedSyncTime = 0;
    }

    /**
     * @DESCRIPTION Automate the ST KPI node sync test by reading required node FDN, running sync
     *              on it and measuring the mo/sec against the required baseline value over a number of runs.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-54680_SyncKPI", title = "KPI test for Node Sync")
    @Context(context = { Context.CLI })
    @DataDriven(name = "stkpinodesync")
    @Test(groups = { "STCDB", "NEAD" })
    public void kpiNodeSyncTest(
            @Input("nodeFdn") final String nodeFdn,
            @Input("moPerSecBaseline") final int moPerSecBaseline,
            @Input("timesToRun") final int timesToRun,
            @Input("moReadDeviationPercent") final int moReadDeviationPercent) {

        setTestStep("Get Node Under Test");
        final Fdn fdn = nodeOperator.getSpecifiedSynchedNode(nodeFdn);
        assertThat("No synched node found", fdn, notNullValue());
        setTestInfo("Node selected for test: " + fdn);
        final List<Fdn> listOfActiveNodes = Arrays.asList(fdn);

        accumulatedSyncTime = 0;

        for (int countSyncRuns = 1; countSyncRuns <= timesToRun; ++countSyncRuns) {
            setTestInfo("Running sync command on node " + countSyncRuns + " of " + timesToRun + " times");
            setTestStep("Execute an adjust to trigger a sync on the node");
            final long startTime = System.currentTimeMillis();
            syncOperator.startSyncOnNodes(listOfActiveNodes);
            final List<Fdn> failedNodes = syncOperator.checkForFailedNodes(listOfActiveNodes, 300);
            assertThat("List of failed Nodes: " + failedNodes, failedNodes, is(empty()));
            final int expectedNumCifLogs = listOfActiveNodes.size() * LOGS_PER_FULL_NODE_SYNC;
            final List<CIFLogItem> cifLogEntries = syncOperator.getSyncStatusCifLogEntriesWithRetry(listOfActiveNodes, startTime, expectedNumCifLogs);
            assertThat("Expected number of CIF logs does not match retrieved logs", cifLogEntries, hasSize(expectedNumCifLogs));

            setTestStep("Query CIF logs for the syncInfoLog entry");
            cifSyncInfoLog = syncOperator.getSyncInfoCifLogEntries(listOfActiveNodes, startTime);
            assertThat("Expected number of SYNCINFO logs does not match retrieved logs", cifSyncInfoLog, hasSize(1));

            setTestStep("Read the sync Total Time value from the syncInfoLog and add to accumulated total time so far");
            final Integer syncTotalTime = KpiUtil.getTotalTimeFromLog(cifSyncInfoLog);
            assertThat("Expected sync total time value was not returned ", syncTotalTime, notNullValue());
            accumulatedSyncTime += syncTotalTime;
        }

        setTestStep("Read number of MOs read value from the syncInfoLog and store this value");
        final Integer numberOfMosRead = KpiUtil.getNumberOfMosRead(cifSyncInfoLog);
        assertThat("Expected number of MOs read value was not returned ", numberOfMosRead, notNullValue());

        setTestStep("Calculate the average sync total time as accumulated total time/number of test runs");
        final Integer avgSyncTotalTime = accumulatedSyncTime / timesToRun;
        setTestInfo("Average Total sync time: " + avgSyncTotalTime + " secs");

        setTestStep("Calculate average MO/sec read as number of MOs read/average sync total time");
        final Integer avgMoSecRead = numberOfMosRead / avgSyncTotalTime;
        final int moPerSecWithDeviation = KpiUtil.getRateWithDeviation(moPerSecBaseline, moReadDeviationPercent);
        final float actualDeviation = KpiUtil.getActualDeviationPercent(avgMoSecRead, moPerSecBaseline);

        setAdditionalResultInfo(listOfActiveNodes.get(0).toString());
        setAdditionalResultInfo("Total No. MOs read on Node = " + numberOfMosRead);
        setAdditionalResultInfo("Average MO read rate = " + avgMoSecRead + " Mo/Sec");
        setAdditionalResultInfo("Expected average number of MOs read to be: " + moPerSecBaseline + " Mo/sec");
        setAdditionalResultInfo("Deviation Allowed -" + moReadDeviationPercent + "%" + " (" + moPerSecWithDeviation + " Mo/sec)");
        setAdditionalResultInfo("Actual Deviation from Baseline: " + actualDeviation + "%");
        assertThat("MOs read per sec was not in " + moReadDeviationPercent + " % of acceptable KPI ", avgMoSecRead, greaterThanOrEqualTo(moPerSecWithDeviation));

    }
}