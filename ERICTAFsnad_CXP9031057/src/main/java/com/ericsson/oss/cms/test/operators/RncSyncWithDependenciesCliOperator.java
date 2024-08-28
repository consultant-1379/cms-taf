/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.UNLIMITED_DEPTH_LEVEL;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.RECOVERED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.FdnConstants.RNC_FUNCTION;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.ADJACENT_CELL;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.INTER_UTRAN_RELATION;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.MAX_TIME_TO_READ_CIF_LOGS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.NODE_RELATION_TYPE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Snad.SNAD_MC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder.Qualifier;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmurran
 */
public class RncSyncWithDependenciesCliOperator implements RncSyncWithDependenciesOperator {

    private SnadCacheCliOperator snadCacheCliOperator;

    @Inject
    private NodeCliOperator nodeCliOperator;

    @Inject
    CIFLogCliOperator cifLogCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<Fdn> getInterelatedRNCNodes(final List<Fdn> rncSynchedNodeList, final SnadCacheCliOperator snadCacheCliOperator, final String moType) {

        List<Fdn> utranRelationList = new ArrayList<Fdn>();

        this.snadCacheCliOperator = snadCacheCliOperator;

        for (int i = 0; i < rncSynchedNodeList.size(); i++) {

            final Map<Fdn, List<Attribute>> utranRelationFirstMap = getUtranRelationMoWithAdjacentCell(rncSynchedNodeList.get(i), moType);

            if (!utranRelationFirstMap.isEmpty()) {

                for (int j = i + 1; j < rncSynchedNodeList.size(); j++) {

                    final Map<Fdn, List<Attribute>> utranRelationSecondMap = getUtranRelationMoWithAdjacentCell(rncSynchedNodeList.get(j), moType);

                    if (!utranRelationSecondMap.isEmpty()) {

                        utranRelationList = getTwoUtranRelations(utranRelationFirstMap, utranRelationSecondMap, rncSynchedNodeList.get(i),
                                rncSynchedNodeList.get(j));

                        if (!utranRelationList.isEmpty()) {
                            return utranRelationList;
                        }
                    }
                }
            }
        }
        logger.debug("No relation found in any of active node, so returning empty list to test class");
        return utranRelationList;
    }

    private List<Fdn> getTwoUtranRelations(
            final Map<Fdn, List<Attribute>> utranRelationFirstMap,
            final Map<Fdn, List<Attribute>> utranRelationSecondMap,
            final Fdn firstNodeMeContext,
            final Fdn secondNodeMeContext) {
        final List<Fdn> utranRelationList = new ArrayList<Fdn>();

        final Fdn firstUtranRelation = getRelation(firstNodeMeContext, utranRelationSecondMap);

        if (firstUtranRelation != null) {
            final Fdn secondUtranRelation = getRelation(secondNodeMeContext, utranRelationFirstMap);

            if (secondUtranRelation != null) {
                utranRelationList.add(firstUtranRelation);
                utranRelationList.add(secondUtranRelation);

                return utranRelationList;
            }
        }

        logger.debug("No relation found in these nodes:\n" + "1 :" + firstNodeMeContext + "\n" + "2 :" + secondNodeMeContext);
        return utranRelationList;
    }

    private Map<Fdn, List<Attribute>> getUtranRelationMoWithAdjacentCell(final Fdn meContextFdn, final String moType) {

        if (isMoInSnadManagedCache(nodeCliOperator.getManagedElementChild(meContextFdn, RNC_FUNCTION))) {
            final Filter filter = SimpleFilterBuilder.builder().type(moType).and().attr(NODE_RELATION_TYPE).equalTo(INTER_UTRAN_RELATION).build();
            return csHandler.getChildMosWithAttribute(meContextFdn, UNLIMITED_DEPTH_LEVEL, filter, ADJACENT_CELL);
        } else {
            logger.debug("This node is not in SnadManaged cache:" + meContextFdn + ", so returning empty map");
        }
        return Collections.emptyMap();
    }

    private Fdn getRelation(final Fdn meContextFdn, final Map<Fdn, List<Attribute>> secondMap) {

        for (final Map.Entry<Fdn, List<Attribute>> secondMapEntry : secondMap.entrySet()) {
            final Fdn adjacentCellFdnMecontext = new Fdn(secondMapEntry.getValue().get(0).getValue()).getMeContext();

            if (meContextFdn.equals(adjacentCellFdnMecontext) && isMoInSnadManagedCache(secondMapEntry.getKey().getParentFdn())) {
                return secondMapEntry.getKey();
            }
        }
        return null;
    }

    private boolean isMoInSnadManagedCache(final Fdn fdn) {
        return snadCacheCliOperator.isInMasterCache(fdn);
    }

    @Override
    public List<CIFLogItem> checkRncNodeRecoveryMsgInCifLog(final Fdn node, final long startTime) {
        final Set<String> nodes = new HashSet<String>();
        nodes.add(node.getFdn());

        final CriteriaBuilder criteria = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.EQUALS, SNAD_MC)
                .withAdditional_info(Qualifier.CONTAINS, RECOVERED)
                .withMutlipleResource(Qualifier.EQUALS, nodes)
                .withTimeRange(startTime, System.currentTimeMillis() + MAX_TIME_TO_READ_CIF_LOGS);

        final List<CIFLogItem> cifLogEntries = cifLogCliOperator.waitForCifLogs(criteria, MAX_TIME_TO_READ_CIF_LOGS);

        return cifLogEntries;
    }

}