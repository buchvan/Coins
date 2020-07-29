package io.neolab.internship.coins.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdGeneratorTest {
    @Test
    public void testGetCurrentId() {
        assertEquals(0, IdGenerator.getCurrentId());
        assertEquals(1, IdGenerator.getCurrentId());
        assertEquals(2, IdGenerator.getCurrentId());
        assertEquals(3, IdGenerator.getCurrentId());
        assertEquals(4, IdGenerator.getCurrentId());
    }
}
