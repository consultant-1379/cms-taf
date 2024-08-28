/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.GENERATION_COUNTER;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MIB_ADAPTER;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
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
 * @author egergro
 */
public class FullSyncWithAVC extends TorTestCaseHelper implements TestCase {

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Inject
    private NodeCliOperator nodeOperator;

    @Inject
    private SetMoCliOperator setMoOperator;

    @Inject
    private SyncNodesCliOperator syncNodesOperator;

    private static final String GENERATION_COUNTER_RESET = "-1";

    /**
     * @DESCRIPTION Verifies that when there is a change made to an mo on the node while the node is detached from NEAD,
     *              NEAD will do a full sync when node is re-attached.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-68308_FullSyncWithAttributeChange", title = "Full Sync With AVC")
    @Context(context = { Context.CLI })
    @DataDriven(name = "deltasync")
    @Test(groups = { "NEAD", "KGB" })
    public void fullSync(
            @Input("nodeType") final String nodeType,
            @Input("moType") final String moType,
            @Input("attributes") final String[] attributes,
            @Input("primaryAttributeValues") final String[] primaryAttributeValues,
            @Input("secondaryAttributeValues") final String[] secondaryAttributeValues) {

        setTestStep("Get an MO of required type on a synced node of required type from OSS database");
        final Fdn moChildFdn = nodeOperator.getChildMoFromSyncedNode(nodeType, moType);
        assertThat("No mo child of type " + moType + " has been found for node type " + nodeType, moChildFdn, notNullValue());

        setTestInfo("Extract node Fdn from mo child Fdn");
        final Fdn syncedNodeFdn = moChildFdn.getMeContext();
        setTestInfo("Synced node under test is: " + syncedNodeFdn);

        setTestStep("Read the attribute values on mo from the database and store values");
        final List<Attribute> attributesBeforeSync = csHandler.getAttributes(moChildFdn, attributes);

        setTestStep("Select attribute value(s) to set");
        final String[] selectedAttributeValues = setMoOperator.selectAttributeValuesToSet(attributes, attributesBeforeSync, primaryAttributeValues,
                secondaryAttributeValues);

        setTestStep("Read generation counter value before sync");
        final int genCounterBeforeSync = nodeOperator.getGenerationCounter(syncedNodeFdn);

        setTestStep("Detach node from the NEAD mibadapter");
        csHandler.detach(syncedNodeFdn);
        assertFalse("Node is still attached to NEAD mibadapter", csHandler.isAttached(syncedNodeFdn, NEAD_MIB_ADAPTER));

        setTestStep("Set attribute value(s) on node");
        final long cifLogReadStartTime = System.currentTimeMillis();
        final boolean commandResult = setMoOperator.setAttributeValues(moChildFdn, attributes, selectedAttributeValues);
        assertTrue("Setting of attribute on node did not succeed", commandResult);

        setTestStep("Set generation counter value to -1 in database");
        csHandler.setAttributeValue(syncedNodeFdn, GENERATION_COUNTER, GENERATION_COUNTER_RESET);
        assertEquals("Generation counter is not updated to -1", csHandler.getAttributeValue(syncedNodeFdn, GENERATION_COUNTER), GENERATION_COUNTER_RESET);

        setTestStep("Attach node to NEAD mibadapter");
        csHandler.attach(syncedNodeFdn, NEAD_MIB_ADAPTER);
        assertTrue("Node is not attached to NEAD mibadapter", csHandler.isAttached(syncedNodeFdn, NEAD_MIB_ADAPTER));

        setTestStep("Check the sync state of node in the database");
        final List<Fdn> failedNode = syncNodesOperator.checkForFailedNodes(Arrays.asList(syncedNodeFdn), 300);
        assertThat("Node has failed to sync", failedNode, is(empty()));

        setTestStep("Read the CIF logs for Sync Progress logs related to the target NE");
        final List<CIFLogItem> syncLogEntries = syncNodesOperator.getSyncStatusCifLogEntries(Arrays.asList(syncedNodeFdn), cifLogReadStartTime);
        final int expectedNoLogs = syncNodesOperator.getExpectedNumCifLogs(1);
        assertThat("Expected no. of CIF logs does not match retrived logs ", syncLogEntries, hasSize(expectedNoLogs));

        setTestStep("Query CIF logs for the syncInfoLog entry");
        final List<CIFLogItem> cifSyncInfoLog = syncNodesOperator.getSyncInfoCifLogEntries(Arrays.asList(syncedNodeFdn), cifLogReadStartTime);
        assertThat("number of SYNCINFO logs does not match retrieved logs", cifSyncInfoLog, hasSize(1));

        setTestStep("Read the mo attribute value(s) in the database after Sync");
        final boolean attributeValueIsSetAfterSync = setMoOperator.isAttributesSetInDatabase(moChildFdn, attributes, selectedAttributeValues);
        assertTrue("Full Sync did not result in attribute value being set in database", attributeValueIsSetAfterSync);

        setTestStep("Read generation counter value after sync");
        final int genCounterAfterSync = nodeOperator.getGenerationCounter(syncedNodeFdn);
        assertThat("Generation Counter has not increased after sync", genCounterAfterSync, is(greaterThan(genCounterBeforeSync)));

        setTestStep("Clean Up:Reset the value of attribute(s) to original values");
        setMoOperator.setAttributeValues(moChildFdn, attributesBeforeSync);

    }
}
