package com.ericsson.oss.cms.test.operators;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * Interface describing all Snad related operations.
 *
 * @author edunsea
 */
public interface SnadOperator {

    /**
     * Get the current status of the Snad MC.
     *
     * @return The current status of the Snad MC as displayed by smtool.
     */
    String getMCStatus();

    /**
     * Execute the Snad smtool action <code>getMaster</code>.
     *
     * @param fdn
     *        The FDN of the master MO to execute <code>getMaster</code> on.
     * @return The details of the master MO in the cache as returned by the smtool action.
     */
    String getMaster(final Fdn fdn);

    /**
     * Execute the Snad smtool action <code>getProxy</code>.
     *
     * @param fdn
     *        The FDN of the proxy MO to execute <code>getProxy</code> on.
     * @return The details of the proxy MO in the cache as returned by the smtool action.
     */
    String getProxy(final Fdn fdn);

    /**
     * Execute the Snad smtool action <code>getMaster</code> and return just the state of the master MO in the cache.
     *
     * @param fdn
     *        The FDN of the master MO to execute <code>getMaster</code> on.
     * @return The state of the master MO in the cache as returned by the smtool action.
     */
    String getMasterState(final Fdn fdn);

    /**
     * Execute the Snad smtool action <code>getProxy</code> and return just the state of the proxy MO in the cache.
     *
     * @param fdn
     *        The FDN of the proxy MO to execute <code>getProxy</code> on.
     * @return The state of the proxy MO in the cache as returned by the smtool action.
     */
    String getProxyState(final Fdn fdn);

    /**
     * Execute the Snad smtool action <code>getMasterForProxy</code> to find the master MO for this proxy in the cache.
     *
     * @param fdn
     *        The FDN of the proxy MO to execute <code>getMasterForProxy</code> on.
     * @return The FDN of the master MO.
     */
    Fdn getMasterForProxy(final Fdn fdn);

    /**
     * Execute the Snad smtool action <code>getProxiesForMaster</code> to find all the proxies for this master MO.
     *
     * @param fdn
     *        The FDN of the master MO to execute <code>getProxiesForMaster</code> on.
     * @return A list containing the FDNs of this masters proxies.
     */
    List<Fdn> getProxiesForMaster(final Fdn fdn);

    /**
     * Execute the Snad smtool action <code>getClusterMembers</code> to get a list of RNCs in a cluster.
     *
     * @param fdn
     *        The FDN of the master RncFunction to execute <code>getClusterMembers</code> on.
     * @return A list containing the FDNs of each RNC in the cluster.
     */
    List<Fdn> getClusterMembers(final Fdn fdn);

    /**
     * Check whether all proxy Mos in the provided list are Consistent.
     *
     * @param proxyMos
     *        The list of string representation of proxy Mo FDNs.
     * @return <code>true</code> if all the proxy Mos are in Consistent state in Snad cache, otherwise <code>false</code>.
     */
    boolean areProxiesConsistent(List<Fdn> proxyMos);

    /**
     * Blocking call to wait until the Snad long sleep message is found in the logs.
     */
    void waitForCCToComplete();

    /**
     * Sleeps the main execution thread for a given number of milliseconds.
     *
     * @param milliseconds
     *        Number of milliseconds to sleep for
     */
    void waitFor(int milliseconds);

    /**
     * Checks for sleep message in CIF logs until CIF log entry is returned or timeout is expired.
     *
     * @param startTime
     *        The time to start searching for the sleep message in the CIF logs.
     * @param timeout
     *        The max time to wait for the sleep message in the CIF logs. Measured in units of milliseconds.
     * @return
     *         <code>true</code> if CIF log sleep message is found, otherwise <code>false</code>.
     */
    boolean waitForSleep(long startTime, long timeOut);

    /**
     * Gets a CIF log message indicating that SubNetwork has recovered successfully
     *
     * @param rootMO
     *        The SubNetwork RootMO
     * @param startTime
     *        The start time from when to retrieve the CIF log. This will be included in the query
     * @return
     *         <code>true</code> if a log message has been received.
     */
    Boolean hasSubNetworkRecovered(final String rootMO, final long startTime);

    /**
     * @param fdn
     * @return
     *         <code>true</code> if a there any pciConflict exist in the given FDN.
     */

    boolean isConflictExist(Fdn fdn);

    /**
     * @param activeNodeFdns
     * @param moType
     * @param proxyCount
     * @return
     *         List of Fdns with zero conflicts
     *         MasterFdn will be at the first index of List.
     */

    List<Fdn> getZeroConflictMasterProxyFromSyncedNodes(final List<Fdn> activeNodeFdns, final String moType, final int proxyCount);

    /**
     * @param activeMoFdns
     * @param proxyCount
     * @return
     *         List of Fdns with zero conflicts
     *         MasterFdn will be at the first index of List.
     */

    List<Fdn> getNoConflictMasterAndProxy(List<Fdn> activeMoFdns, int proxyCount);

    /**
     * @param masterMoFdn
     * @param activeNodeFdn
     * @return
     *         plmIdentity values.
     */

    Map<String, String> getPlmIdentity(Fdn masterMoFdn, Fdn activeNodeFdn);

    /**
     * @param plmIdentity
     * @param inputCount
     * @return
     *         input for pciConflictCell/pciDetectingCell.
     */

    String setPciCellValues(Map<String, String> plmIdentity, int cellId, int inputCount);

    /**
     * @param plmIdentity
     * @param inputCount
     * @return
     *         input for pciDetectingCell.
     */

    String setPciDetCellValues(Map<String, String> plmIdentity, int cellId, int inputCount);

    /**
     * @param conflictValues
     * @return
     *         input for pciConflict.
     */

    String setPciConflictValues(String... conflictVals);

     /**
     * @param fdn
     *        Perform check on the fdn
     */
    void performCheckonFDN(Fdn moFdn);

}
