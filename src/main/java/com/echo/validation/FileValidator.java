package com.echo.validation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.echo.logging.RosterException;
import com.echo.service.ImportUtils;


/**
 * Centralized validation service for files used throughout the application.
 *
 * Provides a comprehensive set of validation methods for checking files before they are processed by the application. 
 * Uses a validation chain pattern that allows multiple validation steps to be combined, terminating early if a step fails.
 *
 * FUTURE - rework validation chain approach so that files themselves are only parsed once?
 */
public class FileValidator {

    /**
     * Enum defining the different types of file validation issues.
     * Each issue type has a user-friendly text description that can be
     * displayed in error messages and dialogs.
     */
    public enum FileIssueSummary {
        INVALID_FILE("Invalid File"),
        FILE_NOT_FOUND("File Not Found"),
        INVALID_FILE_EXTENSION("Invalid File Extension"),
        CANNOT_READ_FILE("Cannot Read File"),
        MISSING_DATA("Missing Data"),
        MALFORMED_DATA("Malformed Data"),
        MISSING_HEADERS("Missing Headers");

        public final String text;

        FileIssueSummary(String _text) {
            text = _text;
        }
    }

    /**
     * Performs basic validation of a file's properties.
     * This method checks that the file exists, is readable, and has the correct extension.
     * It's typically used as a first step before attempting more specific validations.
     *
     * @param file The file to validate
     * @throws RosterException if the file is null, doesn't exist, can't be read, or has an invalid extension
     */
    public static void validateFile(File file) throws RosterException {
        ValidationResult result = validateNotNull(file)
            .andThen(FileValidator::validateFileExists)
            .andThen(FileValidator::validateFileReadable)
            .andThen(FileValidator::validateFileExtension);

        if (result.getException() != null) {
            throw result.getException();
        }
    }

    /**
     * Checks that the given file is not null.
     *
     * @param file The file to check
     * @return ValidationResult with exception if check fails
     */
    static ValidationResult validateNotNull(File file) {
        if (file == null) {
            return new ValidationResult(null, RosterException.fileException(
                FileIssueSummary.INVALID_FILE.text,
                "The file path is null"
            ));
        }
        return new ValidationResult(file, null);
    }

    /**
     * Checks that the given file exists.
     *
     * @param file The file to check
     * @return ValidationResult with exception if check fails
     */
    private static ValidationResult validateFileExists(File file) {
        if (!file.exists()) {
            return new ValidationResult(file, RosterException.fileException(
                FileIssueSummary.FILE_NOT_FOUND.text,
                String.format("Couldn't find file '%s'\nIt may have been moved or deleted.\nFull file path:\n%s",
                              file.getName(), file.getPath())
            ));
        }
        return new ValidationResult(file, null);
    }

    /**
     * Checks that the given file is readable.
     *
     * @param file The file to check
     * @return ValidationResult with exception if check fails
     */
    private static ValidationResult validateFileReadable(File file) {
        if (!file.canRead()) {
            return new ValidationResult(file, RosterException.fileException(
                FileIssueSummary.CANNOT_READ_FILE.text,
                String.format("Cannot read file '%s'\nMake sure another application isn't using it and you have permission to access it.",
                              file.getName())
            ));
        }
        return new ValidationResult(file, null);
    }

