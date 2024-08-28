/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFile.NameSpace.MOCFILTER_PREFIX;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFlags.CMD_EXPORT_FLAG;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGFlags.PARAM_DOMAIN_FLAG;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGPaths.BCGTOOL;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGPaths.EXPORTFILEPATH;
import static com.ericsson.oss.cms.test.constants.BCGConstants.BCGPaths.IMPORT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.cms.test.constants.BCGConstants;

/**
 * @author egergro
 */
public class BCGToolUtility implements BCGToolHandler {

    private static final Logger logger = LoggerFactory.getLogger(BCGToolUtility.class);

    private final CLICommandHelper cliHelper;

    private final RemoteFileHandler remoteFileHandler;

    public BCGToolUtility(final Host host) {
        cliHelper = new CLICommandHelper(host);
        remoteFileHandler = new RemoteFileHandler(host);
    }

    @Override
    public boolean exportByMoClass(final String fileName, final String moClass) {
        final String mocFilterArg = PARAM_DOMAIN_FLAG + MOCFILTER_PREFIX + moClass;
        final String commandOutput = executeCommand(CMD_EXPORT_FLAG, fileName, mocFilterArg);
        return exportIsSuccessful(commandOutput, EXPORTFILEPATH + fileName);
    }

    private boolean exportIsSuccessful(final String commandOutput, final String defaultFilePath) {
        final String exportSuccessMessage = "Export has succeeded";
        return commandOutputContainsSuccessMessage(commandOutput, exportSuccessMessage) && isExitValueZero() && fileExistsAtDefaultPath(defaultFilePath);
    }

    private boolean commandOutputContainsSuccessMessage(final String commandOutput, final String searchPattern) {
        if (commandOutput.contains(searchPattern)) {
            return true;
        }
        final String errorMsg = String.format("Command output does not contain %s. Full output printed for info %s", searchPattern, commandOutput);
        logger.error(errorMsg);
        return false;
    }

    private boolean isExitValueZero() {
        final int exitValue = cliHelper.getCommandExitValue();
        if (exitValue == 0) {
            return true;
        }
        logger.error("A non zero exit value has been returned from command " + exitValue);
        return false;
    }

    private boolean fileExistsAtDefaultPath(final String defaultFilePath) {
        if (remoteFileHandler.remoteFileExists(defaultFilePath)) {
            return true;

        }
        logger.error("File is not existing at the default path " + defaultFilePath);
        return false;
    }

    private String executeCommand(final String cmd, final String... args) {
        final StringBuilder cmdToExec = new StringBuilder();
        cmdToExec.append(BCGTOOL);
        cmdToExec.append(" ");
        cmdToExec.append(cmd);
        cmdToExec.append(" ");
        for (final String arg : args) {
            cmdToExec.append(arg);
            cmdToExec.append(" ");
        }

        return cliHelper.simpleExec(cmdToExec.toString());
    }

    @Override
    public boolean copyRemoteBcgFileToLocal(final String fileName, final String localFilePath) {
        final String defaultPath = EXPORTFILEPATH;
        return remoteFileHandler.copyRemoteFileToLocal(defaultPath + fileName, localFilePath);
    }

    @Override
    public boolean copyLocalBcgFileToRemote(final String fileName, final String localPath) {
        final String defaultPath = IMPORT;
        remoteFileHandler.copyLocalFileToRemote(fileName, defaultPath + fileName, localPath);
        return fileExistsAtDefaultPath(defaultPath + fileName);
    }

    @Override
    public void deleteRemoteBCGFiles(final String exportFileName, final String importCreateFileName, final String importDeleteFileName) {
        remoteFileHandler.deleteRemoteFile(IMPORT + importCreateFileName);
        remoteFileHandler.deleteRemoteFile(IMPORT + importDeleteFileName);
        remoteFileHandler.deleteRemoteFile(EXPORTFILEPATH + exportFileName);
    }

    @Override
    public boolean verifyTotalImportCommands(final String fileName, final int noOfCommands, final String commandType) {

        final String totalCommands = cliHelper.simpleExec(String.format("grep -i '%s' %s | wc -l", commandType, IMPORT + fileName)).split("\\n")[0].trim();
        return noOfCommands == Integer.parseInt(totalCommands);
    }

    @Override
    public boolean importToPlannedArea(final String fileName, final String planName) {
        final String commandOutput = executeCommand(BCGConstants.BCGFlags.CMD_IMPORT, fileName, BCGConstants.BCGFlags.PARAM_PLAN, planName);
        return operationIsSuccessful(commandOutput, BCGConstants.BCGPaths.IMPORT, "Import has succeeded");
    }

    @Override
    public boolean activatePlan(final String planName) {
        final String commandOutput = executeCommand(BCGConstants.BCGFlags.CMD_ACTIVATE, planName);
        return operationIsSuccessful(commandOutput, "Activation SUCCESSFUL for plan");
    }

    @Override
    public boolean removePlan(final String planName) {
        final String commandOutput = executeCommand(BCGConstants.BCGFlags.CMD_REMOVEPLAN, planName);
        return operationIsSuccessful(commandOutput, "The Plan(s) have been successfully deleted");
    }

    private boolean operationIsSuccessful(final String commandOutput, final String successMessage) {
        return commandOutputContainsSuccessMessage(commandOutput, successMessage) && isExitValueZero();
    }

    private boolean operationIsSuccessful(final String commandOutput, final String defaultFilePath, final String successMessage) {
        return operationIsSuccessful(commandOutput, successMessage) && fileExistsAtDefaultPath(defaultFilePath);
    }

}
