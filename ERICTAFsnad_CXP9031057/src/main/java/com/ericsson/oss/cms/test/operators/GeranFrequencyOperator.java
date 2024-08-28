/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author egokdag
 */
public interface GeranFrequencyOperator {

    /**
     * Returns a GeranFreqGroup MO which satisfy the following criteria:
     * </br>The GeranFreqGroup MO must be on a connected and synced node.
     * </br>The selected MO can be on a node which is using new or old GeranFrequency Model determined by the provided parentType.
     * </br>The master of the GeranFreqGroup must be in Consistent state in SNAD cache.
     * </br>The master of the GeranFreqGroup must have proxies on more than one node.
     *
     * @param nodeType
     *        Conventional representation of the neType attribute value. See {@link NeType} for details.
     * @param freqGroupType
     *        Specifies the MO type that is required, GeranFreqGroup in this case.
     * @param parentType
     *        Specifies the parent MO type which will be used to identify which type of model required, i.e., new or old GeranFrequency
     *        model.
     * @param proxyType
     *        Specifies the child MO type that is required specifically under provided parentType.
     * @return
     *         The {@link Fdn} of a MO of GeranFreqGroup.
     */
    Fdn getSharedGeranFreqGroupFdn(String nodeType, String freqGroupType, String parentType, String proxyType);

    /**
     * This is just a wrapper around {@link CreateMoCliOperator#buildMoFdn(Fdn, String, String)} to accommodate different parentTypes while
     * creating GeranFrequencies in new and old GeranFrequencyModel. This method will decide which parent {@link Fdn} to use and pass the
     * call to {@link CreateMoCliOperator#buildMoFdn(Fdn, String, String)}.
     *
     * @param parentType
     *        Specifies the parent MO type which will be used to identify which type of model required, i.e., new or old GeranFrequency
     *        model.
     * @param sharedGeranFreqGroupFdn
     *        The {@link Fdn} of GeranFreqGroup which will be used to create the GeranFrequency.
     * @param proxyType
     *        Type of the MO to be created.
     * @param testId
     *        Name of the MO to be created.
     * @return
     *         The {@link Fdn} of the child MO.
     * @see CreateMoCliOperator#buildMoFdn(Fdn, String, String)
     */
    Fdn buildMoFdn(String parentType, Fdn sharedGeranFreqGroupFdn, String proxyType, String testId);

    /**
     * This is just a wrapper around {@link SetMoCliOperator#addMoRefData(String[], String)} to accommodate different attribute sets while
     * creating GeranFrequencies in new and old GeranFrequencyModel. This method will decide whether to add geranFreqGroupRef attribute to
     * the attribute set and pass the call to {@link SetMoCliOperator#addMoRefData(String[], String)}.
     *
     * @param parentType
     *        Specifies the parent MO type which will be used to identify which type of model required, i.e., new or old GeranFrequency
     *        model.
     * @param proxyAttrNames
     *        Existing attribute data.
     * @param proxyGeranGroupRefAttr
     *        Name of the ref attribute, i.e., geranFreqGroupRef.
     * @return
     *         This method adds an attribute name to an existing array of attribute names.
     * @see SetMoCliOperator#addMoRefData(String[], String)
     */
    String[] addMoRefData(String parentType, String[] proxyAttrNames, String proxyGeranGroupRefAttr);

    /**
     * This is just a wrapper around {@link SetMoCliOperator#addMoRefData(String[], String, String)} to accommodate different attribute
     * value sets while creating GeranFrequencies in new and old GeranFrequencyModel. This method will decide whether to add
     * geranFreqGroupRef attribute value to the attribute value set and pass the call to
     * {@link SetMoCliOperator#addMoRefData(String[], String, String)}.
     *
     * @param parentType
     *        Specifies the parent MO type which will be used to identify which type of model required, i.e., new or old GeranFrequency
     *        model.
     * @param proxyAttrValues
     *        Existing attribute data.
     * @param ldn
     *        The reference data to add.
     * @param proxyGeranGroupRefAttrType
     *        The type of reference attribute, if reference attribute not applicable, an empty string.
     * @return
     *         This method adds an attribute value to an existing array of attribute values.
     * @see SetMoCliOperator#addMoRefData(String[], String, String)
     */
    String[] addMoRefData(String parentType, String[] proxyAttrValues, String ldn, String proxyGeranGroupRefAttrType);

    /**
     * Executes the Snad smtool action getProxy and returns the state of the proxy MO in the cache.
     * Will retry until a state other than TRANSIENT_CONSISTENT is returned from the cache.
     *
     * @param proxyMoFdn
     *        The FDN of the proxy MO to execute getProxy on.
     * @return
     *         The state of the proxy MO in the cache as returned by the smtool action.
     */
    String getProxyStateAfterCC(Fdn proxyMoFdn);

}
