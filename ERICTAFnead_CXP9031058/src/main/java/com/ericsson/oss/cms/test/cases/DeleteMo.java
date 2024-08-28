/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.MAX_TIME_TO_READ_GEN_COUNTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_DELETE;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
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
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.DeleteMoOperator;
import com.ericsson.oss.cms.test.operators.LogFileCliOperator;
import com.ericsson.oss.cms.test.operators.NodeOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * Class performs the delete operation on the MO that are created by Create MO test case.
 * 
 * @author xmurran
 */
public class DeleteMo extends TorTestCaseHelper implements TestCase {

    @Inject
    private OperatorRegistry<NodeOperator> nodeOperatorRegistry;

    @Inject
    private OperatorRegistry<DeleteMoOperator> deleteMoOperatorRegistry;

    @Inject
    private OperatorRegistry<SyncNodesOperator> syncNodesOperatorRegistry;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    /**
     * @DESCRIPTION Verify that NEAD listens for Delete MO notifications from the node side,
     *              and deletes the corresponding MO in the CS.
     * @PRE CreateMO test case should be executed.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-48113_DeleteMO", title = "NEAD handles Delete MO Notifications successfully")
    @Context(context = { Context.CLI })
    @Test(groups = { "NEAD, KGB" })
    @DataDriven(name = "deleteMo")
    public void handleDeleteMoNotification(@Input("fdn") final Fdn moFdn) {

        final NodeOperator nodeOperator = nodeOperatorRegistry.provide(NodeOperator.class);
        final DeleteMoOperator deleteMoOperator = deleteMoOperatorRegistry.provide(DeleteMoOperator.class);
        final SyncNodesOperator syncNodesOperator = syncNodesOperatorRegistry.provide(SyncNodesOperator.class);

        setTestStep("Read the latest generation counter for the node and store it");
        final int genCounterBeforeDelete = nodeOperator.getGenerationCounter(moFdn);

        setTestStep("Record the current time and store as start time");
        final long startTime = System.currentTimeMillis();

        setTestInfo("Record the number of %s notifications for MO: %s", NOTIFICATION_DELETE, moFdn);
        final int notificationCountBeforeDelete = logFileCliOperator.getNotificationCount(NOTIFICATION_DELETE, moFdn);

        setTestStep("Perform Delete MO operation on Node");
        final boolean isMoDeletedOnNode = deleteMoOperator.deleteMo(moFdn);
        assertThat("Mo is not deleted on Node", isMoDeletedOnNode, equalTo(true));

        final boolean isMoExistInDb = csHandler.moExists(moFdn);
        assertThat("MO is not deleted on OSS", isMoExistInDb, equalTo(false));

        setTestStep("Read the latest generation counter for the Node");
        final int genCounterAfterDelete = nodeOperator.getIncreasedGenerationCounter(moFdn, genCounterBeforeDelete, MAX_TIME_TO_READ_GEN_COUNTER);
        assertThat("Generation counter is not increased after Delete operation", genCounterAfterDelete, greaterThan(genCounterBeforeDelete));

        setTestStep("Check the NEAD logs(Notification.log) for notification trace");
        final int notificationCountAfterDelete = logFileCliOperator.getNotificationCount(NOTIFICATION_DELETE, moFdn);
        assertThat("Delete MO details are missing in notification logs", notificationCountAfterDelete, is(equalTo(notificationCountBeforeDelete + 1)));

        setTestStep("Check the CIF logs for this node");
        final List<Fdn> listOfActiveNodes = Arrays.asList(moFdn.getMeContext());
        final List<CIFLogItem> cifLogEntries = syncNodesOperator.getSyncStatusCifLogEntries(listOfActiveNodes, startTime);
        assertThat("CIF log entries should not contain node sync messages during delete mo operation.", cifLogEntries, is(empty()));

    }
}
