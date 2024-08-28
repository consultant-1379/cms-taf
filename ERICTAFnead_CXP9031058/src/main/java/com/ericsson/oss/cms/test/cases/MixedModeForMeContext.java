/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2017 Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.AUX_PLUGIN_UNIT_FOR_MIXEDMODE_EPIC_PED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.*;

import javax.inject.Inject;

import org.testng.annotations.*;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.oss.cms.test.operators.*;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.smhandler.SMHandler;
import com.ericsson.oss.taf.smhandler.SmtoolHandler;

/**
 * @author xaggpar
 */
@SuppressWarnings("deprecation")
public class MixedModeForMeContext extends TorTestCaseHelper implements TestCase {

    private final SMHandler smtool = new SmtoolHandler(HostGroup.getOssmaster());

    @Inject
    private OperatorRegistry<NodeOperator> nodeOperatorRegistry;

    @Inject
    private CreateMoCliOperator createMoCliOperator;

    @Inject
    private MixedModeEpicCliOperator mixedModeEpicCliOpertor;

    @Inject
    private DeleteMoCliOperator deleteMoCliOperator;

    private String pedParameterValue = "false";

    @BeforeTest
    void beforeTest() {
        pedParameterValue = smtool.getConfigurationForMC(NEAD_MC, AUX_PLUGIN_UNIT_FOR_MIXEDMODE_EPIC_PED);
        if (pedParameterValue != null && pedParameterValue.equalsIgnoreCase("false")) {
            setTestStep("Set auxpluginUnitForMixedMode Ped paramter value to true for NEAD MC");
            final boolean commandResult = smtool.setConfigurationForMC(NEAD_MC, AUX_PLUGIN_UNIT_FOR_MIXEDMODE_EPIC_PED, "true");
            assertTrue("failed to set value of parameter", commandResult);
            pedParameterValue = "true";
        }
    }

