/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CIFLogType;
import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.cms.test.operators.AttributeCliOperator;
import com.ericsson.oss.cms.test.operators.BCGImportOperator;
import com.ericsson.oss.cms.test.operators.CreateMoCliOperator;
import com.ericsson.oss.cms.test.operators.InitialSyncCliOperator;
import com.ericsson.oss.cms.test.operators.NeadRetryActivationCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.NodeExceptionCliOperator;
import com.ericsson.oss.cms.test.operators.NodeExceptionOperator;
import com.ericsson.oss.cms.test.operators.SyncNodesCliOperator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.ddchandler.DDCPlannedAreaCliHandler;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author eeimgrr
 */

public class NeadRetryActivation extends TorTestCaseHelper implements TestCase {

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    final static String MOID = "OSS_53413_test";

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private NeadRetryActivationCliOperator neadRetryOperator;

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private AttributeCliOperator csCliOperator;

    @Inject
    private OperatorRegistry<BCGImportOperator> bcgImportOperatorRegistery;

    @Inject
    private SyncNodesCliOperator syncNodesCliOperator;

    private Fdn eUtranCellRelation;

    private Fdn eUtranCellFDD;

    private List<Fdn> externalEUtranCellFDDMos = new ArrayList<Fdn>();

    private final DDCPlannedAreaCliHandler ddcHandler = new DDCPlannedAreaCliHandler(HostGroup.getOssmaster());

    private final NodeExceptionOperator nodeExceptionCliOperator = new NodeExceptionCliOperator();

    private NetworkElement networkElement = null;

    private boolean exceptionCreatedInNetsim = false;

    private boolean exceptionActivatedInNetsim = false;

