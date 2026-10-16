package com.boomlibrary.service;

import com.boomlibrary.dto.request.BookRequest;
import com.boomlibrary.util.TextNormalizer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads the first sheet of an .xlsx workbook into book requests.
 * The header row is the first of the top rows that names a book title column, and
 * columns are matched by keywords in their header ("Author name", "Book category", ...),
 * so the sheet's column order and exact wording do not matter.
 */
@Component
public class ExcelBookParser {

    public record ParseResult(
            List<BookRequest> books,
            List<String> errors,
            Map<String, String> columns,
            List<String> unmatchedHeaders) {}

    private record Column(int index, String header) {}

    private static final int HEADER_SCAN_ROWS = 10;

    private static final Set<String> TRUE_VALUES = Set.of("y", "yes", "true", "1", "x", "read", "done", "finished");

    // Day-first, since that's the usual format for dates typed in non-US sheets.
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("d MMM uuuu")
    );

    /** Parses the named sheet, or the first sheet when sheetName is null. */
    public ParseResult parse(InputStream in, String sheetName) throws IOException {
        List<BookRequest> books = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = sheetName == null
                    ? workbook.getSheetAt(0)
                    : workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in the workbook");
            }

            // The header is the scanned row that maps the most columns, so a title line
            // above the table (e.g. "My Book Collection") is not mistaken for it.
            int headerIndex = -1;
            Map<String, Column> columns = Map.of();
            int lastScanned = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + HEADER_SCAN_ROWS - 1);
            for (int i = sheet.getFirstRowNum(); i <= lastScanned; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, Column> found = mapColumns(row, formatter);
                if (found.containsKey("bookName") && found.size() > columns.size()) {
                    headerIndex = i;
                    columns = found;
                }
            }
            if (headerIndex < 0) {
                throw new IllegalArgumentException(
                        "No book title column found in the first rows. Add a column named 'Book name' or 'Title'.");
            }

            Map<String, String> mapped = new LinkedHashMap<>();
            columns.forEach((field, column) -> mapped.put(field, column.header()));
            List<String> unmatched = unmatchedHeaders(sheet.getRow(headerIndex), columns, formatter);

            for (int i = headerIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int rowNumber = i + 1;
                try {
                    BookRequest book = toBookRequest(row, columns, formatter);
                    if (book != null) books.add(book);
                } catch (IllegalArgumentException ex) {
                    errors.add("Row " + rowNumber + ": " + ex.getMessage());
                }
            }
            return new ParseResult(books, errors, mapped, unmatched);
        }
    }

    private Map<String, Column> mapColumns(Row header, DataFormatter formatter) {
        Map<String, Column> columns = new LinkedHashMap<>();
        for (Cell cell : header) {
            String text = formatter.formatCellValue(cell).trim();
            String field = classify(normalize(text));
            if (field != null) columns.putIfAbsent(field, new Column(cell.getColumnIndex(), text));
        }
        return columns;
    }

    private List<String> unmatchedHeaders(Row header, Map<String, Column> columns, DataFormatter formatter) {
        List<String> unmatched = new ArrayList<>();
        Set<Integer> used = new java.util.HashSet<>();
        columns.values().forEach(c -> used.add(c.index()));
        for (Cell cell : header) {
            String text = formatter.formatCellValue(cell).trim();
            if (!text.isEmpty() && !used.contains(cell.getColumnIndex())) unmatched.add(text);
        }
        return unmatched;
    }

    /**
     * Maps a normalized header to a book field. Order matters: "Book title" must be a title
     * and "Publisher" must not be read as a book name, so the specific keywords are checked
     * before the generic "book" and "name".
     */
    private String classify(String key) {
        if (key.isEmpty()) return null;
        if (key.contains("title")) return "bookName";
        if (key.contains("publisher")) return "publisher";
        if (key.contains("author") || key.contains("auther")) return "author";
        if (key.contains("categor") || key.contains("genre")) return "category";
        if (key.contains("bought") || key.contains("purchas") || key.contains("date")) return "boughtAt";
        if (key.contains("read") || key.contains("status")) return "read";
        if (key.contains("comment") || key.contains("note") || key.contains("remark")) return "comments";
        if (key.contains("language")) return "language";
        if (key.contains("book") || key.equals("name")) return "bookName";
        return null;
    }

    /** Returns null for rows with no book name (blank lines). */
    private BookRequest toBookRequest(Row row, Map<String, Column> columns, DataFormatter formatter) {
        String bookName = text(row, columns, "bookName", formatter);
        if (bookName == null) return null;

        String author = text(row, columns, "author", formatter);
        String publisher = text(row, columns, "publisher", formatter);
        String category = text(row, columns, "category", formatter);
        String comments = text(row, columns, "comments", formatter);
        String language = text(row, columns, "language", formatter);
        if (language != null) {
            comments = joinNotes(comments, "Language: " + language);
        }

        if (bookName.length() > 500) throw new IllegalArgumentException("book name is longer than 500 characters");
        if (author != null && author.length() > 255) throw new IllegalArgumentException("author is longer than 255 characters");
        if (publisher != null && publisher.length() > 255) throw new IllegalArgumentException("publisher is longer than 255 characters");
        if (category != null && category.length() > 100) throw new IllegalArgumentException("category is longer than 100 characters");

        Instant boughtAt = null;
        Column dateColumn = columns.get("boughtAt");
        if (dateColumn != null) {
            Cell dateCell = row.getCell(dateColumn.index());
            try {
                boughtAt = parseDate(dateCell, formatter);
            } catch (IllegalArgumentException ex) {
                // A note such as "* From MNM" is not a date: keep it as a comment instead of rejecting the row.
                comments = joinNotes(comments, "Bought: " + formatter.formatCellValue(dateCell).trim());
            }
        }

        boolean read = false;
        String readText = text(row, columns, "read", formatter);
        if (readText != null) {
            read = TRUE_VALUES.contains(readText.toLowerCase());
        }

        return new BookRequest(bookName, author, publisher, category, boughtAt, read, comments, null);
    }

    private String text(Row row, Map<String, Column> columns, String field, DataFormatter formatter) {
        Column column = columns.get(field);
        if (column == null) return null;
        return TextNormalizer.clean(formatter.formatCellValue(row.getCell(column.index())));
    }

    private String joinNotes(String existing, String note) {
        return existing == null ? note : existing + "\n" + note;
    }

    private Instant parseDate(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        }

        String value = formatter.formatCellValue(cell).trim();
        if (value.isEmpty()) return null;

        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        throw new IllegalArgumentException("unrecognised date '" + value + "'");
    }

    private String normalize(String header) {
        return header == null ? "" : header.toLowerCase().replaceAll("[^a-z]", "");
    }
}
