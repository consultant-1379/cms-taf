/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xmanvas
 */
public class NeadPreCheckCliOperator {

    final static Logger LOGGER = Logger.getLogger(NeadPreCheckCliOperator.class);

    private static final String TRIGGER_PATH = "/netsim/inst/POC/";

    private static String FileName = "simulationTextFile.txt";

    public boolean simulationOperator(final String networkElement, final String script, final boolean flag) {

        final User user = new User("netsim", "netsim", UserType.CUSTOM);
        final CLICommandHelper cli = new CLICommandHelper(HostGroup.getAllNetsims().get(0), user);

        if (flag) {

            LOGGER.info("Restarting Netsim");
            final String netsimRestartCommand = "/netsim/inst/restart_netsim";
            cli.execute(netsimRestartCommand);
            LOGGER.info("Sleeping fro 20 Minutes after netsim restart");
            try {
                Thread.sleep(1200000);
            } catch (final Exception e) {

            }

        }

        final String neList[] = networkElement.split(";");

        final String initialCommand = "rm -rf " + TRIGGER_PATH + FileName + " | touch " + TRIGGER_PATH + FileName;

        LOGGER.info("Creating file on the path :  " + TRIGGER_PATH);

        cli.execute(initialCommand);

        for (final String ne : neList) {

            cli.execute("echo " + ne + " >> " + TRIGGER_PATH + FileName);

        }
        String result;
        LOGGER.info("Waiting for the script to execute....");
        if (!flag) {
            LOGGER.info("Stopping all nodes in netsim");
            FileName = "all";
            result = cli.execute(". " + TRIGGER_PATH + script + " " + FileName);
        } else {
            result = cli.execute(". " + TRIGGER_PATH + script + " " + TRIGGER_PATH + FileName);
        }

        LOGGER.info("Result from the script = " + result);

        if (result.contains("0")) {

            return true;
        }
        return false;
    }

}