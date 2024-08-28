/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xrajnka
 */
public interface CreateMoOperator {

    /**
     * Builds an MO Fdn with the given parent MO Fdn and MO Type.
     * 
     * @param parentMoFdn
     *        Parent Fdn of the MO to be created.
     * @param moType
     *        Type of the MO to be created.
     * @param moName
     *        Name of the MO to be created.
     * @return
     *         Fdn of the child MO.
     */
    Fdn buildMoFdn(Fdn parentMoFdn, String moType, String moName);

    /**
     * Creates a MO on the node.
     * 
     * @param expectedMoFdn
     *        Expected MO Fdn of the MO to be created.
     * @param attrs
     *        Attributes to be set on the created MO.
     * @param attrValues
     *        Attribute values to be set on the created MO.
     * @return
     *         boolean <code>true<code> if MO creation is successful on the node, else <code>false</code>.
     */
    boolean createMo(Fdn expectedMoFdn, String[] attrs, String[] attrValues);

    /**
     * Creates a MO on the node.
     * 
     * @param expectedMoFdn
     *        Expected MO Fdn of the MO to be created.
     * @param attributes
     *        The attributes to be set on the created MO.
     * @return
     *         boolean <code>true<code> if MO creation is successful on the node, else <code>false</code>.
     */
    boolean createMo(Fdn expectedMoFdn, List<Attribute> attributes);
}
