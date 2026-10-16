package com.boomlibrary.dto.response;

import java.util.List;
import java.util.Map;

/**
 * @param imported new books added
 * @param updated existing books that had blank fields filled from the sheet
 * @param skipped rows that duplicated a book already in the library or an earlier row in the file
 * @param columns field name to the sheet header that was mapped to it, e.g. author -> "Author name"
 * @param unmatchedHeaders sheet headers that were not used, so mismatches are visible
 */
public record BookImportResponse(
        int imported,
        int updated,
        int skipped,
        List<String> errors,
        Map<String, String> columns,
        List<String> unmatchedHeaders) {}
