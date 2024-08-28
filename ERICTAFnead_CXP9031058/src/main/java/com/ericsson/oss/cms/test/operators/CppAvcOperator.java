/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.cms.test.operators;

import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;


public interface CppAvcOperator {

	/**
	 * The requested percent of Network Elements of the requested node type ensuring they are started
	 * @param nodeType Type of NE to filter on
	 * @param percentage Amount of NEs of type nodeType to return
	 * @return NeGroup
	 */
	public NeGroup filterNetworkElements(String nodeType, double percentage, int offset);

	/**
	 * Send an avcburst command to the provided NeGroup
	 * @param neGroup NeGroup relevant to this test case
	 * @param frequency Value for 'freq' parameter in avcburst command
	 * @param managedObject MO part of 'avcdata' parameter in avcburst command
	 * @param attribute Attribute part of elements in 'avcdata' array parameter in avcburst command
	 * @param numEvents Value for 'num_events' parameter in avcburst command
	 * @param values List of values to use for @param attribute in each element of 'avcdata' array parameter to avcburst command
	 */
	public void sendAvcCommands(NeGroup neGroup, double frequency, String managedObject, String attribute,
			int numEvents, String[] values);

	/**
	 * Send a dumpmotree command to the provided NeGroup
	 * @param neGroup NeGroup relevant to this test case
	 * @param managedObject MO part of 'avcdata' parameter in avcburst command
	 * @param attribute Attribute part of elements in 'avcdata' array parameter in avcburst command
	 */
	public Map<NetworkElement, NetSimResult> sendDumpMoTreeCommands(NeGroup neGroup, String managedObject,
			String attribute);

	/**
	 * Check log file in OSS to see how many notifications were received for given MO. Filter by attribute, sort by NE.
	 * If there are no results for an NE, received will be set to 0 for that NE.
	 * @param networkElements List of NEs to sort by
	 * @param managedObject MO to match in log entry
	 * @param attribute Attribute to filter returned results on
	 * @return Number of matching events that were found in the log per network element
	 */
	public Map<NetworkElement, Integer> countReceivedNotifications(List<NetworkElement> networkElements,
			String managedObject, String attribute);

	/**
	 * Check log file in OSS to see how many notifications were received for given MO. Filter by attribute, sort by NE.
	 * Remove notifications received in notificationsBefore parameter.
	 * @param networkElements List of NEs to sort by
	 * @param managedObject MO to match in log entry
	 * @param attribute Attribute to filter returned results on
	 * @param notificationsBefore Map generated before any notifications sent
	 * @return Number of matching events that were found in the log per network element, since notificationsBefore
	 */
	public Map<NetworkElement, Integer> countReceivedNotifications(List<NetworkElement> networkElements,
			String managedObject, String attribute, Map<NetworkElement, Integer> notificationsBefore);

	/**
	 * @param networkElements List of relevant NEs to this test case
	 * @param numEvents number of events/notifications per NE
	 * @return a map of NetworkElements and their expected number of events that can be compared against the actual numbers
	 */
	public Map<NetworkElement, Integer> expectedCountReceivedNotifications(List<NetworkElement> networkElements,
			int numEvents, Map<NetworkElement, Boolean> valueAlreadySet);

	/**
	 * Put FDD and TDD cell nodes into a map.
	 * @param neGroup NeGroup relevant to this test case
	 * @param outputOfDumpMoTreeCommand The output of the dumpMoTree command from earlier
	 */
	public void dealWithFddTDDNodes(Map<NetworkElement, NetSimResult> outputOfDumpMoTreeCommand);

	/**
	 * @param dumpMoTreeResults results map from dumpmotree command
	 * @param managedObject mo to check existence of
	 * @return Map of <NetworkElement, boolean> indicating whether managedObject exists
	 */
	public Map<NetworkElement, Boolean> checkMoExists(Map<NetworkElement, NetSimResult> dumpMoTreeResults,
			String managedObject);

	/**
	 * @param dumpMoTreeResults dumpMoTreeResults results map from dumpmotree command
	 * @param managedObject mo on which to check attribute existence
	 * @param attribute attribute to check existence of under managedObject
	 * @return Map of <NetworkElement, boolean> indicating whether attribute exists under MO
	 */
	public Map<NetworkElement, Boolean> checkAttrExists(Map<NetworkElement, NetSimResult> dumpMoTreeResults,
			String managedObject, String attribute);

	/**
	 * @param dumpMoTreeResults dumpMoTreeResults results map from dumpmotree command
	 * @param managedObject
	 * @param attribute
	 * @param value the value to check if already set
	 * @return Map of <NetworkElement, boolean> indicating whether value already set on attribute
	 */
	public Map<NetworkElement, Boolean> checkValueNotSet(Map<NetworkElement, NetSimResult> dumpMoTreeResults,
			String managedObject, String attribute, String value);

	/**
	 * Sleep current thread for provided time. Intended to be used to wait for avc notifications to arrive in OSS from NETSim
	 * @param seconds time in seconds to wait
	 */
	public void waitForNotificationsToArrive(double seconds);
}
