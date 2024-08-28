/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Attribute;

/**
 * @author xaggpar
 */
public interface BlackListAttributesOperator {

    /**
     * @return List<String>
     */
    List<String> readBlackListAttrFileContent();

    /**
     * @param attributesBefore
     * @param primaryAttributeValues
     * @return string
     */
    String prepareValueToSetOnNetsim(Attribute attributesBefore, String[] primaryAttributeValues);

}

