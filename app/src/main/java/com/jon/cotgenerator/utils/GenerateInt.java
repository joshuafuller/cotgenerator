package com.jon.cotgenerator.utils;

public final class GenerateInt {
    private static int i = 3;

    private GenerateInt() { }

    public static int next() {
        return i++;
    }
}