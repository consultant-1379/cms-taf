/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.exception.CSTestHandlerException;
import com.ericsson.oss.taf.cshandler.model.Associations;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;
import com.ericsson.oss.taf.hostconfigurator.OssHost;

/**
 * @author xjyobeh
 */
@SuppressWarnings("deprecation")
public class CSLibTestHandlers implements CSHandler {

    private static final String CSLIBTEST = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cslibtest ";

    private static final String COPYCONFIGFILE = "cp /opt/ericsson/nms_umts_cms_nead_seg/etc/cstest.WRAN_SUBNETWORK_MIRROR_CS.env /etc/opt/ericsson/nms_cif_cs/.";

    private static final String CMD_SA = "sa";

    private static final String DATABASE = "WRAN_SUBNETWORK_MIRROR_CS";

    private static final Logger logger = Logger.getLogger(CSLibTestHandlers.class);

    private final CLICommandHelper handler;

    private final RemoteObjectHandler remote;

    private final String remoteFileLocation = "/tmp/remoteFile.txt";

    /**
     * Construct a new instance of a CSHandler which uses <code>cslibtest</code> as it's interface to the CS.
     *
     * @param host
     *        A host object describing an OSS master server.
     * @param database
     *        The database to be queried by this handler instance.
     */
    public CSLibTestHandlers(final OssHost host) {
        this.handler = new CLICommandHelper(host, host.getNmsadmUser());
        this.remote = new RemoteObjectHandler(HostGroup.getOssmaster());
        logger.info("Copy cstest.WRAN_SUBNETWORK_MIRROR_CS.env file");
        handler.simpleExec(COPYCONFIGFILE);
    }

    @Override
    public boolean createMo(final Fdn fdn, final List<Attribute> attributes) {
        return false;
    }

    @Override
    public boolean deleteMo(final Fdn fdn) {
        return false;
    }

    @Override
    public boolean deleteMos(final List<Fdn> fdns) {
        return false;
    }

    @Override
    public boolean moExists(final Fdn fdn) {
        return false;
    }

    @Override
    public List<Fdn> getByType(final String type) {
        return null;
    }

    @Override
    public List<Fdn> getByType(final String type, final String filterString) {
        return null;
    }

    @Override
    public List<Fdn> getByType(final String type, final Filter filter) {
        return null;
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn, final String filterString) {
        return null;
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn, final Filter filter) {
        return null;
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn, final String level, final Filter filter) {
        return null;
    }

    @Override
    public List<Fdn> getChildMos(final Fdn fdn) {
        return null;
    }

    @Override
    public String getAttributeValue(final Fdn fdn, final String attributeName) {
        return null;
    }

    @Override
    public List<Attribute> getAttributes(final Fdn fdn, final String... attributeNames) {
        return null;
    }

    @Override
    public List<Attribute> getAttributes(final Fdn fdn, final List<String> attributeNames) {
        return null;
    }

    @Override
    public void setAttributes(final Fdn fdn, final List<Attribute> attributes) {

    }

    /**
     * @author xjyobeh
     */
    private String executeWithException(final String cmd, final String args) {
        String stdOut = this.executeCSLib(cmd, args);
        int exitCode = handler.getCommandExitValue();

        if (exitCode != 0 && StringUtils.isBlank(stdOut)) {
            logger.info("Unknown exception in CSLibTest will retry");
            // sleep(5000);
            stdOut = this.executeCSLib(cmd, args);
            exitCode = handler.getCommandExitValue();

        }
        if (exitCode != 0) {
            if (StringUtils.isBlank(stdOut)) {
                stdOut = "Unknown exception in CSLibTest";
            }
            throw new CSTestHandlerException(stdOut);

        }
        return stdOut;
    }

    /**
     * Creating a remote file.
     *
     * @author xjyobeh
     */
    private void createFile() {
        logger.info("Creating a file on a remote server ::: " + remote.createRemoteFile(remoteFileLocation, 3000l, "byte"));
    }

    /**
     * Copying the contents from a local file to a remote file.
     *
     * @author xjyobeh
     */
    private void copyFile() {
        createFile();
        final String pythonscr = FileFinder.findFile("localFile.txt").get(0);
        final String localFileLocation = pythonscr;
        logger.info("Copying filter file from local to server ::: " + remote.copyLocalFileToRemote(localFileLocation, remoteFileLocation));

    }

