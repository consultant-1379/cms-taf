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

package com.ericsson.oss.cms.test.operators

import groovy.util.logging.Log4j

import java.util.regex.Matcher

import com.ericsson.cifwk.netsim.taf.NetworkFiltration
import com.ericsson.cifwk.taf.annotations.Context
import com.ericsson.cifwk.taf.annotations.Operator
import com.ericsson.cifwk.taf.data.Host
import com.ericsson.cifwk.taf.data.HostType
import com.ericsson.cifwk.taf.data.User
import com.ericsson.cifwk.taf.data.UserType
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult
import com.ericsson.cifwk.taf.handlers.netsim.commands.AvcburstCommand
import com.ericsson.cifwk.taf.handlers.netsim.commands.DumpmotreeCommand
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement
import com.ericsson.cifwk.taf.tools.cli.CLI
import com.ericsson.cifwk.taf.tools.cli.Shell
import com.ericsson.oss.cms.test.getters.CppAvcCliGetter
import com.ericsson.oss.taf.hostconfigurator.HostGroup

@Log4j
public class CppAvcCliOperator implements CppAvcOperator {

	static final String COMMA = ","
	static final String EUTRANCELL_REGEX = "(EUtranCell).*"
	static final String EQUALS = "="
	static final String EUTRANCELL_VAGUE_REF=",EUtranCell="
	static final String EUTRANCELLFDD = "EUtranCellFDD"
	static final String EUTRANCELLTDD = "EUtranCellTDD"
	static final String NENAME_WILDCARD = "%NENAME%"
	static final String MATCH_ALL = ".*"
	static final String NEWLINE = "\n"
	static final String SPACE = " "

	Map<NetworkElement, Map<String, Boolean>> fddTddMap = [:]

	CLI cli
	NeGroup allNEs
	Host[] netsimHosts
	NetSimCommandHandler netSimCommandHandler

	public CppAvcCliOperator() {
		netsimHosts = HostGroup.getAllNetsims()
		log.warn("NETSim Hosts: ${netsimHosts}")
		log.warn("NETSim Hosts: ${HostGroup.getAllNetsims()}")
		log.warn("OSS Host: ${HostGroup.getOssmaster()}")
		netSimCommandHandler = NetSimCommandHandler.getInstance(netsimHosts)
		allNEs = netSimCommandHandler.getAllNEs()

		Host rcHost = HostGroup.getOssmaster()
		User operUser = rcHost.getUsers(UserType.OPER)[0]
		cli = new CLI(rcHost, operUser)
	}

	public NeGroup filterNetworkElements(String nodeType, double percentage, int offset) {
		NetworkFiltration.getStartedNEsOfNodeType(netSimCommandHandler, allNEs, nodeType, percentage, offset)
	}

	public void sendAvcCommands(NeGroup neGroup, double frequency, String managedObject, String attribute, int numEvents, String[] values) {

		if (managedObject.contains(EUTRANCELL_VAGUE_REF)) {
			NeGroup neGroupFdd = new NeGroup()
			NeGroup neGroupTdd = new NeGroup()

			fddTddMap.each { networkElement, innerMap ->
				if (innerMap.get(EUTRANCELLFDD) == true) {
					neGroupFdd.add(networkElement)
				} else if (innerMap.get(EUTRANCELLTDD) == true) {
					neGroupTdd.add(networkElement)
				}
			}

			if (neGroupFdd.size() > 0) {
				sendAvcCommands(neGroupFdd, frequency, "ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=%NENAME%-1",
						attribute, numEvents, values)
			}

			if (neGroupTdd.size() > 0) {
				sendAvcCommands(neGroupTdd, frequency, "ManagedElement=1,ENodeBFunction=1,EUtranCellTDD=%NENAME%-1",
						attribute, numEvents, values)
			}
		} else {
			AvcburstCommand avcburstCommand = NetSimCommands.avcburst()
			avcburstCommand.setFreq(frequency)
			List<Map> avcData = []
			values.each { value ->
				avcData += [(managedObject) : [[(attribute) : value]]]
			}
			avcburstCommand.setAvcdata(avcData)
			avcburstCommand.setLoop(false)
			avcburstCommand.setNumEvents(numEvents)
			avcburstCommand.setIdleTime(0)
			avcburstCommand.setMode(AvcburstCommand.Mode.TEMP)
			neGroup.exec(avcburstCommand)
		}
	}

