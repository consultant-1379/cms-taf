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
import static org.hamcrest.Matchers.everyItem;
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
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.operators.CppAvcCliOperator;
import com.ericsson.oss.cms.test.operators.CppAvcOperator;

public class CppAvc extends TorTestCaseHelper implements TestCase {

    private final CppAvcOperator cppAvcOperator = new CppAvcCliOperator();

    /**
     * CPP AVC Test
     *
     * @DESCRIPTION Send and receive AVC notifications from CPP nodes
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-3127_Perf_1", title = "CPP AVC Test")
    @Context(context = { Context.CLI })
    @DataDriven(name = "CppAvc")
    @Test(groups = { "Acceptance" })
    public void sendAvcNotifications(
            @Input("frequency") final double frequency,
            @Input("num_events") final int numEvents,
            @Input("MO") final String managedObject,
            @Input("attribute") final String attribute,
            @Input("values") final String[] values,
            @Input("nodeType") final String nodeType,
            @Input("percentage") final double percentage,
            @Input("offset") final int offset) {

        setTestStep("Get filtered list of started Network Elements");
        setTestInfo("Get filtered list of started NEs (%f%% of type %s) with offset %d", percentage, nodeType, offset);
        final NeGroup neGroup = cppAvcOperator.filterNetworkElements(nodeType, percentage, offset);
        final List<NetworkElement> networkElements = neGroup.getNetworkElements();
        assertThat("There are no started Network Elements", networkElements, is(not(empty())));

        setTestInfo("Perform dumpmotree on selected NEs");
        final Map<NetworkElement, NetSimResult> dumpMoTreeResults = cppAvcOperator.sendDumpMoTreeCommands(neGroup, managedObject, attribute);

        setTestInfo("Check MO %s exists on all selected NEs", managedObject);
        final Map<NetworkElement, Boolean> moExistsMap = cppAvcOperator.checkMoExists(dumpMoTreeResults, managedObject);
        assertThat("Not all selected NEs have the required MO", moExistsMap.values(), everyItem(is(true)));

        setTestInfo("Check attribute %s exists on MO on all selected NEs", attribute);
        final Map<NetworkElement, Boolean> attrExistsMap = cppAvcOperator.checkAttrExists(dumpMoTreeResults, managedObject, attribute);
        assertThat("Not all selected MOs have the required attribute", attrExistsMap.values(), everyItem(is(true)));

        setTestInfo("Check if value %s is already set on attribute %s on any selected NE", values[1], attribute);
        final Map<NetworkElement, Boolean> valueAlreadySet = cppAvcOperator.checkValueNotSet(dumpMoTreeResults, managedObject, attribute, values[1]);

        setTestStep("Check notifications in log file on OSS");
        setTestInfo("Check number of notifications in log file in OSS for each NE with MO %s and attribute %s BEFORE sending", managedObject, attribute);
        final Map<NetworkElement, Integer> notificationsBefore = cppAvcOperator.countReceivedNotifications(networkElements, managedObject, attribute);

        setTestStep("Send notifications from Network Elements");
        cppAvcOperator.sendAvcCommands(neGroup, frequency, managedObject, attribute, numEvents, values);

        setTestStep("Wait for notifications to arrive in OSS");
        cppAvcOperator.waitForNotificationsToArrive(numEvents * (1 / frequency) + Math.min(2 / frequency, 30.0));

        setTestStep("Verify correct amount of relevant notifications arrive in OSS for each Network Element");
        assertEquals("Not all notifications were found in the OSS",
                cppAvcOperator.expectedCountReceivedNotifications(networkElements, numEvents, valueAlreadySet),
                cppAvcOperator.countReceivedNotifications(networkElements, managedObject, attribute, notificationsBefore));
    }
}
