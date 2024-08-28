/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.CC_RESUMED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.LONG_SLEEP;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_UNSYNCHRONIZED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.COLDRESTART_WAIT_TIME;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.model.MibAdapterSyncMessages;
import com.ericsson.oss.cms.test.model.attributes.NeDetails;
import com.ericsson.oss.cms.test.operators.CIFLogCliOperator;
import com.ericsson.oss.cms.test.operators.MoCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SetMoCliOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SelfTestResult;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author eeimacn
 */
public class VerifySNADCacheMaps extends TorTestCaseHelper implements TestCase {

    private final static int LOGWAITTIME = 60;

    private final static int LONGSLEEPWAITTIME = 2700;

    private static final String SELFTEST_CACHE_INTEGRITY = "6";

    private static final String NMA = "nma1";

    private static final int sleepTime = 30;

    @Inject
    private CIFLogCliOperator cifLogCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private MoCliOperator moCliOperator;

    @Inject
    private SetMoCliOperator setMoOperator;

    @Inject
    private SnadApiOperator snadApiOperator;

    private final SmtoolHandler smtoolHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private Fdn activeMoFdn;

    private List<Attribute> attributesBeforeDeltaSync;

    private boolean skip;

    private final int numCells = 1;

    @BeforeTest
    public void setup() {
        SelfTestResult selfTestResult = smtoolHandler.selfTest(SNAD_MC, SELFTEST_CACHE_INTEGRITY);
        if (selfTestResult == SelfTestResult.NOTDEFINED) {
            this.skip = true;
        } else {
            setTestStep("Coldrestart SNAD MC");
            smtoolHandler.coldrestart(SNAD_MC);
            assertTrue("SNAD MC has failed to come online in given time", smtoolHandler.isMCStarted(SNAD_MC, COLDRESTART_WAIT_TIME));

            setTestStep("Wait for log message in CIF logs indicating recovery has finished");
            List<CIFLogItem> cifLogItems = cifLogCliOperator.waitForExpectedMessages(LOGWAITTIME, CC_RESUMED);
            assertThat("Recovery has failed to complete. CC has not restarted", cifLogItems, is(not(empty())));

            setTestStep("Wait for log message in CIF logs indicating CC has gone for long sleep");
            cifLogItems = cifLogCliOperator.waitForExpectedMessages(LONGSLEEPWAITTIME, LONG_SLEEP);
            assertThat("CC has not gone for long sleep", cifLogItems, is(not(empty())));

            setTestStep("Run self test to verify that MOs in cache are unique and maps are not corrupt");
            selfTestResult = smtoolHandler.selfTest(SNAD_MC, SELFTEST_CACHE_INTEGRITY);
            assertEquals("MOs in Cache are not unique or maps are corrupt", selfTestResult, SelfTestResult.PASSED);
        }
    }

