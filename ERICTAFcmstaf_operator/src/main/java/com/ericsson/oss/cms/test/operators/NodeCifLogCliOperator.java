/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder.Qualifier;
import com.ericsson.oss.cms.test.util.TimeRange;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xrajnka
 */
public class NodeCifLogCliOperator implements NodeCifLogOperator {

    @Inject
    CIFLogCliOperator cifLogCliOperator;

    @Override
    public boolean waitForNodeAction(final String mcName, final TimeRange timeRange, final Fdn node, final String additionalInfo) {
        final Set<String> nodes = new HashSet<>();
        nodes.add(node.getFdn());

        final CriteriaBuilder criteria = buildCriteria(mcName, timeRange, additionalInfo, nodes);
        final List<CIFLogItem> cifLogEntries = cifLogCliOperator.waitForCifLogs(criteria, timeRange.getTimeout());
        return !cifLogEntries.isEmpty();
    }

    private CriteriaBuilder buildCriteria(final String mcName, final TimeRange timeRange, final String additionalInfo, final Set<String> nodes) {
        final CriteriaBuilder criteria = new CriteriaBuilder("CMS")
                .withApplication_name(Qualifier.EQUALS, mcName)
                .withAdditional_info(Qualifier.CONTAINS, additionalInfo)
                .withMutlipleResource(Qualifier.EQUALS, nodes)
                .withTimeRange(timeRange.getStartTime(), timeRange.getStartTime() + timeRange.getTimeout());
        return criteria;
    }

}
