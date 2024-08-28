/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2017 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xaggpar
 */
public interface MixedModeEpicOperator {

    /**
     * Method to get mixedModeAttribute of a given FDN.
     *
     * @param Fdn
     *        , fdn of node.
     */
    boolean getMixedModeAttributeValue(final Fdn fdn);

    /**
     * @param meContext
     * @return
     */
    boolean isSharedWithExternalMeSupported(Fdn meContext, final String nodeType, final String baseNodeVersion);

    /**
     * @param listFdn
     *        , value
     * @return Map<Fdn, String>
     */
    Map<Fdn, String> updateAttributeValue(final List<Fdn> listFdn, final String attribute, final String attributeValues);

}
