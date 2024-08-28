/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.cms.test.cases;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.operators.CppAvcCliOperator;
import com.ericsson.oss.cms.test.operators.CppAvcCliOperatorCDB;
import com.ericsson.oss.taf.cshandler.model.Fdn;

public class NewCppAvc extends TorTestCaseHelper implements TestCase {

    private final CppAvcCliOperator cppAvcOperator = new CppAvcCliOperator();

    private final CppAvcCliOperatorCDB cppAvcOperatorCdb = new CppAvcCliOperatorCDB();

    /**
     * CPP AVC Test
     * 
     * @DESCRIPTION Send and receive AVC notifications from CPP nodes
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-3127_Perf_1", title = "CPP AVC Test")
    @Context(context = { Context.CLI })
    @DataDriven(name = "NewCppAvc")
    @Test(groups = { "Acceptance" })
    public void sendAvcNotifications(
            @Input("frequency") final double frequency,
            @Input("num_events") final int numEvents,
            @Input("MO") final String managedObject,
            @Input("attribute") final String attribute,
            @Input("values") final String[] values,
            @Input("nodeType") final String nodeType,
            @Input("percentage") final int percentage,
            @Input("offset") final int offset) {

        setTestStep("Select connected and synchronished nodes");
        final List<Fdn> listOfActiveNodes = cppAvcOperatorCdb.findSynchedNodes(nodeType, percentage);
        assertThat("No sync'd node(s) found", listOfActiveNodes, is(not(empty())));

        final NeGroup elementsUnderTest = cppAvcOperatorCdb.getNodes(listOfActiveNodes);
        assertThat("Could not find selected node(s) in Netsim", elementsUnderTest, is(not(empty())));

        final List<NetworkElement> networkElements = elementsUnderTest.getNetworkElements();

        setTestInfo("Record the number of %s notifications for MO before sending burst AVC: AVC");
        final Map<NetworkElement, Integer> notificationsBefore = cppAvcOperator.countReceivedNotifications(networkElements, managedObject, attribute);
        for (final NetworkElement fdn : notificationsBefore.keySet()) {
            setTestInfo("Key" + fdn + "Values " + notificationsBefore.get(fdn));
        }
        for (final Fdn moUnderTest : listOfActiveNodes) {
            cppAvcOperatorCdb.setAttributeValues(moUnderTest, attribute, "TestTaf");

        }
        sleep(50);

        setTestStep("Send notifications from Network Elements");
        cppAvcOperator.sendAvcCommands(elementsUnderTest, frequency, managedObject, attribute, numEvents, values);

        setTestStep("Wait for notifications to arrive in OSS");
        cppAvcOperator.waitForNotificationsToArrive(numEvents * (1 / frequency) + Math.min(2 / frequency, 30.0));

        setTestInfo("Record the number of %s notifications for MO: AVC");
        final Map<NetworkElement, Integer> notifCountBeforeSet = cppAvcOperator.countReceivedNotifications(networkElements, managedObject, attribute,
                notificationsBefore);

        setTestStep("Verify correct amount of relevant notifications arrive in OSS for each Network Element");

        for (final NetworkElement fdn : notifCountBeforeSet.keySet()) {
            setTestInfo("Key" + fdn + "Values " + notifCountBeforeSet.get(fdn));
            final int value = notifCountBeforeSet.get(fdn);
            assertEquals("Not all notifications were found in the OSS", value, numEvents);

        }

    }
}
