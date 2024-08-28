package com.ericsson.oss.cms.test.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.taf.cshandler.model.Attribute;

public abstract class AttributeValueConverter {

    public final static String NETSIM_MOREF_DELIMITERS = "[\\\\\\]\\['\"]";

    /**
     * Method to convert cstest attribute value representations to netsim attribute value representations.
     * 
     * @param databaseValue
     *        A <code>String</code> representation of attribute value in the database.
     * @return
     *         A <code>String</code> representation of attribute value on the node.
     */
    public static String convertDBToNetsimValue(final Attribute attribute) {
        switch (attribute.getType()) {
            case MO:
                return convertMoRef(attribute.getValue());

            case SEQ:
                return convertSequence(attribute.getValue());

            case STRUCT:
                return convertStruct(attribute.getValue());

            default:
                return attribute.getValue();
        }

    }

    private static String convertSequence(final String databaseValue) {
        final Pattern attributeTypePattern = Pattern.compile("^(alias|struct|sequence|.*)");
        final Matcher attributeTypeMatcher = attributeTypePattern.matcher(databaseValue);
        attributeTypeMatcher.find();
        switch (attributeTypeMatcher.group(1)) {
            case "alias":
                return convertStructSequence(databaseValue);

            case "struct":
                return convertStruct(databaseValue);

            case "sequence":
                return convertSequenceInStruct(databaseValue);

            default:
                return convertSimpleOrSequenceOfSimple(databaseValue);
        }

    }

    private static String convertStructSequence(final String databaseValue) {
        final String structSequencePattern = ".*?\\{\\{(.*)\\}\\}";
        final String structSequenceSeparator = ",";
        return convertComplexAttributes(databaseValue, structSequencePattern, structSequenceSeparator);
    }

    private static String convertComplexAttributes(final String databaseValue, final String regex, final String separator) {
        final Pattern complexAttributePattern = Pattern.compile(regex);
        final Matcher complexAttributeMatcher = complexAttributePattern.matcher(databaseValue);
        complexAttributeMatcher.find();
        final String[] complexAttributeMembers = complexAttributeMatcher.group(1).split(separator);
        final String[] netsimValue = new String[complexAttributeMembers.length];

        for (int i = 0; i < complexAttributeMembers.length; i++) {
            netsimValue[i] = convertSequence(complexAttributeMembers[i]);
        }

        return Arrays.toString(netsimValue);
    }

    private static String convertStruct(final String databaseValue) {
        final String structPattern = ".*?\\{(.*)\\}";
        final String structSeparator = ";";
        return convertComplexAttributes(databaseValue, structPattern, structSeparator);
    }

    private static String convertSequenceInStruct(final String databaseValue) {
        final String sequenceInStructPattern = "\\{(.*)\\}";
        final String sequenceInStructSeparator = ",";
        return convertComplexAttributes(databaseValue, sequenceInStructPattern, sequenceInStructSeparator);
    }

    private static String convertSimpleOrSequenceOfSimple(final String databaseValue) {

        if (isMoRefOrSequenceOfMoRef(databaseValue)) {
            return convertMoRefSequence(databaseValue);
        } else if (isSimpleTypeWithTypeDefinition(databaseValue)) {
            return databaseValue.split("=")[1];
        } else if (isSequenceOfSimpleType(databaseValue)) {
            return convertSimpleSequence(databaseValue);
        }
        return databaseValue;
    }

    private static boolean isMoRefOrSequenceOfMoRef(final String databaseValue) {
        final String pattern = "SubNetwork=.*?MeContext=";
        final Pattern moRefPattern = Pattern.compile(pattern);
        final Matcher moRefMatcher = moRefPattern.matcher(databaseValue);
        return moRefMatcher.find();
    }

    private static boolean isSimpleTypeWithTypeDefinition(final String databaseValue) {
        final Pattern simplePattern = Pattern.compile("\\w+\\s\\w+=");
        final Matcher simpleMatcher = simplePattern.matcher(databaseValue);
        return simpleMatcher.find();
    }

    private static boolean isSequenceOfSimpleType(final String databaseValue) {
        final Pattern simpleSequencePattern = Pattern.compile("\\w\\s.*");
        final Matcher simpleSequenceMatcher = simpleSequencePattern.matcher(databaseValue);
        return simpleSequenceMatcher.find();
    }

    private static String convertSimpleSequence(final String databaseValue) {
        final String[] match = databaseValue.split(" ");
        return Arrays.toString(match);
    }

    private static String convertMoRefSequence(final String databaseValue) {
        final String[] match = databaseValue.split(" ");
        for (int i = 0; i < match.length; i++) {
            match[i] = convertMoRef(match[i]);
        }
        return Arrays.toString(match);
    }

    private static String convertMoRef(final String databaseValue) {
        final int moRefValue = databaseValue.indexOf("ManagedElement");
        return databaseValue.substring(moRefValue);
    }
}