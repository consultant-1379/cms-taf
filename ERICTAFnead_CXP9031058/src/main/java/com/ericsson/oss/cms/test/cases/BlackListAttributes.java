/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.BLACKLIST_FILTER_REQUIRED_NEAD;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static org.hamcrest.Matchers.notNullValue;

import java.util.*;

import javax.inject.Inject;

import org.testng.annotations.*;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.operators.*;
import com.ericsson.oss.taf.cshandler.*;
import com.ericsson.oss.taf.cshandler.model.*;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author xaggpar
 */
@SuppressWarnings("deprecation")
public class BlackListAttributes extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final CppAvcCliOperator cppAvcOperator = new CppAvcCliOperator();

    private final CppAvcCliOperatorCDB cppAvcOperatorCdb = new CppAvcCliOperatorCDB();

    @Inject
    private OperatorRegistry<SetMoOperator> setMoOperatorRegistry;

    // @Inject
    // private SyncNodesCliOperator syncOperator;

    @Inject
    private BlackListAttributeCliOperator blackListoperator;

    private final SMHandler smtool = new SmtoolHandler(HostGroup.getOssmaster());

    private String pedParameterValue = "false";

    private static final int WAIT_TIME = 180 * 1000;

    // private List<String> blackListedMoAttrList;

    @BeforeTest
    void beforeTest() {
        // blackListedMoAttrList = blackListoperator.readBlackListAttrFileContent(); // Reading NEADBlacklistAttribute.csv file content.
        pedParameterValue = smtool.getConfigurationForMC(NEAD_MC, BLACKLIST_FILTER_REQUIRED_NEAD); // Read PED parameter value.
        if (pedParameterValue != null && pedParameterValue.equalsIgnoreCase("false")) {
            setTestStep("Set blacklistFilterRequired_NEAD Ped paramter value to true for NEAD MC");
            final boolean commandResult = smtool.setConfigurationForMC(NEAD_MC, BLACKLIST_FILTER_REQUIRED_NEAD, "true");
            assertTrue("failed to set value of parameter", commandResult);
            smtool.coldrestart(NEAD_MC);
            final boolean mcStart = smtool.isMCStarted(NEAD_MC, WAIT_TIME);
            assertThat("BlackListAttributes >> MC did not restart within 180 seconds in beforeTest()", mcStart);
        }
    }

    /**
     * @DESCRIPTION Test case to automate NEAD - Support for filtering (blacklist) at attribute level (at Network Element Type) for MO Sync
     *              and Notification for CPP nodes.
     * @PRIORITY HIGH
     */
    @TestId(
            id = "OSS-175059",
            title = "Automating NEAD - Support for filtering (blacklist) at attribute level (at Network Element Type) for MO Sync and Notification for CPP nodes.")
    @Context(context = { Context.CLI })
    @DataDriven(name = "blackListAttributes")
    @Test(groups = { "NEAD, KGB" })
    public void blackListAttributes(
            @Input("nodeType") final String nodeType,
            @Input("moType") final String moType,
            @Input("attribute") final String attribute,
            @Input("primaryAttributeValue") final String primaryAttributeValues[]) {
        // if (blackListedMoAttrList.contains(moType + ":" + attribute)) {

        final SetMoOperator setMoOperator = setMoOperatorRegistry.provide(SetMoOperator.class);

        setTestStep("Get required Mo type on a synched node of node type from OSS database");
        final Fdn moChildFdn = nodeOperator.getChildMoFromSyncedNode(nodeType, moType);
        assertThat("No mo child of type " + moType + " has been found for node type " + nodeType, moChildFdn, notNullValue());

        setTestInfo("Extract node Fdn from mo child Fdn");
        final Fdn syncedNodeFdn = moChildFdn.getMeContext();
        setTestInfo("Synced node under test is: " + syncedNodeFdn);

        final NetworkElement elem = cppAvcOperatorCdb.getNetworkElementForFdn(syncedNodeFdn);

        final List<NetworkElement> elemList = new ArrayList<NetworkElement>();
        elemList.add(elem);

        setTestStep("Read the attribute value(s) on mo from the database and store the same attribute value(s) for purpose of resetting the values.");
        final Attribute attributesBefore = csHandler.getAttributes(moChildFdn, attribute).get(0);

        setTestStep("Check notifications in NEAD notification log file on OSS.");
        setTestInfo("Check number of notifications in log file in OSS for selected NE with MO and attribute BEFORE setting the attribute value in NETSIM");
        final Map<NetworkElement, Integer> notificationsBefore = cppAvcOperator.countReceivedNotifications(elemList, moChildFdn.getLdn(), attribute);
        for (final NetworkElement fdn : notificationsBefore.keySet()) {
            setTestInfo("Key" + fdn + "Values " + notificationsBefore.get(fdn));
        }

        setTestStep("Select attribute value(s) to set.");
        final String valueForNetsim = blackListoperator.prepareValueToSetOnNetsim(attributesBefore, primaryAttributeValues);
        final Attribute att = new CSAttribute(attribute, valueForNetsim, attributesBefore.getType());
        final List<Attribute> listAttr = new ArrayList<Attribute>();
        listAttr.add(att);

        setTestStep("Set attribute value(s) on node");
        final boolean commandResult = setMoOperator.setAttributeValues(moChildFdn, listAttr);
        assertTrue("Setting of attribute on node did not succeed", commandResult);

        setTestStep("Wait for notifications in OSS from node for above attributes");
        sleep(30); // Sleep for 30 seconds.

        setTestStep("Check notifications in NEAD notification log file on OSS.");
        setTestInfo("Check number of notifications in log file in OSS for selected NE with MO and attribute AFTER setting the attribute value in NETSIM");
        final Map<NetworkElement, Integer> notificationsAfter = cppAvcOperator.countReceivedNotifications(elemList, moChildFdn.getLdn(), attribute,
                notificationsBefore);
        for (final NetworkElement fdn : notificationsAfter.keySet()) {
            setTestInfo("Key" + fdn + "Values " + notificationsAfter.get(fdn));
            assertEquals("Notifications found in OSS/ notification logs for attributes of given MO type.", notificationsAfter.get(fdn).intValue(), 0);
        }

        setTestStep("Read the attribute value(s) on mo from the database and verify the original value and current value");
        final Attribute attributesAfterNotif = csHandler.getAttributes(moChildFdn, attribute).get(0);
        assertEquals("Atribute value got updated in OSS after changing attribute value in netsim: ", attributesAfterNotif.getValue(),
                attributesBefore.getValue());

        /*
         * setTestStep("Perform Sync on the node.");
         * csHandler.adjust(syncedNodeFdn);
         * final List<Fdn> fdnList = new ArrayList<Fdn>();
         * fdnList.add(syncedNodeFdn);
         * final List<Fdn> failedNodes = syncOperator.checkForFailedNodes(fdnList, 180);
         * assertThat("Failed to sync node within 180 seconds.: " + failedNodes, failedNodes, hasSize(0));
         * setTestStep("Read the attribute value(s) on mo from the database and verify the original value and current value");
         * final List<Attribute> attributesAfterSync = csHandler.getAttributes(moChildFdn, attribute);
         * assertEquals("Values are different after sync : ", attributesAfterSync, attributesBeforeSync);
         */

        setTestStep("Clean Up:Reset the value of attribute(s) to original values");
        setMoOperator.setAttributeValues(moChildFdn, new String[] { attributesBefore.getName() }, new String[] { attributesBefore.getValue() });

        /*
         * } else {
         * logger.info("BlackListAttributes >> MO::Attribute - " + moType + "::" + attribute
         * + " is not present in the NEADBlacklistAttribute.csv file, skipping the use case for this MO::Attribute");
         * // }
         */
    }

    @AfterTest
    void afterTest() {
        if (pedParameterValue.equalsIgnoreCase("false")) {
            setTestStep("Set blacklistFilterRequired_NEAD Ped paramter value to false for NEAD MC");
            final boolean commandResult = smtool.setConfigurationForMC(NEAD_MC, BLACKLIST_FILTER_REQUIRED_NEAD, "false");
            assertTrue("Failed to set value of ped parameter : afterTest", commandResult);
            smtool.coldrestart(NEAD_MC);
            final boolean mcStart = smtool.isMCStarted(NEAD_MC, WAIT_TIME);
            assertThat("BlackListAttributes >> MC did not restart within 180 seconds in afterTest()", mcStart);
        }
    }

}
