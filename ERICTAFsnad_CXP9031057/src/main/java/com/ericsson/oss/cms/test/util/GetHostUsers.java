/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.handlers.RemoteFileHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xsujsud
 */
public class GetHostUsers {

    private static GetHostUsers instance = null;

    // @return CLICommandHelper
    public static CLICommandHelper getCLICommandHelper() {
        return new CLICommandHelper(getHost(), getUser());
    }

    public static RemoteFileHandler getRemoteFileHandler() {
        return new RemoteFileHandler(getHost(), getUser());
    }

    /**
     * @return
     */
    public static User getUser() {

        final Host host = HostGroup.getOssmaster();
        return new User(host.getUser(UserType.OPER), host.getPass(UserType.OPER), UserType.OPER);
    }

    public static Host getHost() {
        return HostGroup.getOssmaster();
    }

    private GetHostUsers() {

    }

    // Singleton to resolve pmd errors.
    public static GetHostUsers getInstance() {
        if (instance == null) {
            instance = new GetHostUsers();
        }

        return instance;
    }

}
