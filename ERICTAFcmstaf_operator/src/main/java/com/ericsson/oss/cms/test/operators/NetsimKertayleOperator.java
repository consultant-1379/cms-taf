/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.io.IOException;
import java.util.List;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * Operator to perform Netsim operations using Kertayle Scripts.
 *
 * @author ecolhar
 */
public interface NetsimKertayleOperator {

    /**
     * Takes an FDN and a list of kertayle format attribute strings and creates on MO on the corresponding Netsim Network Element.
     *
     * @param moFdn
     *        The Fdn of the MO to be created.
     * @param kertayleAttrs
     *        The attribute string in Kertayle format. E.g. "createdBy Integer 1".
     * @return <code>true</code> if MO created ok, otherwise <code>false</code>.
     * @throws IOException
     */
    boolean createMo(Fdn moFdn, List<String> kertayleAttrs) throws IOException;

}
