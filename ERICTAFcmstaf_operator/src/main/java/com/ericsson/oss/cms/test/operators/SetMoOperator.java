/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;

public interface SetMoOperator {

    /**
     * Sets the value of each attribute in a given array of attributes on a given MO.
     * 
     * @param moFdn
     *        The Fdn of the MO on which attributes will be set
     * @param attributeNames
     *        The names of the attributes to be set
     * @param attributeValues
     *        The new values to be set on attributes
     * @return
     *         True if the set command executes successfully on the node
     */
    boolean setAttributeValues(Fdn moFdn, String[] attributeNames, String[] attributeValues);

    /**
     * Sets the value of each attribute in a given list of attributes on a given MO.
     * 
     * @param moFdn
     *        The Fdn of the MO on which attributes will be set
     * @param attributes
     *        The attributes whose values will be set
     * @return
     *         True if the set command executes successfully on the node
     */
    boolean setAttributeValues(Fdn moFdn, List<Attribute> attributes);

    /**
     * Selects attribute values to be set on the node from 2 possible data sets.
     * Basis of selection is that the proposed attribute value is not already set on the node.
     * 
     * @param attributeNames
     *        The name of the attributes to be set.
     * @param moAttributesBeforeSet
     *        The key-value pairs of MO attributes prior to set operation.
     * @param primaryAttributeValues
     *        The first set of attribute values proposed to be set.
     * @param secondaryAttributeValues
     *        The second set of attribute values proposed to be set.
     * @return
     *         An array of the new attribute values. The order of attribute values correspond to the order of the attribute names list.
     */
    String[] selectAttributeValuesToSet(
            String[] attributeNames,
            List<Attribute> moAttributesBeforeSet,
            String[] primaryAttributeValues,
            String[] secondaryAttributeValues);

    /**
     * Reads the MO attribute values in the database and compares them against expected values.
     * 
     * @param moFdn
     *        The {@link Fdn} of the MO whose attribute values to be compared.
     * @param attributeNames
     *        The name of the attributes to be compared.
     * @param valuesExpected
     *        The value of the attributes to be compared against the values in the database.
     * @return
     *         <code>true</code> if expected values are set in database, otherwise <code>false</code>.
     */
    boolean isAttributesSetInDatabase(Fdn moFdn, String[] attributeNames, String[] valuesExpected);

    /**
     * Reads the MO attribute values in the database and compares them against expected values.
     * 
     * @param moFdns
     *        The list of MO {@link Fdn}s whose attribute values to be compared.
     * @param attributeNames
     *        The name of the attributes to be compared.
     * @param valuesExpected
     *        The value of the attributes to be compared against the values in the database.
     * @return
     *         <code>true</code> if expected values are set in database, otherwise <code>false</code>.
     */
    boolean isAttributesSetInDatabase(List<Fdn> moFdns, String[] attributeNames, String[] valuesExpected);

    /**
     * Builds an attribute value pair according to NETSIM command format.<br/>
     * For example, attributes are a,b,c and values are 1,2,3 then attribute value pair is a=1||b=2||c=3.
     * 
     * @param attributes
     *        Attribute names.
     * @param attributeValues
     *        Attribute values.
     * @return
     *         Attribute value pair.
     */
    String buildAttributeValuePair(String[] attributes, String[] attributeValues);

    /**
     * This method adds MO reference data to an existing array of MO data for Netsim usage.
     * The data may first be converted a to a format required in Netsim, depending on the reference data type.
     * 
     * @param attributes
     *        Existing attribute data
     * @param moRefData
     *        The reference data to add
     * @param refType
     *        The type of reference attribute if conversion required for Netsim, else and empty string
     * @return
     *         The existing array with the new data appended
     */
    String[] addMoRefData(final String[] attributes, final String moRefData, String refType);

    /**
     * This method adds MO reference data to an existing array of MO data.
     * 
     * @param attributes
     *        Existing attribute data
     * @param moRefData
     *        The reference data to add
     * @return
     *         The existing array with the new data appended
     */
    String[] addMoRefData(String[] attributes, String moRefData);

}
