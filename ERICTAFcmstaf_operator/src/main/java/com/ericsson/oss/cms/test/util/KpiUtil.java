/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;

/**
 * Utility class for performing common KPI calculations for System Test KPI tests.
 *
 * @author eeieonl
 */
public abstract class KpiUtil {

    /**
     * Calculates and returns the percentage deviation a value is form baseline value
     *
     * @param avgMoSec
     *        value to calculate deviation on from baseline value
     * @param baseline
     *        baseline value
     * @return
     *         percentage deviation value is from baseline
     */
    public static float getActualDeviationPercent(final double avgMoSec, final int baseline) {
        final float deviation = (float) (avgMoSec - baseline) / baseline * 100;
        return Math.round(deviation);
    }

    /**
     * Calculates and returns the percentage deviation a value is form baseline value
     *
     * @param avgMoSec
     *        value to calculate deviation on from baseline value
     * @param baseline
     *        baseline value
     * @return
     *         percentage deviation value is from baseline
     */
    public static double getActualDeviationPercent(final double avgMoSec, final double baseline) {
        final double deviation = (avgMoSec - baseline) / baseline * 100;
        return deviation;
    }

    /**
     * Accepts a baseline value and a percentage, and returns the baseline value less the deviation percentage
     *
     * @param baseline
     *        baseline value to calculate the percentage deviation on
     * @param deviationPercent
     *        percentage to reduce baseline figure by
     * @return
     *         baseline value less deviation percent
     */
    public static int getRateWithDeviation(final int baseline, final int deviationPercent) {
        final float deviationAsFraction = (float) (100 - deviationPercent) / (float) 100;
        return (int) (baseline * deviationAsFraction);
    }

    /**
     * Accepts a baseline value and a percentage, and returns the baseline value less the deviation percentage
     *
     * @param baseline
     *        baseline value to calculate the percentage deviation on
     * @param deviationPercent
     *        percentage to reduce baseline figure by
     * @return
     *         baseline value less deviation percent
     */
    public static double getRateWithDeviation(final double baseLine, final double deviationPercent) {
        final double deviationAsFraction = (100 - deviationPercent) / 100;
        return baseLine * deviationAsFraction;
    }

    /**
     * This method reads a given cif log item for the total time (TT) entry in the log
     *
     * @param cifLogEntry
     *        Cif log data item containing total time (TT) entry
     * @return
     *         Value of the total time (TT) data from the cif log entry, or null if not found
     */
    public static Integer getTotalTimeFromLog(final List<CIFLogItem> cifLogEntry) {

        final String totalTimeDelim = "TT\\s?\\((\\d+)\\)";
        Integer totalTime = 0;
        if (cifLogEntry.size() > 0) {
            totalTime = parseLogData(cifLogEntry.get(0), totalTimeDelim, 1);
        }
        return totalTime;
    }

    /**
     * This method reads a given cif log item for the number of MOs read (R value of CDRWF entry)
     *
     * @param cifLogEntry
     *        Cif log data item containing CDRWF entry
     * @return
     *         Value of the number of MOs read from the log data, or null if not found
     */
    public static Integer getNumberOfMosRead(final List<CIFLogItem> cifLogEntry) {

        final String moReadDelim = "CDRWF\\(\\d+,\\d+,(\\d+),";
        final Integer numberOfMosRead = parseLogData(cifLogEntry.get(0), moReadDelim, 1);
        return numberOfMosRead;
    }

    private static Integer parseLogData(final CIFLogItem logItem, final String logDelim, final int matchGroup) {

        final Pattern p = Pattern.compile(logDelim);
        final Matcher m = p.matcher(logItem.toString());
        Integer matchedData = null;
        if (m.find()) {
            matchedData = Integer.parseInt(m.group(matchGroup));
        }
        return matchedData;
    }

    /**
     * Parse the first CIFLogItem from the list and retrieve the number of MOs altered in a planned area activation.<br />
     * The returned MO count is based on the matchGroup.
     *
     * @param cifPlannedAreaMetrics
     *        List of CIFLogItems, of which only the first log is checked.
     * @param matchGroup
     *        Index indicating which MO count to return.<br/>
     *        MO counts are found after "CDMCPDP" and the index starts from 1.
     * @return The count of MOs.
     */
    public static int getNoOfActivations(final List<CIFLogItem> cifPlannedAreaMetrics, final int matchGroup) {
        final String moReadDelim = "CDMCPDP\\((\\d+),(\\d+),(\\d+),\\d+,\\d+\\)";
        final Integer mosActivated = parseLogData(cifPlannedAreaMetrics.get(0), moReadDelim, matchGroup);
        return mosActivated;
    }

}
