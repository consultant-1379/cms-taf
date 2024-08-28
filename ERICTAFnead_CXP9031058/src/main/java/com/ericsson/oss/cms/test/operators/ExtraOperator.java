/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmagkum
 */
public interface ExtraOperator {

    /**
     * Method to add node using ARNE.
     * 
     * @param fdn
     *        node fdn
     * @param xmlCreateFileName
     *        create xml file
     * @param xmlDeleteFileName
     *        delete xml file
     * @param nodeType
     *        node type
     * @return returns true if node added successfully
     */
    boolean addNodeUsingArne(final String fdn, final String xmlCreateFileName, final String xmlDeleteFileName, final String nodeType);

    /**
     * Method to add UtranCell
     * 
     * @param moFdn
     *        UC fdn
     * @param atrributes
     *        attributesNameList
     * @param attributeValues
     *        attributeValuesList
     * @return returns true if UtraCellAdded successfully
     */

    boolean addUtranCell(final Fdn moFdn, final String[] attributes, final String[] attributeValues);
}