    /**
     * @DESCRIPTION Test case to cover create and delete use cases for MixedMode feature for ERBS and RBS node.
     * @PRE None
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-169469", title = "Automating - NEAD 50659 Mixed Mode Baseband R503 - Support for L17B G1 (Handling for MeContext)")
    @Context(context = { Context.CLI })
    @DataDriven(name = "MixedModeEpicForMeContext")
    @Test(groups = { "NEAD, KGB" })
    public void MixedModeEpicForMeContext(
            @Input("nodeType") final String nodeType,
            @Input("moType") final String[] moType,
            @Input("auxPlugin_attributes") final String[] auxPlugin_attributes,
            @Input("auxPlugin_AttributeValues") final String[] auxPlugin_AttributeValues,
            @Input("sector_attributes") final String[] sector_attributes,
            @Input("sector_AttributeValues") final String[] sector_AttributeValues,
            @Input("baseNodeVersion") final String baseNodeVersion) {

        final NodeOperator nodeOperator = nodeOperatorRegistry.provide(NodeOperator.class);
        Map<Fdn, String> finalrevMap = new HashMap<Fdn, String>();

        setTestStep("Step Get all connected and synced Nodes with parent MO types defined");
        final List<Fdn> parentMoFdn = nodeOperator.getPercentageOfSyncedNodes(nodeType, 100, null);
        assertThat("No mo child has been found for node type " + nodeType, parentMoFdn, notNullValue());

        setTestStep("Step Find two nodes, one which is supporting AuxPlugInUnit::isSharedWithExternalMe attribute and other which is supporting SectorEquipmentFunction::mixedModeRadio / Sector::mixedModeRadio.");
        final Map<Fdn, Boolean> fdnMap = mixedModeEpicCliOpertor.getMixedModeFdns(parentMoFdn, nodeType, baseNodeVersion);
	assertThat(
                "Failed to Find two nodes, one which is supporting AuxPlugInUnit::isSharedWithExternalMe attribute and other which is supporting SectorEquipmentFunction::mixedModeRadio / Sector::mixedModeRadio. ",
                fdnMap.entrySet(), hasSize(2));

        for (final Map.Entry<Fdn, Boolean> map : fdnMap.entrySet()) {

            if (map.getValue()) {
                setTestStep("Step  Take one node which supports AuxPlugInUnit::isSharedWithExternalMe attribute.");

                final List<Fdn> listFdn = nodeOperator.getChildrenFromSyncedNodeFiltering(map.getKey(), moType[0]);

                setTestStep("Step Update isSharedWithExternalMe attribute of other AuxPlugInUnit MOs to false, if any (MeContext::mixedModeRadio should be false before the creation of AuxPlugInUnit MO).");
                final Map<Fdn, String> revMap = mixedModeEpicCliOpertor.updateAttributeValue(listFdn, auxPlugin_attributes[0], "false");
                if (!revMap.isEmpty()) {
                    finalrevMap = revMap;
                }

                setTestStep("Step Verify the mixedModeRadio attribute value of MeContext to be false before creating AuxPluginUnit MO.");
                final boolean initialMixedModeValue = mixedModeEpicCliOpertor.getMixedModeAttributeValue(map.getKey());
                assertFalse("Failed to update mixedModeRadio attribute value of MeContext to false before creating AuxPluginUnit Mo: " + initialMixedModeValue,
                        initialMixedModeValue);

                final List<Fdn> _parentMo = nodeOperator.getChildrenFromSyncedNodeFiltering(map.getKey(), "Equipment");

                setTestStep("Step Build AuxPlugInUnit MO Fdn.");
                final Fdn childMoFdn = createMoCliOperator.buildMoFdn(_parentMo.get(0), moType[0], getTestId());
                setTestInfo("Child Mo Build : " + childMoFdn.getFdn());

                setTestStep("Step Create AuxPlugInUnit MO with attribute isSharedWithExternalMe set to true.");
                final boolean cmdResult = createMoCliOperator.createMo(childMoFdn, auxPlugin_attributes, auxPlugin_AttributeValues);
                assertTrue("Failed to create AuxPlugInUnit Mo.", cmdResult);
                sleep(30);

                setTestStep("Step Verify the mixedModeRadio attribute value of MeContext has updated to true.");
                final boolean mixedModeValue = mixedModeEpicCliOpertor.getMixedModeAttributeValue(childMoFdn.getMeContext());
                assertTrue("Failed to update mixedModeRadio attribute value of MeContext to true in 30 sec. after creating AuxPluginUnit MO : " + mixedModeValue,
                        mixedModeValue);
                // Deletion Part...
                setTestStep("Step Delete created AuxPlugInUnit MO.");
                final boolean delcmdResult = deleteMoCliOperator.deleteMo(childMoFdn);
                assertTrue("Failed to delete AuxPluginUnit MO", delcmdResult);

                sleep(30);

                setTestStep("Step Verify the mixedModeRadio attribute value of MeContext has updated to false.");
                final boolean mixedModeValue_del = mixedModeEpicCliOpertor.getMixedModeAttributeValue(childMoFdn.getMeContext());
                assertFalse("Failed to update mixedModeRadio attribute value of MeContext to false in 30 sec. after deleting AuxPluginUnit MO : "
                        + mixedModeValue_del, mixedModeValue_del);
            } else {
                setTestStep("Step Select one node which supports SectorEquipmentFunction::mixedModeRadio / Sector::mixedModeRadio attribute and build Sector/ SectorEquipmentFunction MO Fdn.");
                Fdn childMoFdn = null;
                if (nodeType.equalsIgnoreCase("ERBS")) {

                    final List<Fdn> listFdn = nodeOperator.getChildrenFromSyncedNodeFiltering(map.getKey(), moType[1]);

                    setTestStep("Step Update mixedModeRadio attribute of other Sector/ SectorEquipmentFunction MOs to false if any (MeContext::mixedModeRadio should be false before the creation of Sector/ SectorEquipmentFunction MO).");
                    final Map<Fdn, String> revMap1 = mixedModeEpicCliOpertor.updateAttributeValue(listFdn, sector_attributes[0], "false");
                    if (!revMap1.isEmpty()) {
                        finalrevMap.putAll(revMap1);
                    }
                    final List<Fdn> _parentMo = nodeOperator.getChildrenFromSyncedNodeFiltering(map.getKey(), "ManagedElement");

                    setTestStep("Step Build Sector/ SectorEquipmentFunction MO Fdn.");
                    childMoFdn = createMoCliOperator.buildMoFdn(_parentMo.get(0), moType[1], getTestId());
                    setTestInfo("Child Mo Build : " + childMoFdn.getFdn());

                } else if (nodeType.equalsIgnoreCase("RBS")) {

                    final List<Fdn> listFdn = nodeOperator.getChildrenFromSyncedNodeFiltering(map.getKey(), moType[1]);

                    setTestStep("Step  Update mixedModeRadio attribute of other Sector/ SectorEquipmentFunction MOs to false if any (MeContext::mixedModeRadio should be false before the creation of Sector/ SectorEquipmentFunction MO).");
                    final Map<Fdn, String> revMap1 = mixedModeEpicCliOpertor.updateAttributeValue(listFdn, sector_attributes[0], "false");
                    if (!revMap1.isEmpty()) {
                        finalrevMap.putAll(revMap1);
                    }
                    final List<Fdn> _parentMo = nodeOperator.getChildrenFromSyncedNodeFiltering(map.getKey(), "NodeBFunction");

                    setTestStep("Step Build Sector/ SectorEquipmentFunction MO Fdn.");
                    childMoFdn = createMoCliOperator.buildMoFdn(_parentMo.get(0), moType[1], getTestId());
                    setTestInfo("Child Mo Build : " + childMoFdn.getFdn());
                }

                setTestStep("Step Verify the mixedModeRadio attribute value of MeContext to be false before creating Sector/ SectorEquipmentFunction MO.");
                final boolean initialMixedModeValue = mixedModeEpicCliOpertor.getMixedModeAttributeValue(map.getKey());
                assertFalse("Failed to update mixedModeRadio attribute value of MeContext to false before creating Sector/ SectorEquipmentFunction Mo: "
                        + initialMixedModeValue, initialMixedModeValue);

                setTestStep("Step Create Sector/ SectorEquipmentFunction MO with attribute mixedModeRadio set to true.");
                final boolean cmdResult = createMoCliOperator.createMo(childMoFdn, sector_attributes, sector_AttributeValues);
                assertTrue("Failed to create Sector/ SectorEquipmentFunction Mo.", cmdResult);
                sleep(30);
                setTestStep("Step Verify the mixedModeRadio attribute value of MeContext has updated to true.");
                final boolean mixedModeValue = mixedModeEpicCliOpertor.getMixedModeAttributeValue(childMoFdn.getMeContext());
                assertTrue("Failed to update mixedModeRadio attribute value of MeContext to true in 30 sec. after creating Sector/ SectorEquipmentFunction MO : "
                        + mixedModeValue, mixedModeValue);
                // Deletion Part...
                setTestStep("Step Delete created Sector/ SectorEquipmentFunction MO.");
                final boolean delcmdResult = deleteMoCliOperator.deleteMo(childMoFdn);
                assertTrue("Failed to delete Sector/ SectorEquipmentFunction MO", delcmdResult);
                sleep(30);
                setTestStep("Step Verify the mixedModeRadio attribute value of MeContext has updated to false.");
                final boolean mixedModeValue_del = mixedModeEpicCliOpertor.getMixedModeAttributeValue(childMoFdn.getMeContext());
                assertFalse("Failed to update mixedModeRadio attribute value of MeContext to false in 30 sec. after deleting Sector/ SectorEquipmentFunction MO : "
                        + mixedModeValue_del, mixedModeValue_del);

            }
        }

	setTestStep("Step Clean up: Set the original values of the modified attributes of AuxPlugInUnit/Sector/SectorEquipmentFunction MOs in steps 4 or 9, if any.");
                if (!finalrevMap.isEmpty()) {
                    mixedModeEpicCliOpertor.updateAttributeValueFdn(finalrevMap);
                }

    }

    @AfterTest
    void afterTest() {
        if (pedParameterValue.equalsIgnoreCase("true")) {
            setTestStep("Set auxpluginUnitForMixedMode Ped paramter value to false for NEAD MC");
            final boolean commandResult = smtool.setConfigurationForMC(NEAD_MC, AUX_PLUGIN_UNIT_FOR_MIXEDMODE_EPIC_PED, "false");
            assertTrue("failed to set value of ped parameter", commandResult);
        }
    }
}

