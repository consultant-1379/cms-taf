/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MIB_ADAPTER;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.CSAttribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmagkum
 */
public class ExtraStructCliOperator implements ExtraOperator {

    private final static Logger logger = Logger.getLogger(ExtraStructCliOperator.class);

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final CSLibTestHandler csLibHandler = new CSLibTestHandler(HostGroup.getOssmaster());

    @Override
    public boolean addNodeUsingArne(final String fdn, final String xmlCreateFileName, final String xmlDeleteFileName, final String nodeType) {

        final Fdn fdnToCheck = new Fdn(fdn);

        final boolean nodeExist = csHandler.moExists(fdnToCheck);

        if (nodeExist) {
            logger.debug("Copying the Delete XML file to the remote server");
            initialSyncCliOperator.copyXMLFileToRemote(xmlDeleteFileName);

            logger.debug("Delete the node operation is started");
            initialSyncCliOperator.deleteNode(xmlDeleteFileName, nodeType);
        }

        logger.debug("Copying the Create XML file to the remote server");
        initialSyncCliOperator.copyXMLFileToRemote(xmlCreateFileName);

        logger.debug("Add the node operation is started");
        initialSyncCliOperator.addNode(xmlCreateFileName, nodeType);
        logger.debug("Node Added" + fdn);

        logger.debug("Check MIB Adapter attached ");
        final boolean NodeAdapter = csHandler.isAttached(fdnToCheck, NEAD_MIB_ADAPTER);

        logger.debug("Check MIB Adapter attached" + NodeAdapter);
        return NodeAdapter;
    }

    @Override
    public boolean addUtranCell(final Fdn moFdn, final String[] attributes, final String[] attributeValues) {
        final List<Attribute> utranCellsAtts = new ArrayList<Attribute>();
        final Fdn parentMoFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC21,MeContext=RNC21,ManagedElement=1,RncFunction=1");
        for (int i = 0; i < attributes.length - 1; i++) {
            utranCellsAtts.add(new CSAttribute(attributes[i], attributeValues[i]));
        }
        attributeValues[attributeValues.length - 1] = findIubLink(parentMoFdn, csHandler);
        utranCellsAtts.add(new CSAttribute(attributes[attributes.length - 1], attributeValues[attributes.length - 1]));
        final boolean flag = csLibHandler.createMo(moFdn, utranCellsAtts);
        return flag;
    }

    private String findIubLink(final Fdn rncFunction, final CSHandler cs) {
        final Filter moFilter = SimpleFilterBuilder.builder().type("IubLink").build();
        final List<Fdn> childMos = cs.getChildMos(rncFunction, moFilter);
        if (childMos.isEmpty()) {
            throw new RuntimeException("Unable to find IubLink under rncFunction - Create IubLink should be implemented");
        }
        return childMos.get(0).getFdn();
    }

}