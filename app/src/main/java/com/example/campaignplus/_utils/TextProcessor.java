package com.example.campaignplus._utils;

public class TextProcessor {
    public static int parseInt(String input, int defaultValue) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
