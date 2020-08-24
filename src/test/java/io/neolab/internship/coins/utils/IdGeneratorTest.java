package io.neolab.internship.coins.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdGeneratorTest {
    @Test
    public void testGetCurrentId() {
        int currentId1 = IdGenerator.getCurrentId();
        int currentId2 = IdGenerator.getCurrentId();
        assertTrue(currentId1 > 0);
        assertTrue(currentId2 > currentId1);
        currentId1 = IdGenerator.getCurrentId();
        currentId2 = IdGenerator.getCurrentId();
        assertTrue(currentId2 > currentId1);
        currentId1 = IdGenerator.getCurrentId();
        currentId2 = IdGenerator.getCurrentId();
        assertTrue(currentId2 > currentId1);
        currentId1 = IdGenerator.getCurrentId();
        currentId2 = IdGenerator.getCurrentId();
        assertTrue(currentId2 > currentId1);
        currentId1 = IdGenerator.getCurrentId();
        currentId2 = IdGenerator.getCurrentId();
        assertTrue(currentId2 > currentId1);
    }
}
