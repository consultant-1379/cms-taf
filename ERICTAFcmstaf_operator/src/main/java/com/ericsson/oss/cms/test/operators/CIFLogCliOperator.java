/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.Filter;
import com.ericsson.oss.taf.cifloghandler.service.CIFLogConsumer;
import com.ericsson.oss.taf.cifloghandler.service.CIFLogService;
import com.ericsson.oss.taf.cifloghandler.service.CIFLogServiceFactory;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xrajnka
 */
public class CIFLogCliOperator implements CIFLogOperator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<CIFLogItem> waitForCifLogs(final CriteriaBuilder criteria, final long maxTime) {

        List<CIFLogItem> cifLogEntries = null;

        final long estimatedEndTime = System.currentTimeMillis() + maxTime;
        final CIFLogService cifLogService = CIFLogServiceFactory.getInstance(HostGroup.getOssmaster());
        while (System.currentTimeMillis() <= estimatedEndTime) {
            cifLogEntries = cifLogService.findLogsByCriteria(criteria);
            if (cifLogEntries.isEmpty()) {
                waitFor(5000);
            } else {
                break;
            }
        }
        if (cifLogEntries.isEmpty()) {
            logger.warn("Timeout of " + maxTime + " milliseconds exceeded while waiting for CIF logs with additional info : " + criteria.getAdditional_info());
        }
        cifLogService.dispose();
        return cifLogEntries;
    }

    private void waitFor(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
        }
    }

    @Override
    public List<CIFLogItem> waitForExpectedMessages(final long maxWait, final String... logMessages) {
        final List<String> messages = new ArrayList<String>();
        messages.addAll(Arrays.asList(logMessages));
        final CIFLogService cifLogService = CIFLogServiceFactory.getInstance(HostGroup.getOssmaster());
        final CIFLogConsumer cifLogConsumer = cifLogService.createConsumer(messages);
        final List<CIFLogItem> logItems = cifLogConsumer.getVerifyBucket(maxWait);
        cifLogService.dispose();
        return logItems;
    }

    @Override
    public List<CIFLogItem> getCifLogs(final CriteriaBuilder criteriaBuilder) {
        final CIFLogService cifLogsService = CIFLogServiceFactory.getInstance(HostGroup.getOssmaster());
        final List<CIFLogItem> logs = cifLogsService.findLogsByCriteria(criteriaBuilder);
        cifLogsService.dispose();
        return logs;
    }

    @Override
    public List<CIFLogItem> getCifLogs(final Filter filter) {
        final CIFLogService cifLogsService = CIFLogServiceFactory.getInstance(HostGroup.getOssmaster());
        final List<CIFLogItem> logs = cifLogsService.getLogs(filter);
        cifLogsService.dispose();
        return logs;
    }

}
