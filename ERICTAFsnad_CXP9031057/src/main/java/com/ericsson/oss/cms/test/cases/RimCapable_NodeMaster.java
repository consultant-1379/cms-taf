/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;
import static com.ericsson.oss.taf.nodeOperator.nodeFiltering.AtLeastXChildMOsOfTypeFilter.atLeast;
import static com.ericsson.oss.taf.nodeOperator.nodeFiltering.ChildTypeFilterGroup.childTypeFiltering;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
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
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xindcha
 */
public class RimCapable_NodeMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    @Inject
    private SrvccCapabilityCliOperator srvccCapabilityCliOperator;

    private static int numCells = 1;

    private static final String moId = "TAF_OSS_132977_Test";

    private final Fdn freqManagementFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,FreqManagement=1");

    private Fdn utranFreqRelation, masterExternalUtranFreqFdn, utranCellRelation, proxyFdn;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String UARFCNDL = "uarfcnDl";

    private boolean flag = false;

    /**
     * @DESCRIPTION
     *              Set RimCapable Attribue Value in Proxy MO ExternalUtranCellFDD/TDD
     *              RimCapable Value should be propagated to the Master UtranCell {@link https
     *              ://taftm.lmera.ericsson.se/login/#tm/viewTC/30642}
     * @PRE SNAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY MEDIUM
     */

    @TestId(id = "OSS-132977-RimCapable value propagation of Proxy MO ExternalUtranCellFDD in Node Master",
            title = "RimCapable value propagation of Proxy MO ExternalUtranCellFDD in Node Master")
    @Context(context = { Context.CLI })
    @DataDriven(name = "rimCapable_nodeMaster")
    @Test(groups = { "SNAD, KGB" })
    public void rimCapable(
            @Input("targetNodeType") final String targetNodeType,
            @Input("targetMaster") final String targetMaster,
            @Input("proxyMoType") final String proxyMoType,
            @Input("attributeName") final String rimCapable,
            @Input("rimCapableValue") final String rimCapableValue,
            @Input("updatedRimCapable") final String updatedRimCapable,
            @Input("masterRimCapable") final String masterRimCapable,
            @Input("sourceNodeType") final String sourceNodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("sourceMaster") final String sourceMaster,
            @Input("uarfcnDlattr") final String uarfcnDlattr) {

        setTestStep("Get a connected and synched RNC Node");
        setTestInfo("Searching for a node of %s node Type ", targetNodeType);
        final Fdn targetNodeFdn = nodeCliOperator.getSyncedNode(csHandler, targetNodeType, childTypeFiltering(atLeast(numCells, targetMaster)));
        assertNotNull("No synched node found", targetNodeFdn);
        logger.info("Synched node Mo found is: " + targetNodeFdn);

        setTestStep("Get a Master UtranCell");
        final List<Fdn> childFdns = nodeCliOperator.getChildrenFromSyncedNodeFiltering(targetNodeFdn, targetMaster);
        final Fdn targetMasterFdn = childFdns.get(0);
        assertNotNull("No Master UtranCell MO found", targetMasterFdn);
        logger.info("Master UtranCell MO found is: " + targetMasterFdn);

        setTestStep("Get a Proxy ExternalUtranCellFDD of the selected Master MO");
        setTestInfo("Searching for proxy of %s ", targetMasterFdn);
        final List<Fdn> proxiesFdns = snadOperator.getProxiesForMaster(targetMasterFdn);
        proxyFdn = srvccCapabilityCliOperator.getRequiredProxy(proxiesFdns, proxyMoType);

        if (proxyFdn == null) {

            setTestStep("If proxy is not found create UtranCellRelation from ERBS node to RNC node");
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

            setTestStep("Get uarfcnDl attribute value of Utrancell MO");
            final String downLink = csHandler.getAttributeValue(targetMasterFdn, UARFCNDL);
            logger.info("uarfcnDl Attribute found in Master MO : " + downLink);

            final long startTime = System.currentTimeMillis();
            setTestStep("Create the Master ExternalUtranFreq");
            masterExternalUtranFreqFdn = srvccCapabilityCliOperator.createMaster(freqManagementFdn, downLink, csHandler);

            setTestStep("Create UtranFreqRelation on Source master to the master ExternalUtranFreq");
            utranFreqRelation = srvccCapabilityCliOperator.createFreqRelation(sourceMasterFdn, moId, masterExternalUtranFreqFdn, csHandler);

            setTestStep("Create UtranCellRelation from Source Master to Target Master");
            utranCellRelation = srvccCapabilityCliOperator.createCellRelation(utranFreqRelation, moId, targetMasterFdn, csHandler);

            flag = true;

            snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
            setTestInfo("Searching for proxy of %s ", targetMasterFdn);
            final List<Fdn> proxiesFdn = snadOperator.getProxiesForMaster(targetMasterFdn);
            proxyFdn = srvccCapabilityCliOperator.getRequiredProxy(proxiesFdn, proxyMoType);

        }

        assertNotNull("No ExternalUtranCellFDD proxy MO found", proxyFdn);
        logger.info("Proxy ExternalUtranCellFDD MO found is: " + proxyFdn);

        setTestStep("Get the rimCapable attribute value of Master UtranCell");
        String rim = csHandler.getAttributeValue(targetMasterFdn, rimCapable);
        setTestStep("Verify whether the rimCapable attribute is having default value 0");
        if (rim != "0") {
            csHandler.setAttributeValue(targetMasterFdn, rimCapable, "0");
            rim = csHandler.getAttributeValue(targetMasterFdn, rimCapable);
        }
        logger.info("RimCapable found in Master MO : " + rim);

        long startTime = System.currentTimeMillis();
        setTestStep("Set the rimCapable attibute value 1 in the selected Proxy ExternalUtranCellFDD");
        csHandler.setAttributeValue(proxyFdn, rimCapable, rimCapableValue);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);

        final String proxyRim = csHandler.getAttributeValue(proxyFdn, rimCapable);
        assertEquals("Proxy ExternalUtranCellFDD is not having the RimCapable Attribute value 1", proxyRim, rimCapableValue);
        logger.info("Proxy ExternalUtranCellFDD has RimCapable Attribute after changing :" + proxyRim);

        setTestStep("Verify whether the new value is propagated to the Master UtranCell");
        final String masterRimAttr = csHandler.getAttributeValue(targetMasterFdn, rimCapable);
        assertEquals("RimCapable attribute Value of Proxy ExternalUtranCellFDD is not propagated in its Master UtranCell ", masterRimAttr, rimCapableValue);
        logger.info("Master UtranCell has RimCapable Attribute : " + masterRimAttr);

        startTime = System.currentTimeMillis();
        setTestStep("Set the rimCapable attibute value to 0 in the selected Proxy ExternalUtranCellFDD");
        csHandler.setAttributeValue(proxyFdn, rimCapable, updatedRimCapable);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String proxyRimValue = csHandler.getAttributeValue(proxyFdn, rimCapable);

        setTestStep("Verify whether SNAD revert back the values");
        assertEquals("Proxy ExternalUtranCellFDD is not having the RimCapable Attribute 1", proxyRimValue, rimCapableValue);
        logger.info("Proxy ExternalUtranCellFDD has RimCapable Attribute after changing :" + proxyRimValue);
        final String masterRimValue = csHandler.getAttributeValue(targetMasterFdn, rimCapable);
        assertEquals("Master UtranCell is not having the RimCapable Attribute value 1", masterRimValue, rimCapableValue);
        logger.info("Master UtranCell has RimCapable Attribute Value : " + masterRimValue);

        startTime = System.currentTimeMillis();
        setTestStep("Set the rimCapable attibute value to 0 or 1 in the Master UtranCell");
        csHandler.setAttributeValue(targetMasterFdn, rimCapable, masterRimCapable);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        final String masterRimAttribute = csHandler.getAttributeValue(targetMasterFdn, rimCapable);
        assertEquals("Master UtranCell is not having the RimCapable Attribute value 0", masterRimAttribute, masterRimCapable);
        logger.info("Master UtranCell has RimCapable Attribute after changing :" + masterRimAttribute);

        setTestStep("Verify whether the new value is propagated to its proxy ExternalUtranCellFDD");
        final String proxyRimAttribute = csHandler.getAttributeValue(proxyFdn, rimCapable);
        assertEquals("New value of Master is not propagated to its proxy ExternalUtranCellFDD", proxyRimAttribute, masterRimCapable);
        logger.info("Proxy ExternalUtranCellFDD has RimCapable Attribute :" + proxyRimAttribute);

        setTestStep("Perform a clean up activity by resetting all the old values");
        setTestStep("Reset the RimCapable Attribute value to original value");
        csHandler.setAttributeValue(targetMasterFdn, rimCapable, "0");

    }

    @AfterMethod(alwaysRun = true)
    public void tidyUpAfterTest() {
        if (flag == true) {
            setTestStep("Clean Up UtranCellRelation");
            csHandler.deleteMo(utranCellRelation);
            setTestStep("Clean Up UtranFreqRelation");
            csHandler.deleteMo(utranFreqRelation);
            setTestStep("Clean Up the Master ExternalUtranFreq");
            csHandler.deleteMo(masterExternalUtranFreqFdn);
        }
    }
}
