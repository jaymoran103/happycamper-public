package com.echo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

/**
 * Utility class intended to simplify file and importing process while sorting out file processing bugs
 */
public class ImportUtils {
    
    public static CSVParser createSafeParser(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringReader cleanedDataReader = new StringReader(ContentCleaner.cleanFileContent(reader));
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().build();
        CSVParser parser = CSVParser.parse(cleanedDataReader,format);
        return parser;
    }
}

/**
 * ContentCleaner taken from version 1.
 * Utility class used to clean problematic characters from CSV files, including junk leading/trailing characters and uneven quotes.
 */
class ContentCleaner {
    /** Uses given reader to iterate through a file and clean its content. Used by setupFromCSV()
     * @param reader BufferedReader for a CSV file
     * @return StringBuilder containing cleaned content
     * @throws IOException
     */
    public static String cleanFileContent(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        String line;
        
        while ((line = bufferedReader.readLine()) != null) {
            String cleanedLine = cleanLine(line);
            if (cleanedLine != null) {
                builder.append(cleanedLine).append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * Trims problematic characters from CSV line, ensuring every line starts and ends with quote marks, and has an even number of quotes.
     * @param line The line to clean
     * @return Cleaned line or null if line should be ignored
     */
    private static String cleanLine(String line) {
        // Skip empty lines or lines that are just whitespace
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        // Skip lines that start with a comment character
        if (line.charAt(0) == '#') {
            return null;
        }

        // Clean start of line
        line = cleanLineStart(line);
        
        // Clean end of line
        line = cleanLineEnd(line);

        // Check for valid quote count
        if (!hasValidQuoteCount(line)) {
            System.out.println("Filtered a line with uneven quote count: " + line);
            return null;
        }

        return line.trim();
    }

    /**
     * Removes problematic characters from the start of a line until it begins with a quote.
     * @param line The line to clean
     * @return Line with clean start
     */
    private static String cleanLineStart(String line) {
        while (!line.startsWith("\"") && !line.isEmpty()) {
            System.out.println("Caught a junk character at start: '" + line.charAt(0) + "'");
            line = line.substring(1);
        }
        return line;
    }

    /**
     * Removes problematic characters from the end of a line until it ends with a quote.
     * @param line The line to clean
     * @return Line with clean end
     */
    private static String cleanLineEnd(String line) {
        while (!line.endsWith("\"") && !line.isEmpty()) {
            System.out.println("Caught a junk character at end: '" + line.charAt(line.length()-1) + "'");
            line = line.substring(0, line.length()-1);
        }
        return line;
    }

    /**
     * Checks if a line has a valid number of quotes (even number).
     * @param line The line to check
     * @return true if the line has an even number of quotes
     */
    private static boolean hasValidQuoteCount(String line) {
        return line.chars().filter(ch -> ch == '"').count() % 2 == 0;
    }

}