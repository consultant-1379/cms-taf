/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmanvas
 */
public interface LicenseSyncOperator {
    /**
     * Start a sync on a List of {@link Fdn}s by executing an adjust on each one.
     *
     * @param fdns
     *        The Fdns which will be synced.
     */
    void startSyncOnNodes(List<Fdn> fdns);

    /**
     * @param fdns
     * @param timeout
     * @return List of Fdn
     */
    List<Fdn> checkForFailedNodes(final List<Fdn> fdns, final int timeout);

    /**
     * @param fdns
     * @param timeout
     * @return List of Fdn
     */
    List<Fdn> checkForSuccessCPPNodes(final List<Fdn> fdns, final int timeout);

    /**
     * @param nodeType
     * @return List of Fdn
     */
    List<Fdn> findAllConnectedNodes(final String nodeType);

    /**
     * @param fdn
     * @return
     */
    String getMirrorVerAndNodeType(Fdn fdn);

    /**
     * @param successNodes
     * @param licScenarioMap
     * @return
     */
    boolean readMirrorMibVersionFromCPPfile(List<Fdn> successNodes, Map<String, Boolean> licScenarioMap);

    /**
     * @param installLic
     * @param removeLic
     */
    void installAndRemoveLic(String[] installLic, String[] removeLic);

    /**
     * @param initialState
     */
    void removeCopiedFilesAndRestoreServer(String initialState);

}
