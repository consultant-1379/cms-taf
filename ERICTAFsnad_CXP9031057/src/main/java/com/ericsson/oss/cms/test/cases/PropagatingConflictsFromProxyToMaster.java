/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author xjyobeh
 */

@SuppressWarnings("deprecation")
public class PropagatingConflictsFromProxyToMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static int numCells = 1;

    final static String UNDEFINED = "<UndefinedValue>";

    final static String PCI_RESOLVE_VAL = "0";

    final static String CONFLICT_RESET = "\" \"";

    final static String CEL_VAL_RESET = "\"<UndefinedValue>\"";

    final static String CONFLICT_VALUE = "pciConflict";

    final static String DETECTING_CELL = "pciDetectingCell";

    final static String CONFLICT_CELL = "pciConflictCell";

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final CSLibTestHandlers csLibHandler = new CSLibTestHandlers(HostGroup.getOssmaster());

    /**
     * @DESCRIPTION
     *              Add PCI conflicts, ConflictingCell, DetectingCell to proxy.
     *              And then the conflicts should be propagated to master{@link https
     *              ://taftm.lmera.ericsson.se/#tm/viewTC/OSS_85047_Propagating_conflicts_from_Proxy_to_Master}
     * @PRE SNAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY NORMAL
     */

    @TestId(id = "OSS_85047_Propagating_conflicts_from_Proxy_to_Master", title = "Propagating conflicts from Proxy to Master")
    @Context(context = { Context.CLI })
    @DataDriven(name = "propagatingconflictsfromp2m")
    @Test(groups = { "SNAD, KGB" })
    public void propagatingConflictsFromProxyToMaster(
            @Input("nodeType") final String nodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("parentType") final String moType) {

        final String[] Attributes = { DETECTING_CELL, CONFLICT_CELL };

        setTestStep("Get connected and synced nodes with parent MO types defined from OSS database.");
        setTestInfo("Searching for a nodes of %s node Type ", nodeType);
        final List<Fdn> activeNodeFdns = nodeCliOperator.getListOfSyncedNode(csHandler, nodeType,
                NodeCliOperator.buildNodeFiltering(sourceNodeAdditionalFiltering, numCells, moType));
        assertFalse("No synced nodes found.", activeNodeFdns.isEmpty());

        setTestStep("Get Master MO and a Proxy MO with 0 conflicts from synced Nodes");
        final List<Fdn> noConflictsFDN = snadOperator.getZeroConflictMasterProxyFromSyncedNodes(activeNodeFdns, moType, 1);
        assertNotNull("Failed to find Master Mo and Proxy Mo with zero conflicts .", noConflictsFDN);
        final Fdn masterMO = noConflictsFDN.get(0);
        final Fdn proxyMO = noConflictsFDN.get(1);
        setTestInfo("Master MO found is: " + masterMO);
        setTestInfo("Proxy MO found is: " + proxyMO);
        final Map<String, String> plmIdentity = snadOperator.getPlmIdentity(masterMO, masterMO.getMeContext());
        final String valueConflict_DC = snadOperator.setPciDetCellValues(plmIdentity, Integer.parseInt(plmIdentity.get("cellId")), 1);
        final String valueConflict_CC = snadOperator.setPciCellValues(plmIdentity, Integer.parseInt(plmIdentity.get("cellId")), 1);
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO before adding conflicts : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO before adding conflicts : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));

        // 1st SCENARIO:
        setTestStep("Adding 1 conflicts in one pci container of a proxy and let it propagate to master.");
        setTestStep(
                "Set PCI conflict attributes [pciConflict, pciConflictingCell, pciDetectingCell] for first container on Proxy MO ExternalEUtranCellFDD in NETSIM for the selected node type.");
        final String[] singleConflict = { valueConflict_DC, valueConflict_CC };
        long startTime = System.currentTimeMillis();
        csLibHandler.setAttributes(proxyMO, Attributes, singleConflict);
        csHandler.setAttributeValue(proxyMO, CONFLICT_VALUE, "\"2\"");
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO after adding 1 conflicts in one pci container : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO after adding 1 conflicts in one pci container : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        assertEquals("PCI values not propagated to Master.", csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL).toString(),
                csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL).toString());

        // 2nd SCENARIO:
        setTestStep("Adding more than 1 conflicts simultaneously in one proxy and let it propagate to master.");
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO before setting more than 1 conflicts : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO before setting more than 1 conflicts : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        setTestStep(
                "Set PCI conflict attributes [pciConflict, pciConflictingCell, pciDetectingCell] for more than 1 container on Proxy MO ExternalEUtranCellFDD/TDD simultaneously in NETSIM for the selected node type.");
        startTime = System.currentTimeMillis();
        final String valueConflicts_DC = snadOperator.setPciDetCellValues(plmIdentity, Integer.parseInt(plmIdentity.get("cellId")), 2);
        final String valueConflicts_CC = snadOperator.setPciCellValues(plmIdentity, Integer.parseInt(plmIdentity.get("cellId")), 2);
        final String[] multiConflict = { valueConflicts_DC, valueConflicts_CC };
        csLibHandler.setAttributes(proxyMO, Attributes, multiConflict);
        csHandler.setAttributeValue(proxyMO, CONFLICT_VALUE, "\"2 3\"");
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO after setting more than 1 conflicts : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO after setting more than 1 conflicts : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        assertEquals("Set of PCI values not propagated to Master.", csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL).toString(),
                csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL).toString());

        // 3rd SCENARIO:
        setTestStep("Checking: If any of the conflicts except 1st, is getting resolved then that conflicts only will get resolve in Master also.");
        startTime = System.currentTimeMillis();
        csHandler.setAttributeValue(proxyMO, CONFLICT_VALUE, "\"2 1\"");
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO after resolving conflicts other than first : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO after resolving conflicts other than first : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        assertEquals("Respective Conflicts in the master not resolved", csHandler.getAttributeValue(masterMO, CONFLICT_VALUE).trim(), "2");

        // 4th SCENARIO:
        setTestStep("Checking: If conflict present in the 1st pci container is getting resolved then rest of the conflicts in Master will also get resolve.");
        startTime = System.currentTimeMillis();
        csHandler.setAttributeValue(proxyMO, CONFLICT_VALUE, "\"0 3\"");
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO after resolving first conflicts : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO after resolving first conflicts : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        assertEquals("Conflicts in the master not resolved", csHandler.getAttributeValue(masterMO, CONFLICT_VALUE).trim(), "0");

        // RESETTING TO OLDER VALUE
        setTestStep("Clean up: Reset the changed attributes to original value");
        final String[] pciAttributes = { DETECTING_CELL, CONFLICT_CELL, CONFLICT_VALUE };
        final String[] attribute_old_val = { CEL_VAL_RESET, CEL_VAL_RESET, CONFLICT_RESET };
        csHandler.setAttributes(proxyMO, pciAttributes, attribute_old_val);
        startTime = System.currentTimeMillis();
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        csHandler.setAttributes(masterMO, pciAttributes, attribute_old_val);
        startTime = System.currentTimeMillis();
        snadOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of master MO after resetting : "
                + csHandler.getAttributes(masterMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
        logger.info("pciConflict, pciDetectingCell, pciConflictCell of proxy MO after resetting : "
                + csHandler.getAttributes(proxyMO, CONFLICT_VALUE, DETECTING_CELL, CONFLICT_CELL));
    }
}