	public Map<NetworkElement, Integer> countReceivedNotifications(List<NetworkElement> networkElements, String managedObject, String attribute) {
		Map<NetworkElement, Integer> receivedNotifications = [:]
		Map<String, Integer> allReturnedAsMap = [:]
		String moMatch = managedObject.contains(NENAME_WILDCARD) ? managedObject.replace(NENAME_WILDCARD, MATCH_ALL) : managedObject

		if (managedObject.contains(EUTRANCELL_VAGUE_REF)) moMatch = moMatch.replace(EUTRANCELL_VAGUE_REF, ",EUtranCell[F|T]DD")

		String command = "sed -n 's/^.*MeContext=\\(.*\\),${moMatch},${SPACE}${attribute}=.*/\\1/gp' ${CppAvcCliGetter.NOTIFICATIONS_LOG_GLOB} | sort | uniq -c"
		log.debug("Checking log file using command: ${command}")

		Shell shell = cli.executeCommand(command)
		String result = shell.read(120)

		List<String> logLines = result.split(NEWLINE)

		logLines.each { line ->
			if (!line.isEmpty()) {
				List<String> lineAsList = line.split(SPACE).findAll {element -> element.length() > 0 }
				allReturnedAsMap.put(lineAsList[1].trim(), lineAsList[0] as int)
			}
		}
		networkElements.each { networkElement ->
			if (allReturnedAsMap.containsKey(networkElement.getName())) {
				receivedNotifications.put(networkElement, allReturnedAsMap[networkElement.getName()])
			} else {
				receivedNotifications.put(networkElement, 0)
			}
		}
		return receivedNotifications
	}

	public Map<NetworkElement, Integer> countReceivedNotifications(List<NetworkElement> networkElements, String managedObject, String attribute, Map<NetworkElement, Integer> notificationsBefore) {
		Map<NetworkElement, Integer> receivedNotifications = countReceivedNotifications(networkElements, managedObject, attribute)

		networkElements.each { networkElement ->
			int notifications = receivedNotifications[networkElement] -= notificationsBefore[networkElement]
			log.info "Relevant notifications received for ${networkElement}: ${notifications}"

		}
		return receivedNotifications
	}

	public Map<NetworkElement, Integer> expectedCountReceivedNotifications(List<NetworkElement> networkElements, int numEvents, Map<NetworkElement, Boolean> valueAlreadySet) {
		Map<NetworkElement, Integer> expectedNotifications = [:]
		networkElements.each { networkElement ->
			if (valueAlreadySet[networkElement]) {
				expectedNotifications.put(networkElement, numEvents-1)
			} else {
				expectedNotifications.put(networkElement, numEvents)
			}
		}
		return expectedNotifications
	}

	public Map<NetworkElement, NetSimResult> sendDumpMoTreeCommands(NeGroup neGroup, String managedObject,
			String attribute) {

		boolean vagueCellReference = false
		if (managedObject.contains(EUTRANCELL_VAGUE_REF)) {
			vagueCellReference = true
		}
		Matcher matcher = neNameWildCardMatcher(managedObject)
		managedObject = matcher.matches() ? matcher[0][1] : managedObject

		DumpmotreeCommand dumpmotreeCommand = NetSimCommands.dumpmotree()
		dumpmotreeCommand.moid = managedObject
		dumpmotreeCommand.scope = 1
		dumpmotreeCommand.printattrs = true
		dumpmotreeCommand.includeattrs = [attribute]
		Map<NetworkElement, NetSimResult> dumpMoTreeCommandOutput = neGroup.exec(dumpmotreeCommand)

		if (vagueCellReference) {
			dealWithFddTDDNodes(dumpMoTreeCommandOutput)
		}
		return dumpMoTreeCommandOutput
	}

	public void dealWithFddTDDNodes(Map<NetworkElement, NetSimResult> outputOfDumpMoTreeCommand) {
		outputOfDumpMoTreeCommand.each { networkElement, netSimResult ->
			Map<String, Boolean> innerMap = ["EUtranCellFDD":false, "EUtranCellTDD":false]

			for (String line : netSimResult.getOutput()[0].asList()) {
				if (line == "${EUTRANCELLFDD}=${networkElement.name}-1") {
					innerMap.put(EUTRANCELLFDD, true)
					break
				} else if (line == "${EUTRANCELLTDD}=${networkElement.name}-1") {
					innerMap.put(EUTRANCELLTDD, true)
					break
				}
			}
			fddTddMap.put(networkElement, innerMap)
		}
	}

	/**
	 * Return an instance of Matcher, to use to split an MO if it contains %NENAME%
	 * @param managedObject The MO to split
	 * @return Matcher object
	 */
	static Matcher neNameWildCardMatcher(String managedObject) {
		String regex = "(${MATCH_ALL})${COMMA}((${MATCH_ALL})" + NENAME_WILDCARD + "(${MATCH_ALL}))"
		return (managedObject =~ regex)
	}

	/**
	 * @param subMo lowest part of managedObject from data file
	 * @param receivedMo MO that came back from dumpmotree command
	 * @return whether this is considered to exist or not
	 */
	static boolean moMatches(String subMo, String receivedMo) {
		boolean moMatches = false
		Matcher eutranCellmatcher = (subMo =~ EUTRANCELL_REGEX)

		if (receivedMo.startsWith(subMo)) {
			moMatches = true
		} else if (eutranCellmatcher.matches() && receivedMo.startsWith(eutranCellmatcher[0][1]) &&
		receivedMo.contains(subMo.split(EQUALS)[-1])) {
			moMatches = true
		}
		return moMatches
	}

