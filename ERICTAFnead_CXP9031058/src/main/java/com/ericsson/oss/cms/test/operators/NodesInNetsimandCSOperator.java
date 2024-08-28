package com.ericsson.oss.cms.test.operators;

import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;

public interface NodesInNetsimandCSOperator {

    /**
     * Get the Unsync nodes from CS
     * 
     * @param nodeType
     *        Type of Node
     * @return
     *         Number of UnSync Node
     */
    int getUnsyncNode(String nodeType);

    /**
     * Get the Disconnected nodes from CS
     * 
     * @param nodeType
     *        Type of Node
     * @return
     *         Number of disconnected Node
     */
    int getDisConnetedNode(String nodeType);

    /**
     * Get the neverconnected nodes from CS
     * 
     * @param nodeType
     *        Type of Node
     * @return
     *         Number of neverconnected Node
     */
    int getNeverConnetedNode(String nodeType);

    /**
     * Get the started nodes from netsim
     * 
     * @param allNes
     *        Node group
     * @return
     *         Number of started disconnected/never connected Node
     */
    int getStartedDisconNodes(NeGroup allNes);

}