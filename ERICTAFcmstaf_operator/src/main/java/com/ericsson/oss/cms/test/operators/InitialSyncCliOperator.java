/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.CSTESTLT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.IMPORT_SCRIPT_PATH;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.XML_REMOTE_PATH;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
//import com.ericsson.cifwk.taf.data.Host;
//import com.ericsson.cifwk.taf.data.User;
//import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmanvas
 */

@Operator(context = Context.CLI)
public class InitialSyncCliOperator implements InitialSyncOperator {

    private CLICommandHelper cliCommandHelper;

    private final static Logger logger = Logger.getLogger(InitialSyncCliOperator.class);

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final static String SPACE = " ";

    private final static String OPTION = " -f ";

    private final static String IMPORT = " -import -i_nau";

    private final static String MECONTEXT = "MeContext | grep";

    private final int MAX_CARDIN = 24;

    @Override
    public void addNode(final String xmlFileName, final String nodeType) {
        final String command = IMPORT_SCRIPT_PATH + OPTION + XML_REMOTE_PATH + xmlFileName + IMPORT;
        final String testValue = hostConnect(command);
        logger.info(testValue);
        final String ltCommand = CSTESTLT + SPACE + "MeContext | grep" + SPACE + nodeType;
        final String outputLt = cliCommandHelper.simpleExec(ltCommand);
        logger.info("Check the node is added by lt command");
        logger.info(outputLt);
    }

    @Override
    public void deleteNode(final String xmlFileName, final String nodeType) {
        final String command = IMPORT_SCRIPT_PATH + OPTION + XML_REMOTE_PATH + xmlFileName + IMPORT;
        final String outputImport = hostConnect(command);
        logger.debug("outputImport: " + outputImport);
        final String ltComm = CSTESTLT + SPACE + MECONTEXT + SPACE + nodeType;
        final String outputLt = cliCommandHelper.simpleExec(ltComm);
        logger.debug("Check the node is deleted by executing lt command");
        logger.debug("lt output:" + outputLt);

    }

    @Override
    public void copyXMLFileToRemote(final String xmlFileName) {
        logger.info("Copy XML files to server");
        final Host host = HostGroup.getOssmaster();
        final User rootUser = new User(host.getUser(UserType.ADMIN), host.getPass(UserType.ADMIN), UserType.ADMIN);
        final RemoteObjectHandler remoteFileHandler = new RemoteObjectHandler(host, rootUser);
        final String xmlSource = FileFinder.findFile(xmlFileName).get(0);
        final String localFileLocation = xmlSource;
        final String remoteFileLocation = XML_REMOTE_PATH;
        logger.info(remoteFileLocation);
        remoteFileHandler.copyLocalFileToRemote(localFileLocation, remoteFileLocation);

    }

    @Override
    public String hostConnect(final String command) {
        final Host host = HostGroup.getOssmaster();
        final User operUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        cliCommandHelper = new CLICommandHelper(host, operUser);
        return cliCommandHelper.simpleExec(command);

    }

    @Override
    public void copyFileToRemote(final String scriptName, final String remotePath) {

        logger.info("Copy XML files to server");
        final Host host = HostGroup.getOssmaster();
        final User rootUser = new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
        // final User rootUser = new User(host.getUser(UserType.ADMIN), host.getPass(UserType.ADMIN), UserType.ADMIN);
        // final User rootUser = new User("root", "shroot12", UserType.ADMIN);
        final RemoteObjectHandler remoteFileHandler = new RemoteObjectHandler(host, rootUser);
        final String scriptSource = FileFinder.findFile(scriptName).get(0);
        final String localFileLocation = scriptSource;
        final String remoteFileLocation = remotePath;
        logger.info(remoteFileLocation);
        remoteFileHandler.copyLocalFileToRemote(localFileLocation, remoteFileLocation);

    }

    @Override
    public void removeFileFromRemote(final String scriptName, final String remotePath) {

        logger.debug("Copy script to server");
        final Host host = HostGroup.getOssmaster();
        final User rootUser = new User(host.getUser(UserType.ADMIN), host.getPass(UserType.ADMIN), UserType.ADMIN);
        final RemoteObjectHandler remoteFileHandler = new RemoteObjectHandler(host, rootUser);

        final String remoteFileLocation = remotePath;
        logger.debug(remoteFileLocation);
        remoteFileHandler.deleteRemoteFile(remoteFileLocation + scriptName);

    }

    @Override
    public boolean findAndDeleteMOsFromCS(final Fdn moFdn) {
        final String moType = moFdn.getType();
        logger.info("enter into find&deleteMoFromCs, moFdn.getType()=" + moType);

        if (moType != null && moType.equalsIgnoreCase("ExternalEUtranCellFDD") || moType.equalsIgnoreCase("EUtranCellFDD")
                || moType.equalsIgnoreCase("EUtranCellTDD")) {
            final Fdn parentMoFdn = moFdn.getParentFdn();
            logger.info("current MoFdn=" + moFdn.getFdn());
            logger.info("parentMoFdn=" + parentMoFdn.getFdn());
            logger.info("parentMoFdn..." + parentMoFdn);
            logger.info("current moNames..." + moFdn.getMoName());
            final Filter utranCellFddFilter = SimpleFilterBuilder.builder().type(moType).build();
            logger.info("filter object for" + moType + utranCellFddFilter.getFilter());
            logger.info("fetching children of parent Mo");
            final List<Fdn> list = csHandler.getChildMos(parentMoFdn, utranCellFddFilter);
            logger.info("total no. of childs of type " + moType + "=" + list.size());
            if (list.size() == MAX_CARDIN) {
                final Fdn firstMo = list.get(0);
                logger.info("first mo if list size = 24" + firstMo.getFdn());
                final boolean isreservedByCleared = deleteMosInReservedBy(firstMo);
                if (isreservedByCleared) {
                    final boolean moDeletedSucc = csHandler.deleteMo(firstMo);
                    logger.info("Is first mo of list deleted" + moDeletedSucc);
                }
            }
        }
        return true;
    }

    /**
     * @param string
     * @return
     */
    private boolean deleteMosInReservedBy(final Fdn mo) {
        final boolean flag = true;
        int noOfResByMos = 0;
        logger.info("fetching and deleting reservedBy Mo");
        final String reservedByMos = csHandler.getAttributeValue(mo, "reservedBy");
        logger.info("reservedByMos=" + reservedByMos);
        if (reservedByMos != null && reservedByMos != "" && reservedByMos.contains("SubNetwork")) {
            noOfResByMos = reservedByMos.split(" ").length;
            logger.info("reservedByMos is not empty and it contains " + noOfResByMos + " MOs");
            if (noOfResByMos > 1) {
                final String[] fdnArray = reservedByMos.split(" ");
                final List<Fdn> fdnList = new ArrayList<Fdn>();
                for (final String fdn : fdnArray) {
                    if (fdn != null && fdn != "") {
                        final Fdn fdnForList = new Fdn(fdn);
                        fdnList.add(fdnForList);
                    }
                }
                logger.info("Deleting list of =" + fdnList.size() + " reservedBy Mos");
                final boolean isListOfReservedMosDeleted = csHandler.deleteMos(fdnList);
                logger.info("isListOfReservedMosDeleted=" + isListOfReservedMosDeleted);
            } else {
                logger.info("deleting one reservedBy MO");
                final Fdn fdnToDelete = new Fdn(reservedByMos);
                final boolean isReservedMosDeleted = csHandler.deleteMo(fdnToDelete);
                logger.info("isReservedMosDeleted=" + isReservedMosDeleted);
            }
        }
        return flag;
    }
}
