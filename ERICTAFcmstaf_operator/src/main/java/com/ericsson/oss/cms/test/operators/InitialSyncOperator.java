/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmanvas
 */
public interface InitialSyncOperator {

    /**
     * Method to copy XML file from local server to remote server.
     * 
     * @param XMLFileName
     *        xml file name
     */
    void copyXMLFileToRemote(final String XMLFileName);

    /**
     * Method to copy file from local server to remote server.
     * 
     * @param FileName
     *        file name
     */
    void copyFileToRemote(final String fileName, final String filePath);

    /**
     * Method to delete the node using ARNE.
     * 
     * @param XMLFileName
     *        xml file name
     * @param nodeType
     *        type of the node
     */
    void deleteNode(final String XMLFileName, final String nodeType);

    /**
     * Method to add a node using ARNE
     * 
     * @param xmlFileName
     *        xml file name
     * @param nodeType
     *        type of the node
     */

    void addNode(final String xmlFileName, final String nodeType);

    /**
     * Method to connect to the Remote server and execute the command
     * 
     * @param command
     *        command command
     * @return returns String
     */
    String hostConnect(final String command);

    /**
     * @param scriptName
     * @param remotePath
     */
    void removeFileFromRemote(String scriptName, String remotePath);

    public boolean findAndDeleteMOsFromCS(final Fdn fdn);

}
