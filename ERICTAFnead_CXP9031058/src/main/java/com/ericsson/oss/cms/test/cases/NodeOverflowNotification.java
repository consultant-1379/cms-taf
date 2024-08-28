/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.NEAD_SUBSCRIPTION_TIMEDOUT_MSG;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.SYNCHRONIZED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.UNSYNCHRONIZED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.OVERFLOW_EXCEPTION;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.LogFileCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCifLogCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.NodeOverFlowNotificationCliOperator;
import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author emacraj
 */
public class NodeOverflowNotification extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    @Inject
    private NodeCifLogCliOperator nodeCifLogCliOperator;

    @Inject
    private NodeOverFlowNotificationCliOperator nodeOverFlowNotificationCliOperator;

    @Inject
    private TimeRange timeRange;

    /**
     * @DESCRIPTION Verify NEAD Handles Overflow Notification has been sent from Node for all Supported Nodes.
     * @PRE NEAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-53408_NodeOverFlowNotifications", title = "Verify NEAD Handles Overflow Notification has being sent from Node for all Supported Nodes")
    @Context(context = { Context.CLI })
    @DataDriven(name = "nodeoverflow")
    @Test(groups = { "KGB" })
    public void handleOverFlowNotification(@Input("nodeType") final String nodeType) {

        setTestStep("Get a synced node from OSS.");
        final Fdn activeNodeFdn = nodeCliOperator.getSynchedNode(nodeType);
        assertThat("No synced nodes found of the given type.", activeNodeFdn, notNullValue());
        setTestInfo("Selected node: " + activeNodeFdn);
        final int exceptionCountBeforeOverFlow = logFileCliOperator.getExceptionCount(OVERFLOW_EXCEPTION, activeNodeFdn);

        timeRange.setStartTime(System.currentTimeMillis());

        setTestStep("Send an overflow notification from the selected node.");
        final boolean isOverFlowCmdSuccess = nodeOverFlowNotificationCliOperator.generateNodeOverflow(activeNodeFdn);
        assertTrue("Failed to create node overflow in NETSIM", isOverFlowCmdSuccess);
        setTestInfo("Check NEAD exception logs for over flow notification.");
        final int exceptionCountAfterOverFlow = logFileCliOperator.getExceptionCount(OVERFLOW_EXCEPTION, activeNodeFdn);
        assertThat("Overflow notification exception not logged in NEAD exception logs.", exceptionCountAfterOverFlow, greaterThan(exceptionCountBeforeOverFlow));

        timeRange.setTimeout(MAX_TIME_TO_READ_CIF_LOGS);
        final boolean isNodeUnsyncMessageReceived = nodeCifLogCliOperator.waitForNodeAction(NEAD_MC, timeRange, activeNodeFdn, UNSYNCHRONIZED);
        assertTrue("Failed to receive node unsync message in CIF logs.", isNodeUnsyncMessageReceived);

        timeRange.setTimeout(MAX_TIME_TO_READ_CIF_LOGS * 3);
        final boolean isSubscriptionTimeoutReceived = nodeCifLogCliOperator
                .waitForNodeAction(NEAD_MC, timeRange, activeNodeFdn, NEAD_SUBSCRIPTION_TIMEDOUT_MSG);
        assertTrue("Failed to get Nead Subscription Timeout log", isSubscriptionTimeoutReceived);

        setTestStep("Verify CIF logs for node state changes from UNSYNCHRONIZED to SYNCHRONIZED.");
        timeRange.setTimeout(MAX_TIME_TO_READ_CIF_LOGS);
        final boolean isSynchMessageReceived = nodeCifLogCliOperator.waitForNodeAction(NEAD_MC, timeRange, activeNodeFdn, SYNCHRONIZED);
        assertTrue("Failed to get Node synchronization message", isSynchMessageReceived);
    }
}
