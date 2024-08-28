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
import com.ericsson.oss.cms.test.operators.CppVersionCliOperator;
import com.ericsson.oss.cms.test.operators.DisconnectNodesCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmanvas
 */

public class CppVersion extends TorTestCaseHelper implements TestCase {

    @Inject
    private CppVersionCliOperator cppVersionCliOperator;

    @Inject
    private DisconnectNodesCliOperator disconnectNodesCliOperator;

    @TestId(id = "OSS-96095-CPP-version-updation", title = "cppVersion attribute in MeContext for the CPP8 based nodes onward is set to correct value")
    @Context(context = { Context.CLI })
    @DataDriven(name = "cpp_version")
    @Test(groups = { "NEAD, KGB" })
    public void cppVersion(
            @Input("fdn") final String fdn,
            @Input("cppVersion") final String cppVersion,
            @Input("cppVersionValue") final String cppVersionValue) {

        final Fdn fdnToCheck = new Fdn(fdn);

        setTestStep("Add in the CPP version node RNC/RBS/ERBS/RANAG using Arne ");
        setTestInfo("Verify that the node is already added or not ");
        setTestInfo("If exist -Delete the node from OSS and Add it again");
        setTestInfo("If not-Add the node in OSS");
        setTestInfo("Check MIB Adapter attached");

        setTestStep("Check the Sync status of the Node");

        final boolean cppNodesync = disconnectNodesCliOperator.checkNodesSyncStatus(fdnToCheck, 3, 120);
        assertTrue("Node is synched successfully", cppNodesync);

        setTestStep("Check cppVersion attribute value for  RBS/ERBS/RANAG/RNC");
        final boolean cppNodeVersionValue = cppVersionCliOperator.checkCppVersion(fdn, cppVersion, cppVersionValue);
        assertTrue("Expecting CPP version  value is ", cppNodeVersionValue);

    }
}