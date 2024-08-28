/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.oss.cms.test.operators.MOScriptWriteAccessCliOperator;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmurran
 */
public class MOScriptWriteAccess extends TorTestCaseHelper implements TestCase {

    @Inject
    private MOScriptWriteAccessCliOperator moScriptWriteAccessCliOperator;

    private final Host host = HostGroup.getOssmaster();

    private final static String MO_CREATE_SCRIPT = "createLA.py";

    private final static String MO_DELETE_SCRIPT = "deleteLA.py";

    private final static String MO_MODIFY_SCRIPT = "modifyLA.py";

    @BeforeTest
    void beforeTest() {
        moScriptWriteAccessCliOperator.copyScriptFilesToHost(host, MO_CREATE_SCRIPT);
        moScriptWriteAccessCliOperator.copyScriptFilesToHost(host, MO_DELETE_SCRIPT);
        moScriptWriteAccessCliOperator.copyScriptFilesToHost(host, MO_MODIFY_SCRIPT);
    }

    @TestId(id = "OSS-84609-MO_Script_Writeaccess", title = "Check NEAD moscript write access")
    @Context(context = { Context.CLI })
    @DataDriven(name = "moscript_writeaccess")
    @Test(groups = { "NEAD, KGB" })
    public void checkMOScriptWriteAccess(
            @Input("user_type") final String userType,
            @Input("user_name") final String userName,
            @Input("user_passwd") final String userPass,
            @Input("expected_result") final boolean expectedResult) {

        final UserType user = UserType.valueOf(userType);
        moScriptWriteAccessCliOperator.addUsersInHostConfiguration(host, userName, userPass, user);

        setTestStep("Set the user with role " + userType + " and run run_moscript with create, modify and delete script");
        final boolean isRelationCreated = moScriptWriteAccessCliOperator.executeScript(user, MO_CREATE_SCRIPT, host);
        assertThat("Relation is not created with " + userType + " user successfully", isRelationCreated, equalTo(expectedResult));

        final boolean isRelationModified = moScriptWriteAccessCliOperator.executeScript(user, MO_MODIFY_SCRIPT, host);
        assertThat("Relation is not modified with " + userType + " user successfully", isRelationModified, equalTo(expectedResult));

        final boolean isRelationDeleted = moScriptWriteAccessCliOperator.executeScript(user, MO_DELETE_SCRIPT, host);
        assertThat("Relation is not deleted with " + userType + " user successfully", isRelationDeleted, equalTo(expectedResult));
    }

    @AfterTest
    void afterTest() {
        moScriptWriteAccessCliOperator.removeScriptFileFromHost(host, MO_CREATE_SCRIPT);
        moScriptWriteAccessCliOperator.removeScriptFileFromHost(host, MO_DELETE_SCRIPT);
        moScriptWriteAccessCliOperator.removeScriptFileFromHost(host, MO_MODIFY_SCRIPT);
    }
}