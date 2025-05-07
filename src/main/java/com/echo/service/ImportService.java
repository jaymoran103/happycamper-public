package com.echo.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.echo.domain.ActivityRoster;
import com.echo.domain.Camper;
import com.echo.domain.CamperRoster;
import com.echo.logging.RosterException;
import com.echo.validation.FileValidator;

/**
 * Service for importing data from CSV files into roster objects.
 *
 * The ImportService provides methods for reading, validating, and converting CSV files
 * into various roster types. It handles file validation, header extraction, and data
 * conversion while providing appropriate error handling through RosterExceptions.
 *
 * This service is used by the RosterService and UI components to load data from files
 * selected by the user.
 */
public class ImportService {

    /**
     * Imports data from a CSV file into a list of maps.
     * Each map represents a row of data with column headers as keys.
     *
     * @param file The CSV file to import
     * @return List of maps, each representing a row of data with header-value pairs
     * @throws RosterException if the file is invalid or an error occurs during import
     */
    public List<Map<String, String>> importCSV(File file) throws RosterException {
        // Validate the file before importing
        FileValidator.validateFile(file);

        List<Map<String, String>> result = new ArrayList<>();

        try (Reader reader = new FileReader(file);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                Map<String, String> row = new HashMap<>();
                for (String header : parser.getHeaderNames()) {
                    row.put(header, record.get(header));
                }
                result.add(row);
            }
        } catch (IOException e) {
            throw RosterException.create_normalWrapper("ImportService.importCSV: IOException",e);
        }

        return result;
    }

    /**
     * Extracts column headers from a CSV file.
     * This method reads only the first row of the file to get the headers.
     *
     * @param file The CSV file to extract headers from
     * @return List of header names in the order they appear in the file
     * @throws RosterException if the file is invalid or an error occurs during reading
     */
    public List<String> extractHeaders(File file) throws RosterException {
        // Validate the file before extracting headers
        FileValidator.validateFile(file);

        try (Reader reader = new FileReader(file);
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            return new ArrayList<>(parser.getHeaderNames());
        } catch (IOException e) {
            throw RosterException.create_normalWrapper("ImportService.extractHeaders: IOException", e);
        }
    }

    /**
     * Imports data from a CSV file into a camper roster.
     * This method reads the file, extracts headers and data, and creates a CamperRoster
     * with Camper objects for each row. Each camper is assigned a unique ID based on their
     * name and grade information.
     *
     * @param file The CSV file to import
     * @return A new CamperRoster containing the imported data
     * @throws RosterException if the file is invalid or an error occurs during import
     */
    public CamperRoster importCamperRoster(File file) throws RosterException {
        // Validate the file before importing
        FileValidator.validateFile(file);

        CamperRoster roster = new CamperRoster();

        try {
            List<Map<String, String>> data = importCSV(file);

            // Add headers
            if (!data.isEmpty()) {
                for (String header : data.get(0).keySet()) {
                    roster.addHeader(header);
                }
            }

            // Add campers
            for (Map<String, String> row : data) {
                String camperId = CamperRoster.generateCamperId(row);
                Camper camper = new Camper(camperId, row);
                roster.addCamper(camper);
            }

            return roster;
        } catch (Exception e) {
            throw RosterException.create_normalWrapper("ImportService.importCamperRoster: "+e.getClass().getName(),e);
        }
    }

    /**
     * Imports data from a CSV file into an activity roster.
     * This method reads the file, extracts headers and data, and creates an ActivityRoster
     * with activity entries for each row. Each activity is assigned a unique ID based on
     * the camper and activity information.
     *
     * @param file The CSV file to import
     * @return A new ActivityRoster containing the imported data
     * @throws RosterException if the file is invalid or an error occurs during import
     */
    public ActivityRoster importActivityRoster(File file) throws RosterException {
        // Validate the file before importing
        FileValidator.validateFile(file);

        ActivityRoster roster = new ActivityRoster();

        try {
            List<Map<String, String>> data = importCSV(file);

            // Add headers
            if (!data.isEmpty()) {
                for (String header : data.get(0).keySet()) {
                    roster.addHeader(header);
                }
            }

            // Add activities
            for (Map<String, String> row : data) {
                String activityId = ActivityRoster.generateCamperIdFromActivity(row);
                Camper activity = new Camper(activityId, row);
                roster.addCamper(activity);
            }

            return roster;
        } catch (Exception e) {
            throw RosterException.create_normalWrapper("ImportService.importActivityRoster: "+e.getClass().getName(),e);
        }
    }

    /**
     * Checks if a file exists and is readable.
     * This is a utility method used for basic file validation before attempting to read.
     *
     * @param file The file to check
     * @return true if the file exists, is a regular file, and is readable; false otherwise
     */
    public boolean isFileValid(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }
}
