package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *--------------------------------------------------------------------------------*/

public interface ProxyCRUDOperator {

    /**
     * Create a ProxyMO on the Node using Netsim.
     * 
     * @param proxyType
     *        The moType of the proxy you wish to create.
     * @param parentType
     *        The moType of the proxies parent.
     * @return The {@link Fdn} of the created proxy.
     */
    Fdn createProxy(String proxyType, String parentType);

}
