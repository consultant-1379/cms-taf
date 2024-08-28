/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Attribute;

/**
 * @author EEIMGRR
 */
public interface AttributeOperator {

    /**
     * This method builds a list of {@link Attribute} from given attribute names and values.
     * 
     * @param attributeNames
     *        The attribute names
     * @param attributeValues
     *        The attribute values
     * @return
     *         A list of {@link Attribute}
     */

    List<Attribute> buildAttributesList(String[] attributeNames, String[] attributeValues);

    /**
     * This method adds a new {link:Attribute} to an existing list.
     * 
     * @param attributeList
     *        The {@link List} of {@link Attribute}.
     * @param attributeName
     *        The attribute name to be added.
     * @param attributeValue
     *        The attribute value to be added.
     */
    void updateAttributeList(List<Attribute> attributeList, String attributeName, String attributeValue);

}
