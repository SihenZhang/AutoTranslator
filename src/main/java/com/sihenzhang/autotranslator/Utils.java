package com.sihenzhang.autotranslator;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonObject;

import java.util.regex.Pattern;

public final class Utils {
    private Utils() {
    }

    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private static final Pattern TRANSLATABLE_PATTERN = Pattern.compile(".*\\p{L}+.*");

    /**
     * Checks if the given text contains translatable content by removing format patterns and checking for letter characters.
     *
     * @param text The text to check for translatable content
     * @return true if the text contains translatable content (letters), false otherwise or if text is null/blank
     */
    public static boolean hasTranslatableText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        var stripped = FORMAT_PATTERN.matcher(text).replaceAll("");
        return TRANSLATABLE_PATTERN.matcher(stripped).matches();
    }

    /**
     * Merges multiple JsonObjects into the target JsonObject by copying all entries.
     * Similar to Object.assign() in JavaScript.
     *
     * @param object  The target JsonObject to merge into
     * @param sources Variable number of source JsonObjects to merge from
     * @return The modified target JsonObject containing all merged entries
     */
    @CanIgnoreReturnValue
    public static JsonObject assign(JsonObject object, JsonObject... sources) {
        for (var source : sources) {
            source.entrySet().forEach(entry -> object.add(entry.getKey(), entry.getValue()));
        }
        return object;
    }
}