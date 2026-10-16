package com.boomlibrary;

import com.boomlibrary.dto.request.BookRequest;
import com.boomlibrary.service.ExcelBookParser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelBookParserTest {

    private final ExcelBookParser parser = new ExcelBookParser();

    @Test
    void parsesRowsByHeaderNameInAnyOrder() throws IOException {
        byte[] xlsx = workbook(
                new String[]{"Genre", "Title", "Author", "Status", "Date Bought", "Notes"},
                new Object[]{"Fiction", "Dune", "Frank Herbert", "Yes", "2024-03-05", "Classic"},
                new Object[]{null, "", null, null, null, null},
                new Object[]{"", "Emma", "Jane Austen", "no", "05/03/2024", null}
        );

        ExcelBookParser.ParseResult result = parser.parse(new ByteArrayInputStream(xlsx), null);

        assertThat(result.errors()).isEmpty();
        assertThat(result.books()).hasSize(2);

        BookRequest dune = result.books().get(0);
        assertThat(dune.bookName()).isEqualTo("Dune");
        assertThat(dune.author()).isEqualTo("Frank Herbert");
        assertThat(dune.category()).isEqualTo("Fiction");
        assertThat(dune.read()).isTrue();
        assertThat(dune.comments()).isEqualTo("Classic");
        assertThat(dune.boughtAt()).isEqualTo(Instant.parse("2024-03-05T00:00:00Z"));

        BookRequest emma = result.books().get(1);
        assertThat(emma.read()).isFalse();
        assertThat(emma.boughtAt()).isEqualTo(Instant.parse("2024-03-05T00:00:00Z"));
    }

    @Test
    void matchesHeadersByKeywordAndSkipsTitleRowsAboveHeader() throws IOException {
        byte[] xlsx = workbook(
                new String[]{"My Book Collection"},
                new Object[]{},
                new String[]{"Book name", "Author name", "Publisher Name", "Book category", "Notes", "Shelf"},
                new Object[]{"Dune", "Frank Herbert", "Ace", "Fiction", "Classic", "A3"}
        );

        ExcelBookParser.ParseResult result = parser.parse(new ByteArrayInputStream(xlsx), null);

        assertThat(result.errors()).isEmpty();
        BookRequest dune = result.books().get(0);
        assertThat(dune.author()).isEqualTo("Frank Herbert");
        assertThat(dune.publisher()).isEqualTo("Ace");
        assertThat(dune.category()).isEqualTo("Fiction");
        assertThat(dune.comments()).isEqualTo("Classic");
        assertThat(result.columns()).containsEntry("author", "Author name")
                .containsEntry("category", "Book category");
        assertThat(result.unmatchedHeaders()).containsExactly("Shelf");
    }

    @Test
    void keepsUnreadableDatesAsCommentsInsteadOfRejectingRow() throws IOException {
        byte[] xlsx = workbook(
                new String[]{"Book name", "Bought at"},
                new Object[]{"Good", "2024-01-01"},
                new Object[]{"  Noted  ", "* From MNM"}
        );

        ExcelBookParser.ParseResult result = parser.parse(new ByteArrayInputStream(xlsx), null);

        assertThat(result.errors()).isEmpty();
        assertThat(result.books()).extracting(BookRequest::bookName).containsExactly("Good", "Noted");
        assertThat(result.books().get(1).boughtAt()).isNull();
        assertThat(result.books().get(1).comments()).isEqualTo("Bought: * From MNM");
    }

    @Test
    void reportsRowsThatCannotBeImported()
            throws IOException {
        byte[] xlsx = workbook(
                new String[]{"Book name", "Category"},
                new Object[]{"Good", "Fiction"},
                new Object[]{"x".repeat(501), "Fiction"}
        );

        ExcelBookParser.ParseResult result = parser.parse(new ByteArrayInputStream(xlsx), null);

        assertThat(result.books()).extracting(BookRequest::bookName).containsExactly("Good");
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).startsWith("Row 3:");
    }

    @Test
    void recognisesTypoedAuthorHeader() throws IOException {
        byte[] xlsx = workbook(
                new String[]{"Book Name", "Auther", "Publisher"},
                new Object[]{"Shelf book", "Someone", "Pub"}
        );

        ExcelBookParser.ParseResult result = parser.parse(new ByteArrayInputStream(xlsx), null);

        assertThat(result.books().get(0).author()).isEqualTo("Someone");
        assertThat(result.columns()).containsEntry("author", "Auther");
    }

    @Test
    void rejectsSheetWithoutBookNameColumn() throws IOException {
        byte[] xlsx = workbook(
                new String[]{"Author", "Category"},
                new Object[]{"Someone", "Misc"}
        );

        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xlsx), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No book title column");
    }

    private byte[] workbook(String[] header, Object[]... rows) throws IOException {
        return workbook(new Object[][]{header}, rows);
    }

    /** Builds a sheet where each leading entry is one row (a String[] or Object[]), starting at row 0. */
    private byte[] workbook(Object[][] leading, Object[]... rows) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Books");
            int next = 0;
            for (Object[] cells : leading) {
                Row row = sheet.createRow(next++);
                for (int c = 0; c < cells.length; c++) {
                    if (cells[c] != null) row.createCell(c).setCellValue((String) cells[c]);
                }
            }
            for (Object[] cells : rows) {
                Row row = sheet.createRow(next++);
                for (int c = 0; c < cells.length; c++) {
                    if (cells[c] != null) row.createCell(c).setCellValue((String) cells[c]);
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }
}
