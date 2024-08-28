/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.oss.cms.test.operators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmanvas
 */
public class NetworkFullSyncCliOperator implements NetworkFullSyncOperator {

    private final static Logger logger = Logger.getLogger(RdsNodeCliOperator.class);

    final CLICommandHelper cliCmdHelper = new CLICommandHelper(HostGroup.getOssmaster());

    private final static String totalNodes = "TOTAL_NODES";

    private final static String SCRIPT_COMMAND = "sh";

    private final static String fileName = "neadStatus.log.yang";

    private final static String filePath = "/var/opt/ericsson/nms_umts_cms_nead_seg/";

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    public String getTime(final String time) {

        final String[] eTime = time.split(" ");
        final String timeVal = eTime[5].trim();
        logger.debug(timeVal);
        return timeVal;

    }

    @Override
    public int readTotalNode() {

        final StringBuilder toatlNodeCmd = new StringBuilder();

        toatlNodeCmd.append("grep -i ");
        toatlNodeCmd.append(totalNodes);
        toatlNodeCmd.append(" ");
        toatlNodeCmd.append(filePath + fileName);
        toatlNodeCmd.append(" ");
        toatlNodeCmd.append("| sort | uniq");
        logger.info(toatlNodeCmd);

        final String totalNode = cliCmdHelper.simpleExec(toatlNodeCmd.toString());
        logger.info(totalNode);

        final String[] noOfNode = totalNode.split("=");
        final String nNode = noOfNode[1].trim();
        logger.info(Integer.valueOf(nNode));
        return Integer.valueOf(nNode);

        // return totalNode;
    }

    @Override
    public void SyncNodes(final String scriptName) {
        logger.info("Waiting for the Script Execution to be completed........");
        final String COMMAND = SCRIPT_COMMAND + " " + scriptName;
        final String testValue = initialSyncCliOperator.hostConnect(COMMAND);
        logger.info(testValue);

    }

    @Override
    public String readSyncNode(final int totalNode) {

        final StringBuilder syncNodeCmd = new StringBuilder();

        syncNodeCmd.append("perl");
        syncNodeCmd.append(" ");
        syncNodeCmd.append("-ne");
        syncNodeCmd.append(" ");
        syncNodeCmd.append("'BEGIN");
        syncNodeCmd.append("{ $/ = ");
        syncNodeCmd.append("\"");
        syncNodeCmd.append("\"");
        syncNodeCmd.append(" }");
        syncNodeCmd.append(" ");
        syncNodeCmd.append("print if ");
        syncNodeCmd.append("/SYNCED_NODES ");
        syncNodeCmd.append("= ");
        syncNodeCmd.append(totalNode);
        syncNodeCmd.append("/'");
        syncNodeCmd.append(" ");
        syncNodeCmd.append(filePath + fileName);

        syncNodeCmd.append(" ");
        syncNodeCmd.append("| head -1");
        logger.debug("Command....");

        logger.debug(syncNodeCmd);

        final String syncNodes = cliCmdHelper.simpleExec(syncNodeCmd.toString());
        if (!syncNodes.isEmpty()) {

            final String endTime = getTime(syncNodes);

            return endTime;
        }
        return "";

    }

    @Override
    public String readDumpStartTime() {

        final StringBuilder startTimeCmd = new StringBuilder();
        startTimeCmd.append("grep -i ");
        startTimeCmd.append("\"DUMP START\"");
        startTimeCmd.append(" ");
        startTimeCmd.append(filePath + fileName);
        startTimeCmd.append(" ");
        startTimeCmd.append("| head -1");
        logger.debug(startTimeCmd);
        String startTime = cliCmdHelper.simpleExec(startTimeCmd.toString());
        startTime = getTime(startTime);
        logger.info(startTime);

        return startTime;

    }

    public String calculateAverageOfTime(final String timeInHHmmss) {
        final String[] split = timeInHHmmss.split(" ");
        long seconds = 0;
        for (final String timestr : split) {
            final String[] hhmmss = timestr.split(":");
            seconds += Integer.valueOf(hhmmss[0]) * 60 * 60;
            seconds += Integer.valueOf(hhmmss[1]) * 60;
            seconds += Integer.valueOf(hhmmss[2]);
        }
        seconds /= split.length;
        final long hh = seconds / 60 / 60;
        final long mm = seconds / 60 % 60;
        final long ss = seconds % 60;
        logger.info("Average Time taken for full sync :");
        logger.info(String.format("%02d:%02d:%02d", hh, mm, ss));
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    public String timeDiff(final String startTime, final String endTime) {

        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        Date d1, d2;
        long diff = 0;
        try {
            d1 = format.parse(startTime);
            d2 = format.parse(endTime);
            diff = d2.getTime() - d1.getTime();
        } catch (final ParseException e) {
        }

        final long diffSeconds = diff / 1000 % 60;
        final long diffMinutes = diff / (60 * 1000) % 60;
        final long diffHours = diff / (60 * 60 * 1000) % 24;
        logger.info("Time taken for one sync:");
        logger.info(String.format("%02d:%02d:%02d", diffHours, diffMinutes, diffSeconds));
        return String.format("%02d:%02d:%02d", diffHours, diffMinutes, diffSeconds);

    }

}