/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author emacraj
 */
public interface NodeOverFlowNotificationOperator {

    /**
     * Generates node over flow notifications by executing command - sendcsnotif:type=OverflowType;
     * 
     * @param activeNodeFdn
     *        Node to be used for generating over flow notifications.
     * @return
     *         <code>true</code> if over flow command is successful on the node, else <code>false</code>.
     */
    boolean generateNodeOverflow(Fdn activeNodeFdn);

}
