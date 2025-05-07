package com.echo.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.echo.logging.DetailedRosterException;
import com.echo.logging.RosterException;

class FileValidatorTest {

    @TempDir
    Path tempDir;

    private File validFile;
    private File emptyFile;
    private File malformedFile;
    private File missingHeadersFile;

    private static final List<String> REQUIRED_HEADERS = Arrays.asList("First Name", "Last Name", "Grade");

    @BeforeEach
    void setUp() throws IOException {
        // Create a valid CSV file
        validFile = tempDir.resolve("valid.csv").toFile();
        try (FileWriter writer = new FileWriter(validFile)) {
            writer.write("First Name,Last Name,Grade,Cabin\n");
            writer.write("John,Doe,5th,Lions Lodge\n");
            writer.write("Jane,Smith,6th,Friendship Lodge\n");
        }

        // Create an empty file
        emptyFile = tempDir.resolve("empty.csv").toFile();
        Files.createFile(tempDir.resolve("empty.csv"));

        // Create a file with malformed rows
        malformedFile = tempDir.resolve("malformed.csv").toFile();
        try (FileWriter writer = new FileWriter(malformedFile)) {
            writer.write("First Name,Last Name,Grade,Cabin\n");
            writer.write("John,Doe,Lions Lodge\n"); // Missing grade
            writer.write("Jane,Smith,6th,Friendship Lodge\n");
        }

        // Create a file missing required headers
        missingHeadersFile = tempDir.resolve("missing_headers.csv").toFile();
        try (FileWriter writer = new FileWriter(missingHeadersFile)) {
            writer.write("First Name,Cabin\n"); // Missing Last Name and Grade
            writer.write("John,Lions Lodge\n");
            writer.write("Jane,Friendship Lodge\n");
        }
    }

    @Test
    void testValidateFile_ValidFile() {
        assertDoesNotThrow(() -> FileValidator.validateFile(validFile));
    }

    @Test
    void testValidateFile_NullFile() {
        RosterException exception = assertThrows(RosterException.class,
                () -> FileValidator.validateFile(null));
        assertEquals("Invalid File", exception.getSummary());
    }

    @Test
    void testValidateFile_NonExistentFile() {
        File nonExistentFile = new File(tempDir.toFile(), "nonexistent.csv");
        RosterException exception = assertThrows(RosterException.class,
                () -> FileValidator.validateFile(nonExistentFile));
        assertEquals("File Not Found", exception.getSummary());
    }

    @Test
    void testValidateFile_InvalidExtension() throws IOException {
        File txtFile = tempDir.resolve("test.txt").toFile();
        Files.createFile(tempDir.resolve("test.txt"));

        RosterException exception = assertThrows(RosterException.class,
                () -> FileValidator.validateFile(txtFile));
        assertEquals("Invalid File Extension", exception.getSummary());
    }

    @Test
    void testValidateHeaders_ValidHeaders() {
        assertDoesNotThrow(() -> FileValidator.validateHeaders(validFile, REQUIRED_HEADERS));
    }

    @Test
    void testValidateHeaders_MissingHeaders() {
        DetailedRosterException exception = assertThrows(DetailedRosterException.class,
                () -> FileValidator.validateHeaders(missingHeadersFile, REQUIRED_HEADERS));
        assertEquals("File lacks required headers", exception.getSummary());

        // Verify table data
        String[] tableHeaders = exception.getTableHeaders();
        assertEquals(2, tableHeaders.length);
        assertEquals("Missing Header", tableHeaders[0]);
        assertEquals("Required For", tableHeaders[1]);
    }

    @Test
    void testValidateRowConsistency_ValidRows() {
        assertDoesNotThrow(() -> FileValidator.validateRowConsistency(validFile));
    }

    @Test
    void testValidateRowConsistency_MalformedRows() {
        DetailedRosterException exception = assertThrows(DetailedRosterException.class,
                () -> FileValidator.validateRowConsistency(malformedFile));
        assertEquals("Malformed data detected - double check file contents before selecting", exception.getSummary());

        // Verify table data
        String[] tableHeaders = exception.getTableHeaders();
        String[][] tableData = exception.getTableData();

        assertEquals(2, tableHeaders.length); // 2 columns in header
        assertEquals(3, tableData.length); // 3 rows
        assertEquals("Malformed Row Index", tableData[0][0]);
        assertEquals("Items in row", tableData[1][0]);
        assertEquals("Number of headers (rows should match this)", tableData[2][0]);
    }

    @Test
    void testValidateCSVFile_ValidFile() {
        assertDoesNotThrow(() -> FileValidator.validateCSVFile(validFile, REQUIRED_HEADERS));
    }

    @Test
    void testValidateCSVFile_EmptyFile() {
        RosterException exception = assertThrows(RosterException.class,
                () -> FileValidator.validateCSVFile(emptyFile, REQUIRED_HEADERS));
        assertTrue(exception.getSummary().contains("Missing data"));
    }

    @Test
    void testHasHeaders_ValidFile() throws RosterException {
        assertDoesNotThrow(() -> FileValidator.validateHeaders(validFile, Arrays.asList("First Name", "Last Name"))); 
        assertThrows(DetailedRosterException.class, () -> FileValidator.validateHeaders(validFile, Arrays.asList("First Name", "NonExistentHeader")));    }

}
