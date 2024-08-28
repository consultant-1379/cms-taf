/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xrajnka
 */
public interface NodeCifLogOperator {

    /**
     * Checks for the node message in CIF logs until CIF log entry is returned or timeout is expired.
     * 
     * @param mcName
     *        Name of the MC
     * @param node
     *        Fdn of the disconnected node.
     * @param timeRange
     *        timeRange object contains start time and timeout values.
     * @param additionalInfo
     *        The string to search AdditionalInfo field for in the CIF log entry.
     * @return
     *         <code>true</code> if CIF logs contain the additional info, otherwise <code>false</code>.
     */
    boolean waitForNodeAction(final String mcName, TimeRange timeRange, Fdn node, final String additionalInfo);

}
