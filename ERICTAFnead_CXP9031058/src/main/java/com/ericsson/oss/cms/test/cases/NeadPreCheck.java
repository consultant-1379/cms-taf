/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.cases;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.cms.test.operators.NeadPreCheckCliOperator;

/**
 * @author xmanvas
 */

public class NeadPreCheck extends TorTestCaseHelper implements TestCase {

    @Inject
    private NeadPreCheckCliOperator neadPreCheckCliOperator;

    private String networkElement = null;

    @Test(groups = { "KGB", "CDB", "GAT", "Feature" })
    @DataDriven(name = "Online_Offline_Simulation")
    @Context(context = { Context.API })
    public void simulationTestStart(@TestId @Input("TestID") final String testId, @Input("NE") final String ne) {

        setTestcase(testId, " Online Simulation for Network Element : " + ne);

        final String startScript = "triggerStart.sh";
        final boolean flag = true;
        networkElement = ne;
        assertTrue(neadPreCheckCliOperator.simulationOperator(networkElement, startScript, flag));

    }
}