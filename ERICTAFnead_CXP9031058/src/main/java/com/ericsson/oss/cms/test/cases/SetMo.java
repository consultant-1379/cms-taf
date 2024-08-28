/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.MAX_TIME_TO_READ_GEN_COUNTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_AVC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.LogFileCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SetMoCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author egokdag
 */
public class SetMo extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SetMoCliOperator setMoCliOperator;

    @Inject
    LogFileCliOperator notificationLogCliOperator;

    @Inject
    private SyncNodesCliOperator syncNodesCliOperator;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    /**
     * @DESCRIPTION Verify that NEAD listens for AVC(Attribute Value Change) notifications from the node side, and sets the relevant
     *              attributes of the corresponding MO in the database.
     * @PRE NEAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-48111_AVC", title = "NEAD handles AVC Notifications successfully")
    @Context(context = { Context.CLI })
    @DataDriven(name = "setMo")
    @Test(groups = { "NEAD, KGB" })
    public void handleSetMoNotification(
            @Input("nodeType") final String nodeType,
            @Input("moType") final String moType,
            @Input("attributes") final String[] attributes,
            @Input("primaryAttributeValues") final String[] primaryAttributeValues,
            @Input("secondaryAttributeValues") final String[] secondaryAttributeValues) {

        setTestStep("Get an MO of moType under a node of nodeType from OSS database");
        final Fdn moUnderTest = nodeCliOperator.getChildMoFromSyncedNode(nodeType, moType);
        assertThat("No mo of type " + moType + "has been found", moUnderTest, notNullValue());

        setTestStep("Read the latest generation counter for the node and store it");
        final int genCounterBeforeSet = nodeCliOperator.getGenerationCounter(moUnderTest);

        setTestStep("Read the attribute(s) value from OSS database and store it");
        final List<Attribute> moAttributesBeforeSet = csHandler.getAttributes(moUnderTest, attributes);

        setTestStep("Select new value(s) for attribute(s) to be set to.");
        final String[] moAttributeValuesToSet = setMoCliOperator.selectAttributeValuesToSet(attributes, moAttributesBeforeSet, primaryAttributeValues,
                secondaryAttributeValues);

        setTestInfo("Record the number of %s notifications for MO: %s", NOTIFICATION_AVC, moUnderTest);
        final int notifCountBeforeSet = logFileCliOperator.getNotificationCount(NOTIFICATION_AVC, moUnderTest, attributes);

        setTestStep("Record the current time and store as start time");
        final long cifLogStartTime = System.currentTimeMillis();

        setTestStep("Set attribute(s) value on the node");
        final boolean setIsSuccessfulOnNode = setMoCliOperator.setAttributeValues(moUnderTest, attributes, moAttributeValuesToSet);
        assertTrue("Failed to set attributes on the node", setIsSuccessfulOnNode);
        final boolean setIsSuccessfulInDatabase = setMoCliOperator.isAttributesSetInDatabase(moUnderTest, attributes, moAttributeValuesToSet);
        assertTrue("Failed to set attributes in database", setIsSuccessfulInDatabase);

        setTestStep("Read the latest generation counter for the node");
        final int genCounterAfterSet = nodeCliOperator.getIncreasedGenerationCounter(moUnderTest, genCounterBeforeSet, MAX_TIME_TO_READ_GEN_COUNTER);
        assertThat("Generation counter is not increased after Set operation", genCounterAfterSet, is(greaterThan(genCounterBeforeSet)));
        setTestStep("Check the NEAD notification logs for set notification");
        final int notifCountAfterSet = logFileCliOperator.getNotificationCount(NOTIFICATION_AVC, moUnderTest, attributes);
        assertThat("Set notification is missing in notification logs", notifCountAfterSet, is(equalTo(notifCountBeforeSet + 1)));

        setTestStep("Check the CIF logs for node sync logs for this node");
        final List<Fdn> listOfActiveNode = Arrays.asList(moUnderTest.getMeContext());
        final List<CIFLogItem> cifLogEntries = syncNodesCliOperator.getSyncStatusCifLogEntries(listOfActiveNode, cifLogStartTime);
        assertThat("CIF log entries should not contain node sync messages during set MO operation.", cifLogEntries, is(empty()));

        setTestStep("Clean Up: Reset the value of attribute(s) to original value(s)");
        setMoCliOperator.setAttributeValues(moUnderTest, moAttributesBeforeSet);
    }
}
