/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.CC_RESUMED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.CIFLogCliOperator;
import com.ericsson.oss.cms.test.operators.MoCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.SnadCacheCliOperator;
import com.ericsson.oss.cms.test.operators.StartupRecoveryCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @DESCRIPTION Tests Node recovery after SNAD Managed Component restart.
 *              Node should be recovered in the cache and all masters and proxies for that node
 *              should exist in the cache.
 * @PRE Nodes should be connected and synchronized
 * @PRIORITY Medium
 * @author ECOLHAR
 */
public class StartupNodeRecovery extends TorTestCaseHelper implements TestCase {

    private static final int WAIT_TIME = 180 * 1000;

    @Inject
    private CIFLogCliOperator cifLogOperator;

    @Inject
    private SnadCacheCliOperator snadCacheCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private MoCliOperator moCliOperator;

    @Inject
    StartupRecoveryCliOperator recoverOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final SMHandler smHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private final int numCells = 1;

    @BeforeTest
    public void setUp() {
        setTestStep("Coldrestart the cms_snad_reg Managed Component (MC)");
        smHandler.coldrestart(SNAD_MC);
        assertThat("MC did not restart within 180 seconds", smHandler.isMCStarted(SNAD_MC, WAIT_TIME));

        setTestStep("Check that the CC has resumed after restart");
        cifLogOperator.waitForExpectedMessages(WAIT_TIME, CC_RESUMED);

        setTestStep("Execute SNAD Cache Review");
        snadCacheCliOperator.executeCacheReview();
    }

    @TestId(id = "OSS-72747_StartupNodeRecovery", title = "SNAD startup triggers node recovery")
    @Context(context = { Context.CLI })
    @DataDriven(name = "snadStartupNodeRecovery")
    @Test(groups = { "KGB" })
    public void startupRecoveryTest(
            @Input("nodeType") final String nodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("masterMoTypes") final String[] masterMoTypes,
            @Input("proxyMoTypes") final String[] proxyMoTypes,
            @Input("percentNodesToTest") final int percentNodesToTest) {

        setTestStep("Get list of connected/synched Nodes");
        final List<Fdn> activeSyncFdns = nodeCliOperator.getListOfSyncedNode(csHandler, nodeType,
                NodeCliOperator.buildNodeFiltering(sourceNodeAdditionalFiltering, numCells, masterMoTypes));
        assertThat("Provided node type and additional filter Mo not found in the synched node", activeSyncFdns, not(empty()));

        final List<Fdn> activeFdns = nodeCliOperator.getPercentageSynchedNode(activeSyncFdns, percentNodesToTest);
        assertThat("PercentageNodesToTest not found on Synched node", activeFdns, not(empty()));
        setTestStep("Fetch all Master and Proxy MOs for the Node from the CS activeFdns = " + activeFdns.toString());

        setTestStep("Fetch all Master and Proxy MOs for the Node from the CS");
        final List<Fdn> masterProxyCsMos = new ArrayList<Fdn>();
        masterProxyCsMos.addAll(moCliOperator.getChildrenMoFdns(activeFdns, masterMoTypes));
        masterProxyCsMos.addAll(moCliOperator.getChildrenMoFdns(activeFdns, proxyMoTypes));

        recoverOperator.logTypesNotFound(ArrayUtils.addAll(masterMoTypes, proxyMoTypes), masterProxyCsMos, nodeType);

        setTestStep("Check that each Master and Proxy returned from the CS exists in the SNAD cache");
        final List<Fdn> notInCache = snadCacheCliOperator.getFdnsNotInCache(masterProxyCsMos);
        assertThat("MOs missing from cache: " + snadCacheCliOperator.reportMosNotInCache(notInCache), notInCache.isEmpty());
    }
}
