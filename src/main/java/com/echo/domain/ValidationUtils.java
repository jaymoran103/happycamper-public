package com.echo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.echo.logging.RosterException;

public class ValidationUtils {
    public static void validateHeaders(Set<String> checkedHeaders, List<String> requiredHeaders) throws RosterException {

            List<String> missingHeaders = new ArrayList<>();
            for (String header : requiredHeaders) {
                if (!checkedHeaders.contains(header)) {
                    missingHeaders.add(header);
                }
            }
            if (!missingHeaders.isEmpty()) {
                throw RosterException.missingHeaders(missingHeaders);
            }
        }
}
