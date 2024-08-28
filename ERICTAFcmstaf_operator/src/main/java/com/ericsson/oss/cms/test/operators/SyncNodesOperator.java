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

import java.util.Collection;
import java.util.List;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.taf.cshandler.model.Fdn;

public interface SyncNodesOperator {

    /**
     * Start a sync on a List of {@link Fdn}s by executing an adjust on each one.
     * 
     * @param fdns
     *        The Fdns which will be synced.
     */
    void startSyncOnNodes(List<Fdn> fdns);

    /**
     * Iterates through a list of {@link Fdn}s and checks their sync status.
     * 
     * @param fdns
     *        The List of fdns under test.
     * @param timeout
     *        The max time to wait for the node to finish it's sync. Measured in units of seconds.
     * @return The Fdns from this list that are not synced
     */
    List<Fdn> checkForFailedNodes(List<Fdn> fdns, int timeout);

    /**
     * When a Nead sync is executed for a node, Nead will log 3 separate messages during the sync to signify the current stage of the sync.
     * This method queries the Cif logs for sync logs for each node since the beginning of this test.
     * 
     * @param fdns
     *        The list of node fdns. *
     * @param startTime
     *        Time the test started.
     * @return
     *         List of sync status logs for the nodes in list of fdns.
     */
    List<CIFLogItem> getSyncStatusCifLogEntries(final List<Fdn> fdns, long startTime);

    /**
     * This method takes a list of nodes and calculates the total number of logs expected based on the value
     * of the logsPerNode parameter
     * 
     * @param fdns
     *        The list of nodes which logs are expected for
     * @param logsPerNode
     *        The list of logs expected per node
     * @return
     *         Integer value indicating the total number of logs expected for the list of nodes
     */
    int getExpectedNumCifLogs(final int numberOfNodes);

    /**
     * When a Nead sync is executed successfully for a node, Nead will log sync Info log
     * This method queries the Cif logs for sync Info logs for each node since the beginning of this test.
     * 
     * @param fdns
     *        The list of node fdns.
     * @param startTime
     *        Time the test started.
     * @return
     *         List of sync Info logs for the nodes in list of fdns.
     */
    List<CIFLogItem> getSyncInfoCifLogEntries(List<Fdn> fdns, long startTime);

    /**
     * Queries the CIF Log for a Delta Sync log entry for the supplied fdn(s) starting at the given start time
     * 
     * @param fdns
     *        Node(s) for which a Delta Sync is expected to be observed
     * @param startTime
     *        Time at which query will start search in Logs
     * @return
     *         The CIF Log entry for Delta Sync for the queried node(s) or an empty list if no log retrieved.
     */
    List<CIFLogItem> getDeltaSyncCifLogEntry(List<Fdn> fdns, long startTime);

    /**
     * Print contents of a collection with each element on a new line.
     * 
     * @param collection
     *        Collection to be printed.
     */
    <T> void printCollection(Collection<T> collection);

    /**
     * This method queries the Cif logs for the expected number if sync logs. If the expected number of sync logs is not received,
     * this method will retry once after 5 secs and then return.
     * 
     * @param fdns
     *        The list of node fdns.
     * @param startTime
     *        Time the test started.
     * @param numLogs
     *        expected number of logs to receive
     * @return
     *         List of sync Info logs for the nodes in list of fdns.
     */

    List<CIFLogItem> getSyncStatusCifLogEntriesWithRetry(List<Fdn> fdns, long startTime, int numLogs);

    /**
     * Iterates through a list of {@link Fdn}s and checks their sync status.
     * 
     * @param fdns
     *        The List of fdns under test.
     * @param timeout
     *        The max time to wait for the node to finish it's sync. Measured in units of seconds.
     * @return The Fdns from this list that are not synced
     */


List<Fdn> checkForSuccessNodes(final List<Fdn> fdns, final int timeout);
}
