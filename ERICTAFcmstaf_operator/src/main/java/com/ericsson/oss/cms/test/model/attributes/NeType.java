/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.model.attributes;

/**
 * Enum to help with conversion from Node conventional names to neType.
 * 
 * @author xrajnka
 */
public enum NeType {
    RNC("RNC", 1),
    ERBS("ERBS", 4),
    DG2("DG2", 42),
    RANAG("RANAG", 3),
    RBS("RBS", 2),
    MGW("MGW", 7),
    RADIO("RADIO", 45),
    MSRBS_V1("MSRBS_V1", 32);

    private String neType;

    private int value;

    NeType(final String neType, final int value) {
        this.neType = neType;
        this.value = value;
    }

    /**
     * Get the conventional name of the Node.
     * 
     * @return A String representation of the neType.
     */
    public String getNeType() {
        return neType;
    }

    /**
     * Get the neType value.
     * 
     * @return the value.
     */
    public int getValue() {
        return value;
    }

    public static int getNeTypeValue(final String neType) {
        switch (neType) {
            case "RNC":
            case "1":
                return RNC.getValue();
            case "ERBS":
            case "4":
                return ERBS.getValue();
            case "DG2":
            case "42":
                return DG2.getValue();
            case "MSRBS_V1":
            case "32":
                return MSRBS_V1.getValue();
            case "RBS":
            case "2":
                return RBS.getValue();
            case "RXI":
            case "RANAG":
            case "3":
                return RANAG.getValue();
            case "MGW":
            case "7":
                return MGW.getValue();
            case "RADIO":
            case "45":
                return RADIO.getValue();
            default:
                return -1;
        }
    }
}