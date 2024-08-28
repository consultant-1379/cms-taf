/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.MAX_TIME_TO_READ_GEN_COUNTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_SDN_ADD;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_SDN_REMOVE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_SEQUENCE_DELTA;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
import com.ericsson.oss.cms.test.operators.CreateMoCliOperator;
import com.ericsson.oss.cms.test.operators.DeleteMoCliOperator;
import com.ericsson.oss.cms.test.operators.LogFileCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SetMoCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.cms.test.operators.InitialSyncCliOperator;


/**
 * @author eeieonl
 */
public class SequenceDeltaNotification extends TorTestCaseHelper implements TestCase {
    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private SetMoCliOperator setMoCliOperator;

    @Inject
    private SyncNodesCliOperator syncNodesCliOperator;

    @Inject
    private DeleteMoCliOperator deleteMoCliOperator;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    public final static String RESERVED_BY = "reservedBy";

    /**
     * @DESCRIPTION Verify that NEAD listens for SDN(Sequence Delta Notifications) from the node, and sets the relevant
     *              reservedBy attributes of the corresponding MO in the database.
     * @PRE NEAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY HIGH
     */

    @TestId(id = "OSS-53407_SDN", title = "Nead handles sequence delta notifications")
    @Context(context = { Context.CLI })
    @DataDriven(name = "sequencedelta")
    @Test(groups = { "NEAD, KGB" })
    public void handleSDNotification(
            @Input("nodeType") final String nodeType,
            @Input("targetMoType") final String targetMoType,
            @Input("sourceMoType") final String sourceMoType,
            @Input("parentMoType") final String parentMoType,
            @Input("attributeNames") String[] attributeNames,
            @Input("attributeValues") String[] attributeValues,
            @Input("referenceAttribute") final String referenceAttribute,
            @Input("referenceType") final String referenceType) {

        setTestStep("Get Target MO type and Parent MO type under the same node from OSS database.");
        final List<Fdn> childFdns = nodeCliOperator.getChildrenFromMimScopedSyncedNode(nodeType, targetMoType, parentMoType);
        assertThat("Provided target MO type and parent MO type were not found on the same synced node.", childFdns, hasSize(2));

        final Fdn targetMoFdn = childFdns.get(0);
        final Fdn parentMoFdn = childFdns.get(1);
        setTestInfo("Target MO found is: " + targetMoFdn + "\nParent MO found is: " + parentMoFdn);

        setTestStep("Read the latest generation counter for the node and store it");
        final int genCounterBeforeSdn = nodeCliOperator.getGenerationCounter(targetMoFdn);

        setTestStep("Store the current time as start time");
        long cifLogStartTime = System.currentTimeMillis();

        setTestInfo("Record the number of %s - %s notifications for MO: %s", NOTIFICATION_SEQUENCE_DELTA, NOTIFICATION_SDN_ADD, targetMoFdn);
        final Fdn childMoFdn = createMoCliOperator.buildMoFdn(parentMoFdn, sourceMoType, getTestId());
        final int notifCountBeforeSdn = logFileCliOperator.getNotificationCount(NOTIFICATION_SEQUENCE_DELTA, targetMoFdn, NOTIFICATION_SDN_ADD,
                childMoFdn.getFdn());

        setTestStep("Create source MO on node with reference attribute set to target MO");
        attributeNames = setMoCliOperator.addMoRefData(attributeNames, referenceAttribute);
        attributeValues = setMoCliOperator.addMoRefData(attributeValues, targetMoFdn.getLdn(), referenceType);
        setTestStep("Checking Cardinality and Deleting the Required MO");
        final boolean deleteFlag = initialSyncCliOperator.findAndDeleteMOsFromCS(childMoFdn);
        assertTrue("Deleting ExternalEUtranCellFDD  MO is SUCCESS ", deleteFlag);
        final boolean moCreatedInNetSim = createMoCliOperator.createMo(childMoFdn, attributeNames, attributeValues);
        assertTrue("Failed to create MO in NETSIM", moCreatedInNetSim);

        boolean moExistInCS = csHandler.moExists(childMoFdn);
        assertTrue("Failed to create MO in CS database", moExistInCS);

        final boolean attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(childMoFdn, attributeNames, attributeValues);
        assertTrue("Created MO attributes in database does not match with input data", attributesSetInDatabase);

        String targetReservedBy = csHandler.getAttributeValue(targetMoFdn, RESERVED_BY);
        assertThat("ReservedBy attribute not set ", targetReservedBy, containsString(childMoFdn.getFdn()));
        setTestInfo("Target ReservedBy attribute contains: " + childMoFdn.getFdn());

        setTestStep("Check the latest generation counter from the node has increased and store it");
        final int genCounterAfterSdnAdd = nodeCliOperator.getIncreasedGenerationCounter(targetMoFdn.getMeContext(), genCounterBeforeSdn,
                MAX_TIME_TO_READ_GEN_COUNTER);
        assertThat("Generation counter is not increased after SDN add", genCounterAfterSdnAdd, is(greaterThan(genCounterBeforeSdn)));

        setTestStep("Check Nead notification logs for SDN Add notification");
        final int notifCountAfterSdnAdd = logFileCliOperator.getNotificationCount(NOTIFICATION_SEQUENCE_DELTA, targetMoFdn, NOTIFICATION_SDN_ADD,
                childMoFdn.getFdn());
        assertThat("SDN Add notification is missing in notification logs", notifCountAfterSdnAdd, is(equalTo(notifCountBeforeSdn + 1)));

        setTestStep("Check the CIF logs for node sync logs for the node");
        List<CIFLogItem> cifLogEntries = syncNodesCliOperator.getSyncStatusCifLogEntries(Arrays.asList(targetMoFdn.getMeContext()), cifLogStartTime);
        assertThat("CIF log entries should not contain node sync messages during SDN add", cifLogEntries, is(empty()));

        setTestInfo("Record the number of %s - %s notifications for MO: %s", NOTIFICATION_SEQUENCE_DELTA, NOTIFICATION_SDN_REMOVE, targetMoFdn);
        final int notifCountBeforeSdnRemove = logFileCliOperator.getNotificationCount(NOTIFICATION_SEQUENCE_DELTA, targetMoFdn, NOTIFICATION_SDN_REMOVE,
                childMoFdn.getFdn());
        cifLogStartTime = System.currentTimeMillis();

        setTestStep("Delete the source MO created in Netsim");
        final boolean moDeletedOnNode = deleteMoCliOperator.deleteMo(childMoFdn);
        assertThat("Mo is not deleted in Netsim", moDeletedOnNode, equalTo(true));
        setTestInfo("Check MO does not exist in CS");
        moExistInCS = csHandler.moExists(childMoFdn);
        assertThat("MO is not deleted in OSS", moExistInCS, equalTo(false));

        targetReservedBy = csHandler.getAttributeValue(targetMoFdn, RESERVED_BY);
        assertThat("Deleted MO reference was not removed from " + RESERVED_BY + " attribute on target MO", targetReservedBy,
                not(containsString(childMoFdn.getFdn())));
        setTestStep("Check the latest generation counter from the node has increased");
        final int genCounterAfterSdnRemove = nodeCliOperator.getIncreasedGenerationCounter(targetMoFdn.getMeContext(), genCounterAfterSdnAdd,
                MAX_TIME_TO_READ_GEN_COUNTER);
        assertThat("Generation counter is not increased after SDN remove", genCounterAfterSdnRemove, is(greaterThan(genCounterAfterSdnAdd)));

        setTestStep("Check Nead notification logs for SDN Remove notification");
        final int notifCountAfterSdnRemove = logFileCliOperator.getNotificationCount(NOTIFICATION_SEQUENCE_DELTA, targetMoFdn, NOTIFICATION_SDN_REMOVE,
                childMoFdn.getFdn());
        assertThat("SDN Remove notification is missing in notification logs", notifCountAfterSdnRemove, is(equalTo(notifCountBeforeSdnRemove + 1)));

        setTestStep("Check CIF logs for node sync logs after SDN remove");
        cifLogEntries = syncNodesCliOperator.getSyncStatusCifLogEntries(Arrays.asList(targetMoFdn.getMeContext()), cifLogStartTime);
        assertThat("CIF log entries should not contain node sync messages during SDN add", cifLogEntries, is(empty()));

    }
}
