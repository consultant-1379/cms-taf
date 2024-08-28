/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;

/**
 * @author eeitky
 */
public interface NodeExceptionOperator {

    /**
     * This method will setup exception handling for an NE type in netsim.
     * The exception will occur when the specified IDL corba method is called.
     * 
     * @param networkElement
     *        NetworkElement holds simulation and netype details.
     * @param exceptionName
     *        Name of exception to be created.
     * @param idlcommand
     *        IDL method called in netsim.
     * @param condition
     *        How frequent the exception will occur.
     * @return
     *         True if all netsim commands return "OK".
     */
    boolean createIDLException(NetworkElement networkElement, String exceptionName, String idlcommand, String condition);

    /**
     * This method will remove exception handling for an NE type in netsim.
     * 
     * @param networkElement
     *        NetworkElement holds simulation and netype details.
     * @param exceptionName
     *        Name of exception to be deleted.
     * @return
     *         True if all netsim commands return "OK".
     */
    boolean deleteException(NetworkElement networkElement, String exceptionName);

    /**
     * This method will activate exception handling for an NE in netsim.
     * 
     * @param networkElement
     *        NetworkElement holds simulation and netype details.
     * @param exceptionName
     *        Name of exception to be activated.
     * @return
     *         True if all netsim commands return "OK".
     */
    boolean activateException(NetworkElement networkElement, String exceptionName);

    /**
     * This method will deactivate exception handling for an NE in netsim.
     * 
     * @param networkElement
     *        NetworkElement holds simulation and netype details.
     * @param exceptionName
     *        Name of exception to be deactivated.
     * @return
     *         True if all netsim commands return "OK".
     */
    boolean deactivateException(NetworkElement networkElement, String exceptionName);

}
