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
import com.ericsson.oss.cms.test.operators.NodesInNetsimandCSCliOperator;

/**
 * @author xmanvas
 */

public class NodesInNetsimandCS extends TorTestCaseHelper implements TestCase {

    @Inject
    private NodesInNetsimandCSCliOperator nodesInNetsimandCSCliOperator;

    @TestId(id = "OSS-39424-started netsim nodes in CS", title = "Verify all Netsim nodes against nodes in CS")
    @Context(context = { Context.CLI })
    @DataDriven(name = "netsim_cs")
    @Test(groups = { "NEAD, KGB" })
    public void nodesInNetsimandCS(@Input("nodeType") final String nodeType) {

        int numOfDisConNode = 0;
        int numOfNeverConNode = 0;

        setTestStep(" Check the Sync status of all Nodes managed by NEAD in CS");
        final int numOfUnsynNode = nodesInNetsimandCSCliOperator.getUnsyncNode(nodeType);
        if (numOfUnsynNode > 0) {
            setTestStep("Check the connection status of the Unsync node in CS");
            numOfDisConNode = nodesInNetsimandCSCliOperator.getDisConnetedNode(nodeType);
            numOfNeverConNode = nodesInNetsimandCSCliOperator.getNeverConnetedNode(nodeType);

        }
        assertEquals("UnSync nodes are present in CS    :", numOfUnsynNode, 0);
        assertEquals("Disconnected nodes are present in CS    :", numOfDisConNode, 0);
        assertEquals("Neverconnected nodes are present in CS   :", numOfNeverConNode, 0);

    }
}