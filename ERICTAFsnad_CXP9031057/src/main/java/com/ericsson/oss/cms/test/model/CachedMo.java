/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.model;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * Cached Managed Object representation to be used in SNAD Cache Operator.
 *
 * @author egokdag
 */
public class CachedMo {

    private final Fdn fdn;

    private final String globalId;

    private final String consistencyState;

    /**
     * Representation of a SNAD managed object used in the SNAD Cache Operator.
     *
     * @param moFdn
     *        The Fdn of the cached MO.
     * @param globalId
     *        The unique identifier for the MO.
     * @param consistencyState
     *        The state in SNAD cache.
     */
    public CachedMo(final Fdn moFdn, final String globalId, final String consistencyState) {
        this.fdn = moFdn;
        this.globalId = globalId;
        this.consistencyState = consistencyState;
    }

    /**
     * Get the Fdn of this cached MO.
     *
     * @return the Fdn
     */
    public Fdn getFdn() {
        return fdn;
    }

    /**
     * Get the GlobalId of this cached MO.
     * 
     * @return the globalId
     */
    public String getGlobalId() {
        return globalId;
    }

    /**
     * Get the Consistency state of this cached MO.
     * 
     * @return the consistencyState
     */
    public String getConsistencyState() {
        return consistencyState;
    }
}
