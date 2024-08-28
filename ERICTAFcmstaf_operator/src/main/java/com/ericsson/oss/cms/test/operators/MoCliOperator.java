/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author egergro
 */
@Operator(context = Context.CLI)
public class MoCliOperator implements MoOperator {

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    @Override
    public Fdn getChildMoFdn(final Fdn moFdn, final String moType) {
        Fdn childMoFdn = null;

        final List<Fdn> childMos = getChildrenMoFdns(moFdn, moType);
        if (childMos.size() > 0) {
            childMoFdn = childMos.get(0);
        }
        return childMoFdn;
    }

    @Override
    public List<Fdn> getChildrenMoFdns(final Fdn moFdn, final String... moType) {
        return getChildrenMoFdnsWithLevel(moFdn, "-1", moType);
    }

    @Override
    public List<Fdn> getChildrenMoFdnsWithLevel(final Fdn moFdn, final String level, final String... moType) {
        final Filter moFilter = SimpleFilterBuilder.builder().type(moType).build();
        return csHandler.getChildMos(moFdn, level, moFilter);
    }

    @Override
    public List<Fdn> getChildrenMoFdns(final List<Fdn> moFdns, final String... moType) {
        final List<Fdn> allMotypeFdns = new ArrayList<Fdn>();

        for (final Fdn fdn : moFdns) {
            allMotypeFdns.addAll(getChildrenMoFdns(fdn, moType));
        }
        return allMotypeFdns;
    }
}
