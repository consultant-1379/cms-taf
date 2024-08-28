/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author xrajnka
 */
public class DateTimeUtility {

    /**
     * Converts the given date and time to given format.
     * 
     * @param dateTimeInMilli
     *        Date and time in milliseconds
     * @param dateFormat
     *        Date and Time format. e.g., HH:SS or DD:MM:YY:HH:SS
     * @return
     *         Converted date in the given format
     */
    public static String convertDateTime(final long dateTimeInMilli, final String dateFormat) {
        final DateFormat formatter = new SimpleDateFormat(dateFormat);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTimeInMilli);
        return formatter.format(calendar.getTime());
    }
}
