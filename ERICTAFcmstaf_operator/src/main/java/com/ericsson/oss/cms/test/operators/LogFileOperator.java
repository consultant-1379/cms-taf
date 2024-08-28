/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import com.ericsson.oss.taf.cshandler.model.Fdn;

/**
 * @author xrajnka
 */
public interface LogFileOperator {

    /**
     * Checks if MO detail is logged in notification.log
     * 
     * @param dateNtime
     *        Date and Time from when notification.log needs to be checked.
     * @param Fdn
     *        Fdn of MO for which log is expected.
     * @param operation
     *        MO operation to check in notification.log. example: CN for create, DN for delete
     * @return
     *         True if mo and operation details exist, else false
     */
    boolean notificationExists(String dateTime, Fdn moFdn, String operation);

    /**
     * Gets the total number of notifications in Nead notification logs for provided MO {@link Fdn}, notification type and search strings
     * 
     * @param notificationType
     *        The type of the notification, e.g., CN for create, DN for delete.
     * @param moFdn
     *        The {@link Fdn} of MO for which log is expected.
     * @param searchStrings
     *        The name of other strings expected along with the notifications.
     * @return
     *         The count of notifications
     */
    int getNotificationCount(final String notificationType, final Fdn moFdn, final String... searchStrings);

    /**
     * Gets the total number of exceptions in Nead exception logs for provided search string.
     * 
     * @param searchString
     *        The type of the search String.
     * @param nodeFdn
     *        Fdn of the node for which overflow notification exception is expected.
     * @return
     *         The count of search String
     */
    int getExceptionCount(final String searchString, final Fdn nodeFdn);

}