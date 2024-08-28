/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

/**
 * @author eeimgrr
 *
 */
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.taf.cshandler.cli.CommandExecutor;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

@Operator(context = Context.CLI)
public class BCGImportCliOperator implements BCGImportOperator {

    private static final Logger LOGGER = Logger.getLogger(BCGImportCliOperator.class);

    private static final String BCGTOOLPLANACTIVATION = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a ";

    private final CommandExecutor handler = new CommandExecutor(HostGroup.getOssmaster());

    @Override
    public void activatePlan(final String planName) {
        final String planActivationCommand = BCGTOOLPLANACTIVATION + planName;
        final String planActivationOutput = handler.execute(planActivationCommand);
        LOGGER.info("Plan activation command :" + planActivationCommand + " Output: " + planActivationOutput);

    }

}
