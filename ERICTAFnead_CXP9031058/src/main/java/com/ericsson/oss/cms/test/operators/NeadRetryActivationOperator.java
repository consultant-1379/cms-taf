/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CIFLogType;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author eeimgrr
 */
public interface NeadRetryActivationOperator {

    /**
     * Retrieves the NEAD CIF logs for a plan activation
     * 
     * @param fdns
     *        A list of {@link Fdn}
     * @param startTime
     *        The start time of log retrieval
     * @param planName
     *        The name of the plan area.
     * @param logType
     *        The type of CIF log to retrieve
     * @return
     */
    List<CIFLogItem> getNeadCifLogsPlanActivation(List<Fdn> fdns, long startActivationTime, String planName, CIFLogType logType);

    /**
     * Gets a list of Fdns of a given type that contain the attribute name and value supplied.
     * 
     * @param moType
     *        Type of MO
     * @param attributeName
     *        The name of the attribute
     * @param value
     *        The attribute value
     * @return
     *         The {@link List} of proxy MO's found
     */
    List<Fdn> getGeneratedProxyMos(String moType, String attributeName, String value);

}
