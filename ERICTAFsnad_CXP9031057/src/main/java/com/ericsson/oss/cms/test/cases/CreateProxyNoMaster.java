/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MISSINGMASTER_AUTOFIX_ON;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.RESERVED_BY;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.CONSISTENT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.Cache.REDUNDANT_PROXY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.CreateMoCliOperator;
import com.ericsson.oss.cms.test.operators.DeleteMoCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
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
 * @author eeieonl
 */
public class CreateProxyNoMaster extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private SetMoCliOperator setMoCliOperator;

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private SnadApiOperator snadOperator;

    @Inject
    private DeleteMoCliOperator deleteMoCliOperator;

    private static int numCells = 1;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final SMHandler smtool = new SmtoolHandler(HostGroup.getOssmaster());

    /**
     * @DESCRIPTION
     *              Create Proxy MO when no Master MO exists and AutoFix is on.
     *              Proxy is created as Redundant Proxy. Add a relation that references the Proxy MO and the missing Master MO should be
     *              created.
     * @PRE SNAD MC is online, Synchronized and Connected Nodes are available.
     * @PRIORITY HIGH
     */

    @TestId(id = "OSS-51713_CreateProxy", title = "CreateProxy when no master exists - AutoFix On")
    @Context(context = { Context.CLI })
    @DataDriven(name = "createproxynomaster")
    @Test(groups = { "SNAD, KGB" })
    public void createProxyNoMasterTest(
            @Input("nodeType") final String nodeType,
            @Input("sourceNodeAdditionalFiltering") final String sourceNodeAdditionalFiltering,
            @Input("proxyParentType") final String proxyParentType,
            @Input("relationParentType") final String relationParentType,
            @Input("proxyType") final String proxyMoType,
            @Input("proxyAttrs") final String[] proxyAttrNames,
            @Input("proxyVals") final String[] proxyAttrValues,
            @Input("relationType") final String relationMoType,
            @Input("relationAttrs") final String[] relationAttrNames,
            @Input("relationVals") String[] relationAttrVals,
            @Input("masterAttrs") final String[] masterAttrNames,
            @Input("relationMasterRef") final String relationMasterRef,
            @Input("autoFixParam") final String autoFixParam) {

        setTestStep("Get connected and synced Node with parent MO types defined");
        final Fdn nodeFdn = nodeCliOperator.getSyncedNode(csHandler, nodeType,
                NodeCliOperator.buildNodeFiltering(sourceNodeAdditionalFiltering, numCells, proxyParentType, relationParentType));
        assertNotNull("Provided node type not found in the synched node", nodeFdn);
        setTestInfo("Synched node Mo found is: " + nodeFdn);
        final List<Fdn> childFdns = nodeCliOperator.getChildrenFromSyncedNodeFiltering(nodeFdn, proxyParentType, relationParentType);
        setTestInfo("Searching for %s and %s on synched %s node ", proxyParentType, relationParentType, nodeType);
        assertThat("Provided MO types were not found on the same synced node", childFdns, hasSize(2));

        final Fdn proxyParent = childFdns.get(0);
        final Fdn relationParent = childFdns.get(1);
        setTestInfo("Proxy parent MO found is: " + proxyParent);
        setTestInfo("Relation parent MO found is: " + relationParent);

        setTestStep("Read the value of the AutoFix parameter from SNAD MC and store it");
        final String autoFixOrigVal = smtool.getConfigurationForMC(SNAD_MC, autoFixParam);
        setTestInfo("AutoFix parameter before test " + autoFixOrigVal);

        setTestStep("Set the value of SNAD AutoFix parameter to include Missing_Master");
        final boolean autoFixOn = smtool.setConfigurationForMC(SNAD_MC, autoFixParam, MISSINGMASTER_AUTOFIX_ON);
        assertTrue("Failed to set AutoFix parameter", autoFixOn);

        setTestStep("Create proxy MO on node, with provided attribute values");
        final Fdn proxyMoFdn = createMoCliOperator.buildMoFdn(proxyParent, proxyMoType, getTestId());
        boolean moCreatedInNetSim = createMoCliOperator.createMo(proxyMoFdn, proxyAttrNames, proxyAttrValues);
        assertTrue("Failed to create MO in NETSIM ", moCreatedInNetSim);
        sleep(180);
        boolean moExistInCS = csHandler.moExists(proxyMoFdn);
        assertTrue("Failed to create MO in database", moExistInCS);
        boolean attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(proxyMoFdn, proxyAttrNames, proxyAttrValues);
        assertTrue("Created MO attributes in database does not match with input data", attributesSetInDatabase);
        setTestInfo("performing check on the proxy mo ");
        snadOperator.performCheckonFDN(proxyMoFdn);
        assertThat("Proxy MO state is not as expected", snadOperator.getProxyState(proxyMoFdn), is(equalTo(REDUNDANT_PROXY)));
        setTestInfo("Proxy MO State after create : " + snadOperator.getProxyState(proxyMoFdn));

        setTestStep("Create Relation MO on node pointing to new Proxy");
        relationAttrVals = ArrayUtils.add(relationAttrVals, proxyMoFdn.getLdn());
        final Fdn relationMoFdn = createMoCliOperator.buildMoFdn(relationParent, relationMoType, getTestId());
        moCreatedInNetSim = createMoCliOperator.createMo(relationMoFdn, relationAttrNames, relationAttrVals);
        assertTrue("Failed to create MO in NETSIM", moCreatedInNetSim);
        sleep(180);
        moExistInCS = csHandler.moExists(relationMoFdn);
        assertTrue("Failed to create MO in CS database", moExistInCS);
        attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(relationMoFdn, relationAttrNames, relationAttrVals);
        assertTrue("Created MO attributes in database does not match with i nput data", attributesSetInDatabase);

        setTestStep("Check proxy MO is updated");
        final String proxyReservedBy = csHandler.getAttributeValue(proxyMoFdn, RESERVED_BY);
        assertThat("ReservedBy attribute not set ", proxyReservedBy, containsString(relationMoFdn.getFdn()));
        setTestInfo("Proxy ReservedBy attribute contains: " + relationMoFdn.getFdn());
        final Fdn masterMo = snadOperator.getMasterForProxy(proxyMoFdn);
        assertNotNull("Get master for proxy does not contain master fdn", masterMo);
        setTestInfo("Master for proxy is: " + masterMo);
        setTestInfo("performing check on the proxy mo ");
        snadOperator.performCheckonFDN(proxyMoFdn);
        sleep(180);
        final String proxyStateAfterRelationCreate = snadOperator.getProxyState(proxyMoFdn);
        assertThat("Proxy MO state is not as expected after relation create", proxyStateAfterRelationCreate, is(equalTo(CONSISTENT)));
        setTestInfo("Proxy state after relation create is :" + proxyStateAfterRelationCreate);

        setTestStep("Check Master MO for Proxy is created");
        moExistInCS = csHandler.moExists(masterMo);
        assertTrue("Master MO is not in database", moExistInCS);
        attributesSetInDatabase = setMoCliOperator.isAttributesSetInDatabase(masterMo, masterAttrNames, proxyAttrValues);
        // assertTrue("Master MO attributes in database does not match with proxy attribute values", attributesSetInDatabase);
        setTestInfo("performing check on the master mo ");
        snadOperator.performCheckonFDN(masterMo);
        assertThat("Master MO state is not as expected", snadOperator.getMasterState(masterMo), is(equalTo(CONSISTENT)));
        setTestInfo("Master MO State: %s", snadOperator.getMasterState(masterMo));

        setTestStep("Check relation master reference attribute contains new Master reference");
        final String adjacentRef = csHandler.getAttributeValue(relationMoFdn, relationMasterRef);
        assertThat("Master reference attribute not set in relation ", adjacentRef, is(equalTo(masterMo.getFdn())));
        setTestInfo("Relation adjacent reference attribute (%s) is set to: %s", relationMasterRef, adjacentRef);

        setTestStep("Clean up: Delete created MOs and reset AutoFix to original value");
        deleteMoCliOperator.deleteMo(relationMoFdn);
        sleep(180);
        deleteMoCliOperator.deleteMo(proxyMoFdn);
        csHandler.deleteMo(masterMo);
        smtool.setConfigurationForMC(SNAD_MC, autoFixParam, autoFixOrigVal);

    }
}
