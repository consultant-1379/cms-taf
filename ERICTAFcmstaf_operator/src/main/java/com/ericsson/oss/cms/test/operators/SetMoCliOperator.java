/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.NETSIM_CMD_EXEC_SUCCESS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;
import static com.ericsson.oss.cms.test.util.AttributeValueConverter.NETSIM_MOREF_DELIMITERS;
import static com.ericsson.oss.cms.test.util.AttributeValueConverter.convertDBToNetsimValue;
import static com.ericsson.oss.cms.test.util.StringUtil.equalsIgnoreDelimiters;
import static com.ericsson.oss.cms.test.util.StringUtil.equalsIgnoreSpace;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SetmoattributeCommand;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author egergro
 */
@Operator(context = Context.CLI)
public class SetMoCliOperator implements SetMoOperator {

    private final NetSimCommandHandler netSimCmdHandler = NetSimCommandHandler.getInstance();

    final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean setAttributeValues(final Fdn moFdn, final List<Attribute> attributes) {

        final String[] attributeNames = new String[attributes.size()];
        final String[] attributeValues = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            attributeNames[i] = attributes.get(i).getName();
            attributeValues[i] = convertDBToNetsimValue(attributes.get(i));
        }

        return setAttributeValues(moFdn, attributeNames, attributeValues);

    }

    @Override
    public boolean setAttributeValues(final Fdn moFdn, final String[] attributeNames, final String[] attributeValues) {
        final String attributeValuePair = buildAttributeValuePair(attributeNames, attributeValues);
        logger.info("Setting attribute value pair(s) as follows: " + attributeValuePair.toString());
        final NetworkElement networkElement = getNetworkElementForFdn(moFdn);

        final SetmoattributeCommand setAttributeCommand = NetSimCommands.setmoattribute(moFdn.getLdn(), attributeValuePair);
        final NetSimResult commandResult = networkElement.exec(setAttributeCommand);
        return commandResult.getRawOutput().trim().endsWith(NETSIM_CMD_EXEC_SUCCESS);

    }

    @Override
    public String buildAttributeValuePair(final String[] attributeNames, final String[] attributeValues) {
        final StringBuilder attrValuePairBuilder = new StringBuilder();

        for (int i = 0; i < attributeNames.length; i++) {
            if (attrValuePairBuilder.length() != 0) {
                attrValuePairBuilder.append("||");
            }
            attrValuePairBuilder.append(attributeNames[i]);
            attrValuePairBuilder.append("=");
            attrValuePairBuilder.append(attributeValues[i]);
        }
        return attrValuePairBuilder.toString();

    }

    private NetworkElement getNetworkElementForFdn(final Fdn moFdn) {
        final String meContextName = moFdn.getNameOfFdnPart(MECONTEXT);
        final NetworkElement networkElement = netSimCmdHandler.getAllStartedNEs().get(meContextName);
        return networkElement;
    }

    @Override
    public String[] selectAttributeValuesToSet(
            final String[] attributeNames,
            List<Attribute> moAttributesBeforeSet,
            final String[] primaryAttributeValues,
            final String[] secondaryAttributeValues) {
        final String[] attributeValuesToSet = new String[attributeNames.length];

        // database doesn't necessarily return attributes in order that is asked.
        moAttributesBeforeSet = sortMoAttributes(attributeNames, moAttributesBeforeSet);

        for (int i = 0; i < moAttributesBeforeSet.size(); i++) {
            final Attribute attributeInDatabase = moAttributesBeforeSet.get(i);
            attributeValuesToSet[i] = selectValue(attributeInDatabase, primaryAttributeValues[i], secondaryAttributeValues[i]);
        }
        return attributeValuesToSet;
    }

    private List<Attribute> sortMoAttributes(final String[] attributeNames, final List<Attribute> moAttributesBeforeSet) {
        if (moAttributesBeforeSet.size() > 1) {
            final List<Attribute> sortedMoAttributesBeforeSet = new ArrayList<Attribute>();
            for (final String attributeName : attributeNames) {
                for (final Attribute moAttributeBeforeSet : moAttributesBeforeSet) {
                    if (attributeName.equals(moAttributeBeforeSet.getName())) {
                        sortedMoAttributesBeforeSet.add(moAttributeBeforeSet);
                    }
                }
            }
            return sortedMoAttributesBeforeSet;
        }
        return moAttributesBeforeSet;
    }

    private String selectValue(final Attribute attributeInDatabase, final String primaryValue, final String secondaryValue) {
        final String attributeValueInDatabase = convertDBToNetsimValue(attributeInDatabase);
        if (equalsIgnoreSpace(primaryValue, attributeValueInDatabase)) {
            return secondaryValue;
        }
        return primaryValue;
    }

    @Override
    public boolean isAttributesSetInDatabase(final Fdn moFdn, final String[] attributeNames, final String[] valuesExpected) {
        List<Attribute> moAttributesAfterSet = csHandler.getAttributes(moFdn, attributeNames);
        moAttributesAfterSet = sortMoAttributes(attributeNames, moAttributesAfterSet);
        boolean result = true;
        for (int i = 0; i < attributeNames.length; i++) {

            final String attributeValueInDatabase = convertDBToNetsimValue(moAttributesAfterSet.get(i));
            if (!equalsIgnoreDelimiters(attributeValueInDatabase, valuesExpected[i], NETSIM_MOREF_DELIMITERS)) {
                logger.error("MO FDN: " + moFdn + ", attribute Name: " + attributeNames[i] + ", expected value: " + valuesExpected[i] + " but found "
                        + attributeValueInDatabase);
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean isAttributesSetInDatabase(final List<Fdn> moFdns, final String[] attributeNames, final String[] valuesExpected) {
        boolean result = true;
        for (final Fdn moFdn : moFdns) {
            if (!isAttributesSetInDatabase(moFdn, attributeNames, valuesExpected)) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public String[] addMoRefData(final String[] attributes, final String moRefData, final String refType) {
        String convertedRef = moRefData;
        if (refType.equalsIgnoreCase("sequence")) {
            convertedRef = "'[\\\"'\"" + moRefData + "\"'\\\"]'";
        }
        return addMoRefData(attributes, convertedRef);
    }

    @Override
    public String[] addMoRefData(final String[] attributes, final String moRefData) {

        return ArrayUtils.add(attributes, moRefData);
    }

}