    private final CSTestHandler csPlanHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment, MOID);

    private final static String TAFIDLEXCEPTION = "tafIDLexception";

    @TestId(id = "OSS_53413", title = "NEAD retries a failed activation")
    @Context(context = { Context.CLI })
    @DataDriven(name = "neadRetryActivation")
    @Test(groups = { "NEAD, KGB" })
    public void handleNeadRetryActivation(@Input("attributes") final String[] attributes, @Input("attributeValues") final String[] attributeValues) {

        setTestStep("Find a SectorCarrier MO on a synchronized ERBS node");
        final Fdn sectorCarrier = nodeCliOperator.getChildMoFromSyncedNode(NeType.ERBS.toString(), "SectorCarrier");
        assertNotNull("Mandatory referenced MO of type SectorCarrier not found on available nodes.", sectorCarrier);

        setTestStep("Find the parent MO for the EUtranCellFDD to be created");
        final Fdn eNodeBFunction = sectorCarrier.getParentFdn();

        setTestInfo("Build the EUtranCellFDD FDN with parent MO Fdn and MO type.");
        eUtranCellFDD = createMoCliOperator.buildMoFdn(eNodeBFunction, "EUtranCellFDD", getTestId());

        setTestStep("Find the parent MO EUtranFreqRelation for the EUtranCellRelation to be created on a second syncnhronized ERBS node.");
        final Fdn eUtranFreqRelation = nodeCliOperator.getMoFromDifferentNode(NeType.ERBS.toString(), "EUtranFreqRelation", eNodeBFunction);
        assertNotNull("Provided parent MO type EUtranFreqRelation not found on chosen node.", eUtranFreqRelation);

        setTestInfo("Build the EUtranCellRelation FDN with parent MO Fdn and MO type.");
        eUtranCellRelation = createMoCliOperator.buildMoFdn(eUtranFreqRelation, "EUtranCellRelation", getTestId());

        setTestStep("Create a planned area");
        final boolean planCreated = ddcHandler.createPlannedArea(MOID, MOID);
        assertTrue("Planned area was not created", planCreated);

        setTestInfo("Build the mandatory attributes for the creation of a EUtranCellFDD");
        List<Attribute> mandatoryAttributes = csCliOperator.buildAttributesList(attributes, attributeValues);
        csCliOperator.updateAttributeList(mandatoryAttributes, "sectorCarrierRef", sectorCarrier.getFdn());

        setTestStep("Checking Cardinality and Deleting the Required MO");
        final boolean deleteFlag = initialSyncCliOperator.findAndDeleteMOsFromCS(eUtranCellFDD);
        assertTrue("Deleting EUtranCellFDD MO is Failed ", deleteFlag);
        setTestStep("Create the EUtranCellFDD in the planned area");
        csPlanHandler.createMo(eUtranCellFDD, mandatoryAttributes);
        final boolean moExistInCS = csPlanHandler.moExists(eUtranCellFDD);
        assertTrue("EUtranCellFDD MO does not exist in the planned area", moExistInCS);

        setTestInfo("Build the mandatory attributes for EUtranCellRelation");
        mandatoryAttributes = csCliOperator.buildAttributesList(new String[] { "adjacentCell" }, new String[] { eUtranCellFDD.getFdn() });

        setTestStep("Create the EUtranCellRelation setting adjacentCell attribute to the newly created EUtranCellFDD.");
        csPlanHandler.createMo(eUtranCellRelation, mandatoryAttributes);
        final boolean eUtranRelationExists = csPlanHandler.moExists(eUtranCellRelation);
        assertTrue("EUtranCellRelation MO does not exist in the planned area", eUtranRelationExists);
        final String requiredProxiesBeforeActivation = csPlanHandler.getAttributeValue(eUtranCellRelation, "requiredProxies");
        assertThat("EUtranCellRelation::requiredProxies is undefined", requiredProxiesBeforeActivation, not(containsString("UndefinedValue")));

        setTestStep("Create a netsim exception on ‘basic_mo_create’ for ‘next time’ on the second ERBS");
        networkElement = nodeCliOperator.getNetworkElement(eUtranCellRelation.getNameOfFdnPart("MeContext"));
        exceptionCreatedInNetsim = nodeExceptionCliOperator.createIDLException(networkElement, TAFIDLEXCEPTION, "configuration:basic_create_MO", "next_time");
        assertTrue("Failed to Create exception in NETSIM", exceptionCreatedInNetsim);

        setTestStep("Activate exception on the second ERBS.");
        exceptionActivatedInNetsim = nodeExceptionCliOperator.activateException(networkElement, TAFIDLEXCEPTION);
        assertTrue("Failed to Activate exception in NETSIM", exceptionActivatedInNetsim);

        setTestStep("Record the current time and store as start time");
        final long startActivationTime = System.currentTimeMillis();

        setTestStep("Activate the plan");
        final BCGImportOperator bcgImportOperator = bcgImportOperatorRegistery.provide(BCGImportOperator.class);
        bcgImportOperator.activatePlan(MOID);

        final boolean eUtranCellFDDInValid = csHandler.moExists(eUtranCellFDD);
        assertTrue("EUtranCellFDD MO does not exist in the valid area", eUtranCellFDDInValid);

        final boolean eUtranCellRelationInValid = csHandler.moExists(eUtranCellRelation);
        assertTrue("EUtranCellRelation MO does not exist in the valid area", eUtranCellRelationInValid);

        final String requiredProxiesAfterActivation = csHandler.getAttributeValue(eUtranCellRelation, "requiredProxies");
        assertThat("requiredProxies is not undefined", requiredProxiesAfterActivation, containsString("UndefinedValue"));

        externalEUtranCellFDDMos = neadRetryOperator.getGeneratedProxyMos("ExternalEUtranCellFDD", "ExternalEUtranCellFDDId", getTestId());
        assertThat("ExternalEUtranCellFDD proxy not found in CS", externalEUtranCellFDDMos, hasSize(1));
        assertThat("ExternalEUtranCellFDD not on expected ERBS", externalEUtranCellFDDMos.get(0).getMeContext().getFdn(),
                containsString(eUtranCellRelation.getMeContext().getFdn()));

        final List<Fdn> listOfActiveNodes = nodeCliOperator.getMeContexts(eUtranCellFDD, eUtranCellRelation);
        List<CIFLogItem> cifLogEntriesForPlan = neadRetryOperator.getNeadCifLogsPlanActivation(listOfActiveNodes, startActivationTime, MOID,
                CIFLogType.COMMAND_LOG);
        // assertThat("The cifLogEntries size should be 4.", cifLogEntriesForPlan, hasSize(4));

        setTestInfo("cifLogEntriesForPlan size is: " + cifLogEntriesForPlan.size());

        cifLogEntriesForPlan = neadRetryOperator.getNeadCifLogsPlanActivation(listOfActiveNodes, startActivationTime, MOID, CIFLogType.SYSTEM_EVENT_LOG);
        // assertThat("The cifLogEntries size should be 2.", cifLogEntriesForPlan, hasSize(2));

        setTestInfo("cifLogEntriesForPlan size is: " + cifLogEntriesForPlan.size());

        setTestStep("Check the CIF logs for resynch message related to both NE");
        final List<CIFLogItem> cifLogEntriesSync = syncNodesCliOperator.getSyncStatusCifLogEntries(listOfActiveNodes, startActivationTime);
        assertThat("CIF log entries should not contain node sync messages during test run.", cifLogEntriesSync, hasSize(0));

    }

    @AfterTest(alwaysRun = true)
    public void afterTest() {

        ddcHandler.deletePlannedArea(MOID);
        if (eUtranCellRelation != null) {
            csHandler.deleteMo(eUtranCellRelation);
        }
        if (eUtranCellFDD != null) {
            csHandler.deleteMo(eUtranCellFDD);
        }
        if (!externalEUtranCellFDDMos.isEmpty()) {
            csHandler.deleteMo(externalEUtranCellFDDMos.get(0));
        }
        if (exceptionActivatedInNetsim) {
            nodeExceptionCliOperator.deactivateException(networkElement, TAFIDLEXCEPTION);
        }
        if (exceptionCreatedInNetsim) {
            nodeExceptionCliOperator.deleteException(networkElement, TAFIDLEXCEPTION);
        }

    }

}