    /**
     * Checks that the given file has the correct extension (.csv).
     *
     * @param file The file to check
     * @return ValidationResult with exception if check fails
     */
    private static ValidationResult validateFileExtension(File file) {
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            return new ValidationResult(file, RosterException.fileException(
                FileIssueSummary.INVALID_FILE_EXTENSION.text,
                String.format("The file '%s' is not a valid CSV file.\nPlease provide a file with the .csv extension.",
                              file.getName())
            ));
        }
        return new ValidationResult(file, null);
    }

    /**
     * Validates that a CSV file contains all the required headers.
     *
     * @param file The file to validate
     * @param requiredHeaders List of header names that must be present in the file
     * @throws RosterException if the file is invalid or missing any required headers
     */
    public static void validateHeaders(File file, List<String> requiredHeaders) throws RosterException {
        ValidationResult result = validateNotNull(file)
            .andThen(f -> validateRequiredHeaders(f, requiredHeaders));

        if (result.getException() != null) {
            throw result.getException();
        }
    }

    /**
     * Checks that the given file has all required headers.
     *
     * @param file The file to check
     * @param requiredHeaders List of required headers
     * @return ValidationResult with exception if check fails
     */
    private static ValidationResult validateRequiredHeaders(File file, List<String> requiredHeaders) {
        if (requiredHeaders == null || requiredHeaders.isEmpty()) {
            return new ValidationResult(file, null);
        }

        try (CSVParser parser = ImportUtils.createSafeParser(file)) {

            Set<String> fileHeaders = new HashSet<>(parser.getHeaderNames());
            List<String> missingHeaders = new ArrayList<>();

            for (String header : requiredHeaders) {
                if (!fileHeaders.contains(header)) {
                    missingHeaders.add(header);
                }
            }

            if (!missingHeaders.isEmpty()) {
                return new ValidationResult(file,
                    RosterException.missingHeaders(missingHeaders));
            }

            return new ValidationResult(file, null);
        } catch (IOException e) {
            return new ValidationResult(file,
                RosterException.create_normalWrapper("Error reading CSV headers: " + e.getMessage(), e));
        }
    }

    /**
     * Validates that all rows in a CSV file have consistent lengths.
     *
     * @param file The file to validate
     * @throws RosterException if the file is invalid or contains rows with inconsistent lengths
     */
    public static void validateRowConsistency(File file) throws RosterException {
        ValidationResult result = validateNotNull(file)
            .andThen(FileValidator::validateRowLengths);

        if (result.getException() != null) {
            throw result.getException();
        }
    }

    /**
     * Checks that all rows in the given file have consistent lengths.
     *
     * @param file The file to check
     * @return ValidationResult with exception if check fails
     */
    private static ValidationResult validateRowLengths(File file) {
        try (CSVParser parser = ImportUtils.createSafeParser(file)) {

            int headerCount = parser.getHeaderNames().size();
            int rowNumber = 1; // Start at 1 because header is row 0

            for (CSVRecord record : parser) {
                rowNumber++;
                int cellCount = record.size();

                if (cellCount != headerCount) {
                    List<String> rowData = new ArrayList<>();
                    for (int i = 0; i < cellCount; i++) {
                        rowData.add(record.get(i));
                    }

                    RosterException malformedRowException = RosterException.create_malformedRowException(file.getName(), headerCount, cellCount, rowNumber);
                    return new ValidationResult(file,malformedRowException);
                }
            }

            return new ValidationResult(file, null);
        } catch (IOException e) {
            return new ValidationResult(file,
                RosterException.create_normalWrapper("Error validating CSV rows: " + e.getMessage(), e));
        }
    }

    /**
     * Combines all individual validation steps thoroughly validate a CSV file.
     * - Checks file existence and basic properties
     * - Validates file content (headers and data rows)
     * - Ensures all required headers are present (if specified)
     * - That all rows have consistent lengths
     *
     * @param file The file to validate
     * @param requiredHeaders List of required headers (optional, can be null for no header requirements)
     * @throws RosterException if any validation step fails, with a specific error message
     */
    public static void validateCSVFile(File file, List<String> requiredHeaders) throws RosterException {
        ValidationResult result = validateNotNull(file)
            .andThen(FileValidator::validateFileExists)
            .andThen(FileValidator::validateFileReadable)
            .andThen(FileValidator::validateFileExtension)
            .andThen(FileValidator::validateHasContent)
            .andThen(f -> validateRequiredHeaders(f, requiredHeaders))
            .andThen(FileValidator::validateRowLengths);

        if (result.getException() != null) {
            throw result.getException();
        }
    }

    /**
     * Checks that the given file has content (headers and data rows).
     *
     * @param file The file to check
     * @return ValidationResult with exception if check fails
     */
    private static ValidationResult validateHasContent(File file) {
        try (CSVParser parser = ImportUtils.createSafeParser(file)) {

            if (parser.getHeaderNames().isEmpty()) {
                return new ValidationResult(file,
                    RosterException.noData(file.getName(), false));
            }

            if (!parser.iterator().hasNext()) {
                return new ValidationResult(file,
                    RosterException.noData(file.getName(), true));
            }

            return new ValidationResult(file, null);
        } catch (IOException e) {
            return new ValidationResult(file,
                RosterException.create_normalWrapper("Error validating CSV content: " + e.getMessage(), e));
        }
    }


}
