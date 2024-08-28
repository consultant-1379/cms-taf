/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MO_NAME_PREFIX;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.cif.logs.domain.CIFLogItem;
import com.ericsson.oss.cif.logs.domain.CIFLogType;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder;
import com.ericsson.oss.cif.logs.domain.CriteriaBuilder.Qualifier;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author eeimgrr
 */

public class NeadRetryActivationCliOperator implements NeadRetryActivationOperator {

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Inject
    CIFLogCliOperator cifLogCliOperator;

    @Override
    public List<CIFLogItem> getNeadCifLogsPlanActivation(final List<Fdn> fdns, final long startActivationTime, final String planName, final CIFLogType logType) {

        String additionalInfo = null;
        final List<CIFLogItem> cifLogEntries = new ArrayList<CIFLogItem>();

        for (final Fdn anFdn : fdns) {
            if (logType.compareTo(CIFLogType.COMMAND_LOG) == 0) {
                additionalInfo = "Plan Name = " + planName + "Name: " + anFdn + " Value: SUCCESS";
            } else {
                additionalInfo = "Planned Area Metrics, MibName(" + anFdn + ") Plan Name(" + planName + ")";
            }
            final CriteriaBuilder criteriaBuilder = new CriteriaBuilder("CMS")
                    .withApplication_name(Qualifier.EQUALS, "cms_nead_seg")
                    .withAdditional_info(Qualifier.CONTAINS, additionalInfo)
                    .withTimeRange(startActivationTime, System.currentTimeMillis());

            criteriaBuilder.setLog_type(logType);
            cifLogEntries.addAll(cifLogCliOperator.getCifLogs(criteriaBuilder));

        }
        return cifLogEntries;

    }

    @Override
    public List<Fdn> getGeneratedProxyMos(final String moType, final String attributeName, final String value) {
        final Filter filter = SimpleFilterBuilder.builder().attr(attributeName).equalTo(MO_NAME_PREFIX + value).build();
        return csHandler.getByType(moType, filter);
    }
}
