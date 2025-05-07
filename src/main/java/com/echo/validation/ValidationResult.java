package com.echo.validation;

import java.io.File;

import com.echo.logging.RosterException;

/**
 * ValidationResult is a helper class for chaining validation steps.
 * It holds a file and an optional exception that occurred during validation.
 */
public class ValidationResult {
    private final File file;
    private final RosterException exception;
    
    /**
     * Creates a new ValidationResult.
     * 
     * @param file The file being validated
     * @param exception The exception that occurred during validation, or null if validation passed
     */
    public ValidationResult(File file, RosterException exception) {
        this.file = file;
        this.exception = exception;
    }
    
    /**
     * Chains this validation result with another validation function.
     * If this result has an exception, the next validation is skipped.
     * 
     * @param next The next validation function to apply
     * @return The result of the next validation, or this result if this has an exception
     */
    public ValidationResult andThen(ValidationFunction next) {
        if (exception != null) {
            return this;
        }
        return next.validate(file);
    }
    
    /**
     * Gets the exception from this validation result.
     * @return The exception, or null if validation passed
     */
    public RosterException getException() {
        return exception;
    }
    
    /**
     * Gets the file from this validation result.
     * @return The file
     */
    public File getFile() {
        return file;
    }
    
    /**
     * Functional interface for validation functions.
     */
    @FunctionalInterface
    public interface ValidationFunction {
        ValidationResult validate(File file);
    }
}
