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
public class CppVersionCliOperator implements CppVersionOperator {

    private final static Logger logger = Logger.getLogger(CppVersionCliOperator.class);

    private final static String SPACE = " ";

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public boolean addCppVersionNodeUsingArne(final String fdn, final String xmlCreateFileName, final String xmlDeleteFileName, final String nodeType) {

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
        final boolean cppNodeAdapter = csHandler.isAttached(fdnToCheck, NEAD_MIB_ADAPTER);

        logger.debug("Check MIB Adapter attached" + cppNodeAdapter);
        return cppNodeAdapter;
    }

    @Override
    public boolean checkCppVersion(final String fdn, final String cppVersion, final String cppVersionValue) {
        final String command = CSTESTLA + SPACE + fdn + SPACE + cppVersion;
        logger.debug("error :" + command);
        final String csOutput = initialSyncCliOperator.hostConnect(command);
        logger.debug("cs output " + csOutput.toString());
        logger.info("value :" + csOutput.contains(cppVersionValue));
        return csOutput.contains(cppVersionValue) ? true : false;

    }

}