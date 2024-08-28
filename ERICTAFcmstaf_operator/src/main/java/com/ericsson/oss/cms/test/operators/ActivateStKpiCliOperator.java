package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder.Qualifier;
import com.ericsson.oss.cms.test.util.BCGExportFileParser;
import com.ericsson.oss.cms.test.util.BCGImportFileGenerator;
import com.ericsson.oss.cms.test.util.KpiUtil;
import com.ericsson.oss.cms.test.util.ParsedFileData;
import com.ericsson.oss.taf.cshandler.model.Fdn;

@Operator(context = Context.CLI)
public class ActivateStKpiCliOperator implements ActivateStKpiOperator {

    @Inject
    private CIFLogCliOperator cifLogCliOperator;

    @Inject
    private LogFileCliOperator logFileCliOperator;

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivateStKpiCliOperator.class);

    private final static String DEFAULT_TEMPFILE_PATH = System.getProperty("java.io.tmpdir");

    private final static int MAX_TIMEOUT = 300000;

    private final static String WRAN_PCA = "wran_pca";

    @Override
    public String createDirectoryInTemp(final String dirName) {
        final String tempDir = DEFAULT_TEMPFILE_PATH + dirName;
        final File file = new File(tempDir);
        if (!file.exists()) {
            file.mkdir();
            LOGGER.info("Directory created : " + tempDir);
        }
        return tempDir;
    }

    @Override
    public boolean fileExistsOnLocalPath(final String filePath) {
        final File file = new File(filePath);
        return file.exists() && file.length() > 0;
    }

    @Override
    public void deleteLocalFiles(final String localExportPath, final String localCreatePath, final String localDeletePath) {
        new File(localExportPath).delete();
        new File(localCreatePath).delete();
        new File(localDeletePath).delete();
    }

    @Override
    public boolean createImportFile(
            final String modifier,
            final int totalMOs,
            final String importType,
            final String localExportFilePath,
            final String localImportFilePath) throws IOException, XMLStreamException, FactoryConfigurationError, ParserConfigurationException,
            TransformerException {

        final BCGExportFileParser exportFileParser = new BCGExportFileParser();
        final ParsedFileData parsedFileData = exportFileParser.getParsedFileData(importType, localExportFilePath);

        final BCGImportFileGenerator importFileGenerator = new BCGImportFileGenerator(parsedFileData, importType, modifier, totalMOs);
        return importFileGenerator.writeXML(localExportFilePath, localImportFilePath);

    }

    @Override
    public void convertCreateFileToDeleteFile(final String localCreatePath, final String localDeletePath) throws IOException {
        final Path localCreateFilePath = Paths.get(localCreatePath);
        final Path localDeleteFilePath = Paths.get(localDeletePath);

        try (final BufferedReader bufferedReader = Files.newBufferedReader(localCreateFilePath, Charset.forName("UTF-8"));
                final BufferedWriter bufferedWriter = Files.newBufferedWriter(localDeleteFilePath, Charset.forName("UTF-8"))) {
            LOGGER.info("Buffer reader reading the file :: " + localCreateFilePath);
            while (bufferedReader.ready()) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("modifier=\"create\"")) {
                        bufferedWriter.write(line.replaceAll("create", "delete") + "\n");
                    } else {
                        bufferedWriter.write(line + "\n");
                    }
                    bufferedReader.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CIFLogItem> getActivationCifLogs(final String additionalInfo, final long startTime) {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.EQUALS, WRAN_PCA)
                .withAdditional_info(Qualifier.CONTAINS, additionalInfo)
                .withTimeRange(startTime, System.currentTimeMillis() + MAX_TIMEOUT);
        final List<CIFLogItem> cifLogEntries = cifLogCliOperator.waitForCifLogs(criteriaBuilder, MAX_TIMEOUT);
        if (cifLogEntries.isEmpty()) {
            LOGGER.warn("Activation log not found.");
        }
        return cifLogEntries;
    }

    private void waitFor(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
        }
    }

    @Override
    public int waitForExpectedNotificationCount(
            final String notificationType,
            final Fdn fdn,
            final String moType,
            final int expectedNoOfLogs,
            final long maxTimeOut) {
        int noOfLogs = 0;
        long timeExpired = 0;
        while (noOfLogs < expectedNoOfLogs && timeExpired < maxTimeOut) {
            noOfLogs = logFileCliOperator.getNotificationCount(notificationType, fdn, moType);
            waitFor(5000);
            timeExpired += 5000;
        }
        if (noOfLogs < expectedNoOfLogs) {
            LOGGER.error("No of logs afer activation : " + noOfLogs + "is less than " + expectedNoOfLogs + "and timeout is exceeded");
        }
        return noOfLogs;
    }

    @Override
    public int getTotalDeletedMosDuringActivation(final long startTime) {
        return getTotalMosActivated(startTime, "Delete", 2);
    }

    @Override
    public int getTotalCreatedMosDuringActivation(final long startTime) {
        return getTotalMosActivated(startTime, "Create", 1);
    }

    private int getTotalMosActivated(final long startTime, final String activationType, final int matchGroup) {
        final List<CIFLogItem> cifPlannedAreaMetrics = getPlannedAreaMetrics("Planned Area Metrics", startTime);

        final int totalMosActivated = KpiUtil.getNoOfActivations(cifPlannedAreaMetrics, matchGroup);
        LOGGER.info("Mos activated for " + activationType + " is " + totalMosActivated);

        final int totalMosActivatedAsModified = KpiUtil.getNoOfActivations(cifPlannedAreaMetrics, 3);
        LOGGER.info("Mos activated as Modified during " + activationType + " is " + totalMosActivatedAsModified);

        return totalMosActivated + totalMosActivatedAsModified;
    }

    private List<CIFLogItem> getPlannedAreaMetrics(final String additionalInfo, final long startTime) {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.EQUALS, NEAD_MC)
                .withAdditional_info(Qualifier.CONTAINS, additionalInfo)
                .withTimeRange(startTime, System.currentTimeMillis() + MAX_TIMEOUT);

        final List<CIFLogItem> cifLogEntries = cifLogCliOperator.waitForCifLogs(criteriaBuilder, MAX_TIMEOUT);
        if (cifLogEntries.isEmpty()) {
            LOGGER.warn("Planned Area Metrics log not found.");
        }

        LOGGER.info(cifLogEntries.toString());
        return cifLogEntries;
    }

}
