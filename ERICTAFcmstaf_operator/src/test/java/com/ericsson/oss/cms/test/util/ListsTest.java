package com.ericsson.oss.cms.test.util;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

public class ListsTest {

    @Test
    public void checkPercentageTestEven() {
        final List<?> listUnderTest = Collections.nCopies(100, new Object());

        assertEquals(Lists.reduce(listUnderTest, 0).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 1).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 10).size(), 10);
        assertEquals(Lists.reduce(listUnderTest, 80).size(), 80);
        assertEquals(Lists.reduce(listUnderTest, 11).size(), 11);
        assertEquals(Lists.reduce(listUnderTest, 99).size(), 99);

        assertEquals(listUnderTest.size(), 100);
    }

    @Test
    public void checkPercentageTestOdd() {
        final List<?> listUnderTest = Collections.nCopies(123, new Object());

        assertEquals(Lists.reduce(listUnderTest, 0).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 1).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 10).size(), 12);
        assertEquals(Lists.reduce(listUnderTest, 80).size(), 98);
        assertEquals(Lists.reduce(listUnderTest, 11).size(), 13);
        assertEquals(Lists.reduce(listUnderTest, 99).size(), 121);

        assertEquals(listUnderTest.size(), 123);
    }

    @Test
    public void checkPercentageTestSmallOdd() {
        final List<?> listUnderTest = Collections.nCopies(7, new Object());

        assertEquals(Lists.reduce(listUnderTest, 0).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 1).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 10).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 80).size(), 5);
        assertEquals(Lists.reduce(listUnderTest, 11).size(), 1);
        assertEquals(Lists.reduce(listUnderTest, 99).size(), 6);

        assertEquals(listUnderTest.size(), 7);
    }
}