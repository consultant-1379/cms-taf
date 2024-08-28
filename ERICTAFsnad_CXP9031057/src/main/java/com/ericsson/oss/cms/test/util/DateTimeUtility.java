/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author xindcha
 */
public class DateTimeUtility {

    public static String convertDateTime(final long dateTimeInMilli, final String dateFormat) {
        final DateFormat formatter = new SimpleDateFormat(dateFormat);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTimeInMilli);
        return formatter.format(calendar.getTime());
    }

    public static String testStartMesage(final String testId) {
        final long time = System.currentTimeMillis();
        final String timeString = DateTimeUtility.convertDateTime(time, "dd-MM-YYYY HH:mm:ss");
        return testId + " start time (dd-MM-YYYY HH:mm:ss) :" + timeString;
    }

    public static String testEndMesage(final String testId) {
        final long time = System.currentTimeMillis();
        final String timeString = DateTimeUtility.convertDateTime(time, "dd-MM-YYYY HH:mm:ss");
        return testId + " end time (dd-MM-YYYY HH:mm:ss) :" + timeString;
    }

}
