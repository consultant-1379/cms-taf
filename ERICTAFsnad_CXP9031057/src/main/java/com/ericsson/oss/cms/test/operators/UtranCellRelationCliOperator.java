/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.FilterBuilder;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.CSAttribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;

/**
 * @author xindcha
 */

public class UtranCellRelationCliOperator {

    @Inject
    private SnadApiOperator snadOperator;

    private static final String arfcnValueUtranDl = "arfcnValueUtranDl";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String masterMoType() {
        return "ExternalUtranFreq";
    }

    private Integer getAvailableArfcnValueUtranDl() {
        for (Integer arfcnValueUtranDlValue = 16383; arfcnValueUtranDlValue > -1; arfcnValueUtranDlValue--) {
            final Fdn fdn = snadOperator.getMoFdnWithAttributeValue("ExternalUtranFreq", arfcnValueUtranDl, arfcnValueUtranDlValue.toString());
            if (fdn != null) {
                logger.info("arfcnValueUtranDl : " + arfcnValueUtranDlValue + " already used");
            } else {
                logger.info("Using arfcnValueUtranDl : " + arfcnValueUtranDlValue);
                return arfcnValueUtranDlValue;
            }
        }
        return -1;
    }

    protected List<Attribute> masterMoAttributes(final Fdn parent, final String moId, final CSHandler cs) {
        final List<Attribute> attributes = new ArrayList<Attribute>();

        final Integer arfcnValueUtranDlValue = getAvailableArfcnValueUtranDl();
        assertThat("Unable to find a free value for arfcn thus unable to create master UtranFreq under '" + parent + "'", arfcnValueUtranDlValue,
                not(equalTo(-1)));

        attributes.add(new CSAttribute(arfcnValueUtranDl, arfcnValueUtranDlValue.toString()));

        return attributes;
    }

    public <T> String toString(final List<T> list) {
        if (list.size() == 0) {
            return "[]";
        }

        final StringBuilder str = new StringBuilder('[');
        for (int i = 0; i + 1 < list.size(); ++i) {
            str.append(list.get(i)).append(", ");
        }
        return str.append(list.get(list.size() - 1)).append(']').toString();
    }

    public Fdn createMaster(final Fdn parent, final String moId, final CSHandler cs) {

        final List<Attribute> masterAttributes = masterMoAttributes(parent, moId, cs);
        final String moid = moId + "_" + masterAttributes.get(0).getValue().toString();
        final Fdn masterFdn = parent.newChild(masterMoType(), moid);
        final String fdnAndAttrs = fdnAndAttributesString(masterFdn, masterAttributes);
        logger.info("Creating " + fdnAndAttrs);
        assertThat("Failed to create " + fdnAndAttrs, cs.createMo(masterFdn, masterAttributes), is(true));
        logger.info("The Master ExternalUtranFreq is created successfully");
        return masterFdn;
    }

    public String fdnAndAttributesString(final Fdn fdn, final List<Attribute> attributes) {
        return fdn + " with attributes : " + toString(attributes);
    }

    public Fdn createMaster(final Fdn parent, final String moId, final Fdn masterExternalUtranFreqFdn, final CSHandler cs, final String type) {

        final List<Attribute> masterUtranCellAtts = new ArrayList<Attribute>();

        if (type.equals("ExternalUtranCell")) {
            masterUtranCellAtts.add(new CSAttribute("uarfcnDl", cs.getAttributeValue(masterExternalUtranFreqFdn, "arfcnValueUtranDl")));
            masterUtranCellAtts.add(new CSAttribute("primaryScramblingCode", "1"));
            masterUtranCellAtts.add(new CSAttribute("uarfcnUl", "1"));
            masterUtranCellAtts.add(new CSAttribute("lac", "1"));
            masterUtranCellAtts.add(new CSAttribute("rac", "1"));
        }
        final String moid = moId + "_" + masterUtranCellAtts.get(0).getValue();
        final Fdn masterUtranCellFdn = parent.newChild(type, moid);
        final List<Fdn> externalUtranPlmns = cs.getByType("ExternalUtranPlmn");
        final String mnc = cs.getAttributeValue(externalUtranPlmns.get(0), "mnc");
        final String mcc = cs.getAttributeValue(externalUtranPlmns.get(0), "mcc");
        final String mncLength = cs.getAttributeValue(externalUtranPlmns.get(0), "mncLength");

        // The parentSystem attribute needs to be set even though it is not a mandatory attribute
        masterUtranCellAtts.add(new CSAttribute("parentSystem", externalUtranPlmns.get(0).getFdn()));
        masterUtranCellAtts.add(new CSAttribute("mnc", mnc));
        masterUtranCellAtts.add(new CSAttribute("mcc", mcc));
        masterUtranCellAtts.add(new CSAttribute("mncLength", mncLength));
        final String rncId = "1";
        final String cId = "1";
        populateRncIdAndCIdAtts(cs, masterUtranCellAtts, mnc, mcc, mncLength, rncId, cId);

        assertThat("Failed to create '" + masterUtranCellFdn + "' with attributes " + toString(masterUtranCellAtts),
                cs.createMo(masterUtranCellFdn, masterUtranCellAtts), is(true));
        logger.info("Subnetwork Master ExternalUtranCell is created successfully");
        return masterUtranCellFdn;
    }

    private void populateRncIdAndCIdAtts(
            final CSHandler cs,
            final List<Attribute> masterUtranCellAtts,
            final String mnc,
            final String mcc,
            final String mncLength,
            String rncId,
            String cId) {

        final FilterBuilder filter = SimpleFilterBuilder
                .builder()
                .attr("mcc")
                .equalTo(mcc)
                .and()
                .attr("mnc")
                .equalTo(mnc)
                .and()
                .attr("mncLength")
                .equalTo(mncLength);
        final Filter moFilter = filter.build();
        final List<Fdn> listOfRncFunctions = cs.getByType("RncFunction", moFilter);
        if (listOfRncFunctions.isEmpty()) {
            masterUtranCellAtts.add(new CSAttribute("rncId", rncId));
            masterUtranCellAtts.add(new CSAttribute("cId", cId));
            return;
        } else {
            rncId = findAvailableAttValue(cs, listOfRncFunctions, "rncId", 4095);
        }
        final FilterBuilder filter2 = SimpleFilterBuilder.builder();
        final Filter moFilter2 = filter2.group(moFilter).and().attr("rncId").equalTo(rncId).build();
        List<Fdn> listOfExternalUtranCells;

        listOfExternalUtranCells = cs.getByType("ExternalUtranCell", moFilter2);

        if (listOfExternalUtranCells.isEmpty()) {
            masterUtranCellAtts.add(new CSAttribute("rncId", rncId));
            masterUtranCellAtts.add(new CSAttribute("cId", cId));
            return;
        }
        cId = findAvailableAttValue(cs, listOfExternalUtranCells, "cId", 65535);
        masterUtranCellAtts.add(new CSAttribute("rncId", rncId));
        masterUtranCellAtts.add(new CSAttribute("cId", cId));
    }

    /*
     * @param cs
     * @param fdns
     * @param attName
     * @param upperLimit
     * @return
     */
    private String findAvailableAttValue(final CSHandler cs, final List<Fdn> fdns, final String attName, final int upperLimit) {
        for (int attValue = upperLimit; attValue > -1; attValue--) {
            for (final Fdn fdn : fdns) {
                if (!cs.getAttributeValue(fdn, attName).equals(attValue)) {
                    return Integer.toString(attValue);
                }
            }
        }
        return null;
    }

}
