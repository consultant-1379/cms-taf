package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_CREATE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_DELETE;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.ActivateStKpiCliOperator;
import com.ericsson.oss.cms.test.operators.LogFileCliOperator;
import com.ericsson.oss.cms.test.util.BCGImportFileGenerator;
import com.ericsson.oss.cms.test.util.BCGToolHandler;
import com.ericsson.oss.cms.test.util.BCGToolUtility;
import com.ericsson.oss.cms.test.util.KpiUtil;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

public class Activate1000RelationKpi extends TorTestCaseHelper implements TestCase {

    @Inject
    private ActivateStKpiCliOperator stKpiOperator;

    @Inject
    TestContext delete1000RelationContext;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    private final SMHandler smToolHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private final BCGToolHandler bcgToolHandler = new BCGToolUtility(HostGroup.getOssmaster());

    private static final String BCG_MC = "wran_bcg";

    private static final String UNSYNCED_NODE_PARAM = "ExportNodeIfUnsynched";

    private double activateTotalTimeInMs = 0;

    private List<CIFLogItem> cifActivateLog;

    private static final String PLAN_METRICS = "PlanMetrics";

    private static final String DELETE_PLAN = "deleteIntra";

    private static final String CREATE_PLAN = "createIntra";

    String pedParameterValueBeforeUpdate;

    private final SoftAssert softAssert = new SoftAssert();

    private String localExportPath;

    private String localCreatePath;

    private String localDeletePath;

    private double accumulatedDeleteMoRatePerSec;

    private double accumulatedCreateMoRatePerSec;

    private static final long NOTIFICATION_MAX_WAIT_MS = 120000;

