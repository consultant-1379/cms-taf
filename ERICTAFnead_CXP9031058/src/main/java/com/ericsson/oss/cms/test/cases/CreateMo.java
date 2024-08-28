/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.MAX_TIME_TO_READ_GEN_COUNTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_CREATE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.CreateMoCliOperator;
import com.ericsson.oss.cms.test.operators.LogFileCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SetMoCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xrajnka
 */
public class CreateMo extends TorTestCaseHelper implements TestCase {

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SetMoCliOperator setMoCliOperator;

    @Inject
    private SyncNodesCliOperator syncNodesCliOperator;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    @Inject
    TestContext crudMoContext;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    /**
     * @DESCRIPTION Verify that NEAD listens for Create MO notifications from the
     *              node side and creates corresponding MO in the CS.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-47933_CreateMO", title = "NEAD handles Create MO Notifications successfully")
    @Context(context = { Context.CLI })
    @DataDriven(name = "createMo")
    @Test(groups = { "NEAD, KGB" })
    public void handleCreateMoNotification(
            @Input("nodeType") final String nodeType,
            @Input("parentMoType") final String parentMoType,
            @Input("moType") final String moType,
            @Input("attributes") final String[] attributes,
            @Input("attributeValues") final String[] attributeValues) {

        setTestStep("Get an MO of parentMoType under a node of nodeType from OSS database.");
        final Fdn parentMoFdn = nodeCliOperator.getChildMoFromSyncedNode(nodeType, parentMoType);
        assertNotNull("Provided parent MO Type = " + parentMoType + " not found on available nodes.", parentMoFdn);

        setTestStep("Get node fdn from parent Mo Fdn");
        final Fdn activeNodeFdn = parentMoFdn.getMeContext();

        setTestStep("Get the GenerationCounter for the above node from CS and store it.");
        final int genCounterBeforeCreate = nodeCliOperator.getGenerationCounter(activeNodeFdn);

        setTestInfo("Build child MO fdn with parent MO Fdn and MO type");
        final Fdn childMoFdn = createMoCliOperator.buildMoFdn(parentMoFdn, moType, getTestId());

        setTestInfo("Get the notification log count for the MO Fdn and store it.");
        final int notifCountBeforeCreate = logFileCliOperator.getNotificationCount(NOTIFICATION_CREATE, childMoFdn);

        setTestStep("Create a Mo with the supplied attributes on the node");
        final long testStartTime = System.currentTimeMillis();
        final boolean moCreatedInNetSim = createMoCliOperator.createMo(childMoFdn, attributes, attributeValues);
        assertTrue("Failed to create MO in NETSIM", moCreatedInNetSim);

        crudMoContext.dataSource("deleteMo").addRecord().setField("fdn", childMoFdn);

        final boolean moExistInCS = csHandler.moExists(childMoFdn);
        assertTrue("Failed to create MO in CS database", moExistInCS);
        final boolean valuesMatch = setMoCliOperator.isAttributesSetInDatabase(childMoFdn, attributes, attributeValues);
        assertTrue("Created MO attributes in database does not match with input data", valuesMatch);

        setTestStep("Get the GenerationCounter from CS for the node");
        final int genCounterAfterCreate = nodeCliOperator.getIncreasedGenerationCounter(activeNodeFdn, genCounterBeforeCreate, MAX_TIME_TO_READ_GEN_COUNTER);
        assertThat("Generation Counter has not increased after create operation", genCounterAfterCreate, greaterThan(genCounterBeforeCreate));

        setTestStep("Check the NEAD logs(Notification.log) for notification trace");
        final int notifCountAfterCreate = logFileCliOperator.getNotificationCount(NOTIFICATION_CREATE, childMoFdn);
        assertThat("Create notification is missing in notification logs.", notifCountAfterCreate, is(equalTo(notifCountBeforeCreate + 1)));

        setTestStep("Check the CIF logs for resynch message related to the target NE");
        final List<Fdn> listOfActiveNodes = Arrays.asList(activeNodeFdn);
        final List<CIFLogItem> cifLogEntries = syncNodesCliOperator.getSyncStatusCifLogEntries(listOfActiveNodes, testStartTime);
        assertThat("CIF log entries should not contain node sync messages during create mo operation.", cifLogEntries, hasSize(0));

    }
}
