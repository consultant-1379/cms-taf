/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

/**
 * @author xmurran
 */
public class MOScriptWriteAccessCliOperator implements MOScriptWriteAccessOperator {

    private CLICommandHelper cliCommandHelper;

    private RemoteFileHandler remote;

    private final static Logger logger = Logger.getLogger(MOScriptWriteAccessCliOperator.class);

    private final static String RUN_MO_SCRIPT_PATH_ON_HOST = "/opt/ericsson/nms_umts_cms_lib_com/bin/run_moscript ";

    private final static String SCRIPT_PATH_ON_HOST = "/opt/ericsson/nms_umts_cms_lib_com/info/";

    private final static String EXIT = "exit";

    @Override
    public boolean executeScript(final UserType userType, final String scriptName, final Host host) {
        cliCommandHelper = new CLICommandHelper(host, setUserOnHost(userType, host));
        cliCommandHelper.openShell();
        cliCommandHelper.execute(RUN_MO_SCRIPT_PATH_ON_HOST + " " + SCRIPT_PATH_ON_HOST + "" + scriptName);

        final int result = cliCommandHelper.getCommandExitValue();
        cliCommandHelper.execute(EXIT);

        return result == 0 ? true : false;
    }

    private User setUserOnHost(final UserType userType, final Host host) {
        final User user = new User(host.getUser(userType), host.getPass(userType), userType);
        return user;
    }

    @Override
    public void copyScriptFilesToHost(final Host host, final String localFileName) {
        remote = new RemoteFileHandler(host);
        final boolean isFileCopied = remote.copyLocalFileToRemote(localFileName, SCRIPT_PATH_ON_HOST);

        if (!isFileCopied) {
            logger.info("File " + localFileName + " is not copied to path :" + SCRIPT_PATH_ON_HOST);
        }
    }

    @Override
    public void addUsersInHostConfiguration(final Host host, final String userName, final String userPass, final UserType userType) {
        host.addUser(userName, userPass, userType);
    }

    @Override
    public void removeScriptFileFromHost(final Host host, final String scriptFile) {
        remote = new RemoteFileHandler(host);
        final boolean isFileDeleted = remote.deleteRemoteFile(SCRIPT_PATH_ON_HOST + "" + scriptFile);

        if (!isFileDeleted) {
            logger.info("File " + scriptFile + " is not deleted from path :" + SCRIPT_PATH_ON_HOST);
        }
    }

}