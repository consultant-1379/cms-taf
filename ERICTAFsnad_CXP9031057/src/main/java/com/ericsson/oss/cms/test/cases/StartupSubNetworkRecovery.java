/**
 * -----------------------------------------------------------------------
 Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.CC_RESUMED;
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
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cms.test.operators.CIFLogCliOperator;
import com.ericsson.oss.cms.test.operators.MoCliOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.cms.test.operators.SnadCacheCliOperator;
import com.ericsson.oss.cms.test.operators.StartupRecoveryCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author eeimacn
 */
public class StartupSubNetworkRecovery extends TorTestCaseHelper implements TestCase {

    @Inject
    private SnadCacheCliOperator snadCacheCliOperator;

    @Inject
    private SnadApiOperator snadApiOperator;

    @Inject
    private MoCliOperator moCliOperator;

    @Inject
    private CIFLogCliOperator cifLogCliOperator;

    @Inject
    StartupRecoveryCliOperator recoverOperator;

    private final CSHandler csTestHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Region);

    private final SmtoolHandler smtoolHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private final static int LOGWAITTIME = 60;

    private final static String FILTERLEVEL = "4";

    /**
     * @DESCRIPTION This test is to test that SNAD recovers SubNetwork adds master and proxy MOs to cache on startup.
     * @PRE SNAD & NEAD mc is up
     *      SubNetwork is recovered and SubNetwork MOs are in SNAD cache
     * @PRIORITY HIGH
     */

    @TestId(id = "OSS-73132_StartupSubNetworkRecovery", title = "SNAD startup triggers recovery of SubNetwork")
    @Context(context = { Context.CLI })
    @DataDriven(name = "startupSubnetRecovery")
    @Test(groups = { "KGB" })
    public void startupSubnetworkRecovery(@Input("masterMoTypes") final String[] masterMoTypes) {

        setTestStep("Get a list of all Master MOs that exist in the CS for the SubNetwork");
        final Fdn rootMo = csTestHandler.getSubNetworkRootMo();
        final List<Fdn> subNetworkMOs = moCliOperator.getChildrenMoFdnsWithLevel(rootMo, FILTERLEVEL, masterMoTypes);
        recoverOperator.logTypesNotFound(masterMoTypes, subNetworkMOs, "SubNetwork");
        assertThat("No Master MO FDNs for given types have been found in SubNetwork", subNetworkMOs, not(empty()));

        setTestStep("Check that the SubNetwork is recovered and all expected MOs are in Cache");
        snadCacheCliOperator.executeCacheReview();
        List<Fdn> fdnsNotInCache = snadCacheCliOperator.getFdnsNotInCache(subNetworkMOs);
        assertThat("The following MOs were not found in the cache: " + snadCacheCliOperator.reportMosNotInCache(fdnsNotInCache), fdnsNotInCache, is(empty()));

        setTestStep("Coldrestart SNAD MC");
        smtoolHandler.coldrestart(SNAD_MC);
        final long startTime = System.currentTimeMillis();
        assertTrue("SNAD MC has failed to come online in given time", smtoolHandler.isMCStarted(SNAD_MC, COLDRESTART_WAIT_TIME));

        setTestStep("Wait for log message in CIF logs indicating recovery has finished");
        setTestInfo("First waiting to get log that recovery has finished");
        final List<CIFLogItem> cifLogItems = cifLogCliOperator.waitForExpectedMessages(LOGWAITTIME, CC_RESUMED);
        assertThat("Recovery has failed to complete. CC has not restarted", cifLogItems, is(not(empty())));

        setTestStep("Run query on CIF logs to get log indicating SubNetwork Recovery completed successfully");
        setTestInfo("Recovery has finished, so running query on log db to verify that SubNetwork was recovered successfully");
        assertThat("SuNetwork has not recovered successfully", snadApiOperator.hasSubNetworkRecovered(rootMo.getFdn(), startTime));

        setTestStep("Check that the SubNetwork is recovered and all expected MOs are in Cache");
        snadCacheCliOperator.executeCacheReview();
        fdnsNotInCache = snadCacheCliOperator.getFdnsNotInCache(subNetworkMOs);
        assertThat("The following MOs were not found in the cache: " + snadCacheCliOperator.reportMosNotInCache(fdnsNotInCache), fdnsNotInCache, is(empty()));
    }
}
