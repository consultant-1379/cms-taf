/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.oss.cms.test.operators;

import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.CONN_STATUS_CONNECTED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.MECONTEXT;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.NE_TYPE;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_SYNCHRONIZED;
import static com.ericsson.oss.cms.test.constants.CmsConstants.MeContext.SYNCH_STATUS_UNSYNCHRONIZED;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.cms.test.constants.CmsConstants.NodeLicNeadContants;
import com.ericsson.oss.cms.test.model.attributes.NeType;
import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSHandler;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.SimpleFilterBuilder;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.cshandler.model.Filter;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xaggpar
 */
public class LicenseSyncCliOperator implements LicenseSyncOperator {

    @Inject
    private InitialSyncCliOperator initialSyncCliOperator;

    private final static Logger logger = Logger.getLogger(LicenseSyncCliOperator.class);

    final Host host = HostGroup.getOssmaster();

    CLICommandHelper cliCmdHelper = new CLICommandHelper(host);

    private final CSHandler csHandler = new CSTestHandler(host, CSDatabase.Segment);

    private final static String SPACE = " ";

    private final static String MIRRORMIBVERSION = "mirrorMIBversion";

    private final static String NEMIMNAME = "neMIMName";

    @Override
    public List<Fdn> findAllConnectedNodes(final String nodeType) {
        final String[] nodeTypes = nodeType.split(",");
        final int neType1 = NeType.getNeTypeValue(nodeTypes[0]);
        final int neType2 = NeType.getNeTypeValue(nodeTypes[1]);
        final int neType3 = NeType.getNeTypeValue(nodeTypes[2]);
        logger.info("Looking for Connected Nodes with neTypes of: " + neType1 + ", " + neType2 + " and" + neType3);
        final Filter filter = SimpleFilterBuilder
                .builder()
                .attr(NE_TYPE)
                .equalTo(neType1)
                .and()
                .attr(CONN_STATUS)
                .equalTo(CONN_STATUS_CONNECTED)
                .or()
                .attr(NE_TYPE)
                .equalTo(neType2)
                .and()
                .attr(CONN_STATUS)
                .equalTo(CONN_STATUS_CONNECTED)
                .or()
                .attr(NE_TYPE)
                .equalTo(neType3)
                .and()
                .attr(CONN_STATUS)
                .equalTo(CONN_STATUS_CONNECTED)
                .build();
        final List<Fdn> listOfActiveNodes = csHandler.getByType(MECONTEXT, filter);
        logger.info("No. of ActiveNodes: " + listOfActiveNodes.size());
        return listOfActiveNodes;
    }

    /**
     * @param script_name
     */
    public boolean script(final String script_name) {
        // TODO Auto-generated method stub (Jul 7, 2017:11:52:18 AM by xmanvas)
        final String license = cliCmdHelper.simpleExec(script_name.toString());
        logger.debug(license);
        final boolean lic = license.contains("Trial License");
        return lic;
    }

    @Override
    public String getMirrorVerAndNodeType(final Fdn fdn) {
        final List<Attribute> csOutput = csHandler.getAttributes(fdn, MIRRORMIBVERSION, NEMIMNAME);
        String ver = "", type = "", verAndType = "";
        for (final Attribute attr : csOutput) {
            if (attr.getName().equalsIgnoreCase(MIRRORMIBVERSION)) {
                ver = attr.getValue();
            } else if (attr.getName().equalsIgnoreCase(NEMIMNAME)) {
                final String[] arr = attr.getValue().split("_");
                type = arr[0];
            }
        }
        verAndType = ver + SPACE + type;
        return verAndType;
    }

    public void executeScript(final String scriptName, final String pathName) {
        final String test = "chmod +x " + pathName + scriptName;
        final String execute_permission = cliCmdHelper.simpleExec(test);
        final String scriptResult = cliCmdHelper.simpleExec(pathName + scriptName);
        logger.info("exe. permission: " + execute_permission + " scriptResult:" + scriptResult);
    }

