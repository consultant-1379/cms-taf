/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2017 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.CSLibTestHandlers;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xgggjjj
 */
public class ProxyToMasterPropagationSixthConflict extends TorTestCaseHelper implements TestCase {
    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    private static int numCells = 1;

    final static String CONFLICT_RESET = "\" \"";

    final static String CEL_VAL_RESET = "\"<UndefinedValue>\"";

    final String[] pciAttributes = { "pciDetectingCell", "pciConflictCell", "pciConflict" };

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final CSLibTestHandlers csLibHandler = new CSLibTestHandlers(HostGroup.getOssmaster());

    /**
     * @DESCRIPTION
     *              Taking one master and its two proxies with no pci conflicts.
     *              Adding 5 conflicts in one proxy and check it is propagated to master.
     *              Adding one more conflict in the second proxy and check the master.
     *              Resolve one conflict from first proxy, and the new conflict from second proxy will be updated in master. {@link https
     *              ://taftm.lmera.ericsson.se/#tm/viewTC/OSS_132843_Verifying_sixth_conflict_propagation}
     * @PRE SNAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY Normal
     */

    @TestId(id = "OSS_132843_Verifying_sixth_conflict_propagation", title = "Verifying sixth conflict propagation")
    @Context(context = { Context.CLI })
    @DataDriven(name = "proxyToMasterPropagationSixthConflict")
    @Test(groups = { "KGB" })
    public void pciConflictPropagationTest(
            @Input("nodeType") final String nodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("moType") final String moType) {

        setTestStep("Get a connected and synced nodes with parent MO types defined from OSS database");
        setTestInfo("Searching for a nodes with type " + nodeType);
        final List<Fdn> activeNodeFdns = nodeCliOperator.getListOfSyncedNode(csHandler, nodeType,
                NodeCliOperator.buildNodeFiltering(sourceNodeAdditionalFiltering, numCells, moType));
        assertFalse("No synced nodes found.", activeNodeFdns.isEmpty());
        setTestStep("Get Master MO and 2 Proxy MOs with 0 conflicts from synced Nodes");
        final List<Fdn> noConflictMasterAndProxy = snadOperator.getZeroConflictMasterProxyFromSyncedNodes(activeNodeFdns, moType, 2);
        assertNotNull("No Master MO found with 0 conflicts.", noConflictMasterAndProxy);
        final Fdn masterMoFdn = noConflictMasterAndProxy.get(0);
        final Fdn proxyMoFdn1 = noConflictMasterAndProxy.get(1);
        final Fdn proxyMoFdn2 = noConflictMasterAndProxy.get(2);
        setTestInfo("Selected Master MO: " + masterMoFdn);
        setTestInfo("Selected Proxy MOs: " + proxyMoFdn1 + '\n' + proxyMoFdn2);

        setTestInfo("Getting the plmIdentity values for pciConflictCell and pciDetectingCell");
        final Map<String, String> plmIdentity = snadOperator.getPlmIdentity(masterMoFdn, masterMoFdn.getMeContext());
        final int cellId = Integer.parseInt(plmIdentity.get("cellId"));

        final String detecting_cell_values_p1 = snadOperator.setPciCellValues(plmIdentity, cellId, 5);
        final String conflicting_cell_values_p1 = snadOperator.setPciCellValues(plmIdentity, cellId + 5, 5);
        final String detecting_cell_values_p2 = snadOperator.setPciCellValues(plmIdentity, cellId + 10, 1);
        final String conflicting_cell_values_p2 = snadOperator.setPciCellValues(plmIdentity, cellId + 11, 1);
        final String conflicts_p1 = snadOperator.setPciConflictValues("2", "2", "2", "2", "2");
        final String conflictValues_resolve = snadOperator.setPciConflictValues("2", "2", "2", "2", "0");
        final String conflicts_p2 = snadOperator.setPciConflictValues("3");

        final String[] attributeValues_p1 = { detecting_cell_values_p1, conflicting_cell_values_p1, conflicts_p1 };
        final String[] attributeValues_p2 = { detecting_cell_values_p2, conflicting_cell_values_p2, conflicts_p2 };
        final String[] attribute_old_val = { CEL_VAL_RESET, CEL_VAL_RESET, CONFLICT_RESET };

        long startTime = System.currentTimeMillis();
        String masterPciVals_after_change, proxyPciVals_after_change;
        setTestStep("Adding 5 conflicts in first proxy");
        csLibHandler.setAttributes(proxyMoFdn1, pciAttributes, attributeValues_p1);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        setTestStep("Verify all 5 conflicts are propagated to master from first proxy");
        for (final String pciAttribute : pciAttributes) {
            masterPciVals_after_change = csHandler.getAttributeValue(masterMoFdn, pciAttribute);
            proxyPciVals_after_change = csHandler.getAttributeValue(proxyMoFdn1, pciAttribute);
            assertEquals("Attribute is failed propagated to master from first proxy", masterPciVals_after_change, proxyPciVals_after_change);
            setTestInfo(pciAttribute + " is propagated to master successfully from first proxy");
        }

        startTime = System.currentTimeMillis();
        setTestStep("Adding a conflict in second proxy " + proxyMoFdn2);
        csLibHandler.setAttributes(proxyMoFdn2, pciAttributes, attributeValues_p2);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        setTestStep("Verify the conflict from second proxy is not propagated to master");
        for (final String pciAttribute : pciAttributes) {
            masterPciVals_after_change = csHandler.getAttributeValue(masterMoFdn, pciAttribute);
            if (!pciAttribute.equalsIgnoreCase("pciConflict")) {
                proxyPciVals_after_change = csHandler.getAttributeValue(proxyMoFdn2, pciAttribute).substring(15);
            } else {
                proxyPciVals_after_change = csHandler.getAttributeValue(proxyMoFdn2, pciAttribute);
            }
            assertFalse("Invalid propagation", masterPciVals_after_change.contains(proxyPciVals_after_change));
            setTestInfo(pciAttribute + " is not propagated to master from second proxy, since the container is full");
        }

        startTime = System.currentTimeMillis();
        setTestStep("Resolving a conflict from first proxy");
        csHandler.setAttributeValue(proxyMoFdn1, pciAttributes[2], conflictValues_resolve);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        setTestStep("Verify the conflict from second proxy is propagated to master");
        for (final String pciAttribute : pciAttributes) {
            masterPciVals_after_change = csHandler.getAttributeValue(masterMoFdn, pciAttribute);
            if (!pciAttribute.equalsIgnoreCase("pciConflict")) {
                proxyPciVals_after_change = csHandler.getAttributeValue(proxyMoFdn2, pciAttribute).substring(15);
            } else {
                proxyPciVals_after_change = csHandler.getAttributeValue(proxyMoFdn2, pciAttribute);
            }
            assertTrue("Attribute is failed propagated to master from second proxy", masterPciVals_after_change.contains(proxyPciVals_after_change));
            setTestInfo(pciAttribute + " is propagated to master successfully from second proxy");
        }

        startTime = System.currentTimeMillis();
        setTestStep("Clean up: Resetting the master and proxies attributes to old value");
        csHandler.setAttributes(proxyMoFdn1, pciAttributes, attribute_old_val);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        csHandler.setAttributes(proxyMoFdn2, pciAttributes, attribute_old_val);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        csHandler.setAttributes(masterMoFdn, pciAttributes, attribute_old_val);
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
    }
}
