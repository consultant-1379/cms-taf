/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

/**
 * @author xmanvas
 */
public interface CppVersionOperator {

    /**
     * Method to add cpp version nodes using ARNE.
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
    boolean addCppVersionNodeUsingArne(final String fdn, final String xmlCreateFileName, final String xmlDeleteFileName, final String nodeType);

    /**
     * Method to check the CPPversion of node.
     * 
     * @param fdnProd
     *        CPPVERSION fdn
     * @param cppVersion
     *        mo type
     * @param cppVersionValue
     *        mo value
     * @return returns true if prodData is same as in the node
     */
    boolean checkCppVersion(final String fdnProd, final String cppVersion, final String cppVersionValue);
}