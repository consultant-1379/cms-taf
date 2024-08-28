/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014-5 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.DeleteMoCliOperator;
import com.ericsson.oss.cms.test.operators.NetsimKertayleCliOperator;
import com.ericsson.oss.cms.test.operators.NodeCliOperator;
import com.ericsson.oss.cms.test.operators.X2EventCliOperator;
import com.ericsson.oss.cms.test.util.AttributeValueConverter;
import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author ehenger
 */
public class X2EventCellRelationMO extends TorTestCaseHelper implements TestCase {

    @Inject
    private NetsimKertayleCliOperator netsimKertayleOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    private DeleteMoCliOperator deleteMoOperator;

    @Inject
    private X2EventCliOperator x2EventOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private static final String CREATEDBYATTRNAME = "createdBy";

    private static final String CREATEDBYX2VALUE = "1";

    private static final String EVENT_X2_NEIGHBREL_ADD = "EVENT_X2_NEIGHBREL_ADD";

    /**
     * @throws IOException
     * @DESCRIPTION Verify that when EUtranCellRelation is added by X2 (createdBy set to X2),
     *              CMS logs it as a X2 event
     * @PRE CS's, NEAD and SNAD ONLINE.
     *      Synchronized Node with a cell relation.
     * @PRIORITY MEDIUM
     */
    @TestId(id = "OSS-53409_X2event", title = "Add X2 Event for the Creating of LTE EUtranCell Relation MO")
    @Context(context = { Context.CLI })
    @DataDriven(name = "x2event")
    @Test(groups = { "KGB" })
    public void addX2EventEUtranCellRelation(
            @Input("nodeType") final String nodeType,
            @Input("cellRelationType") final String cellRelationType,
            @Input("cellRelAttrName") final String cellRelAttributeName,
            @Input("cellRelAttrDataType") final String cellRelAttributeDataType) throws IOException {

        setTestStep("Find a cell relation of cellRelationType on a synched node of nodeType");
        setTestInfo("Cell Relation type: " + cellRelationType + "  Node Type: " + nodeType);
        final Fdn activeNode = nodeCliOperator.getMimScopedSynchedNode(nodeType);
        assertNotNull("No Synced node found", activeNode);
        final Fdn cellRelationMoFdn = x2EventOperator.getInterNodeCellRelation(activeNode, cellRelationType, cellRelAttributeName);
        assertNotNull("No MO of type " + cellRelationType + " found on a " + nodeType + " Node ", cellRelationMoFdn);

        setTestStep("Store the mandatory attribute values of the cell relation");
        setTestInfo("Store values of Attributes: " + cellRelAttributeName + " of MO: " + cellRelationMoFdn);
        final List<Attribute> cellRelAttributes = csHandler.getAttributes(cellRelationMoFdn, cellRelAttributeName);
        setTestInfo("Stored values:" + cellRelAttributes);

        final List<String> netsimAttrsKertayleFormat = new ArrayList<String>();
        netsimAttrsKertayleFormat.add(cellRelAttributes.get(0).getName() + " " + cellRelAttributeDataType + " \""
                + AttributeValueConverter.convertDBToNetsimValue(cellRelAttributes.get(0)) + "\"");
        netsimAttrsKertayleFormat.add(CREATEDBYATTRNAME + " Integer " + CREATEDBYX2VALUE);

        setTestStep("Delete the cell Relation");
        final boolean isMoDeletedOnNode = deleteMoOperator.deleteMo(cellRelationMoFdn);
        assertThat("Mo is not deleted on Node", isMoDeletedOnNode, equalTo(true));
        final boolean isMoExistInDb = csHandler.moExists(cellRelationMoFdn);
        assertThat("MO is not deleted on OSS", isMoExistInDb, equalTo(false));

        setTestStep("Store the current time as startTime");
        final long cifLogStartTime = System.currentTimeMillis();
        setTestInfo("startTime stored:" + (cifLogStartTime > 0));

        setTestStep("Create a relation of RelationType using stored values and createdBy=1 (X2)");
        final boolean isCreated = netsimKertayleOperator.createMo(cellRelationMoFdn, netsimAttrsKertayleFormat);
        assertThat("Failed to create MO in Netsim: " + cellRelationMoFdn.toString(), isCreated);
        assertThat("Failed to create MO in OSS", csHandler.moExists(cellRelationMoFdn));

        setTestStep("Find CIF LOG Entry with Command: EVENT_X2_NEIGHBREL_ADD since startTime");
        final TimeRange timeRange = new TimeRange();
        timeRange.setStartTime(cifLogStartTime);
        timeRange.setTimeout(MAX_TIME_TO_READ_CIF_LOGS);
        final boolean x2EventLogged = x2EventOperator.waitForX2EventCommand(SNAD_MC, timeRange, EVENT_X2_NEIGHBREL_ADD, cellRelationMoFdn.getFdn());
        assertTrue("No X2 Event found in CIF logs", x2EventLogged);
    }

}