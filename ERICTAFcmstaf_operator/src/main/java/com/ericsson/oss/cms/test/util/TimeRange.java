/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

/**
 * @author xrajnka
 */
public class TimeRange {

    private long startTime = 0;

    private long timeout = 0;

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     *        the startTime to set
     */
    public void setStartTime(final long testStartTime) {
        this.startTime = testStartTime;
    }

    /**
     * @return the timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout
     *        the timeout to set
     */
    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
}