    @Override
    public boolean readMirrorMibVersionFromCPPfile(final List<Fdn> successNodes, final Map<String, Boolean> licScenarioMap) {
        boolean exist = true;
        for (final Fdn successFdn : successNodes) {
            String mirrorMibVer = "";
            final String verAndType = getMirrorVerAndNodeType(successFdn);
            final String[] dataArr = verAndType.split(" ");
            if (dataArr[1].equalsIgnoreCase("ERBS")) {
                mirrorMibVer = "\"" + "ERBS_NODE_MODEL_v" + dataArr[0] + "\"";
            } else if (dataArr[1].equalsIgnoreCase("RNC")) {
                mirrorMibVer = "\"" + "RNC_NODE_MODEL_v" + dataArr[0] + "\"";
            } else if (dataArr[1].equalsIgnoreCase("RBS")) {
                mirrorMibVer = "\"" + "RBS_NODE_MODEL_v" + dataArr[0] + "\"";
            }
            mirrorMibVer = mirrorMibVer.replace(" ", "");
            mirrorMibVer.replaceAll("\\r?\\n", "");

            String mirrorMibVersion = "";
            String ver = "";

            mirrorMibVer = mirrorMibVer.replaceAll("\\.", "_").trim();
            mirrorMibVersion = "grep -i" + SPACE + mirrorMibVer + " " + "/opt/ericsson/nms_umts_wranmom/lib/CPPNodeReleaseToMimVersionMap.properties";
            final String mibVersion = cliCmdHelper.simpleExec(mirrorMibVersion);
            if (mibVersion == null || mibVersion.isEmpty()) {
                exist = false;
                logger.info("MIM version " + mirrorMibVer + " is not existing !!!!");
                return exist;
            }

            final String[] version = mibVersion.split("=");
            if (version[1].contains(",")) {
                final String[] radioVer = version[1].split(",");
                if (radioVer[0].contains(dataArr[1])) {
                    ver = radioVer[0].trim();
                } else if (radioVer[1].contains(dataArr[1])) {
                    ver = radioVer[1].trim();
                }

            } else {
                ver = version[1].trim();
            }

            if (licScenarioMap.get(NodeLicNeadContants.NODE_LIC_17B) && licScenarioMap.get(NodeLicNeadContants.NODE_LIC_18A)
                    && licScenarioMap.get(NodeLicNeadContants.NODE_LIC_19X)) {
                // Just compare the versions, no need of release check here.
            } else if (licScenarioMap.get(NodeLicNeadContants.NODE_LIC_17B) && licScenarioMap.get(NodeLicNeadContants.NODE_LIC_18A)
                    && !licScenarioMap.get(NodeLicNeadContants.NODE_LIC_19X)) {
                if (version[0].contains("19.Q2")) {
                    exist = false;
                    logger.info("Node is sync with unsupported release." + version[0]);
                    return exist;
                }
            } else if (licScenarioMap.get(NodeLicNeadContants.NODE_LIC_17B) && !licScenarioMap.get(NodeLicNeadContants.NODE_LIC_18A)
                    && !licScenarioMap.get(NodeLicNeadContants.NODE_LIC_19X)) {
                if (version[0].contains("18.Q1") || version[0].contains("18.Q2") || version[0].contains("18.Q3") || version[0].contains("19.Q2")) {
                    exist = false;
                    logger.info("Node is sync with unsupported release." + version[0]);
                    return exist;
                }
            } else if (!licScenarioMap.get(NodeLicNeadContants.NODE_LIC_17B) && !licScenarioMap.get(NodeLicNeadContants.NODE_LIC_18A)
                    && !licScenarioMap.get(NodeLicNeadContants.NODE_LIC_19X)) {
                if (version[0].contains("17.Q3") || version[0].contains("17.Q4") || version[0].contains("18.Q1") || version[0].contains("18.Q2")
                        || version[0].contains("18.Q3") || version[0].contains("19.Q2")) {
                    exist = false;
                    logger.info("Node is sync with unsupported release when both lic. are absent" + version[0]);
                    return exist;
                }
            }
            mirrorMibVer = mirrorMibVer.replace("\"", "");
            if (!mirrorMibVer.equalsIgnoreCase(ver)) {
                exist = false;
                logger.info("MIM version differs ....." + mirrorMibVer + "!=" + ver);
                return exist;
            }
        }
        return exist;
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public Map<String, Boolean> prepareServer(final Map<String, Boolean> licScenario) {
        Map<String, Boolean> newmap = new LinkedHashMap<String, Boolean>();
        logger.info("Copy the script files to server");
        copyLicFilesToServer();
        // License Part.
        final boolean lic17B = script(NodeLicNeadContants.NODE_LIC_17B_CHECK_CMD);
        final boolean lic18A = script(NodeLicNeadContants.NODE_LIC_18A_CHECK_CMD);
        final boolean lic19X = script(NodeLicNeadContants.NODE_LIC_19X_CHECK_CMD);
        logger.info("Initial License Values : lic17B=" + lic17B + " lic18A=" + lic18A + " lic19X=" + lic19X);
        if (lic17B && lic18A && lic19X) {
            logger.info(" License Installed on the server!!");
            licScenario.put("19XLicPresent", true);
        } else if (!lic17B && !lic18A && !lic19X) {
            licScenario.put("AllLicAbsent", true);
        } else if (lic17B && lic18A && !lic19X) {
            licScenario.put("17BLicPre_18ALicPre_19XLicAb", true);
        } else if (lic17B && !lic18A && !lic19X) {
            licScenario.put("17BLicPre_18ALicAb_19XLicAb", true);
        }

        newmap = (Map<String, Boolean>) ((LinkedHashMap<String, Boolean>) licScenario).clone();
        licScenario.clear();
        for (final String str : newmap.keySet()) {
            if (newmap.get(str)) {
                licScenario.put(str, newmap.get(str));
                break;
            }
        }
        licScenario.putAll(newmap);
        return licScenario;
    }

    private void copyLicFilesToServer() {
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.REMOVE_18A_NODE_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.REMOVE_17B_NODE_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.CLEANUP_AND_RESTART_MC, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.FILE_17B_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.FILE_18A_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.FILE_19X_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.copyFileToRemote(NodeLicNeadContants.INSTALL_NODE_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
    }

    /**
     *
     */
    public void removeCopiedFiles() {
        // TODO Auto-generated method stub (Jan 3, 2018:12:18:43 PM by xaggpar)
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.REMOVE_18A_NODE_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.REMOVE_17B_NODE_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.CLEANUP_AND_RESTART_MC, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.FILE_17B_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.FILE_18A_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.FILE_19X_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        initialSyncCliOperator.removeFileFromRemote(NodeLicNeadContants.INSTALL_NODE_LICENSE, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
    }

    @Override
    public void installAndRemoveLic(final String[] installLic, final String[] removeLic) {
        // TODO Auto-generated method stub (Jan 3, 2018:10:41:22 AM by xaggpar)
        // Intallation part...
        final boolean lic17B = script(NodeLicNeadContants.NODE_LIC_17B_CHECK_CMD);
        final boolean lic18A = script(NodeLicNeadContants.NODE_LIC_18A_CHECK_CMD);
        final boolean lic19X = script(NodeLicNeadContants.NODE_LIC_19X_CHECK_CMD);
        int counter = 0;
        if (installLic != null) {
            for (final String insLic : installLic) {
                if (insLic.contains("17B") && !lic17B) {
                    executeScript(insLic, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
                } else if (insLic.contains("18A") && !lic18A) {
                    executeScript(insLic, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
                } else if (insLic.contains("19X") && !lic19X) {
                    executeScript(insLic, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
                }
                counter++;
            }
        }
        // Lic. Removal Part..
        if (removeLic != null) {
            for (final String remLic : removeLic) {
                if (remLic.contains("17B") && lic17B) {
                    executeScript(remLic, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
                } else if (remLic.contains("18A") && lic18A) {
                    executeScript(remLic, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
                } else if (remLic.contains("19X") && lic19X) {
                    executeScript(remLic, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
                }
                counter++;
            }
        }
        if (counter > 0) {
            executeScript(NodeLicNeadContants.CLEANUP_AND_RESTART_MC, NodeLicNeadContants.SCRIPT_REMOTE_PATH);
        }
    }

    @Override
    public List<Fdn> checkForFailedNodes(final List<Fdn> fdns, final int timeout) {
        final List<Fdn> failedNodes = new ArrayList<Fdn>();
        for (final Fdn fdn : fdns) {
            if (!checkSyncStatus(fdn, timeout)) {
                logger.warn("The Node " + fdn + " failed to sync.");
                failedNodes.add(fdn);
            }
        }
        return failedNodes;
    }

    @Override
    public List<Fdn> checkForSuccessCPPNodes(final List<Fdn> fdns, final int timeout) {
        final List<Fdn> successNodes = new ArrayList<Fdn>();
        for (final Fdn fdn : fdns) {
            if (checkSyncStatus(fdn, timeout)) {
                logger.debug("The Node " + fdn + " success to sync.");
                successNodes.add(fdn);
            }
        }
        return successNodes;
    }

    private boolean checkSyncStatus(final Fdn fdn, int timeout) {
        final int numOfSleeps = 100;
        if (!fdn.toString().contains("ERBS") && !fdn.toString().contains("RBS")) {
            timeout = timeout + 100;
        }

        final int sleepDuration = timeout * 1000 / numOfSleeps;
        int sleepCounter = 0;
        while (sleepCounter++ <= numOfSleeps) {
            final String value = csHandler.getAttributeValue(fdn, SYNCH_STATUS);
            switch (Integer.parseInt(value)) {
                case SYNCH_STATUS_SYNCHRONIZED:
                    return true;
                case SYNCH_STATUS_UNSYNCHRONIZED:
                    return false;
            }

            sleep(sleepDuration);

        }
        logger.warn("Timeout of " + timeout + " seconds exceeded while waiting for Node to sync");
        return false;
    }

    @Override
    public void startSyncOnNodes(final List<Fdn> fdns) {

        logger.debug("Starting a sync on " + fdns.size() + " node(s)");
        int counter = 0;
        for (final Fdn fdn : fdns) {
            counter++;
            if (counter == 50) {
                counter = 0;
                sleep(180000);
            }
            logger.debug("Starting a sync on " + fdn);
            csHandler.adjust(fdn);
        }
    }

    /**
     * @param sleepDuration
     */
    private void sleep(final int sleepDuration) {
        try {
            Thread.sleep(sleepDuration);
        } catch (final InterruptedException e) {
            logger.warn("Sleep was interrupted");
        }

    }

    @Override
    public void removeCopiedFilesAndRestoreServer(final String initialState) {
        // TODO Auto-generated method stub (Jan 4, 2018:2:03:16 PM by xaggpar)
        removeCopiedFiles();
        if (initialState.equalsIgnoreCase("19XLicPresent")) {
            installAndRemoveLic(new String[] { NodeLicNeadContants.INSTALL_17B_LIC, NodeLicNeadContants.INSTALL_18A_LIC, NodeLicNeadContants.INSTALL_19X_LIC },
                    null);
        } else if (initialState.equalsIgnoreCase("17BLicPre_18ALicPre_19XLicAb")) {
            installAndRemoveLic(new String[] { NodeLicNeadContants.INSTALL_17B_LIC, NodeLicNeadContants.INSTALL_18A_LIC },
                    new String[] { NodeLicNeadContants.REMOVE_19X_NODE_LICENSE });
        } else if (initialState.equalsIgnoreCase("17BLicPre_18ALicAb_19XLicAb")) {
            installAndRemoveLic(new String[] { NodeLicNeadContants.INSTALL_17B_LIC }, new String[] { NodeLicNeadContants.REMOVE_18A_NODE_LICENSE,
                    NodeLicNeadContants.REMOVE_19X_NODE_LICENSE });
        } else if (initialState.equalsIgnoreCase("AllLicAbsent")) {
            installAndRemoveLic(null, new String[] { NodeLicNeadContants.REMOVE_17B_NODE_LICENSE, NodeLicNeadContants.REMOVE_18A_NODE_LICENSE,
                    NodeLicNeadContants.REMOVE_19X_NODE_LICENSE });
        }
    }

}
