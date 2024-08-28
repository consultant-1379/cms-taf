/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ericsson.oss.taf.cshandler.model.Fdn;

public class StartupRecoveryCliOperator implements StartupRecoveryOperator {

    private static final Logger logger = Logger.getLogger(StartupRecoveryCliOperator.class);

    @Override
    public void logTypesNotFound(final String[] moTypes, final List<Fdn> existingMos, final String nodeType) {
        final Map<String, Fdn> typeMap = new HashMap<String, Fdn>();
        for (final Fdn fdn : existingMos) {
            typeMap.put(fdn.getType(), fdn);
        }

        for (final String type : moTypes) {
            if (!typeMap.containsKey(type)) {
                logger.warn("No instances of: " + type + " found on node type: " + nodeType);
            }
        }
    }
}
