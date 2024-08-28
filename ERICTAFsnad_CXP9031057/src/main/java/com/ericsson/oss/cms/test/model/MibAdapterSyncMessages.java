/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.model;

import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.SUCCESSFUL_NEAD_SYNCH;
import static com.ericsson.oss.cms.test.constants.CmsConstants.CifLogMessages.SUCCESSFUL_NMA_SYNCH;
import static com.ericsson.oss.cms.test.constants.CmsConstants.Nead.NEAD_MIB_ADAPTER;

import java.util.HashMap;
import java.util.Map;

public abstract class MibAdapterSyncMessages {

    public static final String NMA_MIB_ADAPTER = "nma1";

    private static final Map<String, String> successMessage = new HashMap<String, String>();

    static {
        successMessage.put(NEAD_MIB_ADAPTER, SUCCESSFUL_NEAD_SYNCH);
        successMessage.put(NMA_MIB_ADAPTER, SUCCESSFUL_NMA_SYNCH);
    }

    public static String getSuccessMessage(final String mibAdapter) {
        return successMessage.get(mibAdapter);
    }

}
