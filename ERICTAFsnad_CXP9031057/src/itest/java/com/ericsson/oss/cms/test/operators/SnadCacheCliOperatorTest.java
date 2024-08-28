/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author egokdag
 */
public class SnadCacheCliOperatorTest {

    private final SnadCacheOperator snadCliOperator = new SnadCacheCliOperator();

    @Before
    public void executeCacheReview() {
        snadCliOperator.executeCacheReview();
    }

    @Test
    public void testIsMasterInManagedCache() {
        final Fdn masterMoFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1");
        final boolean result = snadCliOperator.isInMasterCache(masterMoFdn);
        assertTrue("Master MO: " + masterMoFdn.getFdn() + " is not in Managed Cache", result);
    }

    @Test
    public void testGetMasterConsistencyState() {
        final Fdn masterMoFdn = new Fdn("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1");
        final String consistencyState = snadCliOperator.getMasterConsistencyState(masterMoFdn);
        assertThat("Master MO: " + masterMoFdn.getFdn() + " is not Consistent", consistencyState, is(equalTo("Consistent")));
    }
}