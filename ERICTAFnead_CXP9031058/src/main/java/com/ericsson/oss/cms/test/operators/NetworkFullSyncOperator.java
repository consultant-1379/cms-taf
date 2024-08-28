/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

/**
 * @author xmanvas
 */
public interface NetworkFullSyncOperator {

    /**
     * This method will get the total Number of Nodes from yang file.
     * 
     * @return
     *         returns the total number of Nodes
     */

    int readTotalNode();

    /**
     * This method will execute the MC restart and full sync.
     * 
     * @param String
     *        Script Name
     * @return
     *         void
     */

    void SyncNodes(String scriptName);

    /**
     * This method will get the number of synced nodes.
     * 
     * @param int
     *        total number of nodes
     * @return
     *         void
     */

    String readSyncNode(int totalNode);

    /**
     * This method will get the Starttime from yang file.
     * 
     * @return
     *         String start time
     */

    String readDumpStartTime();

}
