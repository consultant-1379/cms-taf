/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTLA;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MIB_ADAPTER;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmanvas
 */
public class RdsNodeCliOperator implements RdsNodeOperator {

    private final static Logger logger = Logger.getLogger(RdsNodeCliOperator.class);

    private final static String SPACE = " ";

    private final static String PRODVALUE = "2";

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public boolean addRDSNodeUsingArne(
            final String fdn,
            final String xmlCreateFileName,
            final String xmlDeleteFileName,
            final String fdnProd,
            final String prodDesignation,
            final String nodeType) {

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
        final boolean rdsRbsAdapter = csHandler.isAttached(fdnToCheck, NEAD_MIB_ADAPTER);

        logger.debug("Check MIB Adapter attached" + rdsRbsAdapter);
        return rdsRbsAdapter;
    }

    @Override
    public boolean checkProdVersion(final String fdnProd, final String prodDesig) {
        final String command = CSTESTLA + SPACE + fdnProd + SPACE + prodDesig;
        final String csOutput = initialSyncCliOperator.hostConnect(command);
        logger.debug("cs output " + csOutput.toString());
        return csOutput.contains(PRODVALUE) ? true : false;

    }
}