	/**
	 * This is more or less just an adapter between the condition-specific closures, and the methods
	 */
	Closure readDumpMoTreeResultsMap = {
		Closure innerClosure, Map inputMap, Map outputMap, String managedObject, String attribute, String value ->

		inputMap.keySet().each({ outputMap[it] = false })

		Matcher matcher = neNameWildCardMatcher(managedObject)

		inputMap.each { NetworkElement networkElement, NetSimResult netsimResult ->
			List<String> commandOutputAsList = netsimResult.structuredOutput.find({
				it.rawOutput.startsWith(">> dumpmotree") }).asList()

			String subMo = matcher.matches() ? matcher[0][3] + networkElement.name + matcher[0][4] : managedObject
			subMo = subMo.split(COMMA)[-1]

			innerClosure(commandOutputAsList, outputMap, networkElement, subMo, attribute, value)
		}
	}

	/**
	 * Goes through commandOutput, and sets "outputMap[networkElement]" to true if "condition" passes
	 */
	Closure iterateCmdOut = { List commandOutput, Closure condition, Map outputMap,
		NetworkElement networkElement, String subMo, String attribute, String value ->

		for (int pos=0; pos<commandOutput.size(); pos++) {
			if (condition(subMo, attribute, value, commandOutput, pos)) {
				outputMap[networkElement] = true
				break
			}
		}
	}

	/**
	 * Iterates over dumpmotree results to check if MO exists
	 */
	Closure checkMo = { List commandOutput, Map outputMap,
		NetworkElement networkElement, String subMo, String attribute, String value ->

		Closure moExistsCondition = { String mo, String attr, String val, List cmdOut, int pos ->
			return (cmdOut[pos].startsWith(mo) || moMatches(mo, cmdOut[pos]))
		}
		iterateCmdOut(commandOutput, moExistsCondition, outputMap, networkElement, subMo, attribute, value)

		if (!outputMap[networkElement])
			log.error "${networkElement.name} doesn't have $subMo"
	}

	/**
	 * Iterates over dumpmotree results to check if attribute exists
	 */
	Closure checkAttr = { List commandOutput, Map outputMap,
		NetworkElement networkElement, String subMo, String attribute, String value ->

		Closure attrExistsCondition = { String mo, String attr, String val, List cmdOut, int pos ->
			return (cmdOut[pos].startsWith(attr) && moMatches(mo, cmdOut[pos-1]))
		}
		iterateCmdOut(commandOutput, attrExistsCondition, outputMap, networkElement, subMo, attribute, value)

		if (!outputMap[networkElement])
			log.error "${networkElement.name} doesn't have $subMo with attribute $attribute"
	}

	/**
	 * Iterates over dumpmotree results to check if value already set
	 */
	Closure checkValue = { List commandOutput, Map outputMap,
		NetworkElement networkElement, String subMo, String attribute, String value ->

		Closure valueSetCondition = { String mo, String attr, String val, List cmdOut, int pos ->
			return (cmdOut[pos].startsWith(attr) && moMatches(mo, cmdOut[pos-1]) &&
			cmdOut[pos].split("=")[-1].equals(val))
		}
		iterateCmdOut(commandOutput, valueSetCondition, outputMap, networkElement, subMo, attribute, value)
		if (outputMap[networkElement])
			log.warn "${networkElement.name}: ${subMo}::${attribute} already set to $value"
	}

	/* Convenience closures */
	Closure readDumpMoTreeResultsForMo = readDumpMoTreeResultsMap.curry(checkMo).rcurry("", "")
	Closure readDumpMoTreeResultsForAttribute = readDumpMoTreeResultsMap.curry(checkAttr).rcurry("")
	Closure readDumpMoTreeResultsForValue = readDumpMoTreeResultsMap.curry(checkValue)

	public Map<NetworkElement, Boolean> checkMoExists(Map<NetworkElement, NetSimResult> dumpMoTreeResults,
			String managedObject) {
		Map<NetworkElement, Boolean> moExists = [:]
		readDumpMoTreeResultsForMo(dumpMoTreeResults, moExists, managedObject)
		return moExists
	}

	public Map<NetworkElement, Boolean> checkAttrExists(Map<NetworkElement, NetSimResult> dumpMoTreeResults,
			String managedObject, String attribute) {
		Map<NetworkElement, Boolean> attributeExists = [:]
		readDumpMoTreeResultsForAttribute(dumpMoTreeResults, attributeExists, managedObject, attribute)
		return attributeExists
	}

	public Map<NetworkElement, Boolean> checkValueNotSet(Map<NetworkElement, NetSimResult> dumpMoTreeResults,
			String managedObject, String attribute, String value) {
		Map<NetworkElement, Boolean> valueAlreadySet = [:]
		readDumpMoTreeResultsForValue(dumpMoTreeResults, valueAlreadySet, managedObject, attribute, value)
		return valueAlreadySet
	}

	public void waitForNotificationsToArrive(double seconds) {
		try {
			log.info "Waiting for ${seconds} seconds"
			Thread.sleep((int)seconds * 1000)
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt()
		}
	}

}
