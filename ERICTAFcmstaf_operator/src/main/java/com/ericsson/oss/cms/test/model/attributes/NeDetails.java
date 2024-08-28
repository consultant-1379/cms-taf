/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.model.attributes;

/**
 * ENUM to map conventional node name to its function mo and mim name.
 * 
 * @author ecolhar
 */
public enum NeDetails {
    RNC("RncFunction", "RNC_NODE_MODEL"),
    RBS("NodeBFunction", "RBS_NODE_MODEL"),
    EPIC_RBS("NodeBFunction", "EPIC_WCDMA_MOM"),
    MSV1_RBS("NodeBFunction", "MSRBS_V1_NodeBFunction"),
    ERBS("ENodeBFunction", "ERBS_NODE_MODEL"),
    LTEDG2("ENodeBFunction", "Lrat"),
    MSRBS_V1("ENodeBFunction", "MSRBS_V1_eNodeBFunction"),
    RADIO("ENodeBFunction", "ECIM_TOP"),
    MGW("MgwApplication", "MGW_NODE_MODEL");

    private String functionMo;

    private String mim;

    NeDetails(final String functionMo, final String mim) {
        this.functionMo = functionMo;
        this.mim = mim;
    }

    /**
     * Gets the Function MO name for this neType.
     * 
     * @return The function mo name for this neType.
     */
    public String getFunctionMo() {
        return functionMo;
    }

    /**
     * Gets the Mim name name for this neType.
     * 
     * @return The mim name for this neType.
     */
    public String getMim() {
        return mim;
    }

    /**
     * Gets the function mo name for the given neType.
     * 
     * @param neType
     *        Conventional neType name.
     * @return The function mo name for this neType.
     */
    public static String getNeFunctionMo(final String neType) {
        switch (neType) {
            case "RNC":
                return RNC.getFunctionMo();
            case "RBS":
                return RBS.getFunctionMo();
            case "EPIC_RBS":
                return EPIC_RBS.getFunctionMo();
            case "MSV1_RBS":
                return MSV1_RBS.getFunctionMo();
            case "ERBS":
                return ERBS.getFunctionMo();
            case "LTEDG2":
                return LTEDG2.getFunctionMo();
            case "MSRBS_V1":
                return MSRBS_V1.getFunctionMo();
            case "RADIO":
                return RADIO.getFunctionMo();
	    case "MGW":
		return MGW.getFunctionMo();
            default:
                return "";
        }
    }

    /**
     * Gets the mim name for the given neType.
     * 
     * @param neType
     *        Conventional neType name.
     * @return The mim name for this neType.
     */
    public static String getNeMimName(final String neType) {
        switch (neType) {
            case "RNC":
                return RNC.getMim();
            case "RBS":
                return RBS.getMim();
            case "EPIC_RBS":
                return EPIC_RBS.getMim();
            case "MSV1_RBS":
                return MSV1_RBS.getMim();
            case "ERBS":
                return ERBS.getMim();
            case "LTEDG2":
                return LTEDG2.getMim();
            case "MSRBS_V1":
                return MSRBS_V1.getMim();
            case "RADIO":
                return RADIO.getMim();
	    case "MGW":
		return MGW.getMim();
            default:
                return "";
        }
    }
}
