package com.tanxe.quran.util;

import java.util.regex.Pattern;

public class ArabicUtils {

    // Arabic diacritics (tashkeel) Unicode range
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E4\\u06E7\\u06E8\\u06EA-\\u06ED\\u08D4-\\u08E1\\u08D4-\\u08ED\\u08F0-\\u08FE]");

    // Tatweel (kashida)
    private static final Pattern TATWEEL_PATTERN = Pattern.compile("[\\u0640]");

    public static String stripDiacritics(String text) {
        if (text == null) return "";
        String result = DIACRITICS_PATTERN.matcher(text).replaceAll("");
        result = TATWEEL_PATTERN.matcher(result).replaceAll("");
        return result.trim();
    }

    public static String normalizeArabic(String text) {
        if (text == null) return "";
        text = stripDiacritics(text);
        // Normalize alef variations
        text = text.replace('\u0622', '\u0627'); // Alef Madda -> Alef
        text = text.replace('\u0623', '\u0627'); // Alef Hamza Above -> Alef
        text = text.replace('\u0625', '\u0627'); // Alef Hamza Below -> Alef
        // Normalize teh marbuta -> heh
        text = text.replace('\u0629', '\u0647');
        // Normalize alef maksura -> yeh
        text = text.replace('\u0649', '\u064A');
        return text;
    }

    public static boolean isArabic(String text) {
        if (text == null || text.isEmpty()) return false;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ARABIC) {
                return true;
            }
        }
        return false;
    }

    public static String formatAyahNumber(int number) {
        // Convert to Arabic-Indic numerals
        StringBuilder sb = new StringBuilder();
        String numStr = String.valueOf(number);
        for (char c : numStr.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append((char) (c - '0' + '\u0660'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Get Arabic ordinal for Juz
    public static String getJuzLabel(int juz) {
        return "الجزء " + formatAyahNumber(juz);
    }
}
