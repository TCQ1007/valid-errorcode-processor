package io.github.tcq1007.valid.errorcode.util;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for tracking and validating uniqueness of error codes across the codebase.
 * 
 * <p>This class maintains a registry of all error codes encountered during compilation
 * and provides functionality to check for duplicates. It helps ensure that error codes
 * remain unique across different enum classes in the project.</p>
 * 
 * <p>The registry is cleared between processing rounds to ensure clean validation
 * state for each compilation.</p>
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
            // 检查是否是不同的枚举常量
            if (!existingElement.equals(element) || 
                !existingElement.getEnclosingElement().equals(element.getEnclosingElement())) {
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
