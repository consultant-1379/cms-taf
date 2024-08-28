/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.cms.test.util;

import static org.apache.commons.lang.StringUtils.deleteWhitespace;

import org.apache.commons.lang.StringUtils;

public class StringUtil {

    /**
     * Compares two string objects to each other after removing delimiters and white space.
     * 
     * @param firstValue
     *        The first <code>String</code> value to be compared.
     * @param secondValue
     *        The second <code>String</code> value to be compared.
     * @param regexDelimiter
     *        The regular expression <code>String</code> that represents the delimiters to be replaced
     * @return
     *         <code>true</code> if the given two strings represent the same <code>String</code> after their delimiters and white spaces are
     *         removed, false otherwise.
     */
    public static boolean equalsIgnoreDelimiters(final String firstValue, final String secondValue, final String regexDelimiter) {

        final String firstValueModified = firstValue.replaceAll(regexDelimiter, "");
        final String secondValueModified = secondValue.replaceAll(regexDelimiter, "");
        return equalsIgnoreSpace(firstValueModified, secondValueModified);
    }

    /**
     * Compares two string objects to each other after removing their empty spaces.
     * 
     * @param firstValue
     *        The first <code>String</code> value to be compared.
     * @param secondValue
     *        The second <code>String</code> value to be compared.
     * @return
     *         <code>true</code> if the given two strings represent the same <code>String</code> after their empty spaces are
     *         removed, false otherwise.
     */
    public static boolean equalsIgnoreSpace(final String firstValue, final String secondValue) {
        return StringUtils.equals(deleteWhitespace(firstValue), deleteWhitespace(secondValue));
    }
}