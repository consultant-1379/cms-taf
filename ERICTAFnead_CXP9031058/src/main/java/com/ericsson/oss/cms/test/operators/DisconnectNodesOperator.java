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

import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.oss.taf.cshandler.model.Fdn;

public interface DisconnectNodesOperator {

    /**
     * Gets the Netsim representation of nodes
     * 
     * @param listOfActiveNodes
     *        List of nodes to retrieve from Netsim
     * @return
     *         List of Netsim nodes
     */
    public NeGroup getNodesFromNetsim(List<Fdn> listOfActiveNodes);

    /**
     * Runs a Netsim stop on each fdn.
     * 
     * @param nes
     *        List of Netsim nodes
     */
    public void stopNodes(NeGroup nes);

    /**
     * Waits for nodes to become disconnected for maximum timeout of node type
     * 
     * @param nodes
     *        List of nodes
     * @param nodeTimeout
     *        Node timeout parameter
     * @return
     *         List of nodes that failed
     */
    public List<Fdn> waitForNodesStop(List<Fdn> nodes, int nodeTimeout);

    /**
     * Runs a Netsim start on each fdn.
     * 
     * @param nes
     *        List of Netsim nodes
     */
    public void startDisconnectedNodes(NeGroup nes);

    /**
     * Waits for nodes to become connected for maximum timeout of node type
     * 
     * @param listOfActiveNodes
     *        List of nodes
     * @param nodeTimeout
     *        Node timeout parameter
     * @return
     *         List of nodes that failed
     */
    public List<Fdn> waitForNodesStart(List<Fdn> listOfActiveNodes, int nodeTimeout);

}