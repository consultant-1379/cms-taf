/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.CSAttribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xindcha
 */
public class SrvccCapabilityCliOperator {

    @Inject
    private SnadApiOperator snadApiOperator;

    private static final String arfcnValueUtranDl = "arfcnValueUtranDl";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Fdn getRequiredProxy(final List<Fdn> proxies, final String moName) {

        for (final Fdn proxyMo : proxies) {
            if (proxyMo.toString().contains(moName)) {
                return proxyMo;
            }
        }

        return null;
    }

    public Fdn createMaster(final Fdn parent, final String attributeValue, final CSHandler cs) {

        final Fdn ExternalUtranFreq = snadApiOperator.getMoFdnWithAttributeValue("ExternalUtranFreq", arfcnValueUtranDl, attributeValue);

        if (ExternalUtranFreq == null) {
            final String ExternalUtranFreqId = "TAF_test_" + attributeValue;
            final Fdn masterFdn = parent.newChild("ExternalUtranFreq", ExternalUtranFreqId);
            final List<Attribute> attributes = new ArrayList<Attribute>();
            attributes.add(new CSAttribute(arfcnValueUtranDl, attributeValue));
            assertThat("Failed to create " + masterFdn, cs.createMo(masterFdn, attributes), is(true));
            logger.info("The MO is successfully created");
            return masterFdn;
        }
        return ExternalUtranFreq;

    }

    public Fdn createFreqRelation(final Fdn parent, final String moId, final Fdn master, final CSHandler cs) {
        final Fdn relationFdn = parent.newChild("UtranFreqRelation", moId);
        final List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new CSAttribute("adjacentFreq", master.getFdn()));
        assertThat("Failed to create " + attributes, cs.createMo(relationFdn, attributes), is(true));
        logger.info("The relation is created successfully");
        return relationFdn;
    }

    public Fdn createCellRelation(final Fdn utranFreqRelation, final String moId, final Fdn masterCellFdn, final CSHandler cs) {
        final Fdn utranCellRelFdn = utranFreqRelation.newChild("UtranCellRelation", moId);
        final List<Attribute> utranCellAttrs = new ArrayList<Attribute>();
        final CSAttribute att = new CSAttribute("adjacentCell", masterCellFdn.getFdn());
        utranCellAttrs.add(att);
        assertThat("Failed to create '" + utranCellRelFdn, cs.createMo(utranCellRelFdn, utranCellAttrs), is(true));
        logger.info("The UtranCellRelation is created successfully");
        return utranCellRelFdn;
    }

}
