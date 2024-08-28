/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.IPADDRESS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.EXCEPTION_FILE_PATTERN;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_LOG_FILE_PATH;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NOTIFICATION_LOG_FILE_PATTERN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xrajnka
 */
@Operator(context = Context.CLI)
public class LogFileCliOperator implements LogFileOperator {

    final CLICommandHelper cliCmdHelper = new CLICommandHelper(HostGroup.getOssmaster());

    final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public boolean notificationExists(final String dateNtime, final Fdn moFdn, final String operation) {

        final String notificationLogRecords = getNotificationLogs(dateNtime);
        final String notificationSearchPattern = "\\s" + operation + "\\(\\d+, " + moFdn;

        final Pattern p = Pattern.compile(notificationSearchPattern);
        final Matcher m = p.matcher(notificationLogRecords);

        return m.find();
    }

    private String getNotificationLogs(final String dateTime) {

        final StringBuilder lineNumberCmd = new StringBuilder();
        lineNumberCmd.append("cd " + NOTIFICATION_LOG_FILE_PATH);
        lineNumberCmd.append("grep -ni ");
        lineNumberCmd.append(dateTime);
        lineNumberCmd.append(" ");
        lineNumberCmd.append(NOTIFICATION_LOG_FILE_PATTERN);
        lineNumberCmd.append(" ");
        lineNumberCmd.append("| tail -1 | cut -d':' -f2");

        final String lineNumber = cliCmdHelper.simpleExec(lineNumberCmd.toString());
        final String logRecordsCmd = "tail " + NOTIFICATION_LOG_FILE_PATH + NOTIFICATION_LOG_FILE_PATTERN + " -n " + lineNumber;

        return cliCmdHelper.simpleExec(logRecordsCmd);
    }

    @Override
    public int getNotificationCount(final String notificationType, final Fdn moFdn, final String... searchStrings) {

        final StringBuilder countNotificationsCmd = new StringBuilder();
        countNotificationsCmd.append("egrep ");
        countNotificationsCmd.append("\"");
        countNotificationsCmd.append(notificationType);
        countNotificationsCmd.append(".*");
        countNotificationsCmd.append(moFdn);
        countNotificationsCmd.append("\"");
        countNotificationsCmd.append(" ");
        countNotificationsCmd.append(NOTIFICATION_LOG_FILE_PATH + NOTIFICATION_LOG_FILE_PATTERN);
        countNotificationsCmd.append(" | ");

        for (final String searchString : searchStrings) {
            countNotificationsCmd.append("grep ");
            countNotificationsCmd.append(searchString);
            countNotificationsCmd.append(" | ");
        }
        countNotificationsCmd.append("wc -l");
        return executeCmd(countNotificationsCmd);
    }

    @Override
    public int getExceptionCount(final String searchString, final Fdn nodeFdn) {

        final StringBuilder countExceptionsCmd = new StringBuilder();
        countExceptionsCmd.append("egrep ");
        countExceptionsCmd.append("\"");
        countExceptionsCmd.append("NEIPADR ==> ");
        countExceptionsCmd.append("\'");
        countExceptionsCmd.append(csHandler.getAttributeValue(nodeFdn, IPADDRESS));
        countExceptionsCmd.append("\' , ");
        countExceptionsCmd.append("REASON ==> ");
        countExceptionsCmd.append("\'");
        countExceptionsCmd.append(searchString);
        countExceptionsCmd.append("\'");
        countExceptionsCmd.append("\"");
        countExceptionsCmd.append(" ");
        countExceptionsCmd.append(NOTIFICATION_LOG_FILE_PATH + EXCEPTION_FILE_PATTERN);
        countExceptionsCmd.append(" | ");
        countExceptionsCmd.append("wc -l");

        return executeCmd(countExceptionsCmd);
    }

    private int executeCmd(final StringBuilder countNotificationsCmd) {
        final CLICommandHelper cliCmdHelper = new CLICommandHelper(HostGroup.getOssmaster());
        final String notificationCount = cliCmdHelper.simpleExec(countNotificationsCmd.toString()).trim();
        return Integer.parseInt(notificationCount);
    }

}
