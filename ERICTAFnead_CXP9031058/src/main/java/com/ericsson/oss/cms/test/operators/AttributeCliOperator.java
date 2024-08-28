/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.CSAttribute;

/**
 * @author EEIMGRR
 */
public class AttributeCliOperator implements AttributeOperator {

    @Override
    public List<Attribute> buildAttributesList(final String[] attributeNames, final String[] attributeValues) {
        final List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < attributeNames.length; i++) {
            final CSAttribute anAttribute = new CSAttribute(attributeNames[i], attributeValues[i]);
            attributes.add(anAttribute);
        }
        return attributes;

    }

    @Override
    public void updateAttributeList(final List<Attribute> mandatoryAttributes, final String attributeName, final String attributeValue) {
        mandatoryAttributes.add(new CSAttribute(attributeName, attributeValue));
    }

}