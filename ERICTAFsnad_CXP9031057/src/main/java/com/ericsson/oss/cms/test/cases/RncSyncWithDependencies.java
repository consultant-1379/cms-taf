/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.FdnConstants.RNC_FUNCTION;
import static com.ericsson.oss.cms.test.constants.CmsConstants.FdnConstants.RNC_NODE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.FdnConstants.UTRAN_RELATION;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.COLDRESTART_WAIT_TIME;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.RncSyncWithDependenciesCliOperator;
import com.ericsson.oss.cms.test.operators.SnadCacheCliOperator;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author xmurran
 */
public class RncSyncWithDependencies extends TorTestCaseHelper implements TestCase {

    @Inject
    private RncSyncWithDependenciesCliOperator rncSyncWithDependenciesCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SnadCacheCliOperator snadCacheCliOperator;

    private final SmtoolHandler smtoolHandler = new SmtoolHandler(HostGroup.getOssmaster());

    /**
     * @DESCRIPTION Verify that SNAD's SubNetwork synchronize on RNC's isn't dependent on
     *              other RNCs that have inter UtranRelations.
     * @PRE Nodes are connected and synched.
     *      Two RNC's have inter UtranRelations to each other.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-51704_RNC_Sync", title = "SNAD Synchronises RNC's with dependencies on one another")
    @Context(context = { Context.CLI })
    @Test(groups = { "KGB" })
    public void handleRncSyncWithDependencies() {

        setTestStep("Get two Inter UtranRelations between two synched RNCs in the SubNetwork");
        setTestInfo("Verify that each UtranRelation exist on different RNC node.");
        setTestInfo("Verify each UtranRelation points to a UtranCell on the other RNC node");

        final List<Fdn> rncSynchedNodeList = nodeCliOperator.getPercentageMimScopedSynchedNode(RNC_NODE, 100);
        snadCacheCliOperator.executeCacheReview();
        final List<Fdn> utranRelMoFdnList = rncSyncWithDependenciesCliOperator.getInterelatedRNCNodes(rncSynchedNodeList, snadCacheCliOperator, UTRAN_RELATION);
        assertEquals("Fdn list expected to have two RNC nodes", utranRelMoFdnList.size(), 2);
        setTestInfo("Discovered two nodes with inter UtranRelations:\n" + "1 :" + utranRelMoFdnList.get(0) + "\n" + "2 :" + utranRelMoFdnList.get(1));

        setTestStep("Get the system time and store it as start time.");
        final long startTime = System.currentTimeMillis();

        setTestStep("Cold restart the SNAD MC.");
        smtoolHandler.coldrestart(SNAD_MC);
        assertTrue("SNAD MC has failed to come online in given time", smtoolHandler.isMCStarted(SNAD_MC, COLDRESTART_WAIT_TIME));

        setTestStep("Check CIF log for recovery message for both RNC nodes");
        final Fdn firstRncNodeFdn = utranRelMoFdnList.get(0).getMeContext();
        final Fdn secondRncNodeFdn = utranRelMoFdnList.get(1).getMeContext();

        final List<CIFLogItem> cifLogItemForFirstNode = rncSyncWithDependenciesCliOperator.checkRncNodeRecoveryMsgInCifLog(firstRncNodeFdn, startTime);
        assertThat("First RNC Node RECOVER message is not found in CIF log", cifLogItemForFirstNode, is(not(empty())));

        final List<CIFLogItem> cifLogItemForSecondNode = rncSyncWithDependenciesCliOperator.checkRncNodeRecoveryMsgInCifLog(secondRncNodeFdn, startTime);
        assertThat("Second RNC Node RECOVER message is not found in CIF log", cifLogItemForSecondNode, is(not(empty())));

        setTestStep("Check SNAD managed cache");
        snadCacheCliOperator.executeCacheReview();

        final Fdn rncFunctionMoFdnOfFirstNode = utranRelMoFdnList.get(0).getAncestor(RNC_FUNCTION);
        assertTrue("RncFunction Mo of first node is not exist in snad managed cache", snadCacheCliOperator.isInMasterCache(rncFunctionMoFdnOfFirstNode));

        final Fdn rncFunctionMoFdnOfSecondNode = utranRelMoFdnList.get(1).getAncestor(RNC_FUNCTION);
        assertTrue("RncFunction Mo of second node is not exist in snad managed cache", snadCacheCliOperator.isInMasterCache(rncFunctionMoFdnOfSecondNode));

        final Fdn utranCellMoFdnOfFirstNode = utranRelMoFdnList.get(0).getParentFdn();
        assertTrue("UtranCell Mo of first node is not exist in snad managed cache", snadCacheCliOperator.isInMasterCache(utranCellMoFdnOfFirstNode));

        final Fdn utranCellMoFdnOfSecondNode = utranRelMoFdnList.get(1).getParentFdn();
        assertTrue("UtranCell Mo of second node is not exist in snad managed cache", snadCacheCliOperator.isInMasterCache(utranCellMoFdnOfSecondNode));
    }
}