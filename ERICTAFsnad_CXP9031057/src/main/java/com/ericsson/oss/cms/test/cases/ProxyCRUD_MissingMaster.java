package com.ericsson.oss.cms.test.cases;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import javax.inject.Inject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.ProxyCRUDCLIOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class ProxyCRUD_MissingMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private ProxyCRUDCLIOperator proxyOperator;

    @Inject
    private SnadApiOperator snadOperator;

    private final CSHandler cs = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private Fdn proxy;

    /**
     * @DESCRIPTION This test is to test that Snad's Master Proxy handling functions. It will test that Snad handles proxies which are
     *              MissingMaster correctly, i.e. It will create the MasterMO. It also verifies the Master to Proxy propagation and deletion
     *              of SubNetwork Masters.
     * @PRE cms_snad_reg MC is online Nodes are Connected and Synchronized.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-38669_FUNC_1", title = "Create Set & Delete of MissingMaster Proxy")
    @Context(context = { Context.CLI })
    @DataDriven(name = "proxycrud_missingmaster")
    @Test(groups = { "CDB", "Functional Tests", "KGB" })
    public void createProxy_MissingMaster(
            @Input("testId") final String testId,
            @Input("proxyType") final String proxyType,
            @Input("parentType") final String parentType,
            @Input("mandatoryAttributes") final String[] mandatoryAttributes,
            @Input("mandatoryAttributeValues") final String[] mandatoryAttributeValues,
            @Input("attributeToSet") final String attributeToSet,
            @Input("valueToSet") final String valueToSet) {

        final String testCaseId = String.format("%s_%s", getTestId(), testId);
        setTestCase(testCaseId, getCurTcName());

        setTestStep("Create ProxyMO in MissingMaster state");
        proxy = proxyOperator.createProxy(proxyType, parentType);
        assertTrue("ProxyMO is created in the CS", cs.moExists(proxy));
        assertEquals("CONSISTENT", snadOperator.getProxyState(proxy));
        // assertTrue("SubNetwork MasterMO is created in the CS", false);
        // assertTrue("SubNetwork MasterMO is in the Snad cache and is Consistent", false);

        // setTestStep("Set a common attribute on the MasterMO");
        // assertTrue("MasterMO attribute is changed in the CS", false);
        // assertTrue("MasterMO is Consistent in the Snad cache", false);
        // assertTrue("All proxies have their attribute changed in the CS", false);
        // assertTrue("All proxies are Consistent in the Snad cache", false);

        // setTestStep("Delete SubNetwork MasterMO");
        // assertTrue("MasterMO is deleted in the CS", false);
        // assertTrue("MasterMO is removed from the Snad cache", false);
        // assertTrue("All proxies are deleted in the CS", false);
        // assertTrue("All proxies are removed from the Snad cache", false);
    }

    @AfterMethod
    public void teardown() {
        cs.deleteMo(proxy);
        assertFalse("ProxyMO was not deleted during cleanup.", cs.moExists(proxy));
    }
}