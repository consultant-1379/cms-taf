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
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xrajnka
 */
public class PCI_Conflict1 extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @TestId(id = "OSS-85047_PCI_Conflict1", title = "Propagating conflicts from Proxy to Master")
    @Context(context = { Context.CLI })
    @Test(groups = { "SNAD, KGB" })
    public void handleConflictsFromProxyToMaster() {

        Fdn activeNodeFdn = null;

        setTestStep("Get a connected and synced ERBS node from OSS database.");
        activeNodeFdn = nodeCliOperator.getSynchedNode("ERBS");
        assertNotNull("No synced ERBS node found.", activeNodeFdn);
        setTestInfo("Selected node: " + activeNodeFdn);
    }
}
