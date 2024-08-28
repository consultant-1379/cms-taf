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
import com.ericsson.oss.cms.test.operators.RdsNodeCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmanvas
 */

public class RdsNode extends TorTestCaseHelper implements TestCase {

    @Inject
    private RdsNodeCliOperator rdsNodeCliOperator;

    @Inject
    private DisconnectNodesCliOperator disconnectNodesCliOperator;

    @TestId(id = "OSS-87508-InitialSync_RDS_RBS", title = "CMSNead Initial Sync rds RBS/ERBS")
    @Context(context = { Context.CLI })
    @DataDriven(name = "rds_node")
    @Test(groups = { "NEAD, KGB" })
    public void rdsNode(@Input("fdn") final String fdn, @Input("fdnProd") final String fdnProd, @Input("prodDesignation") final String prodDesignation) {

        final Fdn fdnToCheck = new Fdn(fdn);

        setTestStep("Add in the RDS RBS/ERBS using Arne ");
        setTestInfo("Verify that the node is already added or not ");
        setTestInfo("If exist -Delete the node from OSS and Add it again");
        setTestInfo("If not-Add the node in OSS");
        setTestInfo("Check MIB Adapter attached");

        setTestStep("Check the Sync status of the Node");

        final boolean rdsNodesync = disconnectNodesCliOperator.checkNodesSyncStatus(fdnToCheck, 3, 120);
        assertTrue("Node is synched successfully", rdsNodesync);

        setTestStep("Check ProdVersion attribute value for rds RBS/ERBS");
        final boolean rdsNodeProdValue = rdsNodeCliOperator.checkProdVersion(fdnProd, prodDesignation);
        assertTrue("Expecting Production Version value is 2", rdsNodeProdValue);

    }
}