/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.DISCONNECTED_NODE_REMOVED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.RECOVERED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS_DISCONNECTED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_UNSYNCHRONIZED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MIB_ADAPTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.LTE_DISCONNECTED_NODE_TIMEOUT_DURATION;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.COMMAND_CONSISTENCY_CHECK;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.NODE_UNSYNCHRONIZED;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.cms.test.operators.MoCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCifLogCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.cms.test.operators.SnadCacheCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.cms.test.util.GetHostUsers;
import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author xrajnka
 */
public class ReconnectedERBS extends TorTestCaseHelper implements TestCase {

    private final SMHandler smtool = new SmtoolHandler(HostGroup.getOssmaster());

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final static CLICommandHelper cliCmdHandler = GetHostUsers.getCLICommandHelper();

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private MoCliOperator moCliOperator;

    @Inject
    private SnadCacheCliOperator snadCacheCliOperator;

    @Inject
    private SnadApiOperator snadApiOperator;

    @Inject
    private NodeCifLogCliOperator nodeCifLogCliOperator;

    @Inject
    private SyncNodesCliOperator syncNodesCliOperator;

    @Inject
    private TimeRange timeRange;

    private Fdn activeNodeFdn = null;

    private String timeoutDurationBeforeDetach = null;

    /**
     * @DESCRIPTION Verify that a Reconnected ERBS will be added back into the cache.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-51707_ReconnectedERBS", title = "Reconnected ERBS will be added back into the Cache")
    @Context(context = { Context.CLI })
    @DataDriven(name = "reconnectedERBS")
    @Test(groups = { "SNAD, KGB" })
    public void handleReconnectedERBSInCache(@Input("lteDisconnectedNodeTimeoutDuration") final String lteDisconnectedNodeTimeoutDuration) {

        setTestStep("Read the value of lteDisconnectedNodeTimeout parameter from SNAD MC.");
        final String lteDisconnectedNodeTimeoutValue = smtool.getConfigurationForMC(SNAD_MC, "lteDisconnectedNodeTimeout");
        assertTrue("Disconnected Node Timeout for ERBS is not enabled.", Boolean.valueOf(lteDisconnectedNodeTimeoutValue));

        setTestStep("Get a connected and synced ERBS node from OSS database.");
        activeNodeFdn = nodeCliOperator.getMimScopedSynchedNode("ERBS");
        assertNotNull("No synced ERBS node found.", activeNodeFdn);
        setTestInfo("Selected node: " + activeNodeFdn);
       

        setTestStep("Check whether the node is part of the managed section of the cache.");
        final Fdn eNodeBFunMOFdn = moCliOperator.getChildMoFdn(activeNodeFdn, "ENodeBFunction");
        String isMoInMasterCache = snadApiOperator.getMaster(eNodeBFunMOFdn);
        assertNotNull("Selected node " + eNodeBFunMOFdn.getFdn() + " is not in managed cache.", isMoInMasterCache);

        setTestStep("Read the value of lteDisconnectedNodeTimeoutDuration and store it.");
        timeoutDurationBeforeDetach = smtool.getConfigurationForMC(SNAD_MC, LTE_DISCONNECTED_NODE_TIMEOUT_DURATION);

        setTestStep("Set the new value of lteDisConnectedNodeTimeoutDuration to provided value.");
        final boolean commandResult = smtool.setConfigurationForMC(SNAD_MC, LTE_DISCONNECTED_NODE_TIMEOUT_DURATION, lteDisconnectedNodeTimeoutDuration);
        assertTrue("Setting of parameter value on MC did not succeed", commandResult);

        setTestStep("Detach the node in OSS.");
        csHandler.detach(activeNodeFdn);
        assertFalse("Node is still attached to NEAD mibadapter", csHandler.isAttached(activeNodeFdn, NEAD_MIB_ADAPTER));

        long startTime = System.currentTimeMillis();

        setTestStep("Check the node sync and connection state in CS.");
        final String nodeConnStateInCS = csHandler.getAttributeValue(activeNodeFdn, CONN_STATUS);
        final String nodeSyncStateInCS = csHandler.getAttributeValue(activeNodeFdn, SYNCH_STATUS);
        assertEquals("Node is not disconnected.", nodeConnStateInCS, Integer.toString(CONN_STATUS_DISCONNECTED));
        assertEquals("Node is not unsynced.", nodeSyncStateInCS, Integer.toString(SYNCH_STATUS_UNSYNCHRONIZED));

        setTestStep("Check the snad cache for node details.");
        snadCacheCliOperator.executeCacheReview();
        final boolean isMoExistInMasterCache = snadCacheCliOperator.isInMasterCache(eNodeBFunMOFdn);
        assertTrue("Node does not exist in master cache", isMoExistInMasterCache);
        final String nodeStateInCache = snadCacheCliOperator.getMasterConsistencyState(eNodeBFunMOFdn);
        assertEquals("Node state is not unsynced in cache.", nodeStateInCache, NODE_UNSYNCHRONIZED);

        setTestStep("Wait for SNAD's lteDisConnectedNodeTimeoutDuration then nudge a full ConsistencyCheck.");
        final long elapsedTime = System.currentTimeMillis() - startTime;
        final long lteDisconnectedNodeTimeoutDurationInMillis = Integer.parseInt(lteDisconnectedNodeTimeoutDuration) * 60 * 1000;
        final int remainingWaitTime = Math.max(0, (int) (lteDisconnectedNodeTimeoutDurationInMillis - elapsedTime));
        snadApiOperator.waitFor(remainingWaitTime);
        cliCmdHandler.simpleExec(COMMAND_CONSISTENCY_CHECK);
        
        startTime = System.currentTimeMillis();
        final long temp = MAX_TIME_TO_READ_CIF_LOGS + 120000;
        timeRange.setStartTime(startTime);
        timeRange.setTimeout(temp);
        

        setTestStep("Wait for sleep message in CIF logs.");

        final boolean isSleepLogReceived = snadApiOperator.waitForSleep(startTime, temp);
        assertTrue("Sleep message not found in CIF logs.", isSleepLogReceived);

        setTestStep("Check CIF logs for message node has been removed from the cache");
        final boolean isNodeRemovedLogReceived = nodeCifLogCliOperator.waitForNodeAction(SNAD_MC, timeRange, activeNodeFdn, DISCONNECTED_NODE_REMOVED);
        assertTrue("Disconnected node removed message not found in CIF logs.", isNodeRemovedLogReceived);

        setTestStep("Check the snad cache for node details.");
        snadCacheCliOperator.executeCacheReview();
        final boolean moExistInMasterCache = snadCacheCliOperator.isInMasterCache(eNodeBFunMOFdn);
        assertFalse("Selected node " + eNodeBFunMOFdn.getFdn() + " is still in managed cache. We expect it to be removed.", moExistInMasterCache);
        final boolean moExistInUnmanagedCache = snadCacheCliOperator.isInUnmanagedCache(eNodeBFunMOFdn);
        assertFalse("Selected node " + eNodeBFunMOFdn.getFdn() + " is in unmanaged cache. We expect it not to be here.", moExistInUnmanagedCache);

        setTestStep("Reattach the node to MibAdapter.");
        csHandler.attach(activeNodeFdn, NEAD_MIB_ADAPTER);
        assertTrue("Node is not attached to NEAD midadapter", csHandler.isAttached(activeNodeFdn, NEAD_MIB_ADAPTER));

        setTestStep("Check the node sync state in OSS.");
        final List<Fdn> fdnAsList = Arrays.asList(activeNodeFdn);
        final List<Fdn> syncFailedNodes = syncNodesCliOperator.checkForFailedNodes(fdnAsList, 60);
        assertThat("Node has failed to sync", syncFailedNodes, is(empty()));

        setTestStep("Check CIF logs for node RECOVER message.");
        final boolean isNodeRecoverLogReceived = nodeCifLogCliOperator.waitForNodeAction(SNAD_MC, timeRange, activeNodeFdn, activeNodeFdn + RECOVERED);
        assertTrue("Node RECOVER message not found in CIF logs.", isNodeRecoverLogReceived);

        setTestStep("Check the snad cache for node details.");
        isMoInMasterCache = snadApiOperator.getMaster(eNodeBFunMOFdn);
        assertNotNull("Selected node " + eNodeBFunMOFdn.getFdn() + " is not in managed cache.", isMoInMasterCache);

    }

    @AfterTest
    public void afterTest() {
        setTestStep("Clean Up:Reattach the node to MibAdapter.");
        csHandler.attach(activeNodeFdn, NEAD_MIB_ADAPTER);

        setTestStep("Clean Up:Reset the lteDisConnectedNodeTimeoutDuration value with original value.");
        smtool.setConfigurationForMC(SNAD_MC, LTE_DISCONNECTED_NODE_TIMEOUT_DURATION, timeoutDurationBeforeDetach);
    }
}
