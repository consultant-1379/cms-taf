/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.DisconnectNodesCliOperator;
import com.ericsson.oss.cms.test.operators.MixedNodeCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmanvas
 */

public class MixedNode extends TorTestCaseHelper implements TestCase {

    @Inject
    private MixedNodeCliOperator mixedNodeCliOperator;

    @Inject
    private DisconnectNodesCliOperator disconnectNodesCliOperator;

    // private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @TestId(id = "OSS-80627-Initial_Sync_Mixed_RBS_ERBS", title = "CMSNead Initial Sync Mixed RBS/ERBS")
    @Context(context = { Context.CLI })
    @DataDriven(name = "mixed_node")
    @Test(groups = { "NEAD, KGB" })
    public void mixedNode(@Input("fdn") final String fdn, @Input("fdnProd") final String fdnProd, @Input("prodDesignation") final String prodDesignation) {

        setTestStep("Add in the MIXED RBS/ERBS using Arne ");
        setTestInfo("Verify that the node is already added or not ");
        setTestInfo("If exist -Delete the node from OSS and Add it again");
        setTestInfo("If not-Add the node in OSS");
        setTestInfo("Check MIB Adapter attached");
        final Fdn fdnCheck = new Fdn(fdn);

        setTestStep("Check the status of the Node");
        final boolean mixedNodesync = disconnectNodesCliOperator.checkNodesSyncStatus(fdnCheck, 3, 120);
        assertTrue("Node is synched successfully", mixedNodesync);

        setTestStep("Check ProdVersion attribute value for MIXED RBS/ERBS");
        final boolean mixedNodeProdValue = mixedNodeCliOperator.checkProdVersion(fdnProd, prodDesignation);
        assertTrue("Expecting Production Version value is 3", mixedNodeProdValue);

    }
}