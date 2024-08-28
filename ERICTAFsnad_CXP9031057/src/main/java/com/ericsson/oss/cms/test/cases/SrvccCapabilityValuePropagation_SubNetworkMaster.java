/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.cms.test.operators.SrvccCapabilityCliOperator;
import com.ericsson.oss.cms.test.operators.UtranCellRelationCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xindcha
 */
public class SrvccCapabilityValuePropagation_SubNetworkMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private SrvccCapabilityCliOperator srvccCapabilityCliOperator;

    @Inject
    private UtranCellRelationCliOperator utranCellRelationCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    private static int numCells = 1;

    private final CSTestHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String moId = "TAF_OSS_128811_Test";

    private final Fdn freqManagementFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,FreqManagement=1");

    private final Fdn subNetworkFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R");

    private Fdn masterExternalUtranFreqFdn, masterExternalUtranCellFdn, utranFreqRelation, utranCellRelation;

    @BeforeTest
    public void SetupMaster() {
        setTestStep("Create the master ExternalUtranFreq");
        masterExternalUtranFreqFdn = utranCellRelationCliOperator.createMaster(freqManagementFdn, moId, csHandler);
        setTestStep("Create the Subnetwork Master ExternalUtranCell");
        masterExternalUtranCellFdn = utranCellRelationCliOperator.createMaster(subNetworkFdn, moId, masterExternalUtranFreqFdn, csHandler, "ExternalUtranCell");
    }

    /**
     * @DESCRIPTION
     *              Set SrvccCapability Attribue Value in Proxy MO ExternalUtranCellFDD
     *              SrvccCapability Value should be propagated to its Subnetwork Master ExternalUtranCell
     *              Set SrvccCapability Attribue Value in Subnetwork Master ExternalUtranCell
     *              SrvccCapability Value should be propagated to its Proxy MO ExternalUtranCellFDD {@link https
     *              ://taftm.lmera.ericsson.se/#tm/viewTC/29158}
     * @PRE SNAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY MEDIUM
     */

    @TestId(id = "OSS-128811-SrvccCapability value propagation of Proxy MO ExternalUtranCellFDD in Subnetwork Master.",
            title = "SrvccCapability value propagation of Proxy MO ExternalUtranCellFDD in Subnetwork Master")
    @Context(context = { Context.CLI })
    @DataDriven(name = "srvccCapabilityPropagation_subnetworkMaster")
    @Test(groups = { "SNAD, KGB" })
    public void SrvccCapabilityValuePropagation(
            @Input("sourceNodeType") final String sourceNodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("sourceMaster") final String sourceMaster,
            @Input("proxyMoType") final String proxyMoType,
            @Input("attributeName") final String srvccCapability,
            @Input("srvccValue") final String srvccValue,
            @Input("updatedSrvcc") final String updatedSrvcc,
            @Input("masterSrvcc") final String masterSrvcc) {

        setTestStep("Get a Synchronized and connected node from CS of the given sourceNodeType from the data file");
        final Fdn sourceNodeFdn = nodeCliOperator.getSyncedNode(csHandler, sourceNodeType,
                NodeCliOperator.buildNodeFiltering(sourceNodeAdditionalFiltering, numCells, sourceMaster));
        assertNotNull("No synched ERBS node found", sourceNodeFdn);
        logger.info("Synched ERBS node found is: " + sourceNodeFdn);

        setTestStep("Get a sourceMaster EUtranCellFDD/TDD");
        final List<Fdn> sourceChildFdns = nodeCliOperator.getChildrenFromSyncedNodeFiltering(sourceNodeFdn, sourceMaster);
        final Fdn sourceMasterFdn = sourceChildFdns.get(0);
        assertNotNull("No Source Master EUtranCellFDD/TDD MO found", sourceMasterFdn);
        logger.info("Master EUtranCellFDD/TDD MO found is: " + sourceMasterFdn);

        long startTime = System.currentTimeMillis();
        setTestStep("Create UtranFreqRelation on Source master to the master ExternalUtranFreq");
        utranFreqRelation = srvccCapabilityCliOperator.createFreqRelation(sourceMasterFdn, moId, masterExternalUtranFreqFdn, csHandler);

        setTestStep("Create UtranCellRelation from Source Master to Subnetwork Master ExternalUtranCell");
        utranCellRelation = srvccCapabilityCliOperator.createCellRelation(utranFreqRelation, moId, masterExternalUtranCellFdn, csHandler);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);

        setTestStep("Get the Proxy ExternalUtranCellFDD of the Subnetwork Master MO which is created");
        final List<Fdn> proxiesFdn = snadOperator.getProxiesForMaster(masterExternalUtranCellFdn);
        final Fdn proxy = srvccCapabilityCliOperator.getRequiredProxy(proxiesFdn, proxyMoType);
        assertNotNull("No ExternalUtranCellFDD proxy MO found", proxy);
        logger.info("Proxy ExternalUtranCellFDD MO found is: " + proxy);

        setTestStep("Get the srvccCapability attribute value of Subnetwork Master ExternalUtranCell");
        String srvcc = csHandler.getAttributeValue(masterExternalUtranCellFdn, srvccCapability);
        setTestStep("Verify whether the srvccCapability attribute is having default value 0");
        if (srvcc != "0") {
            csHandler.setAttributeValue(masterExternalUtranCellFdn, srvccCapability, "0");
            srvcc = csHandler.getAttributeValue(masterExternalUtranCellFdn, srvccCapability);
        }
        logger.info("SrvccCapability found in Master MO : " + srvcc);

        startTime = System.currentTimeMillis();
        setTestStep("Set the srvccCapability attibute value 1 or 2 in the selected Proxy ExternalUtranCellFDD");
        csHandler.setAttributeValue(proxy, srvccCapability, srvccValue);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String proxySrvcc = csHandler.getAttributeValue(proxy, srvccCapability);
        assertEquals("Proxy ExternalUtranCellFDD is not having the SrvccCapability Attribute value 1", proxySrvcc, srvccValue);
        logger.info("Proxy ExternalUtranCellFDD has srvccCapability Attribute after changing :" + proxySrvcc);

        setTestStep("Verify whether the new value is propagated to the Subnetwork Master ExternalUtranCell");
        final String masterSrvccAttr = csHandler.getAttributeValue(masterExternalUtranCellFdn, srvccCapability);
        assertEquals("SrvccCapability attribute Value of Proxy ExternalUtranCellFDD is not propagated in its Master ExternalUtranCell ", masterSrvccAttr,
                srvccValue);
        logger.info("Subnetwork Master ExternalUtranCell has SrvccCapability Attribute : " + masterSrvccAttr);

        startTime = System.currentTimeMillis();
        setTestStep("Set the srvccCapability attibute value to 0,1 or 2 in the selected Proxy ExternalUtranCellFDD");
        csHandler.setAttributeValue(proxy, srvccCapability, updatedSrvcc);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String proxySrvccValue = csHandler.getAttributeValue(proxy, srvccCapability);

        setTestStep("Verify whether SNAD revert back the values");
        assertEquals("Proxy ExternalUtranCellFDD is not having the srvccCapability Attribute 1", proxySrvccValue, srvccValue);
        logger.info("Proxy ExternalUtranCellFDD has srvccCapability Attribute after changing :" + proxySrvccValue);
        final String masterSrvccValue = csHandler.getAttributeValue(masterExternalUtranCellFdn, srvccCapability);
        assertEquals("Master ExternalUtranCell is not having the SrvccCapability Attribute value 1", masterSrvccValue, srvccValue);
        logger.info("Master ExternalUtranCell has SrvccCapability : " + masterSrvccValue);

        startTime = System.currentTimeMillis();
        setTestStep("Set the srvccCapability attibute value to 0,1 or 2 in the Subnetwork Master ExternalUtranCell");
        csHandler.setAttributeValue(masterExternalUtranCellFdn, srvccCapability, masterSrvcc);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String masterSrvccAttribute = csHandler.getAttributeValue(masterExternalUtranCellFdn, srvccCapability);
        assertEquals("Master ExternalUtranCell is not having the SrvccCapability Attribute value 2", masterSrvccAttribute, masterSrvcc);
        logger.info("Master UtranCell has srvccCapability Attribute after changing :" + masterSrvccAttribute);

        setTestStep("Verify whether the new value is propagated to its proxy ExternalUtranCellFDD");
        final String proxySrvccAttribute = csHandler.getAttributeValue(proxy, srvccCapability);
        assertEquals("New value of Master is not propagated to its proxy ExternalUtranCellFDD", proxySrvccAttribute, masterSrvcc);
        logger.info("Proxy ExternalUtranCellFDD has srvccCapability Attribute :" + proxySrvccAttribute);

        setTestStep("Perform a clean up activity by resetting all the old values");
        setTestStep("Reset the SrvccCapability Attribute value to original value");
        csHandler.setAttributeValue(masterExternalUtranCellFdn, srvccCapability, "0");

    }

    @AfterMethod(alwaysRun = true)
    public void TidyUpAfterTest() {
        setTestStep("Clean Up");
        setTestStep("Delete the UtranCellRelation");
        csHandler.deleteMo(utranCellRelation);
        setTestStep("Delete the UtranFreqRelation");
        csHandler.deleteMo(utranFreqRelation);

    }

    @AfterTest(alwaysRun = true)
    public void TidyMaster() {
        setTestStep("Delete the Master ExternalUtranFreq");
        csHandler.deleteMo(masterExternalUtranFreqFdn);
        setTestStep("Delete the Subnetwork Master ExternalUtranCell");
        csHandler.deleteMo(masterExternalUtranCellFdn);
    }

}
