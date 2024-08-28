/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.Filter;

/**
 * @author xrajnka
 */
public interface CIFLogOperator {

    /**
     * Queries the CIF Log for the supplied criteria until CIF log entry is returned or timeout is expired.
     * 
     * @param criteria
     *        Criteria to be used to query CIF logs.
     * @param maxTime
     *        The maximum time(in milliseconds) to wait for reading CIF logs.
     * @return
     *         The list of expected CIF log messages that arrived
     */
    List<CIFLogItem> waitForCifLogs(final CriteriaBuilder criteria, final long maxTime);

    /**
     * Waits for expected Log message(s) in CIF logs
     * 
     * @param logMessage
     *        The expected CIF log message(s) to wait for
     * @param maxWait
     *        The max time to wait for the log in seconds
     * @return
     *         The list of expected CIF log messages that arrived
     */

    List<CIFLogItem> waitForExpectedMessages(final long maxWait, final String... logMessages);

    /**
     * Runs a query on CIF log to get a CIF log message which matches provided criteria
     * 
     * @param criteriaBuilder
     *        The criteria on which the query will be run
     * @return
     *         The list of CIF log messages which matched the provided criteria
     */
    List<CIFLogItem> getCifLogs(final CriteriaBuilder criteriaBuilder);

    /**
     * Runs a query on CIF log to get a CIF log message which matches provided filter
     * 
     * @param filter
     *        The filter on which the query will be run
     * @return
     *         The list of CIF log messages which matched the provided filter
     */
    List<CIFLogItem> getCifLogs(Filter filter);
}
