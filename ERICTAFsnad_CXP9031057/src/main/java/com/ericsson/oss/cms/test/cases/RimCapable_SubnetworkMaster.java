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
public class RimCapable_SubnetworkMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    @Inject
    private SrvccCapabilityCliOperator srvccCapabilityCliOperator;

    @Inject
    private UtranCellRelationCliOperator utranCellRelationCliOperator;

    private static int numCells = 1;

    private final CSTestHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String moId = "TAF_OSS_128811_Test";

    private final Fdn freqManagementFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,FreqManagement=1");

    private final Fdn subNetworkFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R");

    private Fdn masterExternalUtranFreqFdn, masterExternalUtranCellFdn, utranFreqRelation, utranCellRelation;

    @BeforeTest
    public void setupMaster() {
        setTestStep("Create the master ExternalUtranFreq");
        masterExternalUtranFreqFdn = utranCellRelationCliOperator.createMaster(freqManagementFdn, moId, csHandler);
        setTestStep("Create the Subnetwork Master ExternalUtranCell");
        masterExternalUtranCellFdn = utranCellRelationCliOperator.createMaster(subNetworkFdn, moId, masterExternalUtranFreqFdn, csHandler, "ExternalUtranCell");
    }

    /**
     * @DESCRIPTION
     *              Set RimCapable Attribue Value in Proxy MO ExternalUtranCellFDD/TDD
     *              RimCapable Value should be propagated to its Subnetwork Master ExternalUtranCell {@link https
     *              ://taftm.lmera.ericsson.se/#tm/viewTC/30649}
     * @PRE SNAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY MEDIUM
     */

    @TestId(id = "OSS-132978-RimCapable value propagation of Proxy MO ExternalUtranCellFDD in Subnetwork Master",
            title = "RimCapable value propagation of Proxy MO ExternalUtranCellFDD in Subnetwork Master")
    @Context(context = { Context.CLI })
    @DataDriven(name = "rimCapable_subnetworkMaster")
    @Test(groups = { "SNAD, KGB" })
    public void rimCapablePropagation(
            @Input("sourceNodeType") final String sourceNodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("sourceMaster") final String sourceMaster,
            @Input("proxyMoType") final String proxyMoType,
            @Input("attributeName") final String rimCapable,
            @Input("rimCapableValue") final String rimCapableValue,
            @Input("updatedRimCapable") final String updatedRimCapable,
            @Input("masterRimCapable") final String masterRimCapable) {

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

        setTestStep("Get the rimCapable attribute value of Subnetwork Master ExternalUtranCell");
        String rim = csHandler.getAttributeValue(masterExternalUtranCellFdn, rimCapable);
        setTestStep("Verify whether the rimCapable attribute is having default value 0");
        if (rim != "0") {
            csHandler.setAttributeValue(masterExternalUtranCellFdn, rimCapable, "0");
            rim = csHandler.getAttributeValue(masterExternalUtranCellFdn, rimCapable);
        }
        logger.info("RimCapable found in Master MO : " + rim);

        startTime = System.currentTimeMillis();
        setTestStep("Set the rimCapable attibute value 1 in the selected Proxy ExternalUtranCellFDD");
        csHandler.setAttributeValue(proxy, rimCapable, rimCapableValue);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String proxyRim = csHandler.getAttributeValue(proxy, rimCapable);
        assertEquals("Proxy ExternalUtranCellFDD is not having the RimCapable Attribute value 1", proxyRim, rimCapableValue);
        logger.info("Proxy ExternalUtranCellFDD has RimCapable Attribute after changing :" + proxyRim);

        setTestStep("Verify whether the new value is propagated to the Subnetwork Master ExternalUtranCell");
        final String masterRimAttr = csHandler.getAttributeValue(masterExternalUtranCellFdn, rimCapable);
        assertEquals("RimCapable attribute Value of Proxy ExternalUtranCellFDD is not propagated in its Master ExternalUtranCell ", masterRimAttr,
                rimCapableValue);
        logger.info("Subnetwork Master ExternalUtranCell has RimCapable Attribute : " + masterRimAttr);

        startTime = System.currentTimeMillis();
        setTestStep("Set the RimCapable attibute value to 0 in the selected Proxy ExternalUtranCellFDD");
        csHandler.setAttributeValue(proxy, rimCapable, updatedRimCapable);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String proxyRimValue = csHandler.getAttributeValue(proxy, rimCapable);

        setTestStep("Verify whether SNAD revert back the values");
        assertEquals("Proxy ExternalUtranCellFDD is not having the RimCapable Attribute 1", proxyRimValue, rimCapableValue);
        logger.info("Proxy ExternalUtranCellFDD has RimCapable Attribute after changing :" + proxyRimValue);
        final String masterRimValue = csHandler.getAttributeValue(masterExternalUtranCellFdn, rimCapable);
        assertEquals("Master ExternalUtranCell is not having the RimCapable Attribute value 1", masterRimValue, rimCapableValue);
        logger.info("Master ExternalUtranCell has RimCapable : " + masterRimValue);

        startTime = System.currentTimeMillis();
        setTestStep("Set the RimCapable attibute value to 0 or 1 in the Subnetwork Master ExternalUtranCell");
        csHandler.setAttributeValue(masterExternalUtranCellFdn, rimCapable, masterRimCapable);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String masterRimAttribute = csHandler.getAttributeValue(masterExternalUtranCellFdn, rimCapable);
        assertEquals("Master ExternalUtranCell is not having the RimCapable Attribute value 0", masterRimAttribute, masterRimCapable);
        logger.info("Master UtranCell has RimCapable Attribute after changing :" + masterRimAttribute);

        setTestStep("Verify whether the new value is propagated to its proxy ExternalUtranCellFDD");
        final String proxyRimAttribute = csHandler.getAttributeValue(proxy, rimCapable);
        assertEquals("New value of Master is not propagated to its proxy ExternalUtranCellFDD", proxyRimAttribute, masterRimCapable);
        logger.info("Proxy ExternalUtranCellFDD has RimCapable Attribute :" + proxyRimAttribute);

        setTestStep("Perform a clean up activity by resetting all the old values");
        setTestStep("Reset the RimCapable Attribute value to original value");
        csHandler.setAttributeValue(masterExternalUtranCellFdn, rimCapable, "0");

    }

    @AfterMethod(alwaysRun = true)
    public void tidyUpAfterTest() {
        setTestStep("Clean Up");
        setTestStep("Delete the UtranCellRelation");
        csHandler.deleteMo(utranCellRelation);
        setTestStep("Delete the UtranFreqRelation");
        csHandler.deleteMo(utranFreqRelation);

    }

    @AfterTest(alwaysRun = true)
    public void tidyMaster() {
        setTestStep("Delete the Master ExternalUtranFreq");
        csHandler.deleteMo(masterExternalUtranFreqFdn);
        setTestStep("Delete the Subnetwork Master ExternalUtranCell");
        csHandler.deleteMo(masterExternalUtranCellFdn);
    }
}
