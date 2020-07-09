package com.jon.cotgenerator.utils;

import java.util.Random;

public final class GenerateInt {
    private static int i = 3;
    private static Random rand = new Random();

    private GenerateInt() { }

    public static int next() {
        return i++;
    }

    public static int random(final int max) {
        return rand.nextInt(max);
    }
}