    @TestId(id = "OSS-54724_1000RelationsKPI", title = "Create 1000 Intra UtranRelations In a Plan")
    @Context(context = { Context.CLI })
    @DataDriven(name = "stintraUtranRelationKpi")
    @Test(groups = { "STCDB" })
    public void activate1000RelationsTest(
            @Input("relationMo") final String relationMo,
            @Input("nodeRelationType") final String nodeRelationType,
            @Input("createBaselineRate") final double createBaselineRate,
            @Input("deleteBaselineRate") final double deleteBaselineRate,
            @Input("kpiPercentDeviation") final double kpiPercentDeviation,
            @Input("iterations") final int iterations,
            @Input("localFileDir") final String localFileDir,
            @Input("totalMos") final int totalMos) throws IOException, XMLStreamException, FactoryConfigurationError, ParserConfigurationException,
            TransformerException {

        delete1000RelationContext.setAttribute("deleteAvgMoSec", 0.0);

        setTestInfo("Create directory in temp folder on local machine. XML files will be copied to here for parsing");
        final String tempDir = stKpiOperator.createDirectoryInTemp(localFileDir);

        setTestStep("Read the BCG PED parameter ExportNodeIfUnsynced value and store it");
        pedParameterValueBeforeUpdate = smToolHandler.getConfigurationForMC(BCG_MC, UNSYNCED_NODE_PARAM);

        setTestStep("Update ExportNodeIfUnsynced value to false");
        smToolHandler.setConfigurationForMC(BCG_MC, UNSYNCED_NODE_PARAM, "false");
        assertEquals("PED parameter is not set to false", "false", smToolHandler.getConfigurationForMC(BCG_MC, UNSYNCED_NODE_PARAM));

        setTestStep("Export all UtranRelations from full Network");
        final String exportFileName = relationMo + System.currentTimeMillis() + ".xml";
        final boolean exportSucceeded = bcgToolHandler.exportByMoClass(exportFileName, relationMo);
        assertTrue("Export has not succeeded", exportSucceeded);

        setTestStep("Copy export xml file from remote to local file location");
        localExportPath = tempDir + exportFileName;
        final boolean copySucceeded = bcgToolHandler.copyRemoteBcgFileToLocal(exportFileName, localExportPath);
        assertTrue("Copy from remote to local did not succeed", copySucceeded);
        assertTrue("File is not existing at local path or file is empty" + localExportPath, stKpiOperator.fileExistsOnLocalPath(localExportPath));

        setTestStep("Create import xml file for Creation of 1000 Intra UtranRelations");
        final String importCreateFileName = "Create1000_IntraUtranRelations.xml";
        localCreatePath = tempDir + importCreateFileName;
        final boolean fileCreationSucceeded = stKpiOperator.createImportFile("create", totalMos, nodeRelationType, localExportPath, localCreatePath);
        assertTrue("Creation of import file cannot be completed ", fileCreationSucceeded);
        assertTrue("File is not existing at local path or file is empty" + localCreatePath, stKpiOperator.fileExistsOnLocalPath(localCreatePath));

        setTestStep("Copy create xml file from local to remote file location");
        final boolean copyToRemoteSucceeded = bcgToolHandler.copyLocalBcgFileToRemote(importCreateFileName, localCreatePath);
        assertTrue("Copy from local to remote did not succeed", copyToRemoteSucceeded);
        assertTrue("Import file does not have 1000 Utran Relations for creation",
                bcgToolHandler.verifyTotalImportCommands(importCreateFileName, totalMos, "create"));

        setTestStep("Convert Import Create UtranRelation xml file to Delete UtranRelation xml file");
        final String importDeleteFileName = "Delete1000_IntraUtranRelations.xml";
        localDeletePath = tempDir + importDeleteFileName;
        stKpiOperator.convertCreateFileToDeleteFile(localCreatePath, localDeletePath);
        assertTrue("File is not existing at local path or file is empty " + localDeletePath, stKpiOperator.fileExistsOnLocalPath(localDeletePath));

        setTestStep("Copy delete xml file from local to remote file location");
        final boolean copyOfDeleteSucceeded = bcgToolHandler.copyLocalBcgFileToRemote(importDeleteFileName, localDeletePath);
        assertTrue("Copy of delete xml file from local to remote did not succeed", copyOfDeleteSucceeded);
        assertTrue("Import file does not have 1000 Utran Relations for deletion",
                bcgToolHandler.verifyTotalImportCommands(importDeleteFileName, totalMos, "delete"));
        for (int i = 1; i <= iterations; i++) {

            setTestInfo("Iteration No: " + i);
            setTestStep("Import the Delete xml file to a plan");
            final String planName = DELETE_PLAN + System.currentTimeMillis();
            final boolean importSucceeded = bcgToolHandler.importToPlannedArea(importDeleteFileName, planName);
            assertTrue("Import did not succeed", importSucceeded);

            setTestInfo("Counting delete notifications before activation");
            final String rncInUse = BCGImportFileGenerator.getRncToBeUsed();
            final int noOfLogsBeforeDelete = logFileCliOperator.getNotificationCount(NOTIFICATION_DELETE, new Fdn(rncInUse), relationMo);
            final int expectedNoLogsAfterDelete = noOfLogsBeforeDelete + totalMos;
            setTestInfo("Expected no of new delete logs is : " + totalMos);

            final long deleteStartTime = System.currentTimeMillis();
            setTestStep("Activate the delete plan");
            final boolean activateSucceeded = bcgToolHandler.activatePlan(planName);
            assertTrue("Activate of plan did not succeed", activateSucceeded);

            setTestStep("Calculate the Delete Mo Rate Per Second and add to accumulated Mo Rate Per Sec");
            cifActivateLog = stKpiOperator.getActivationCifLogs(PLAN_METRICS, deleteStartTime);
            softAssert.assertTrue(cifActivateLog.size() > 0, "Expected ACTIVATE log not received for delete 1000 relations");
            activateTotalTimeInMs = KpiUtil.getTotalTimeFromLog(cifActivateLog);
            softAssert.assertTrue(activateTotalTimeInMs > 0, "Expected Total Time for activate delete relations was not found in CIF logs ");

            setTestInfo("Read the CIF Logs for the totalMos Activated During Delete");
            final int totalMosActivated = stKpiOperator.getTotalDeletedMosDuringActivation(deleteStartTime);
            setTestInfo("Total Mos Activated is " + totalMosActivated);

            final double activateTotalTimeInSecs = activateTotalTimeInMs / 1000;
            final double moRatePerSec = totalMosActivated / activateTotalTimeInSecs;

            setTestInfo("Activate total Time for Delete in Seconds in Iteration " + i + " is " + String.format("%.2f", activateTotalTimeInSecs));
            setTestInfo("Mo Rate per second for Delete in Iteration " + i + " is " + String.format("%.2f", moRatePerSec));
            accumulatedDeleteMoRatePerSec += moRatePerSec;

            setTestStep("Wait for Expected Delete Notification Count After Activation");
            final int noOfLogs = stKpiOperator.waitForExpectedNotificationCount(NOTIFICATION_DELETE, new Fdn(rncInUse), relationMo, expectedNoLogsAfterDelete,
                    NOTIFICATION_MAX_WAIT_MS);
            setTestInfo("No of delete logs after activation " + (noOfLogs - noOfLogsBeforeDelete));

            setTestStep("Delete the delete plan");
            final boolean removeSucceeded = bcgToolHandler.removePlan(planName);
            assertTrue("Delete of plan did not succeed", removeSucceeded);

            setTestStep("Import the create file into a plan");
            final String createPlanName = CREATE_PLAN + System.currentTimeMillis();
            final boolean importCreateSucceeded = bcgToolHandler.importToPlannedArea(importCreateFileName, createPlanName);
            assertTrue("Import for creation of 1000 UtranRelations did not succeed ", importCreateSucceeded);

            setTestInfo("Counting create notifications before activation");
            final int noOfLogsBeforeCreate = logFileCliOperator.getNotificationCount(NOTIFICATION_CREATE, new Fdn(rncInUse), relationMo);
            final int expectedNoLogsAfterCreate = noOfLogsBeforeCreate + totalMos;
            setTestInfo("Expected no of new create logs is : " + totalMos);

            setTestStep("Activate the create plan");
            final long createStartTime = System.currentTimeMillis();
            final boolean activateCreateSucceeded = bcgToolHandler.activatePlan(createPlanName);
            assertTrue("Activate of create plan did not succeed ", activateCreateSucceeded);

            setTestStep("Calculate the Create Mo Rate Per Sec and add to accumulated Mo Rate Per Sec");
            cifActivateLog = stKpiOperator.getActivationCifLogs(PLAN_METRICS, createStartTime);
            softAssert.assertTrue(cifActivateLog.size() > 0, "Expected ACTIVATE log not received for create 1000 relations");
            activateTotalTimeInMs = KpiUtil.getTotalTimeFromLog(cifActivateLog);
            softAssert.assertTrue(activateTotalTimeInMs > 0, "Expected Total Time for activate create relations was not found in CIF logs ");

            setTestInfo("Read the CIF Logs for the totalMos Activated During Create");
            final int totalMosActivatedForCreate = stKpiOperator.getTotalCreatedMosDuringActivation(createStartTime);
            setTestInfo("Total Mos Activated is " + totalMosActivatedForCreate);

            final double activateCreateTotalTimeInSecs = activateTotalTimeInMs / 1000;
            final double moCreateRatePerSec = totalMosActivatedForCreate / activateCreateTotalTimeInSecs;
            setTestInfo("Activate total Time for Create in Seconds in Iteration " + i + " is " + String.format("%.2f", activateCreateTotalTimeInSecs));
            setTestInfo("Mo Rate per second for Create in Iteration " + i + " is " + String.format("%.2f", moCreateRatePerSec));
            accumulatedCreateMoRatePerSec += moCreateRatePerSec;

            setTestStep("Wait for Expected Create Notification Count After Activation");
            final int noOfCreateLogs = stKpiOperator.waitForExpectedNotificationCount(NOTIFICATION_CREATE, new Fdn(rncInUse), relationMo,
                    expectedNoLogsAfterCreate, NOTIFICATION_MAX_WAIT_MS);
            setTestInfo("No of create logs after activation " + (noOfCreateLogs - noOfLogsBeforeCreate));

            setTestStep("Delete the create plan");
            final boolean removeCreatePlanSucceeded = bcgToolHandler.removePlan(createPlanName);
            assertTrue("Delete of create plan did not succeed", removeCreatePlanSucceeded);

        }

        setTestStep("Calculate the average moRate/sec");
        final double deleteAvgMoSec = accumulatedDeleteMoRatePerSec / iterations;
        final double createAvgMoSec = accumulatedCreateMoRatePerSec / iterations;

        setTestInfo("Set up test context after Create relations iterations is complete");
        delete1000RelationContext.setAttribute("deleteAvgMoSec", deleteAvgMoSec);
        delete1000RelationContext.setAttribute("deleteBaselineRate", deleteBaselineRate);
        delete1000RelationContext.setAttribute("kpiPercentDeviation", kpiPercentDeviation);

        final double minMoRatePerSec = KpiUtil.getRateWithDeviation(createBaselineRate, kpiPercentDeviation);
        final double actualDeviationPercent = KpiUtil.getActualDeviationPercent(createAvgMoSec, createBaselineRate);
        setTestInfo("Actual average MO activation rate for create = " + String.format("%.2f", createAvgMoSec) + " Mo/Sec");
        setTestInfo("Expected average number of MOs activated to be: " + String.format("%.2f", createBaselineRate) + " Mo/sec");
        setTestInfo("Deviation Allowed -" + kpiPercentDeviation + "%" + " (" + String.format("%.2f", minMoRatePerSec) + " Mo/sec)");
        setTestInfo("Actual Deviation from Baseline: " + String.format("%.2f", actualDeviationPercent) + "%");
        assertTrue("MOs activated per sec for create was not in " + String.format("%.2f", kpiPercentDeviation) + "% of acceptable KPI " + createBaselineRate,
                createAvgMoSec >= minMoRatePerSec);

    }

