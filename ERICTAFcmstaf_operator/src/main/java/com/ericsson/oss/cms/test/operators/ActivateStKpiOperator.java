package com.ericsson.oss.cms.test.operators;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.taf.cshandler.model.Fdn;

public interface ActivateStKpiOperator {

    /**
     * Creates a directory in the temporary folder at the default path for local
     * machine
     *
     * @param dirName
     *        The name of the directory to be created
     * @return The full path of the temp directory created
     */
    String createDirectoryInTemp(String dirName);

    /**
     * Checks if file exists at local path and has a size exceeding zero
     *
     * @param filePath
     *        The local file path at which file is expected to exist
     * @return <code>true</code> if file exists on file path, otherwise <code>false</code>
     */
    boolean fileExistsOnLocalPath(String filePath);

    /**
     * Generates an import file from the export file
     *
     * @param modifier
     *        Type of BCG modifier to use in the import file
     * @param totalMOs
     *        Number of MOs to include in import file
     * @param importType
     *        Type of MOs to include in import file
     * @param localExportFilePath
     *        The export file to be used when generating the import file.
     * @param localImportFilePath
     *        The path on the local machine where import file will be generated
     * @return <code>true</code> if create of import file successful, otherwise <code>false</code>
     * @throws IOException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws TransformerException
     */

    boolean createImportFile(String modifier, int totalMOs, String importType, String localExportFilePath, String localImportFilePath) throws IOException,
    XMLStreamException, FactoryConfigurationError, ParserConfigurationException, TransformerException;

    /**
     * Converts a create file to a delete file.
     *
     * @param localCreatePath
     *        Path on local machine at which Create file exists
     * @param localDeletePath
     *        Path on local machine where Delete file will be generated
     * @throws IOException
     */
    void convertCreateFileToDeleteFile(String localCreatePath, String localDeletePath) throws IOException;

    /**
     * When a planned Area activation is executed successfully for a plan, PCA will log an Activation log with activation metrics
     * This method queries the CIF logs for activation logs since a specified start time
     *
     * @param additionalInfo
     *        Additional info to search the logs for
     * @param startTime
     *        Time to start searching the CIF log from
     * @return
     *         List of activation logs since start time
     */
    List<CIFLogItem> getActivationCifLogs(String additionalInfo, long startTime);

    /**
     * Read the CIF logs for a completed planned area activation log and parse the count of MOs which were deleted by the planned area
     * activation.
     * 
     * @param startTime
     *        The time at which the activation began.
     * @return The count of deleted MOs.
     */
    int getTotalDeletedMosDuringActivation(long startTime);

    /**
     * Read the CIF logs for a completed planned area activation log and parse the count of MOs which were created by the planned area
     * activation.
     * 
     * @param startTime
     *        The time at which the activation began.
     * @return The count of created MOs.
     */
    int getTotalCreatedMosDuringActivation(long startTime);

    /**
     * Deletes xml files from local machine
     *
     * @param localExportPath
     *        Path on local machine at which export file exists
     * @param localCreatePath
     *        Path on local machine at which Create import file exists
     * @param localDeletePath
     *        Path on local machine at which Delete import file exists
     */
    void deleteLocalFiles(String localExportPath, String localCreatePath, String localDeletePath);

    /**
     * Reads the notification logs and waits for the count of the given type of log to reach the expected number.
     * Times out if the expected number of logs is not received within a specified maximum time.
     *
     * @param notificationType
     *        Type of Notification to be retrieved from log
     * @param fdn
     *        The Fdn for which notification is logged
     * @param moType
     *        The type of mo for which notification is logged
     * @param expectedNoOfLogs
     *        The no of logs expected after an activation
     * @param maxTimeOut
     *        The maximum time to wait for the expected no of logs to be retrieved
     * @return
     */
    int waitForExpectedNotificationCount(final String notificationType, final Fdn fdn, final String moType, final int expectedNoOfLogs, final long maxTimeOut);

}
