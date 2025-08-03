package io.github.tcq1007.valid.errorcode.util;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for tracking and validating uniqueness of error codes across the codebase.
 */
public class ErrorCodeRegistry {
    private static final Map<String, Element> errorCodeRegistry = new HashMap<>();

    /**
     * Checks if an error code is unique across all processed enums.
     * @param code The error code to check
     * @param element The element where the error code is defined
     * @return true if the error code is unique, false otherwise
     */
    public static boolean isUniqueErrorCode(String code, Element element) {
        if (errorCodeRegistry.containsKey(code)) {
            Element existingElement = errorCodeRegistry.get(code);
            if (!existingElement.equals(element)) {
                return false;
            }
        }
        errorCodeRegistry.put(code, element);
        return true;
    }

    /**
     * Get the element where an error code was first defined.
     * @param code The error code to look up
     * @return The element where the error code was defined, or null if not found
     */
    public static Element getErrorCodeDefinition(String code) {
        return errorCodeRegistry.get(code);
    }

    /**
     * Clear the registry. Useful between processing rounds.
     */
    public static void clear() {
        errorCodeRegistry.clear();
    }
}
