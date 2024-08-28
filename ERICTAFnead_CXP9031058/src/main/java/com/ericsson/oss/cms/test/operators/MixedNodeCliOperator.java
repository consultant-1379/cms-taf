/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTLA;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTLT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTMI;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmanvas
 */
public class MixedNodeCliOperator implements MixedNodeOperator {

    CLICommandHelper cliCommandHelper;

    final static Logger LOGGER = Logger.getLogger(MixedNodeCliOperator.class);

    private final static String SPACE = " ";

    private final static String XML_REMOTE_PATH = "/var/opt/ericsson/arne/";

    private final static String IMPORT_SCRIPT_PATH = "/opt/ericsson/arne/bin/import.sh";

    private void addNode(final String xmlFileName, final String nodeType) {
        final String command = IMPORT_SCRIPT_PATH + " -f " + XML_REMOTE_PATH + xmlFileName + " -import -i_nau";
        final Host host = HostGroup.getOssmaster();
        final User operUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        cliCommandHelper = new CLICommandHelper(host, operUser);
        cliCommandHelper.simpleExec(command);

        final String ltCommand = CSTESTLT + SPACE + "MeContext | grep" + SPACE + nodeType;
        final String outputLt = cliCommandHelper.simpleExec(ltCommand);

        LOGGER.debug("Check the node is added by lt command");
        LOGGER.debug(outputLt);
    }

    private boolean isNodeAttached(final String fdn) {
        LOGGER.info("Check node is attched");
        final String csTestCommand = CSTESTMI + SPACE + fdn;
        final Host host = HostGroup.getOssmaster();
        final User operUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        cliCommandHelper = new CLICommandHelper(host, operUser);
        final String csOutput = cliCommandHelper.simpleExec(csTestCommand);
        LOGGER.debug(csOutput);
        if (!csOutput.contains("Seg_masterservice_NEAD")) {
            LOGGER.info("MIB Adapter is not Attached");
            return false;
        }

        LOGGER.info("MIB Adapter is Attached");
        return true;
    }

    @Override
    public void deleteNode(final String xmlFileName, final String nodeType) {
        final String command = IMPORT_SCRIPT_PATH + " -f " + XML_REMOTE_PATH + xmlFileName + " -import -i_nau" + " > TAF_Log.log";
        final Host host = HostGroup.getOssmaster();
        final User operUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        cliCommandHelper = new CLICommandHelper(host, operUser);

        LOGGER.debug("Import command:" + command);
        final String outputImport = cliCommandHelper.simpleExec(command);

        LOGGER.debug("outputImport: " + outputImport);
        final String ltComm = CSTESTLT + SPACE + "MeContext | grep" + SPACE + nodeType;
        final String outputLt = cliCommandHelper.simpleExec(ltComm);

        LOGGER.info("Check the node is deleted by executing lt command");
        LOGGER.debug("lt output:" + outputLt);

    }

    @Override
    public void copyXMLFile(final String xmlFileName) {
        LOGGER.info("Copy XML files to server");
        final Host host = HostGroup.getOssmaster();
        final User rootUser = new User(host.getUser(UserType.ADMIN), host.getPass(UserType.ADMIN), UserType.ADMIN);
        final RemoteObjectHandler remoteFileHandler = new RemoteObjectHandler(host, rootUser);
        final String xmlSource = FileFinder.findFile(xmlFileName).get(0);
        final String localFileLocation = xmlSource;
        final String remoteFileLocation = XML_REMOTE_PATH;
        LOGGER.info(remoteFileLocation);
        remoteFileHandler.copyLocalFileToRemote(localFileLocation, remoteFileLocation);

    }

    private boolean verfiyNode(final String nodeType, final String fdn) {
        final Host host = HostGroup.getOssmaster();
        final User operUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        cliCommandHelper = new CLICommandHelper(host, operUser);
        final String ltCommand = CSTESTLT + SPACE + "MeContext | grep" + SPACE + nodeType;
        final String outputLt = cliCommandHelper.simpleExec(ltCommand);

        LOGGER.debug("Check the node is already added by executing lt command");
        LOGGER.debug(outputLt);

        if (!outputLt.contains(fdn)) {
            LOGGER.info("Node already added");
            LOGGER.info("Deleting the node");
            return false;
        }

        return true;
    }

    @Override
    public boolean addMixedNodeUsingArne(
            final String fdn,
            final String xmlCreateFileName,
            final String xmlDeleteFileName,
            final String fdnProd,
            final String prodDesignation,
            final String nodeType) {

        final boolean nodeExist = verfiyNode(nodeType, fdn);

        if (nodeExist) {
            LOGGER.info("Copying the Delete XML file to the remote server");
            copyXMLFile(xmlDeleteFileName);

            LOGGER.info("Delete the node operation is started");
            deleteNode(xmlDeleteFileName, nodeType);
        }

        LOGGER.info("Copying the Create XML file to the remote server");
        copyXMLFile(xmlCreateFileName);

        LOGGER.info("Add the node operation is started");
        addNode(xmlCreateFileName, nodeType);
        LOGGER.info("Node Added" + fdn);

        LOGGER.info("Check MIB Adapter attached");
        final boolean mixedRBSAdapter = isNodeAttached(fdn);

        return mixedRBSAdapter;
    }

    @Override
    public boolean checkProdVersion(final String fdnProd, final String prodDesig) {

        final Host host = HostGroup.getOssmaster();
        final User operUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        cliCommandHelper = new CLICommandHelper(host, operUser);
        final String la = CSTESTLA + SPACE + fdnProd + SPACE + prodDesig;
        LOGGER.debug(la);
        final String csOutput = cliCommandHelper.simpleExec(la);
        LOGGER.debug("cs output " + csOutput.toString());

        if (!csOutput.contains("3")) {
            LOGGER.info(" Prod Designation status is not 3 so it is not a MIXED RBS/ERBS");
            return false;
        }

        LOGGER.info(" Prod Designation status is 3 so it is a MIXED RBS/ERBS");
        return true;

    }
}