    /**
     * Deleting the remote file.
     *
     * @author xjyobeh
     */
    private boolean deleteFile() {
        final boolean flag = remote.deleteRemoteFile(remoteFileLocation);
        System.out.println("DELETION = " + flag);
        return flag;
    }

    /**
     * @author xjyobeh
     */
    private String executeCSLib(final String cmd, final String args) {
        final String stdout;
        final String content = cmd + " " + args + '\n' + "sh sleep 10";
        File file = null;
        try {
            file = new File("localFile.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            final FileWriter fw = new FileWriter(file.getAbsoluteFile());
            final BufferedWriter bw = new BufferedWriter(fw);
            // write in file
            bw.write(content);
            bw.close();
        } catch (final Exception e) {
            System.err.println("An IOException was caught!");
            e.printStackTrace();
        }
        copyFile();
        final String cmdToExec = buildCSLibCommandToExecute("remoteFile.txt");
        logger.info("Executing: " + cmdToExec);
        stdout = handler.simpleExec(cmdToExec);
        final int exitCode = handler.getCommandExitValue();

        if (exitCode != 0) {
            logger.error(stdout);
        } else {
            logger.info("Command result: " + stdout);
        }
        deleteFile();
        file.delete();
        return stdout;
    }

    /**
     * @author xjyobeh
     */
    private String buildCSLibCommandToExecute(final String fileName) {
        final StringBuilder cmdToExec = new StringBuilder();
        cmdToExec.append(CSLIBTEST);
        cmdToExec.append(DATABASE);
        cmdToExec.append(" ");
        cmdToExec.append("file /tmp/");
        cmdToExec.append(fileName);
        return cmdToExec.toString();
    }

    @Override
    public boolean adjust(final Fdn fdn) {
        return false;
    }

    @Override
    public void detach(final Fdn fdn) {

    }

    @Override
    public void attach(final Fdn fdn, final String mibAdapterName) {

    }

    @Override
    public boolean isAttached(final Fdn fdn, final String mibAdapterName) {
        return false;
    }

    @Override
    public List<Associations> getAssociations(final Fdn fdn) {
        return null;
    }

    @Override
    public String getRootMo(final Fdn fdn) {
        return null;
    }

    @Override
    public Fdn getSubNetworkRootMo() {
        return null;
    }

    @Override
    public String getMimName(final Fdn fdn) {
        return null;
    }

    @Override
    public List<Fdn> getMeContextByMim(final String type, final String mimName) {
        return null;
    }

    @Override
    public Map<Fdn, List<Attribute>> getChildMosWithAttribute(final Fdn fdn, final String level, final Filter filter, final String... attributeNames) {
        return null;
    }

    @Override
    public String getMibAdapterName(final Fdn fdn) {
        return null;
    }

    @Override
    public void setAttributeValue(final Fdn fdn, final String attributeName, final String attributeValue) {
        final StringBuilder cmd_exec = new StringBuilder();
        cmd_exec.append(fdn.toString());
        cmd_exec.append(" ");
        cmd_exec.append(attributeName);
        cmd_exec.append(" ");
        cmd_exec.append(attributeValue);

        logger.info("Copy cstest.WRAN_SUBNETWORK_MIRROR_CS.env file");
        handler.simpleExec(COPYCONFIGFILE);
        logger.info("Execute cslibtest command");
        logger.info("cmd_exec.toString : " + cmd_exec.toString());
        this.executeWithException(CMD_SA, cmd_exec.toString());
    }

    /**
     * Setting the attributes through csLibTest
     *
     * @param Fdn
     *        String array containing the attribute names to set.
     *        String array containing the attribute values to set for the corresponding attributes.
     * @author xgggjjj
     */
    @Override
    public void setAttributes(final Fdn fdn, final String[] attributeNames, final String[] attributeValues) {

        final StringBuilder cmd_exec = new StringBuilder();
        cmd_exec.append(fdn.toString());
        cmd_exec.append(" ");
        for (int i = 0; i < attributeNames.length; i++) {
            cmd_exec.append(attributeNames[i]);
            cmd_exec.append(" ");
            cmd_exec.append(attributeValues[i]);
            cmd_exec.append(" ");
        }

        logger.info("Execute cslibtest command");
        logger.info("cmd_exec.toString : " + cmd_exec.toString().trim());
        this.executeWithException(CMD_SA, cmd_exec.toString().trim());

    }

}