/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MIB_ADAPTER;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.NodeOperator;
import com.ericsson.oss.cms.test.operators.SetMoOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesOperator;
import com.ericsson.oss.cms.test.util.KpiUtil;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;
import com.ericsson.oss.cms.test.operators.CppAvcCliOperatorCDB;

/**
 * @author xmanvas
 */
public class AFITDeltaSync extends TorTestCaseHelper implements TestCase {

    private final SMHandler smtool = new SmtoolHandler(HostGroup.getOssmaster());

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Inject
    private OperatorRegistry<NodeOperator> nodeOperatorRegistry;

    @Inject
    private OperatorRegistry<SetMoOperator> setMoOperatorRegistry;

    @Inject
    private OperatorRegistry<SyncNodesOperator> syncNodesOperatorRegistry;

    private Integer accumulatedSyncTime = 0;

    private static int SLEEP_THREAD_40_SECONDS = 40;

    private final CppAvcCliOperatorCDB cppAvcOperatorCdb = new CppAvcCliOperatorCDB();

    /**
     * @DESCRIPTION Verifies that when Delta Sync is enabled and there is a change made to an mo on the node,
     *              NEAD will do a delta sync.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-134031", title = "Perform AFIT DeltaSync on node")
    @Context(context = { Context.CLI })
    @DataDriven(name = "deltasync_afit")
    @Test(groups = { "NEAD, KGB" })
    public void DeltaSync(
            @Input("nodeType") final String nodeType,
            @Input("moType") final String moType,
            @Input("attributes") final String[] attributes,
            @Input("primaryAttributeValues") final String[] primaryAttributeValues,
            @Input("secondaryAttributeValues") final String[] secondaryAttributeValues,
            @Input("timesToRun") final int timesToRun,
            @Input("attributeValueGC") final String[] attributeValueGC) {

        final NodeOperator nodeOperator = nodeOperatorRegistry.provide(NodeOperator.class);
        final SetMoOperator setMoOperator = setMoOperatorRegistry.provide(SetMoOperator.class);
        final SyncNodesOperator syncNodesOperator = syncNodesOperatorRegistry.provide(SyncNodesOperator.class);

        setTestStep("Read deltaSyncEnabled parameter for NEAD in OSS");
        final String deltaSyncValue = smtool.getConfigurationForMC(NEAD_MC, "deltaSyncEnabled");
        assertEquals("Delta sync is not enabled", "true", deltaSyncValue);

        setTestStep("Get an MO of moType on a node of nodeType from OSS database");
        final Fdn moChildFdn = nodeOperator.getChildMoFromSyncedNode(nodeType, moType);
        assertThat("No mo child of type " + moType + " has been found for node type " + nodeType, moChildFdn, notNullValue());

        setTestStep("Extract node Fdn from mo child Fdn");
        final Fdn syncedNodeFdn = moChildFdn.getMeContext();
        setTestInfo("Synced node under test is: " + syncedNodeFdn);

        setTestStep("Read the attribute value(s) on mo from the database. Store attribute value(s) for purpose of resetting the values.");
        final List<Attribute> attributesBeforeDeltaSync = csHandler.getAttributes(moChildFdn, attributes);

        setTestStep("Select attribute value(s) to set");
        final String[] selectedAttributeValues = setMoOperator.selectAttributeValuesToSet(attributes, attributesBeforeDeltaSync, primaryAttributeValues,
                secondaryAttributeValues);

        accumulatedSyncTime = 0;

        for (int countSyncRuns = 1; countSyncRuns <= timesToRun; ++countSyncRuns) {

            setTestInfo("Running Deltasync command on node " + countSyncRuns + " of " + timesToRun + " times");

            setTestStep("Read generation counter value before delta sync and store it.");
            final int genCounterBeforeDeltaSync = nodeOperator.getGenerationCounter(syncedNodeFdn);

            if (genCounterBeforeDeltaSync == 0) {
                setTestInfo("Trying to Change attribute(GC value) for nodeType=" + nodeType + " as its GC is 0.");
                cppAvcOperatorCdb.setAttributeValues(moChildFdn, attributes[0], attributeValueGC[0]);
                sleep(SLEEP_THREAD_40_SECONDS);
                final int gcAfterSetAttr = nodeOperator.getGenerationCounter(syncedNodeFdn);
                setTestInfo("Generation counter value after setting test value and wait for " + SLEEP_THREAD_40_SECONDS + " seconds : " + gcAfterSetAttr);
            }

            setTestStep("Detach node from the NEAD mibadapter");
            csHandler.detach(syncedNodeFdn);
            assertFalse("Node is still attached to NEAD mibadapter", csHandler.isAttached(syncedNodeFdn, NEAD_MIB_ADAPTER));

            setTestStep("Set attribute value(s) on node");
            final long testStartTime = System.currentTimeMillis();
            final boolean commandResult = setMoOperator.setAttributeValues(moChildFdn, attributes, selectedAttributeValues);
            assertTrue("Setting of attribute on node did not succeed", commandResult);

            setTestStep("Attach node to NEAD mibadapter");
            csHandler.attach(syncedNodeFdn, NEAD_MIB_ADAPTER);
            assertTrue("Node is not attached to NEAD mibadapter", csHandler.isAttached(syncedNodeFdn, NEAD_MIB_ADAPTER));

            setTestStep("Check the sync state of node in the database");
            final List<Fdn> fdnAsList = Arrays.asList(syncedNodeFdn);
            final List<Fdn> failedNode = syncNodesOperator.checkForFailedNodes(fdnAsList, 60);
            assertThat("Node has failed to sync", failedNode, is(empty()));

            setTestStep("Read the CIF Logs for Delta Sync message related to the target NE");
            final List<CIFLogItem> deltaSyncLogEntries = syncNodesOperator.getDeltaSyncCifLogEntry(fdnAsList, testStartTime);
            assertThat("No Delta Sync has been observed in the CIF Logs", deltaSyncLogEntries, not(empty()));

            setTestStep("Read the sync Total Time value from the syncInfoLog and add to accumulated total time so far");
            final Integer syncTotalTime = KpiUtil.getTotalTimeFromLog(deltaSyncLogEntries);
            assertThat("Expected sync total time value was not returned ", syncTotalTime, notNullValue());
            accumulatedSyncTime += syncTotalTime;

            setTestStep("Get the mo under test and read the attribute value(s)in the database");
            final boolean attributeValueIsSetAfterDeltaSync = setMoOperator.isAttributesSetInDatabase(moChildFdn, attributes, selectedAttributeValues);
            assertTrue("Delta Sync did not result in attribute value being set in database", attributeValueIsSetAfterDeltaSync);

            setTestStep("Read generation counter value after delta sync");
            final int genCounterAfterDeltaSync = nodeOperator.getGenerationCounter(syncedNodeFdn);
            assertThat("Generation Counter has not increased after delta sync", genCounterAfterDeltaSync, is(greaterThan(genCounterBeforeDeltaSync)));

            setTestStep("Clean Up:Reset the value of attribute(s) to original values");
            setMoOperator.setAttributeValues(moChildFdn, attributesBeforeDeltaSync);
        }
        setTestStep("Calculate the average sync total time as accumulated total time/number of test runs");
        final Integer avgSyncTotalTime = accumulatedSyncTime / timesToRun;
        setTestInfo("Average Total sync time: " + avgSyncTotalTime + " secs");

    }
}
