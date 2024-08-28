/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.UserType;

/**
 * @author xmurran
 */
public interface MOScriptWriteAccessOperator {

    /**
     * Creates CLI instance on particular host with supplied UserType.
     * 
     * @param userType
     *        type of user you want to set on host
     * @param scriptName
     *        script you want to execute
     * @param host
     *        host name
     * @return <code>true<code> if script runs successfully, otherwise <code>false<code>
     */
    boolean executeScript(UserType userType, String scriptName, Host host);

    /**
     * Copy files from resource/data folder to host
     * 
     * @param host
     *        host name there you want to execute script
     * @param scriptFile
     *        file name
     */
    void copyScriptFilesToHost(Host host, String scriptFile);

    /**
     * Add User types in host configuration file
     * 
     * @param host
     *        name of the host
     * @param userName
     *        user name
     * @param userPass
     *        user password
     * @param userType
     *        type of user
     */
    void addUsersInHostConfiguration(Host host, String userName, String userPass, UserType userType);

    /**
     * Remove the file from host
     * 
     * @param host
     *        host name
     * @param scriptFile
     *        file name
     */
    void removeScriptFileFromHost(Host host, String scriptFile);

}
