/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2018 Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.oss.cms.test.operators;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.AttributeType;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xaggpar
 */
public class BlackListAttributeCliOperator implements BlackListAttributesOperator {

    final Host host = HostGroup.getOssmaster();

    CLICommandHelper cliCmdHelper = new CLICommandHelper(host);

    private final static Logger logger = Logger.getLogger(BlackListAttributeCliOperator.class);

    /**
     * This method reads content of NEADBlacklistAttribute.csv file from server.
     *
     * @return List<String>
     */
    @Override
    public List<String> readBlackListAttrFileContent() {
        final String result = cliCmdHelper.execute("cat /opt/ericsson/nms_umts_cms_nead_seg/etc/NEADBlacklistAttribute.csv");
        logger.info("Content of the file : \n" + result);
        final List<String> blackListedMoAttrList = new ArrayList<String>();
        final String[] theRows = result.split("\n");
        logger.info("Total Rows : " + theRows.length);
        final int noOfRows = theRows.length;
        for (int i = 0; i < noOfRows; i++) {
            final String row = theRows[i];
            if (i == 0 || i == noOfRows - 1 || row.startsWith("#")) {
                continue; // Skip first(neType,MOName,attribute,blacklistAttribute) and last row (ossmaster{nmsadm} # )
            }
            final String[] theColums = row.split(",");

            if (theColums.length == 4) {
                int j = 0;
                final String neType = theColums[j];
                final String moName = theColums[++j];
                final String attribute = theColums[++j];
                final String isBlacklist = theColums[++j];

                logger.info("neType=" + neType + " moName=" + moName + " attribute=" + attribute + " isBlacklist=" + isBlacklist);
                if (isBlacklist != null && isBlacklist.trim().equalsIgnoreCase("true")) {
                    if (moName != null && attribute != null && !moName.trim().isEmpty() && !attribute.trim().isEmpty()) {
                        if (neType != null && neType.equalsIgnoreCase("ERBS")) {
                            if (attribute.equals(moName + "Id")) { // Skip Id attributes, like: MO:MOId.
                                continue;
                            }
                            blackListedMoAttrList.add(moName + ":" + attribute);
                        } else if (neType != null && neType.equalsIgnoreCase("RNC")) {
                            if (attribute.equals(moName + "Id")) {
                                continue;
                            }
                            blackListedMoAttrList.add(moName + ":" + attribute);
                        } else if (neType != null && neType.equalsIgnoreCase("RBS")) {
                            if (attribute.equals(moName + "Id")) {
                                continue;
                            }
                            blackListedMoAttrList.add(moName + ":" + attribute);
                        } else {
                            logger.info("Unsupported neType Entry:" + neType);
                        }
                    } else {
                        logger.info("Invalid MO or Attribute data, MO=" + moName + " Attribute=" + attribute);
                    }
                } else {
                    logger.info("This attribute is disabled, so do not blacklist:" + row);
                }
            } else {
                logger.info("Invalid MO::Attribute data Entry:" + row);
            }
        }
        logger.info("Attributes List read from server file : " + blackListedMoAttrList);
        return blackListedMoAttrList;
    }

    /**
     * @param attributesBefore
     * @param primaryAttributeValues
     * @return
     */
    @Override
    public String prepareValueToSetOnNetsim(final Attribute attributesBefore, final String[] primaryAttributeValues) {
        final StringBuilder subStr = new StringBuilder();
        // System.out.println("attributesBefore ==>> value :" + attributesBefore.getValue() + " Name :" + attributesBefore.getName() +
        // " Type :"
        // + attributesBefore.getType());
        if (attributesBefore.getType().toString().equalsIgnoreCase(AttributeType.STRUCT.name())) {
            final String str = attributesBefore.getValue();
            final int index1 = str.indexOf("{");
            subStr.append(str.substring(0, index1 + 1));
            for (final String value : primaryAttributeValues) {
                subStr.append("'" + value + "';");
            }
            subStr.append("}");
        } else {
            subStr.append(primaryAttributeValues[0]);
        }
        logger.info("Prepared Value for Netsim--->" + subStr);
        return subStr.toString();
    }

}

