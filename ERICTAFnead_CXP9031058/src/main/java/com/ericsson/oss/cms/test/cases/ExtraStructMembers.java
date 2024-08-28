/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTCM;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTDM;

import javax.inject.Inject;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.CreateMoCliOperator;
import com.ericsson.oss.cms.test.operators.DisconnectNodesCliOperator;
import com.ericsson.oss.cms.test.operators.ExtraStructCliOperator;
import com.ericsson.oss.cms.test.operators.InitialSyncCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmagkum
 */

public class ExtraStructMembers extends TorTestCaseHelper implements TestCase {

    @Inject
    private ExtraStructCliOperator extrastructCliOperator;

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    @Inject
    private DisconnectNodesCliOperator disconnectNodesCliOperator;

    @Inject
    private CreateMoCliOperator createMo;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private Fdn childMoFdn;

    @TestId(id = "OSS-96092", title = "Incompatible extra Struct members are handled properly.")
    @Context(context = { Context.CLI })
    @DataDriven(name = "extra_struct")
    @Test(groups = { "NEAD, KGB" })
    public void extraStructMembers(
            @Input("fdn") final String fdn,
            @Input("NodeType") final String nodeType,
            @Input("ucFdn") final String ucFdn,
            @Input("ucAttrNames") final String[] AttrNames,
            @Input("ucAttrValues") final String[] AttrValues) {

        final Fdn fdnToCheck = new Fdn(fdn);

        setTestStep("Add in the V.32259/32000/32275 version node RNC using Arne ");
        setTestInfo("Verify that the node is already added or not ");
        setTestInfo("If exist -Delete the node from OSS and Add it again");
        setTestInfo("If not-Add the node in OSS");
        setTestInfo("Check MIB Adapter attached");

        setTestStep("Check the Sync status of the Node");

        boolean Nodesync = disconnectNodesCliOperator.checkNodesSyncStatus(fdnToCheck, 3, 120);
        assertTrue("Node is synched successfully", Nodesync);

        setTestStep("Adding Location Area");
        setTestInfo(initialSyncCliOperator.hostConnect(CSTESTCM + "SubNetwork=ONRM_ROOT_MO_R,Areas=1,Plmn=46_6_2,LocationArea=1256 -attr lac 1256"));

        setTestStep("Adding Service Area");
        setTestInfo(initialSyncCliOperator
                .hostConnect(CSTESTCM + "SubNetwork=ONRM_ROOT_MO_R,Areas=1,Plmn=46_6_2,LocationArea=1256,ServiceArea=1256 -attr sac 1256"));

        setTestStep("Adding UtranCell");
        final Fdn parentMoFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC21,MeContext=RNC21,ManagedElement=1,RncFunction=1");
        childMoFdn = createMo.buildMoFdn(parentMoFdn, ucFdn, getTestId());
        final boolean flag = extrastructCliOperator.addUtranCell(childMoFdn, AttrNames, AttrValues);

        assertTrue("Failed to create UtranCell ", flag);

        setTestStep("Check the Sync status of the Node");

        Nodesync = disconnectNodesCliOperator.checkNodesSyncStatus(fdnToCheck, 3, 120);
        assertTrue("Node is synched successfully", Nodesync);

    }

    @AfterTest(alwaysRun = true)
    public void afterTest() {

        setTestStep("Delete the UtranCell from the OSS");

        csHandler.deleteMo(childMoFdn);

        setTestStep("Deleting Location Area");
        setTestInfo(initialSyncCliOperator.hostConnect(CSTESTDM + "SubNetwork=ONRM_ROOT_MO_R,Areas=1,Plmn=46_6_2,LocationArea=1256"));

    }
}