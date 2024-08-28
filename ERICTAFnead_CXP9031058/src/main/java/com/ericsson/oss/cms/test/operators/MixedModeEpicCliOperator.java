/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2017 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.operators;

import java.util.*;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.cshandler.*;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author Paras Aggarwal
 */
public class MixedModeEpicCliOperator implements MixedModeEpicOperator {

    private final static Logger logger = Logger.getLogger(LicenseSyncCliOperator.class);

    CLICommandHelper cliCommandHelper;

    private final CSHandler csHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);

    final private static String MIXEX_MODE_RADIO = "mixedModeRadio";

    private final static String MIRRORMIBVERSION = "mirrorMIBversion";

    final static Logger LOGGER = Logger.getLogger(MixedModeEpicCliOperator.class);

    @Override
    public boolean getMixedModeAttributeValue(final Fdn moFdn) {
        final Fdn meContextFdn = moFdn.getMeContext();
        final String mixedModeRadio = csHandler.getAttributeValue(meContextFdn, MIXEX_MODE_RADIO);
        return Boolean.parseBoolean(mixedModeRadio);
    }

    /*
     * isSharedWithExternalMe Attribute has introduced for ERBS node models >= H.1.220
     * and for RBS >= U.4.490...
     */
    @Override
    public boolean isSharedWithExternalMeSupported(final Fdn meContext, final String nodeType, final String baseNodeVersion) {
        boolean isSupported = false;
        final String[] baseVerArr = baseNodeVersion.split("\\.");
        if (nodeType.equals("ERBS")) {
            final String mirrorVersion = csHandler.getAttributeValue(meContext, MIRRORMIBVERSION);// checkMirrorVersion(meContext);
            final String[] versArr = mirrorVersion.split("\\.");
            final int i = versArr[0].compareTo(baseVerArr[0]);
            if (i > 0) {
                isSupported = true;
            } else if (i == 0) {
                if (Integer.valueOf(versArr[2]) >= Integer.valueOf(baseVerArr[2])) {
                    isSupported = true;
                } else {
                    isSupported = false;
                }
            } else {
                isSupported = false;
            }
        } else if (nodeType.equals("RBS")) {
            final String mirrorVersion = csHandler.getAttributeValue(meContext, MIRRORMIBVERSION);// checkMirrorVersion(meContext);
            final String[] versArr = mirrorVersion.split("\\.");
            final int i = versArr[0].compareTo(baseVerArr[0]);
            if (i > 0) {
                isSupported = true;
            } else if (i == 0) {
                if (Integer.valueOf(versArr[2]) >= Integer.valueOf(baseVerArr[2])) {
                    isSupported = true;
                } else {
                    isSupported = false;
                }
            } else {
                isSupported = false;
            }
        }
        return isSupported;
    }

    @Override
    public Map<Fdn, String> updateAttributeValue(final List<Fdn> listFdn, final String attribute, final String attributeValues) {
        final Map<Fdn, String> map = new HashMap<Fdn, String>();
        if (listFdn.size() > 0) {
            int i = 0;
            for (final Fdn moFdn : listFdn) {
                final String csOutput = csHandler.getAttributeValue(moFdn, attribute);
                if (csOutput.equalsIgnoreCase("true")) {
                    csHandler.setAttributeValue(moFdn, attribute, attributeValues);
                    System.out.println("Attribute " + attribute + " with value = " + attributeValues + " is Updated successfuly for MO : " + moFdn);
                    map.put(moFdn, attribute + "=" + csOutput);
                    i++;
                }
            }
            if (i > 0) {
                logger.info(i + "MOs updated successfully.");
            } else {
                logger.info("No Other MO found with true value of " + attribute + " Attribute.");
            }
        }
        return map;
    }

    public Map<Fdn, Boolean> getMixedModeFdns(final List<Fdn> syncNodes, final String nodeType, final String baseNodeVersion) {
        int count = 0;
        final HashMap<Fdn, Boolean> fdnMap = new HashMap<Fdn, Boolean>();
        if (syncNodes.size() > 0) {
            for (final Fdn moFdn : syncNodes) {
                if (count == 2) {
                    break;
                } else {
                    final boolean flag = isSharedWithExternalMeSupported(moFdn, nodeType, baseNodeVersion);
                    if (!fdnMap.containsValue(flag)) {
                        fdnMap.put(moFdn, flag);
                        count++;
                    }
                }
            }
        }
        return fdnMap;
    }

    /**
     * @param finalrevMap
     * @return
     */
    public void updateAttributeValueFdn(final Map<Fdn, String> finalrevMap) {
        for (final Map.Entry<Fdn, String> map1 : finalrevMap.entrySet()) {
            final Fdn moFdn = map1.getKey();
            final String attValue[] = map1.getValue().split("=");
            csHandler.setAttributeValue(moFdn, attValue[0], attValue[1]);
        }
    }
}
