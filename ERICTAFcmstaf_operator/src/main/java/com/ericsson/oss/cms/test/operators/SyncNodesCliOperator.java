package com.ericsson.oss.cms.test.operators;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MC;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.FieldEnum;
import com.ericsson.oss.cif.logs.domain.Filter;
import com.ericsson.oss.cif.logs.domain.OperatorEnum;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = Context.CLI)
public class SyncNodesCliOperator implements SyncNodesOperator {

    @Inject
    private CIFLogCliOperator cifLogCliOperator;

    private static final int SYNCHRONIZED = 3;

    private static final int UN_SYNCHRONIZED = 4;

    private static final String MIRROR_SYNCH_STATUS = "mirrorMIBsynchStatus";

    private static final int LOGS_PER_NODE_SYNC = 3;

    private static final String SYNC_STATUS_INFO = "has Synch Status";

    private static final String SYNC_PROGRESS_INFO = "FULL SYNC STOPPED";

    private static final String DELTA_SYNC_INFO = "DELTA SYNC STOPPED";

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void startSyncOnNodes(final List<Fdn> fdns) {
        logger.debug("Starting a sync on " + fdns.size() + " node(s)");
        for (final Fdn fdn : fdns) {
            logger.debug("Starting a sync on " + fdn);
            csHandler.adjust(fdn);
        }
    }

    @Override
    public List<Fdn> checkForFailedNodes(final List<Fdn> fdns, final int timeout) {
        final List<Fdn> failedNodes = new ArrayList<Fdn>();
        for (final Fdn fdn : fdns) {
            if (!checkSyncStatus(fdn, timeout)) {
                logger.warn("The Node " + fdn + " failed to sync.");
                failedNodes.add(fdn);
            }
        }
        return failedNodes;
    }

    private boolean checkSyncStatus(final Fdn fdn, final int timeout) {
        final int numOfSleeps = 100;
        final int sleepDuration = timeout * 1000 / numOfSleeps;

        int sleepCounter = 0;
        while (sleepCounter++ <= numOfSleeps) {
            final String value = csHandler.getAttributeValue(fdn, MIRROR_SYNCH_STATUS);
            switch (Integer.parseInt(value)) {
                case SYNCHRONIZED:
                    return true;
                case UN_SYNCHRONIZED:
                    return false;
            }

            sleep(sleepDuration);

        }
        logger.warn("Timeout of " + timeout + " seconds exceeded while waiting for Node to sync");
        return false;
    }

    @Override
    public List<CIFLogItem> getSyncStatusCifLogEntries(final List<Fdn> fdns, final long startTime) {
        return getCifLogsWithAdditionalInfo(fdns, SYNC_STATUS_INFO, startTime);
    }

    @Override
    public List<CIFLogItem> getSyncStatusCifLogEntriesWithRetry(final List<Fdn> fdns, final long startTime, final int numLogs) {
        List<CIFLogItem> cifLogs = getCifLogsWithAdditionalInfo(fdns, SYNC_STATUS_INFO, startTime);
        if (cifLogs.size() < numLogs) {
            sleep(5000);
            cifLogs = getCifLogsWithAdditionalInfo(fdns, SYNC_STATUS_INFO, startTime);
        }
        return cifLogs;
    }

    @Override
    public List<CIFLogItem> getSyncInfoCifLogEntries(final List<Fdn> fdns, final long startTime) {
        return getCifLogsWithAdditionalInfo(fdns, SYNC_PROGRESS_INFO, startTime);
    }

    @Override
    public List<CIFLogItem> getDeltaSyncCifLogEntry(final List<Fdn> fdns, final long startTime) {
        return getCifLogsWithAdditionalInfo(fdns, DELTA_SYNC_INFO, startTime);
    }

    private List<CIFLogItem> getCifLogsWithAdditionalInfo(final List<Fdn> fdns, final String additionalInfo, final long startTime) {

        final Set<Object> nodes = new HashSet<Object>(convertFdnListToStringSet(fdns));
        final Set<Object> timeInterval = new HashSet<Object>();
        timeInterval.add(startTime);
        timeInterval.add(System.currentTimeMillis());

        final Filter filter = new Filter();
        filter.put(FieldEnum.Application, OperatorEnum.IS, NEAD_MC);
        filter.put(FieldEnum.Resource, OperatorEnum.CONTAINS, nodes);
        filter.put(FieldEnum.DateTime, OperatorEnum.BETWEEN, timeInterval);
        filter.put(FieldEnum.AddInfo, OperatorEnum.CONTAINS, additionalInfo);

        return cifLogCliOperator.getCifLogs(filter);
    }

    public List<CIFLogItem> getSnadCifLogsWithAdditionalInfo(final Fdn fdns, final String additionalInfo, final long startTime) {
        final Set<String> fdnValue = new HashSet<>();
        fdnValue.add(fdns.getFdn());
        final Set<Object> fdn = new HashSet<Object>(fdnValue);
        final Set<Object> timeInterval = new HashSet<Object>();
        timeInterval.add(startTime);
        timeInterval.add(System.currentTimeMillis());

        final Filter filter = new Filter();
        filter.put(FieldEnum.Application, OperatorEnum.IS, SNAD_MC);
        filter.put(FieldEnum.Resource, OperatorEnum.CONTAINS, fdn);
        filter.put(FieldEnum.DateTime, OperatorEnum.BETWEEN, timeInterval);
        filter.put(FieldEnum.AddInfo, OperatorEnum.CONTAINS, additionalInfo);

        return cifLogCliOperator.getCifLogs(filter);
    }

    private Set<String> convertFdnListToStringSet(final List<Fdn> fdns) {
        final Set<String> nodes = new HashSet<>();
        for (final Fdn fdn : fdns) {
            nodes.add(fdn.getFdn());
        }
        return nodes;
    }

    @Override
    public <T> void printCollection(final Collection<T> collection) {
        logger.info("<COLLECTION size=\"%s\">", collection.size());
        for (final T element : collection) {
            logger.info(element.toString());
        }
        logger.info("</COLLECTION>");
    }

    private void sleep(final long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (final InterruptedException e) {
            logger.warn("Sleep was interrupted");
        }
    }

    @Override
    public int getExpectedNumCifLogs(final int numberOfNodes) {
        return numberOfNodes * LOGS_PER_NODE_SYNC;
    }

    @Override
    public List<Fdn> checkForSuccessNodes(final List<Fdn> fdns, final int timeout) {
        final List<Fdn> successNodes = new ArrayList<Fdn>();
        for (final Fdn fdn : fdns) {
            if (!checkSyncStatus(fdn, timeout)) {
                logger.debug("The Node " + fdn + " success to sync.");
                successNodes.add(fdn);
            }
        }
        return successNodes;
    }

    public List<Fdn> checkForSuccessCPPNodes(final List<Fdn> fdns, final int timeout) {
        final List<Fdn> successNodes = new ArrayList<Fdn>();
        for (final Fdn fdn : fdns) {
            if (checkSyncStatus(fdn, timeout)) {
                logger.debug("The Node " + fdn + " success to sync.");
                successNodes.add(fdn);
            }
        }
        return successNodes;
    }

}
