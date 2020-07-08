package io.neolab.internship.coins.utils;

public class IdGenerator {
    private static int currentId = 0;

    public static int getCurrentId() {
        return ++currentId;
    }
}
