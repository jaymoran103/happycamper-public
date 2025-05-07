package com.echo.validation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Validator for export files.
 * Provides methods to validate that a file can be written to and has a valid extension.
 */
public class ExportFileValidator {

    /**
     * Result of a file validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        /**
         * Creates a successful validation result.
         * 
         * @return A successful validation result
         */
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        /**
         * Creates a failed validation result with an error message.
         * 
         * @param errorMessage The error message
         * @return A failed validation result
         */
        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        /**
         * Checks if the validation was successful.
         * 
         * @return true if the validation was successful, false otherwise
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Gets the error message if the validation failed.
         * 
         * @return The error message, or null if the validation was successful
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    /**
     * Validates that a file can be exported to.
     * Checks that:
     * - The file path is valid
     * - The file has a valid extension
     * - The directory exists or can be created
     * - The file can be written to
     * 
     * @param file The file to validate
     * @param allowedExtensions The allowed file extensions (without the dot)
     * @return A validation result
     */
    public static ValidationResult validateExportFile(File file, String... allowedExtensions) {
        if (file == null) {
            return ValidationResult.failure("No file selected");
        }
        
        // Check file extension
        String fileName = file.getName().toLowerCase();
        boolean hasValidExtension = false;
        
        for (String ext : allowedExtensions) {
            if (fileName.endsWith("." + ext.toLowerCase())) {
                hasValidExtension = true;
                break;
            }
        }
        
        if (!hasValidExtension) {
            return ValidationResult.failure("Invalid file extension. Allowed extensions: " + 
                                           String.join(", ", allowedExtensions));
        }
        
        // Check if parent directory exists or can be created
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            try {
                // Try to create the directory
                Path dirPath = Paths.get(parentDir.getAbsolutePath());
                Files.createDirectories(dirPath);
            } catch (Exception e) {
                return ValidationResult.failure("Cannot create directory: " + parentDir.getAbsolutePath());
            }
        }
        
        // Check if file can be written to
        if (file.exists() && !file.canWrite()) {
            return ValidationResult.failure("Cannot write to file. It may be in use by another application or you don't have permission.");
        }
        
        // Check for invalid characters in file name
        if (fileName.matches(".*[<>:\"|?*$].*")) {
            return ValidationResult.failure("File name contains invalid characters");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates that a file can be exported to with CSV extension.
     * 
     * @param file The file to validate
     * @return A validation result
     */
    public static ValidationResult validateCSVExportFile(File file) {
        return validateExportFile(file, "csv");
    }
    
    /**
     * Validates that a file can be exported to with TXT or CSV extension.
     * 
     * @param file The file to validate
     * @return A validation result
     */
    public static ValidationResult validateTextExportFile(File file) {
        return validateExportFile(file, "txt", "csv");
    }
    
    /**
     * Ensures a file has the specified extension.
     * If the file doesn't have any of the allowed extensions, the first one is added.
     * 
     * @param file The file to check
     * @param allowedExtensions The allowed extensions (without the dot)
     * @return A file with a valid extension
     */
    public static File ensureExtension(File file, String... allowedExtensions) {
        if (file == null || allowedExtensions.length == 0) {
            return file;
        }
        
        String fileName = file.getName().toLowerCase();
        for (String ext : allowedExtensions) {
            if (fileName.endsWith("." + ext.toLowerCase())) {
                return file; // File already has a valid extension
            }
        }
        
        // Add the first allowed extension
        return new File(file.getAbsolutePath() + "." + allowedExtensions[0]);
    }
}
