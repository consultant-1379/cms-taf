/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.cms.test.getters

import com.ericsson.cifwk.taf.CliGetter


class CppAvcCliGetter implements CliGetter {

	/**
	 * Wildcard expression to match NEAD log files on
	 */
	static final String NOTIFICATIONS_LOG_GLOB = "/var/opt/ericsson/nms_umts_cms_nead_seg/Notifications.log.*"
}
