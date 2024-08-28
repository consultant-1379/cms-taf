/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

public interface BCGImportOperator {

    /**
     * This method will activate the plan
     * 
     * @param planName
     *        The name of the plan to activate.
     */
    void activatePlan(String planName);

}
