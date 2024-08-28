/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xmurran
 */
public interface RncSyncWithDependenciesOperator {

    /**
     * Method Identifies two RNC nodes, those have inter RNC UtranRelation.
     * It is taken care that both RNCs are in same network by checking 'nodeRelationType'
     * and MeContext. It is expected that 'nodeRelationType' equal to "1" and MeContext should
     * not be null.
     * 
     * @param rncSynchedNodeList
     *        List of synched rnc nodes.
     * @param snadCacheCliOperator
     *        holds current snad cache information
     * @param Type
     *        of mo on the node type.
     * @return List contains interrelated RNC nodes.
     */
    List<Fdn> getInterelatedRNCNodes(final List<Fdn> rncSynchedNodeList, final SnadCacheCliOperator snadCacheCliOperator, final String moType);

    /**
     * Method to check node recovery message in cif log.
     * 
     * @param node
     *        Rnc node for which you are interested to check recovery message.
     * @param startTime
     *        start time stored before restarting Snad mc.
     * @return <code>true<code>, if node recovery message exist in cif log
     */
    List<CIFLogItem> checkRncNodeRecoveryMsgInCifLog(final Fdn node, final long startTime);

}
