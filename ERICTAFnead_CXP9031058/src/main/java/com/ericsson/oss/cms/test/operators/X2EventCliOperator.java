/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CSTestConstants.UNLIMITED_DEPTH_LEVEL;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder.Qualifier;
import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author ecolhar
 */
public class X2EventCliOperator implements X2EventOperator {

    private static final String EXTERNAL = "External";

    @Inject
    CIFLogCliOperator cifLogCliOperator;

    @Inject
    NodeCliOperator nodeCliOperator;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public boolean waitForX2EventCommand(final String mcName, final TimeRange timeRange, final String command, final String additionalInfo) {

        final CriteriaBuilder criteria = buildCriteria(mcName, timeRange, command, additionalInfo);
        final List<CIFLogItem> cifLogEntries = cifLogCliOperator.waitForCifLogs(criteria, timeRange.getTimeout());
        return !cifLogEntries.isEmpty();
    }

    @Override
    public Fdn getInterNodeCellRelation(final Fdn nodeFdn, final String cellRelationType, final String cellReferenceAttrName) {

        final Filter relationFilter = SimpleFilterBuilder.builder().type(cellRelationType).build();
        final Map<Fdn, List<Attribute>> relations = csHandler.getChildMosWithAttribute(nodeFdn, UNLIMITED_DEPTH_LEVEL, relationFilter, cellReferenceAttrName);

        for (final Fdn relationFdn : relations.keySet()) {
            final Fdn neighbourFdn = new Fdn(relations.get(relationFdn).get(0).getValue());
            if (!neighbourFdn.getFdn().isEmpty()) {
                if (neighbourFdn.getType().contains(EXTERNAL)) {
                    return relationFdn;
                }
            }
        }
        return null;
    }

    private CriteriaBuilder buildCriteria(final String mcName, final TimeRange timeRange, final String command, final String additionalInfo) {
        final CriteriaBuilder criteria = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.EQUALS, mcName)
                .withCommand_name(Qualifier.EQUALS, command)
                .withAdditional_info(Qualifier.CONTAINS, additionalInfo)
                .withTimeRange(timeRange.getStartTime(), timeRange.getStartTime() + timeRange.getTimeout());

        return criteria;
    }
}
