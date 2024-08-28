/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

/**
 * @author egergro
 */
public interface BCGToolHandler {

    /**
     * Exports all mos of the given managed object class to a file of the given
     * name. Exports to application default export file path unless otherwise
     * specified.
     *
     * @param fileName
     *        The export xml file name. Should be suffixed with .xml
     * @param moClass
     *        The managed object class of the mos requiring export
     * @return <code>true></code> if export succeeds, <code>false</code> otherwise. Errors are logged to stdout.
     */
    boolean exportByMoClass(String fileName, String moClass);

    /**
     * Copies file from the given application default file path on remote
     * machine to the specified local path
     *
     * @param localFilePath
     *        File path on local machine to which file is to be copied
     * @param fileName
     *        The name of file to be copied
     */
    boolean copyRemoteBcgFileToLocal(String fileName, String localFilePath);

    /**
     * Copies file from given local path to application default file path on
     * remote machine
     *
     * @param fileName
     *        Name of file to be copied
     * @param localPath
     *        File path on local machine from where file is to be copied
     * @return <code>true</code> if copy is successful and file exists at
     *         default path on remote machine
     */
    boolean copyLocalBcgFileToRemote(String fileName, String localPath);

    /**
     * Checks if number of commands of specified type in import file matches
     * number of expected commands
     *
     * @param fileName
     *        The name of the file.
     * @param noOfCommands
     *        The number of commands expected in the file
     * @param commandType
     *        The type of BCG command to match
     * @return <code>true</code> if number of commands of specified type in import file matches
     *         number of expected commands
     */
    boolean verifyTotalImportCommands(String fileName, int noOfCommands, String commandType);

    /**
     * Imports a file to a planned area
     *
     * @param fileName
     *        Name of import file
     * @param planName
     *        Name of planned area
     * @return <code>true</code> if import succeeds, otherwise <code>false</code>
     */
    boolean importToPlannedArea(String fileName, String planName);

    /**
     * Activates a plan
     *
     * @param planName
     * @return <code>true</code> if activation succeeds, otherwise <code>false</code>
     */
    boolean activatePlan(String planName);

    /**
     * Removes a plan
     * 
     * @param planName
     * @return <code>true</code> if remove plan succeeds, otherwise <code>false</code>
     */
    boolean removePlan(String planName);

    /**
     * Deletes the given files from the application default paths on remote machine
     *
     * @param exportFileName
     *        Name of export file to be deleted at default export path
     * @param importCreateFileName
     *        Name of import Create File to be deleted at default import path
     * @param importDeleteFileName
     *        Name of import Delete File to be deleted at default import path
     */
    void deleteRemoteBCGFiles(String exportFileName, String importCreateFileName, String importDeleteFileName);
}
