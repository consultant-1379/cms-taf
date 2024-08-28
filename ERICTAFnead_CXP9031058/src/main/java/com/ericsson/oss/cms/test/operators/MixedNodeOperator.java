/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

/**
 * @author xmanvas
 */
public interface MixedNodeOperator {

    /**
     * Method to copy XML file from local server to remote server.
     * 
     * @param XMLFileName
     *        , xml file name
     */
    void copyXMLFile(final String XMLFileName);

    /**
     * Method to delete the node using ARNE.
     * 
     * @param XMLFileName
     *        , xml file name
     * @param nodeType
     *        , type of the node
     */
    void deleteNode(final String XMLFileName, final String nodeType);

    /**
     * Method to add Mixed node using ARNE.
     * 
     * @param fdn
     *        , node fdn
     * @param xmlCreateFileName
     *        , create xml file
     * @param xmlDeleteFileName
     *        , delete xml file
     * @param fdnProd
     *        , proddesignation fdn
     * @param prodDesignation
     *        , mo type
     * @param nodeType
     *        , node type
     * @return returns true if node added successfully
     */
    boolean addMixedNodeUsingArne(
            final String fdn,
            final String xmlCreateFileName,
            final String xmlDeleteFileName,
            final String fdnProd,
            final String prodDesignation,
            final String nodeType);

    /**
     * Method to check the prodversion of node.
     * 
     * @param fdnProd
     *        , proddesignation fdn
     * @param prodDesig
     *        , mo type
     * @return returns true if prodversion is 3
     */
    boolean checkProdVersion(final String fdnProd, final String prodDesig);
}