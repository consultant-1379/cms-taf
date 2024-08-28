/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * Interface describing all Snad cache review related operations.
 *
 * @author egokdag
 */
public interface SnadCacheOperator {

    /**
     * Executes SNAD cache review command, parses the cache file output and populates internal maps.
     */
    void executeCacheReview();

    /**
     * Checks whether a given MO is in any section of the SNAD cache.
     *
     * @param moFdn
     *        The {@link Fdn} of the MO to be checked.
     * @return <code>true</code> if the MO is in any section of the SNAD cache, otherwise <code>false</code>.
     */
    boolean isInCache(Fdn moFdn);

    /**
     * Checks whether a given master MO is in the SNAD masterMO cache.
     *
     * @param masterMoFdn
     *        The {@link Fdn} of the master MO to be checked.
     * @return
     *         <code>true</code> if master MO is in the managed section of the SNAD cache, otherwise <code>false</code>.
     */
    boolean isInMasterCache(Fdn masterMoFdn);

    /**
     * Checks whether a given proxy MO is in the SNAD proxyMO cache.
     *
     * @param proxyMoFdn
     *        The {@link Fdn} of the proxy MO to be checked.
     * @return
     *         <code>true</code> if proxy MO is in the managed section of the SNAD cache, otherwise <code>false</code>.
     */
    boolean isInProxyCache(Fdn proxyMoFdn);

    /**
     * Checks whether the given MO is in the SNAD RNC in Pool cluster cache.
     *
     * @param moFdn
     *        The {@link Fdn} of the MO to be checked.
     * @return
     *         <code>true</code> if MO is in the cluster cache, otherwise <code>false</code>.
     */
    boolean isInClusterCache(Fdn moFdn);

    /**
     * Checks whether the given MO is in the SNAD unmanaged cache.
     *
     * @param moFdn
     *        The {@link Fdn} of the MO to be checked.
     * @return
     *         <code>true</code> if MO is in the unmanaged cache, otherwise <code>false</code>.
     */
    boolean isInUnmanagedCache(Fdn moFdn);

    /**
     * Checks whether the given MO is in SNAD's proxy unsynced cache.
     *
     * @param moFdn
     *        The {@link Fdn} of the MO to be checked.
     * @return
     *         <code>true</code> if MO is in the proxy unsynced cache, otherwise <code>false</code>.
     */
    boolean isInProxyUnsyncedCache(Fdn moFdn);

    /**
     * Checks whether the given MO is in the SNAD RBS LocalCell cache.
     *
     * @param moFdn
     *        The {@link Fdn} of the MO to be checked.
     * @return
     *         <code>true</code> if MO is in the RBS LocalCell cache, otherwise <code>false</code>.
     */
    boolean isInRbsLocalCellCache(Fdn moFdn);

    /**
     * Returns the consistency state of a given master MO.
     *
     * @param masterMoFdn
     *        The {@link Fdn} of the Master MO to be checked
     * @return
     *         The consistency state of the master MO if MO exists in the SNAD cache, otherwise null.
     */
    String getMasterConsistencyState(Fdn masterMoFdn);

    /**
     * Checks the SNAD Cache for the given list of {@link Fdn}'s.
     *
     * @param fdns
     *        The list of {@link Fdn}'s to search the SNAD Cache for.
     * @return List of {@link Fdn}'s which do not exist in the SNAD Cache, or an empty List if all {@link Fdn}'s found.
     */
    List<Fdn> getFdnsNotInCache(final List<Fdn> fdns);

    /**
     * Report {@link Fdn}'s not found in Cache.
     *
     * @param fdns
     *        List of {@link Fdn}'s which do not exist in the SNAD Cache.
     * @return String representation of the {@link Fdn}'s not in the SNAD Cache.
     */
    String reportMosNotInCache(List<Fdn> fdns);
}