    /**
     * @throws InterruptedException
     * @DESCRIPTION This test is to test that no MO is found in the SNAD caches more than once.
     * @PRE SNAD & NEAD mc is up
     *      SubNetwork is recovered and SubNetwork MOs are in SNAD cache.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-73132_Verify that MO only exists once in Cache", title = "Verify that MO only exists once in Cache")
    @Context(context = { Context.CLI })
    @DataDriven(name = "verifySnadMaps")
    @Test(groups = { "KGB" })
    public void verifyMoOnlyExistsOnceInCache(
            @Input("nodeType") final String nodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("moType") final String[] moType,
            @Input("attributeName") final String[] attributeName,
            @Input("primaryAttributeValue") final String[] primaryAttributeValue,
            @Input("secondaryAttributeValue") final String[] secondaryAttributeValue) throws InterruptedException {

        if (!skip) {
            setTestStep("Get a connected and synced " + nodeType);
            final Fdn activeNodeFdn = nodeCliOperator.getSyncedNode(csHandler, nodeType,
                    NodeCliOperator.buildNodeFiltering(sourceNodeAdditionalFiltering, numCells, moType));
            assertNotNull("No synced node found.", activeNodeFdn);
            setTestInfo("Selected node: " + activeNodeFdn);

            setTestStep("Check that the node is recovered.");
            final String functionMOType = NeDetails.getNeFunctionMo(nodeType);
            final Fdn functionMO = moCliOperator.getChildMoFdn(activeNodeFdn, functionMOType);
            String isMoInMasterCache = snadApiOperator.getMaster(functionMO);
            assertNotNull("Selected node " + functionMO.getFdn() + " is not in managed cache.", isMoInMasterCache);

            setTestStep("Get an MO from node " + activeNodeFdn);
            final List<Fdn> activeMoFdns = moCliOperator.getChildrenMoFdns(activeNodeFdn, moType);
            activeMoFdn = activeMoFdns.get(0);
            assertNotNull("No " + moType + " MOs have been found.", activeMoFdn);
            setTestInfo("Selected MO: " + activeMoFdn);

            setTestStep("Read the attribute value(s) on MO from the database. Store attribute value(s) for purpose of resetting the values.");
            attributesBeforeDeltaSync = csHandler.getAttributes(activeMoFdn, attributeName);

            setTestStep("Select attribute value(s) to set");
            final String[] selectedAttributeValues = setMoOperator.selectAttributeValuesToSet(attributeName, attributesBeforeDeltaSync, primaryAttributeValue,
                    secondaryAttributeValue);
            assertThat("Could not select attributes to use for Set command", selectedAttributeValues, is(not(emptyArray())));

            setTestStep("Detach the MibAdapter");
            final String mibAdapterName = csHandler.getMibAdapterName(activeNodeFdn);
            csHandler.detach(activeNodeFdn);
            assertFalse("Node is still attached to mibadapter", csHandler.isAttached(activeNodeFdn, mibAdapterName));
            if (NMA.equals(mibAdapterName)) {
                setTestInfo("Sleeping for " + sleepTime + " seconds to wait for node to go unsynched");
                Thread.sleep(sleepTime * 1000);
            }
            final String nodeSyncStateInCS = csHandler.getAttributeValue(activeNodeFdn, SYNCH_STATUS);
            assertEquals("Node is not unsynced.", nodeSyncStateInCS, Integer.toString(SYNCH_STATUS_UNSYNCHRONIZED));

            setTestStep("Set attribute value(s) on node");
            final boolean commandResult = setMoOperator.setAttributeValues(activeMoFdn, attributeName, selectedAttributeValues);
            assertTrue("Setting of attribute on node did not succeed", commandResult);

            setTestStep("Re-attach MibAdapter");
            csHandler.attach(activeNodeFdn, mibAdapterName);
            assertTrue("Node is not attached to " + mibAdapterName, csHandler.isAttached(activeNodeFdn, mibAdapterName));

            setTestStep("Wait for log message in CIF logs indicating node has resynched");
            final String syncMsg = MibAdapterSyncMessages.getSuccessMessage(mibAdapterName);
            List<CIFLogItem> cifLogItems = cifLogCliOperator.waitForExpectedMessages(LOGWAITTIME, syncMsg);
            assertThat("Node has not resynched: ", cifLogItems, is(not(empty())));

            setTestStep("Wait for log message in CIF logs indicating CC has gone for long sleep");
            cifLogItems = cifLogCliOperator.waitForExpectedMessages(LONGSLEEPWAITTIME, LONG_SLEEP);
            assertThat("CC has not gone for long sleep", cifLogItems, is(not(empty())));

            setTestStep("Check that the node is recovered.");
            isMoInMasterCache = snadApiOperator.getMaster(functionMO);
            assertNotNull("Selected node " + functionMO.getFdn() + " is not in managed cache.", isMoInMasterCache);

            setTestStep("Run self test to verify that MOs in cache are unique and maps are not corrupt");
            final SelfTestResult selfTestResult = smtoolHandler.selfTest(SNAD_MC, SELFTEST_CACHE_INTEGRITY);
            assertNotEquals("MOs in Cache are not unique or maps are corrupt", selfTestResult, SelfTestResult.FAILED);
        } else {
            setTestWarning("SelfTest '" + SELFTEST_CACHE_INTEGRITY + "' is not defined");
        }
    }

    @AfterMethod
    public void cleanup() {
        if (!skip) {
            setTestStep("Clean Up:Reset the value of attribute(s) to original values");
            setMoOperator.setAttributeValues(activeMoFdn, attributesBeforeDeltaSync);
        }
    }
}
