/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author ECOLHAR
 */
public interface StartupRecoveryOperator {

    /**
     * Logs a warning for Mo Types for which no instances have been found
     *
     * @param moTypes
     *        Mo Types which were looked for
     * @param existingMos
     *        List of instances of types found in CS
     * @param nodeType
     *        The nodeType which was searched for the Mo Types
     */
    void logTypesNotFound(String[] moTypes, List<Fdn> existingMos, String nodeType);
}