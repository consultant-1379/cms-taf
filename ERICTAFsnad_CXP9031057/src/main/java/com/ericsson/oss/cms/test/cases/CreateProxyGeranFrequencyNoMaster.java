/**
* -----------------------------------------------------------------------
*     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
* -----------------------------------------------------------------------
*/
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MISSINGMASTER_AUTOFIX_ON;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CONSISTENT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.CreateMoCliOperator;
import com.ericsson.oss.cms.test.operators.GeranFrequencyCLIOperator;
import com.ericsson.oss.cms.test.operators.SetMoCliOperator;
import com.ericsson.oss.cms.test.operators.SnadApiOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
* @author egokdag
*/
public class CreateProxyGeranFrequencyNoMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private GeranFrequencyCLIOperator geranFrequencyCLIOperator;

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private SetMoCliOperator setMoCliOperator;

    @Inject
    private SnadApiOperator snadApiOperator;

    private final SMHandler smtoolHandler = new SmtoolHandler(HostGroup.getOssmaster());

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private String autoFixParameter;

    private String autoFixOrigVal;

    private List<Fdn> proxyGeranFrequencies;

    private Fdn masterExternalGsmFreq;

    private Fdn masterExternalGsmFreqGroup;

    /**
     * @param masterExternalGsmFreqGroup
     * @DESCRIPTION Verify SNAD creates SN Master MO and corresponding proxy MOs when a proxy GeranFrequency is created.
     * @PRE 1) NEAD & SNAD MC is online
     *      2) At least one syncronized/connected node per GeranFrequency model, i.e., one using new and one using old
     *      GeranFrequency model exists for the specified node neType.
     *      3) At least one proxy GeranFreqGroup from the nodes selected in the
     *      above, exists in more than one node.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-55647_Create_GeranFrequency", title = "Create Proxy GeranFrequency when no master exists - Autofix on")
    @Context(context = { Context.CLI })
    @DataDriven(name = "createproxygeranfrequencynomaster")
    @Test(groups = { "KGB" })
    public void createProxyNoMasterTest(
            @Input("nodeType") final String nodeType,
            @Input("freqGroupType") final String freqGroupType,
            @Input("parentType") final String parentType,
            @Input("proxyType") final String proxyType,
            @Input("proxyAttrNames") final String[] proxyAttrNames,
            @Input("proxyAttrValues") final String[] proxyAttrValues,
            @Input("proxyGeranGroupRefAttr") final String proxyGeranGroupRefAttr,
            @Input("proxyGeranGroupRefAttrType") final String proxyGeranGroupRefAttrType,
            @Input("masterAttrNames") final String[] masterAttrNames,
            @Input("autoFixParam") final String autoFixParam) {

        setTestStep("Get an MO of Geran Frequency Group Type from connected and synced node of NodeType");
        setTestInfo("The selected MO can be on a node which is using new or old GeranFrequency Model determined by the provided parentType.");
        setTestInfo("The master of the selected MO should have proxies on more than one node.");
        setTestInfo("The Master MO for the selected MO must be in Consistent state in SNAD cache.");
        final Fdn sharedGeranFreqGroupFdn = geranFrequencyCLIOperator.getSharedGeranFreqGroupFdn(nodeType, freqGroupType, parentType, proxyType);
        setTestInfo("No mo of type " + freqGroupType + ", which satisfy the above criteria was found on a synced node."+ sharedGeranFreqGroupFdn);
        assertThat("No mo of type " + freqGroupType + ", which satisfy the above criteria was found on a synced node.", sharedGeranFreqGroupFdn, notNullValue());
        setTestInfo("Selected MO: " + sharedGeranFreqGroupFdn);

        setTestStep("Read the value of the autoFixParam parameter from SNAD MC and store it");
        autoFixOrigVal = smtoolHandler.getConfigurationForMC(SNAD_MC, autoFixParam);
        autoFixParameter = autoFixParam;
        setTestInfo("AutoFix parameter before test: " + autoFixOrigVal);

        setTestStep("Set the value of SNAD AutoFix parameter to include Missing_Master");
        final boolean autoFixOn = smtoolHandler.setConfigurationForMC(SNAD_MC, autoFixParam, MISSINGMASTER_AUTOFIX_ON);
        assertTrue("Failed to set AutoFix parameter", autoFixOn);

        final long startTime = System.currentTimeMillis();
        setTestStep("Create an MO of proxyType on the node, with provided attribute values");
        final String[] proxyAttrNamesNetsim = geranFrequencyCLIOperator.addMoRefData(parentType, proxyAttrNames, proxyGeranGroupRefAttr);
        final String[] proxyAttrValuesNetsim = geranFrequencyCLIOperator.addMoRefData(parentType, proxyAttrValues, sharedGeranFreqGroupFdn.getLdn(),
                proxyGeranGroupRefAttrType);
        final Fdn proxyMoFdn = geranFrequencyCLIOperator.buildMoFdn(parentType, sharedGeranFreqGroupFdn, proxyType, getTestId());
        final boolean proxyMoCreatedInNetSim = createMoCliOperator.createMo(proxyMoFdn, proxyAttrNamesNetsim, proxyAttrValuesNetsim);
        setTestInfo("Is MO created in NETSIM " + proxyMoCreatedInNetSim);
        assertTrue("Failed to create MO in NETSIM ", proxyMoCreatedInNetSim);
        snadApiOperator.waitForSleep(startTime, MAX_TIME_TO_READ_CIF_LOGS);
        boolean moExistInCS = csHandler.moExists(proxyMoFdn);
        assertTrue("Failed to create MO in CS database", moExistInCS);
        boolean attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(proxyMoFdn, proxyAttrNames, proxyAttrValues);
        assertTrue("Created MO attributes in database does not match with input data", attributesSetInDatabase);

        final String proxyStateAfterCreate = geranFrequencyCLIOperator.getProxyStateAfterCC(proxyMoFdn);
        assertThat("Proxy MO state is not as expected after create", proxyStateAfterCreate, is(equalTo(CONSISTENT)));
        setTestInfo("Proxy GeranFrequency state after create is :" + proxyStateAfterCreate);

        setTestStep("Get Master MO for the proxy MO created in the previous step");
        final Fdn masterMoFdn = snadApiOperator.getMasterForProxy(proxyMoFdn);
        moExistInCS = csHandler.moExists(masterMoFdn);
        assertTrue("SNAD did't create the Master MO in CS database", moExistInCS);
        attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(masterMoFdn, masterAttrNames, proxyAttrValues);
        assertTrue("Master MO attribute values in database don't match with proxy attribute values", attributesSetInDatabase);
        final String masterState = snadApiOperator.getMasterState(masterMoFdn);
        assertThat("Master MO state is not as expected after create", masterState, is(equalTo(CONSISTENT)));
        setTestInfo("Master ExternalGsmFreq state after create is :" + masterState);

        setTestStep("Get all GeranFreqGroup proxies of the master of selected GeranFreqGroup");
        masterExternalGsmFreqGroup = snadApiOperator.getMasterForProxy(sharedGeranFreqGroupFdn);
        final List<Fdn> proxyGeranFreqGroups = snadApiOperator.getProxiesForMaster(masterExternalGsmFreqGroup);

        setTestStep("Get all proxies of the master of the created MO");
        final Fdn masterExternalGsmFreq = snadApiOperator.getMasterForProxy(proxyMoFdn);
        setTestInfo("Master of new GeranFrequency is " + masterExternalGsmFreq.getFdn());
        proxyGeranFrequencies = snadApiOperator.getProxiesForMaster(masterExternalGsmFreq);
        assertThat("Expected number of proxy GeranFrequencies are not created by SNAD", proxyGeranFrequencies, hasSize(proxyGeranFreqGroups.size()));
        setTestInfo("Expected number of proxy GeranFrequencies are created");
        attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(proxyGeranFrequencies, proxyAttrNames, proxyAttrValues);
        assertTrue("Attribute values of Proxy GeranFrequencies don't match with provided attribute values in data file", attributesSetInDatabase);
        final boolean areProxyGeranFrequenciesConsistent = snadApiOperator.areProxiesConsistent(proxyGeranFrequencies);
        assertTrue("Not all proxy GeranFrequencies of Master MO: " + masterExternalGsmFreq + ", are in Consistent State in SNAD cache",
                areProxyGeranFrequenciesConsistent);
        setTestInfo("Proxy GeranFrequencies of Master ExternalGsmFreq are consistent in SNAD cache");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
		setTestInfo("Deleting proxyGeranFrequencies");
        if (proxyGeranFrequencies != null) {
            csHandler.deleteMos(proxyGeranFrequencies);
        }
		setTestInfo("Deleting masterExternalGsmFreq");
        if (masterExternalGsmFreq != null) {
            csHandler.deleteMo(masterExternalGsmFreq);
        }
		setTestInfo("setting autoFixParameter back to oldvalue");
        smtoolHandler.setConfigurationForMC(SNAD_MC, autoFixParameter, autoFixOrigVal);

    }
}
