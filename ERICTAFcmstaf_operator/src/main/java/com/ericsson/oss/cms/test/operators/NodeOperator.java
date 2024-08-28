package com.ericsson.oss.cms.test.operators;

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
 *--------------------------------------------------------------------------------*/

import java.util.List;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.cms.test.model.attributes.NeDetails;
import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.nodeOperator.nodeFiltering.NodeSelectionFilter;

public interface NodeOperator {

    /**
     * Gets a synced node of a certain type.
     * 
     * @deprecated use {@link #getMimScopedSynchedNode(String)} instead
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @return
     *         Randomly selected node.
     */
    @Deprecated
    Fdn getSynchedNode(final String nodeType);

    /**
     * Gets a percentage of synced nodes of a certain type.
     * 
     * @deprecated use {@link #getPercentageMimScopedSynchedNode(String, int)} for a percenatage of nodes or
     *             {@link #getSpecifiedSynchedNode(String)} for a specific node.
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @param percentage
     *        int that represents the percentage of nodes you would like to retrieve.
     * @param nodeFdn
     *        The Fdn of a node if the test is to be executed against a named node. Otherwise null.
     * @return
     *         Randomly selected list of nodes, or list containing only nodeFdn, if nodeFdn is
     *         provided as a parameter
     */
    @Deprecated
    List<Fdn> getPercentageOfSyncedNodes(final String nodeType, final int percentage, final String nodeFdn);

    /**
     * Gets a single child MO of given moType from a node of given nodeType.
     * 
     * @deprecated use {@link #getChildMoFromMimScopedSyncedNode(String, String)} instead
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @param moType
     *        Specifies the mo type that is required.
     * @return
     *         The {@link Fdn} of a single child MO of moType from a synced node of nodeType, or null, if no synced node or child mo exists.
     */
    @Deprecated
    Fdn getChildMoFromSyncedNode(String nodeType, String moType);

    /**
     * Finds a synced node that contains all the child mo types requested, and returns the fdns of one of each child type.
     * 
     * @deprecated use {@link #getChildrenFromMimScopedSyncedNode(String, String[])} instead
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @param moTypes
     *        Specifies the mo types that are required.
     * @return
     *         One {@link Fdn} for each child mo type on the same synced node.
     *         Returns an empty list if synced node of required node type not found, or if a node does not contain at least one of each
     *         child mo type requested.
     */
    @Deprecated
    List<Fdn> getChildrenFromSyncedNode(String nodeType, String... moTypes);

    /**
     * Gets the generation counter of the node that contains the given MO Fdn.
     * 
     * @param moFdn
     *        The {@link Fdn} of the MO to be used for reading generation counter value.
     * @return
     *         The generation counter value.
     */
    int getGenerationCounter(final Fdn moFdn);

    /**
     * Wait for generation counter of node in the MO Fdn to increase and return the new value
     * 
     * @param moFdn
     *        The {@link Fdn} of the MO to be used for reading generation counter value.
     * @param oldGenCounter
     *        Previous generation counter value.
     * @param maxTime
     *        The maximum time(in seconds) to wait for generation counter to increase.
     * @return
     *         The generation counter value.
     */
    int getIncreasedGenerationCounter(final Fdn moFdn, final int oldGenCounter, final int maxTime);

    /**
     * Gets a synced node of a certain type based on MIM.
     * 
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeDetails} for details.
     * @return Randomly selected instance of a synced node of the required type or <code>null</code> if not found.
     */
    Fdn getMimScopedSynchedNode(String nodeType);

    /**
     * Gets the requested percentage of synced nodes of a certain type based on MIM.
     * 
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeDetails} for details.
     * @param percentage
     *        The percentage of nodes required.
     * @return Randomly selected list of synced nodes.
     */
    List<Fdn> getPercentageMimScopedSynchedNode(String nodeType, int percentage);

    /**
     * Gets the network element of given node type.
     * 
     * @param NodeFdnMeContext
     *        Type of network element to be collected from netsim.
     * @return
     *         Network element of the given node type.
     */
    NetworkElement getNetworkElement(String NodeFdnMeContext);

    /**
     * Checks whether command executed successfully or not.
     * 
     * @param cmdResult
     *        command result
     * @return
     *         <code>true</code> if command execution is successful, else <code>false</code>.
     */
    boolean checkCmdResult(NetSimResult cmdResult);

    /**
     * Gets the Fdn representation of the requested synched node fdn.
     * 
     * @param nodeFdn
     *        String representation of the requested node fdn.
     * @return {@link Fdn} representation of the node if synched, otherwise <code>null</code>
     */
    Fdn getSpecifiedSynchedNode(String nodeFdn);

    /**
     * Gets a single child MO of given moType from a node of given nodeType.
     * 
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @param moType
     *        Specifies the mo type that is required.
     * @return
     *         The {@link Fdn} of a single child MO of moType from a synced node of nodeType, or null, if no synced node or child mo exists.
     */
    Fdn getChildMoFromMimScopedSyncedNode(String nodeType, String moType);

    /**
     * Finds a synced node that contains all the child mo types requested, and returns the fdns of one of each child type.
     * 
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @param moTypes
     *        Specifies the mo types that are required.
     * @return
     *         One {@link Fdn} for each child mo type on the same synced node.
     *         Returns an empty list if synced node of required node type not found, or if a node does not contain at least one of each
     *         child mo type requested.
     */
    List<Fdn> getChildrenFromMimScopedSyncedNode(String nodeType, String... moTypes);

    /**
     * Method to constructs the immediate child of ManagedElement with
     * supplied meContext.
     * 
     * @param meContextFdn
     *        , meContext of the Fdn
     * @param childFdnName
     *        , childFdn of ManagedElement
     * @return Fdn of ManagedElement child type
     */
    Fdn getManagedElementChild(final Fdn meContextFdn, final String childFdnName);

    /**
     * Gets an Fdn of a specified type on a synced node. The returned Fdn will be present on a different node to that of the given Fdn,
     * mofdnOnCurrentNode.
     * 
     * @param nodeType
     *        The type of synced node
     * @param moType
     *        Type of MO
     * @param mofdnOnCurrentNode
     *        Fdn of MO on current node
     * @return
     *         {@link Fdn} on a different node or null if none is found.
     */
    Fdn getMoFromDifferentNode(String nodeType, String moType, Fdn mofdnOnCurrentNode);

    /**
     * This method returns a list of MeContext Fdns for the supplied Mo's.
     * 
     * @param fdns
     *        One or more {@link Fdn}
     * @return
     *         List of MeContext {@link Fdn}
     */
    List<Fdn> getMeContexts(Fdn... fdns);

    /**
     * @param csHandler
     * @param nodeType
     * @param restrictions
     * @return
     */

    Fdn getSyncedNode(CSHandler csHandler, String nodeType, NodeSelectionFilter... restrictions);

    /**
     * @param csHandler
     * @param nodeType
     * @param restrictions
     * @return
     */

    List<Fdn> getListOfSyncedNode(CSHandler csHandler, String nodeType, NodeSelectionFilter... restrictions);

    /**
     * @param nodeFdn
     * @param moTypes
     * @return
     */
    List<Fdn> getChildrenFromSyncedNodeFiltering(Fdn nodeFdn, String... moTypes);

    /**
     * @param syncNode
     * @param precentage
     * @return
     */
    List<Fdn> getPercentageSynchedNode(List<Fdn> syncNode, int percentage);

}
