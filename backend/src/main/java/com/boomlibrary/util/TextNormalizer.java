package com.boomlibrary.util;

import java.text.Normalizer;
import java.util.Locale;

/** Cleans text from spreadsheets and builds the key used to recognise the same book. */
public final class TextNormalizer {

    private TextNormalizer() {}

    /** Unicode-normalizes, collapses runs of whitespace (including line breaks) and trims. Null if blank. */
    public static String clean(String value) {
        if (value == null) return null;
        String cleaned = Normalizer.normalize(value, Normalizer.Form.NFC)
                .replaceAll("[\\s\\u00A0]+", " ")
                .trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /** Key for matching duplicates: the cleaned title, ignoring case. */
    public static String bookKey(String bookName) {
        String cleaned = clean(bookName);
        return cleaned == null ? "" : cleaned.toLowerCase(Locale.ROOT);
    }
}
