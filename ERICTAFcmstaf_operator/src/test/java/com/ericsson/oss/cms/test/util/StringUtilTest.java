/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import static com.ericsson.oss.cms.test.util.AttributeValueConverter.NETSIM_MOREF_DELIMITERS;
import static com.ericsson.oss.cms.test.util.StringUtil.equalsIgnoreDelimiters;
import static com.ericsson.oss.cms.test.util.StringUtil.equalsIgnoreSpace;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringUtilTest {

    String firstFdn = "ManagedElement=1,NodeBFunction=1,Sector=1,Carrier=1";

    String secondFdn = "ManagedElement=1,NodeBFunction=1,Sector=1,Carrier=2";

    @Test
    public void equalsIgnoreSpaceWithNoSpacesFalse() {
        final String firstValue = "abc";
        final String secondValue = "def";
        assertFalse(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceFirstOneWithOneSpaceFalse() {
        final String firstValue = "a bd";
        final String secondValue = "abc";
        assertFalse(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceSecondOneWithOneSpaceFalse() {
        final String firstValue = "abc";
        final String secondValue = "bb c";
        assertFalse(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceFirstOneWithMultipleSpacesFalse() {
        final String firstValue = "d  e f";
        final String secondValue = "abc";
        assertFalse(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceSecondOneWithMultipleSpacesFalse() {
        final String firstValue = "abc";
        final String secondValue = "g b  c ";
        assertFalse(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceWithNoSpacesTrue() {
        final String firstValue = "abc";
        final String secondValue = "abc";
        assertTrue(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceFirstOneWithOneSpaceTrue() {
        final String firstValue = "a bc";
        final String secondValue = "abc";
        assertTrue(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceSecondOneWithOneSpaceTrue() {
        final String firstValue = "abc";
        final String secondValue = "ab c";
        assertTrue(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceFirstOneWithMultipleSpacesTrue() {
        final String firstValue = "a  b c";
        final String secondValue = "abc";
        assertTrue(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreSpaceSecondOneWithMultipleSpacesTrue() {
        final String firstValue = "abc";
        final String secondValue = "a b  c ";
        assertTrue(equalsIgnoreSpace(firstValue, secondValue));
    }

    @Test
    public void equalsIgnoreDelimitersOneMoRef() {
        final String secondValue = "'[\\\"'\"" + firstFdn + "\"'\\\"]'";
        final String firstValue = "[" + firstFdn + "]";
        assertTrue(firstValue + " is equal " + secondValue, equalsIgnoreDelimiters(firstValue, secondValue, NETSIM_MOREF_DELIMITERS));
    }

    @Test
    public void equalsIgnoreDelimitersTwoMoRefs() {
        final String secondValue = "'[\\\"'\"" + firstFdn + "\"'\\\"'" + "," + "'\\\"'\"" + secondFdn + "\"'\\\"]'";
        final String firstValue = "[" + firstFdn + ", " + secondFdn + "]";
        assertTrue(firstValue + " is equal " + secondValue, equalsIgnoreDelimiters(firstValue, secondValue, NETSIM_MOREF_DELIMITERS));
    }

    @Test
    public void equalsIgnoreDelimitersIncludingSquareBrackets() {
        final String firstValue = "[" + firstFdn + "]";
        final String secondValue = firstFdn;
        assertTrue(equalsIgnoreDelimiters(firstValue, secondValue, NETSIM_MOREF_DELIMITERS));
    }

}