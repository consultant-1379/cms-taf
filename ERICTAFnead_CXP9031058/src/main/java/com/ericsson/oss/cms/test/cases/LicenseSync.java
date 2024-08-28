package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.constants.CmsConstants.NodeLicNeadContants;
import com.ericsson.oss.cms.test.operators.LicenseSyncCliOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

public class LicenseSync extends TorTestCaseHelper implements TestCase {

    @Inject
    private SyncNodesCliOperator syncOperator;

    @Inject
    private LicenseSyncCliOperator licenseSyncOperator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SMHandler smHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private static final int WAIT_TIME = 180 * 1000;

    private static Map<String, Boolean> licScenarioMap;

    private static String initialState = "";

    private static List<Fdn> listOfActiveNodes;

    @BeforeTest
    void beforeTest() {
        licScenarioMap = new LinkedHashMap<String, Boolean>(5);
        licScenarioMap.put("19XLicPresent", false);
        licScenarioMap.put("17BLicPre_18ALicPre_19XLicAb", false);
        licScenarioMap.put("17BLicPre_18ALicAb_19XLicAb", false);
        // licScenarioMap.put("17BLicAbs_18ALicPre", false);// Skip this scenarios.
        licScenarioMap.put("AllLicAbsent", false);
        licScenarioMap = licenseSyncOperator.prepareServer(licScenarioMap);
    }

    /**
     * @DESCRIPTION Test case to cover node sync for all CPP nodes and
     *              to validate that all nodes supported by NEAD are synched
     *              with the right MIM version when 17B or 18A licenses are installed/ removed.
     * @PRE None
     * @PRIORITY HIGH
     */
    @SuppressWarnings("deprecation")
    @TestId(id = "OSS-163222 / OSS-172626", title = "Node sync for all CPP nodes with different license scenarios")
    @Context(context = { Context.CLI })
    @DataDriven(name = "licensesync")
    @Test(groups = { "CDB", "NEAD", "KGB", "GAT" })
    public void LicenseSyncTest(@Input("neType") final String nodeType, @Input("percentage") final int percentage) {
        boolean mibVer = true;

        setTestStep("Get all Connected Node by Type.");
        listOfActiveNodes = licenseSyncOperator.findAllConnectedNodes(nodeType);
        assertThat("There are no nodes added ", listOfActiveNodes, hasSize(greaterThan(0)));

        for (final String key : licScenarioMap.keySet()) {
            logger.info("key : " + key + " value : " + licScenarioMap.get(key));
            if (licScenarioMap.get(key)) {
                initialState = key;
            }
        }

        setTestStep("Perform below steps for different 17B ,18A and 19X license scenarios.");
        for (final String licKey : licScenarioMap.keySet()) { // licScenarioMap will have 3 entries now.
            final Map<String, Boolean> licValueMap = new HashMap<String, Boolean>();
            if (!licScenarioMap.get(licKey)) {
                if (licKey.equalsIgnoreCase("19XLicPresent")) {
                    licenseSyncOperator.installAndRemoveLic(new String[] { NodeLicNeadContants.INSTALL_17B_LIC, NodeLicNeadContants.INSTALL_18A_LIC,
                            NodeLicNeadContants.INSTALL_19X_LIC }, null);
                } else if (licKey.equalsIgnoreCase("17BLicPre_18ALicPre_19XLicAb")) {
                    licenseSyncOperator.installAndRemoveLic(new String[] { NodeLicNeadContants.INSTALL_17B_LIC, NodeLicNeadContants.INSTALL_18A_LIC },
                            new String[] { NodeLicNeadContants.REMOVE_19X_NODE_LICENSE });
                } else if (licKey.equalsIgnoreCase("17BLicPre_18ALicAb_19XLicAb")) {
                    licenseSyncOperator.installAndRemoveLic(new String[] { NodeLicNeadContants.INSTALL_17B_LIC }, new String[] {
                            NodeLicNeadContants.REMOVE_18A_NODE_LICENSE, NodeLicNeadContants.REMOVE_19X_NODE_LICENSE });
                } else if (licKey.equalsIgnoreCase("AllLicAbsent")) {
                    licenseSyncOperator.installAndRemoveLic(null, new String[] { NodeLicNeadContants.REMOVE_17B_NODE_LICENSE,
                            NodeLicNeadContants.REMOVE_18A_NODE_LICENSE, NodeLicNeadContants.REMOVE_19X_NODE_LICENSE });
                }
            }
            setTestStep("Check MC is online");
            final boolean mcStart_ = smHandler.isMCStarted(NEAD_MC, WAIT_TIME);
            assertThat("MC did not restart within 180 seconds", mcStart_);
            logger.info("MC Restarted");

            setTestStep("Get 17B , 18A and 19X license status from the server.");
            final boolean lic17B_ = licenseSyncOperator.script(NodeLicNeadContants.NODE_LIC_17B_CHECK_CMD);
            final boolean lic18A_ = licenseSyncOperator.script(NodeLicNeadContants.NODE_LIC_18A_CHECK_CMD);
            final boolean lic19X_ = licenseSyncOperator.script(NodeLicNeadContants.NODE_LIC_19X_CHECK_CMD);
            licValueMap.put(NodeLicNeadContants.NODE_LIC_17B, lic17B_);
            licValueMap.put(NodeLicNeadContants.NODE_LIC_18A, lic18A_);
            licValueMap.put(NodeLicNeadContants.NODE_LIC_19X, lic19X_);

            if (!licScenarioMap.get(licKey)) {
                setTestStep("Perform Sync on nodes.");
                licenseSyncOperator.startSyncOnNodes(listOfActiveNodes);
            }
            setTestStep("Check for failed nodes.");
            final List<Fdn> failedNodes_ = licenseSyncOperator.checkForFailedNodes(listOfActiveNodes, percentage);
            assertThat("List of failed Nodes: " + failedNodes_, failedNodes_, hasSize(0));

            setTestStep("Check for Success nodes.");
            final List<Fdn> successNodes_ = licenseSyncOperator.checkForSuccessCPPNodes(listOfActiveNodes, percentage);

            setTestStep("Verfiy the mirrormibversion with the mom properties file,If license is not installed ");
            mibVer = licenseSyncOperator.readMirrorMibVersionFromCPPfile(successNodes_, licValueMap);
            if (mibVer) {
                logger.info("Scenario: " + licKey + " passed with proper value of MibVersions.");
            } else {
                assertTrue("Scenario: " + licKey + " failed because MibVersions differ from property file.", mibVer);
            }
        }

    }

    @AfterTest
    void afterTest() {
        setTestStep("Remove the sync script from server");
        licenseSyncOperator.removeCopiedFilesAndRestoreServer(initialState);
        setTestStep("Check MC is online");
        final boolean mcStart = smHandler.isMCStarted(NEAD_MC, WAIT_TIME);
        assertThat("MC did not restart within 180 seconds in afterTest()", mcStart);
        logger.info("MC Restarted");
        syncOperator.startSyncOnNodes(listOfActiveNodes);
    }
}
