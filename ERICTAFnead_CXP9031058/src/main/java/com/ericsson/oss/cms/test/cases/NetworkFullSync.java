/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.InitialSyncCliOperator;
import com.ericsson.oss.cms.test.operators.NetworkFullSyncCliOperator;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author xmanvas
 */
public class NetworkFullSync extends TorTestCaseHelper implements TestCase {

    @Inject
    private NetworkFullSyncCliOperator networkFullSyncCliOperator;

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    @Inject
    TestContext crudMoContext;

    private static final int WAIT_TIME = 180 * 1000;

    private final SMHandler smHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private String actualTimeTakenForSync = "";

    private String actualTimeTaken = "";

    private int noOfNodes = 0;

    private final static String SCRIPT_NAME = "sync.sh";

    private final static String SCRIPT_REMOTE_PATH = "/home/nmsadm/";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeTest
    void beforeTest() {
        setTestStep("Copy the sync script to server");
        initialSyncCliOperator.copyFileToRemote(SCRIPT_NAME, SCRIPT_REMOTE_PATH);

    }

    /**
     * @DESCRIPTION Automating AFIT manual use cases-NEAD full sync
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-131565", title = "Nead AFIT Network FullSync ")
    @Context(context = { Context.CLI })
    @DataDriven(name = "full_sync_kpi")
    @Test(groups = { "NEAD, KGB" })
    public void networkFullSync(@Input("noOfTime") final int noOfTime) {
        boolean syncStatus = false;

        String endTime = "";
        String startTime = "";
        for (int count = 0; count < noOfTime; count++) {

            setTestStep("Execute the full sync script");
            networkFullSyncCliOperator.SyncNodes(SCRIPT_NAME);

            setTestStep("Check MC is online");
            final boolean mcStart = smHandler.isMCStarted(NEAD_MC, WAIT_TIME);
            assertThat("MC did not restart within 180 seconds", mcStart);
            sleep(35);
            setTestStep("Read the Total Number of Nodes from yang file");
            noOfNodes = networkFullSyncCliOperator.readTotalNode();

            setTestStep("Read the start time from yang file");
            startTime = networkFullSyncCliOperator.readDumpStartTime();
            setTestStep("Read the number of nodes Synced");
            for (int i = 0; i < 20; i++) {
                sleep(35);
                endTime = networkFullSyncCliOperator.readSyncNode(noOfNodes);
                if (!endTime.isEmpty()) {
                    syncStatus = true;
                    break;
                }
            }
            assertThat("Network Full Sync is not successful", syncStatus);
            actualTimeTakenForSync = networkFullSyncCliOperator.timeDiff(startTime, endTime);
            actualTimeTaken = actualTimeTaken + actualTimeTakenForSync + " ";
        }

        setTestStep("Average Time taken for Full sync");
        final String avgTimeTaken = networkFullSyncCliOperator.calculateAverageOfTime(actualTimeTaken);

    }

    @AfterTest
    void afterTest() {
        setTestStep("Remove the sync script from server");
        initialSyncCliOperator.removeFileFromRemote(SCRIPT_NAME, SCRIPT_REMOTE_PATH);

    }
}
