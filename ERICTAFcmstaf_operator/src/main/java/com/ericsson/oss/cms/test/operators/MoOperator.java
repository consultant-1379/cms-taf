/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author egergro
 */
public interface MoOperator {

    /**
     * This method returns a child Fdn of the given mo type on the given parent mo or null if mo type not found.
     *
     * @param moFdn
     *        Fdn of the parent mo.
     * @param moType
     *        Specifies the mo type of the child Fdn that is required.
     * @return Fdn of the given mo type, or null if not found.
     */
    Fdn getChildMoFdn(Fdn moFdn, String moType);

    /**
     * This method returns all children Fdns of the given mo type on the given parent mo or null if mo type not found.
     *
     * @param moFdn
     *        Fdn of the parent mo.
     * @param moType
     *        Specifies the mo type of the child Fdn that is required.
     * @return Fdns of the given mo type, or null if not found.
     */
    List<Fdn> getChildrenMoFdns(Fdn moFdn, String... moType);

    /**
     * This method returns all children Fdns for the given mo type(s) on the given parent mo(s) or an empty list none found.
     *
     * @param moFdns
     *        List of Parent Fdns.
     * @param moType
     *        Specifies the mo type(s) of the child Fdn(s) required.
     * @return Fdns of the given mo type(s) for all parent Fdns, empty list if none found.
     */
    List<Fdn> getChildrenMoFdns(List<Fdn> moFdns, String... moType);

    /**
     * This method returns all children Fdns of the given mo type on the given parent mo down to a given level or null if mo type not found.
     *
     * @param moFdn
     *        Fdn of the parent mo.
     * @param level
     *        The level down the tree to search
     * @param moType
     *        Specifies the mo type of the child Fdn that is required.
     * @return Fdns of the given mo type, or null if not found.
     */
    List<Fdn> getChildrenMoFdnsWithLevel(Fdn moFdn, String level, String... moType);
}