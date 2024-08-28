/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.*;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.model.*;
import com.ericsson.oss.taf.hostconfigurator.OssHost;

/**
 * Handler used to execute cslibtest commands on an OSSRC master server.
 * 
 * @author xmagkum
 */
public class CSLibTestHandler implements CSHandler {

    private static final String CSLIBTEST = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cslibtest ";

    private static final String COPYCONFIGFILE = "cp /opt/ericsson/nms_umts_cms_nead_seg/etc/cstest.WRAN_SUBNETWORK_MIRROR_CS.env /etc/opt/ericsson/nms_cif_cs/.";

    private static final String CMD_CM = "cm";

    private static final String CMD_ARG_ATTR = "-attr";

    private static final String DATABASE = "WRAN_SUBNETWORK_MIRROR_CS";

    private static final Logger logger = Logger.getLogger(CSLibTestHandler.class);

    private final CLICommandHelper handler;

    /**
     * Construct a new instance of a CSHandler which uses <code>cslibtest</code> as it's interface to the CS.
     * 
     * @param host
     *        A host object describing an OSS master server.
     * @param database
     *        The database to be queried by this handler instance.
     */
    public CSLibTestHandler(final OssHost host) {
        this.handler = new CLICommandHelper(host, host.getNmsadmUser());
    }

    @Override
    public boolean createMo(final Fdn fdn, final List<Attribute> attributes) {
        final String[] args = buildCreateMoArgsArray(fdn.getFdn(), attributes);
        logger.info("Copy cstest.WRAN_SUBNETWORK_MIRROR_CS.env file");
        handler.simpleExec(COPYCONFIGFILE);
        logger.info("Execute cslibtest command");
        final String stdout = executeCSLib(CMD_CM, args);
        if (stdout.contains("CstestException")) {
            return false;
        }
        return true;
    }

    private String executeCSLib(final String cmd, final String... args) {
        final String cmdToExec = buildCSLibCommandToExecute(cmd, args);

        logger.info("Executing: " + cmdToExec);
        // System.out.println("Executing: " + cmdToExec);
        final String stdout = handler.simpleExec(cmdToExec);
        final int exitCode = handler.getCommandExitValue();

        if (exitCode != 0) {
            logger.error(stdout);
        } else {
            logger.info("Command result: " + stdout);
        }
        return stdout;
    }

    private String buildCSLibCommandToExecute(final String cmd, final String... args) {
        final StringBuilder cmdToExec = new StringBuilder();
        cmdToExec.append(CSLIBTEST);
        cmdToExec.append(DATABASE);
        cmdToExec.append(" ");
        cmdToExec.append(cmd);
        cmdToExec.append(" ");
        for (final String arg : args) {
            cmdToExec.append(arg);
            cmdToExec.append(" ");
        }
        return cmdToExec.toString();
    }

    private String[] buildCreateMoArgsArray(final String fdn, final List<Attribute> attributes) {
        final List<String> params = new ArrayList<String>();
        params.add(fdn);
        if (attributes != null) {
            params.add(CMD_ARG_ATTR);
            for (final Attribute attribute : attributes) {
                params.add(attribute.getName());
                params.add(attribute.getValue());
            }
        }
        return params.toArray(new String[params.size()]);
    }

    @Override
    public boolean deleteMo(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public boolean moExists(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getByType(final String type) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getByType(final String type, final String filterString) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getByType(final String type, final Filter filter) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn, final String filterString) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn, final Filter filter) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public String getAttributeValue(final Fdn fdn, final String attributeName) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Attribute> getAttributes(final Fdn fdn, final String... attributeNames) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Attribute> getAttributes(final Fdn fdn, final List<String> attributeNames) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void setAttributeValue(final Fdn fdn, final String attributeName, final String attributeValue) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void setAttributes(final Fdn fdn, final String[] attributeNames, final String[] attributeValues) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void setAttributes(final Fdn fdn, final List<Attribute> attributes) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public boolean adjust(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void detach(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void attach(final Fdn fdn, final String mibAdapterName) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public boolean isAttached(final Fdn fdn, final String mibAdapterName) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Associations> getAssociations(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn, final String level, final Filter filter) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public String getMimName(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public String getRootMo(final Fdn fdn) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public Fdn getSubNetworkRootMo() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public boolean deleteMos(final List<Fdn> fdns) {
        throw new RuntimeException("Not imlemented!");
    }

    @Override
    public Map<Fdn, List<Attribute>> getChildMosWithAttribute(final Fdn fdn, final String level, final Filter filter, final String... attributeNames) {
        throw new RuntimeException("Not imlemented!");
    }

    @Override
    public List<Fdn> getMeContextByMim(final String type, final String mimName) {
        throw new RuntimeException("Not imlemented!");
    }

    @Override
    public String getMibAdapterName(final Fdn fdn) {
        throw new RuntimeException("Not imlemented!");
    }
}