    @TestId(id = "OSS-54726_Delete1000RelationsKPI", title = "Delete 1000 Intra UtranRelations In a Plan")
    @Test(groups = { "STCDB" })
    @Context(context = { Context.CLI })
    public void activateDelete1000RelationKpi() {

        setTestStep("Read the averageMoRate/sec for delete 1000 relations");
        final double deleteAvgMoSec = delete1000RelationContext.getAttribute("deleteAvgMoSec");
        assertTrue("KPI for Average MO/Sec rate for Delete 1000 relataions is not available. Check logs for more detail ", deleteAvgMoSec > 0);

        final double deleteBaselineRate = delete1000RelationContext.getAttribute("deleteBaselineRate");
        final double kpiPercentDeviation = delete1000RelationContext.getAttribute("kpiPercentDeviation");
        final double minMoRatePerSec = KpiUtil.getRateWithDeviation(deleteBaselineRate, kpiPercentDeviation);
        final double actualDeviationPercent = KpiUtil.getActualDeviationPercent(deleteAvgMoSec, deleteBaselineRate);

        setTestInfo("Actual average MO activate rate for delete = " + String.format("%.2f", deleteAvgMoSec) + " Mo/Sec");
        setTestInfo("Expected average number of MOs read to be: " + String.format("%.2f", deleteBaselineRate) + " Mo/sec");
        setTestInfo("Deviation Allowed -" + kpiPercentDeviation + "%" + " (" + String.format("%.2f", minMoRatePerSec) + " Mo/sec)");
        setTestInfo("Actual Deviation from Baseline: " + String.format("%.2f", actualDeviationPercent) + "%");
        assertTrue(
                "MOs activated per sec for deletewas not in " + String.format("%.2f", kpiPercentDeviation) + "% of acceptable KPI ("
                        + String.format("%.2f", deleteBaselineRate) + " Mo/sec)", deleteAvgMoSec >= minMoRatePerSec);
    }

    @AfterTest
    public void afterTest() {
        setTestStep("Clean Up:Clean up import and export xml files and set ExportNodeIfUnsynced back to stored value");
        smToolHandler.setConfigurationForMC(BCG_MC, UNSYNCED_NODE_PARAM, pedParameterValueBeforeUpdate);
        stKpiOperator.deleteLocalFiles(localExportPath, localCreatePath, localDeletePath);
    }

}
