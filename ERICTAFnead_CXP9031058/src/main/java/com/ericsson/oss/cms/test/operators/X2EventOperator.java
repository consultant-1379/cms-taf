/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.cms.test.model.attributes.NeDetails;
import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author ecolhar
 */
public interface X2EventOperator {

    /**
     * Waits for the X2 Event specified in command parameter to be logged to CIF logs.
     *
     * @param mcName
     *        The name of the MC loggging the event.
     * @param timeRange
     *        The time range the event is expected to be logged in.
     * @param command
     *        The command logged. I.e. the X2 event.
     * @param additionalInfo
     *        Additional info to narrow the search criteria such as FDN.
     * @return <code>true</code> if the log is found, otherwise <code>false</code>.
     */
    boolean waitForX2EventCommand(String mcName, TimeRange timeRange, String command, String additionalInfo);

    /**
     * Finds an inter-node relation of the passed type from the requested node to a cell on another node.
     *
     * @param nodeFdn
     *        Conventional node name as defined in {@link NeDetails}.
     * @param cellRelationType
     *        The Mo Type of the required relation.
     * @param cellRelAttributeName
     *        The reference attribute to the neighbouring cell.
     * @return Fdn of the inter-node relation.
     */
    Fdn getInterNodeCellRelation(Fdn nodeFdn, String cellRelationType, String cellReferenceAttrName);

}
