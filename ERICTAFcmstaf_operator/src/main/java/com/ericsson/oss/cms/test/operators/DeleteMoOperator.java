/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmurran
 */
public interface DeleteMoOperator {

    /**
     * Method performs the delete operation on supplied moFdn.
     * 
     * @param moFdn
     *        unique identifier of the MO to be deleted
     * @return true if MO deleted successfully on Node
     */
    boolean deleteMo(final Fdn moFdn);

    /**
     * Method performs the delete operation on a list of moFdns.
     * 
     * @param moFdnList
     *        List of Fdns of the MOs to be deleted
     */
    void deleteMos(final List<Fdn> moFdnList);
}