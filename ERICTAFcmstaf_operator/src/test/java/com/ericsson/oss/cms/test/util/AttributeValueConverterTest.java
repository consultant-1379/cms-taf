package com.ericsson.oss.cms.test.util;

import static com.ericsson.oss.cms.test.util.AttributeValueConverter.convertDBToNetsimValue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ericsson.oss.taf.cshandler.model.Attribute;
import com.ericsson.oss.taf.cshandler.model.AttributeType;
import com.ericsson.oss.taf.cshandler.model.CSAttribute;

public class AttributeValueConverterTest {

    @Test
    public void emptyValue() {
        final Attribute csValue = new CSAttribute("test", "", AttributeType.STRING);
        final String expectedValue = "";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void stringWithSpace() {
        final Attribute csValue = new CSAttribute("test", "First Last", AttributeType.STRING);
        final String expectedValue = "First Last";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void simpleTypeLong() {
        final Attribute csValue = new CSAttribute("test", "3", AttributeType.LONG);
        final String expectedValue = "3";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void seqOfSimpleTypeLong() {
        final Attribute csValue = new CSAttribute("test", "1 2 1 2 1 2", AttributeType.SEQ);
        final String expectedValue = "[1, 2, 1, 2, 1, 2]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void simpleTypeBoolean() {
        final Attribute csValue = new CSAttribute("test", "true", AttributeType.BOOLEAN);
        final String expectedValue = "true";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void seqOfSimpleTypeBoolean() {
        final Attribute csValue = new CSAttribute("test", "true false false true false false false", AttributeType.SEQ);
        final String expectedValue = "[true, false, false, true, false, false, false]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void simpleTypeString() {
        final Attribute csValue = new CSAttribute("test", "test1", AttributeType.STRING);
        final String expectedValue = "test1";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void seqOfSimpleTypeString() {
        final Attribute csValue = new CSAttribute("test", "test1 test2 test3 test4", AttributeType.SEQ);
        final String expectedValue = "[test1, test2, test3, test4]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void seqOfStructsContainsMultipleSimpleType() {
        final Attribute csValue = new CSAttribute("test", "alias PlmnIdentitySeq {{struct PlmnIdentity{long mcc=353;long mnc=57;long mncLength=2;},struct PlmnIdentity{long mcc=353;long mnc=57;long mncLength=2;},struct PlmnIdentity{long mcc=353;long mnc=57;long mncLength=2;}}}", AttributeType.SEQ);
        final String expectedValue = "[[353, 57, 2], [353, 57, 2], [353, 57, 2]]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void structContainsMultipleSimpleType() {
        final Attribute csValue = new CSAttribute("test", "struct AseLoadThresholdUlSpeech{long amr12200=100;long amr7950=100;long amr5900=100;long amrWb8850=100;long amrWb12650=100;}", AttributeType.STRUCT);
        final String expectedValue = "[100, 100, 100, 100, 100]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void structContainsSingleSimpleType() {
        final Attribute csValue = new CSAttribute("test", "struct ServiceRestrictions{long csVideoCalls=0;}", AttributeType.STRUCT);
        final String expectedValue = "[0]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void structContainsSimpleLongAndBooleanType() {
        final Attribute csValue = new CSAttribute("test", "struct SIB3{long qHyst=4;long tEvaluation=240;long tHystNormal=240;long nCellChangeMedium=16;long nCellChangeHigh=16;long qHystSfMedium=0;long qHystSfHigh=0;long sIntraSearch=1000;long sNonIntraSearch=0;long threshServingLow=0;long sIntraSearchQ=0;long sIntraSearchP=62;long sNonIntraSearchQ=0;long sNonIntraSearchP=0;long threshServingLowQ=1000;boolean sIntraSearchv920Active=false;boolean sNonIntraSearchv920Active=false;}", AttributeType.STRUCT);
        final String expectedValue = "[4, 240, 240, 16, 16, 0, 0, 1000, 0, 0, 0, 62, 0, 0, 1000, false, false]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void structContainsSimpleAndSequenceOfSimpleType1() {
        final Attribute csValue = new CSAttribute("test", "struct AcBarringConfig{long acBarringFactor=95;long acBarringTime=64;sequence[0]<boolean> acBarringForSpecialAC={false,false,false,false,false};}", AttributeType.STRUCT);
        final String expectedValue = "[95, 64, [false, false, false, false, false]]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void structContainsSimpleAndSequenceOfSimpleType2() {
        final Attribute csValue = new CSAttribute("test", "struct PpacConfig{sequence[0]<long> locRegAcb={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};long locRegRestr=0;long pagingRespRestr=0;}", AttributeType.STRUCT);
        final String expectedValue = "[[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], 0, 0]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void seqOfOneMoRef() {
        final Attribute csValue = new CSAttribute("test", "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC08,MeContext=RNC08RBS03,ManagedElement=1,NodeBFunction=1,Sector=1,Carrier=1", AttributeType.SEQ);
        final String expectedValue = "[ManagedElement=1,NodeBFunction=1,Sector=1,Carrier=1]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void seqOfTwoMoRef() {
        final Attribute csValue = new CSAttribute("test", "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-9,MeContext=LTE48ERBS00001,ManagedElement=1,SectorEquipmentFunction=8 SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-9,MeContext=LTE48ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE48ERBS00001-8", AttributeType.SEQ);
        final String expectedValue = "[ManagedElement=1,SectorEquipmentFunction=8, ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE48ERBS00001-8]";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void moRef() {
        final Attribute csValue = new CSAttribute("test", "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=ERBS-SUBNW-9,MeContext=LTE48ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE48ERBS00001-8", AttributeType.MO);
        final String expectedValue = "ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE48ERBS00001-8";
        final String actualValue = convertDBToNetsimValue(csValue);
        assertEquals(expectedValue, actualValue);
    }
}