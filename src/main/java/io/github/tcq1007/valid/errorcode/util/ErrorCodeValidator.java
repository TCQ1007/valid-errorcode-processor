package io.github.tcq1007.valid.errorcode.util;

import io.github.tcq1007.valid.errorcode.constants.ErrorMessages;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.annotation.processing.Messager;

/**
 * Utility class for validating error code format and rules.
 * 
 * <p>This class provides functionality to validate error codes according to
 * specified rules, including format validation, length checking, prefix validation,
 * and uniqueness verification across the codebase.</p>
 * 
 * <p>All methods in this class are static as it serves as a utility class.</p>
 */
public class ErrorCodeValidator {
    
    /**
     * Validates the format of an error code according to specified rules.
     * 
     * <p>This method performs two main validations:
     * <ul>
     *   <li>Checks if the error code has the correct length</li>
     *   <li>Verifies that the error code starts with the specified prefix</li>
     * </ul></p>
     *
     * @param element    The element to report errors on, typically an enum constant
     * @param code      The numeric error code value to validate
     * @param prefix    The required prefix that the error code must start with
     * @param totalLength The required total length of the error code
     * @param fieldName The name of the error code field for error reporting
     * @param messager  The messager instance for reporting validation errors
     */
    public static void validateErrorCodeFormat(Element element, int code, String prefix, 
                                             int totalLength, String fieldName, Messager messager) {
        String codeStr = String.valueOf(code);
        
        validateCodeLength(element, codeStr, totalLength, fieldName, messager);
        validateCodePrefix(element, codeStr, prefix, fieldName, code, messager);
    }
    
    /**
     * Determines if a value should be excluded from validation.
     * 
     * <p>This method checks if the given value matches any of the values in the
     * exclude list. This is useful for special cases like success codes (0) that
     * might not need to follow the standard validation rules.</p>
     *
     * @param value The numeric value to check
     * @param excludeValues Array of values that should be excluded from validation
     * @return {@code true} if the value should be excluded from validation,
     *         {@code false} if it should be validated
     */
    public static boolean shouldExcludeFromValidation(Integer value, int[] excludeValues) {
        if (value == null || excludeValues == null) {
            return false;
        }
        
        for (int excludeValue : excludeValues) {
            if (value.intValue() == excludeValue) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validates that the error code has the correct length.
     * 
     * <p>Checks if the string representation of the error code matches the
     * expected length. If not, reports an error through the messager.</p>
     *
     * @param element The element where the error should be reported
     * @param codeStr The string representation of the error code
     * @param expectedLength The expected length of the error code
     * @param fieldName The name of the field containing the error code
     * @param messager The messager for reporting validation errors
     */
    private static void validateCodeLength(Element element, String codeStr, int expectedLength,
                                         String fieldName, Messager messager) {
        if (codeStr.length() != expectedLength) {
            String errorMessage = String.format(
                ErrorMessages.ERROR_CODE_LENGTH_MISMATCH,
                fieldName, expectedLength, codeStr.length(), codeStr
            );
            messager.printMessage(Diagnostic.Kind.ERROR, errorMessage, element);
        }
    }
    
    /**
     * Validates that the error code starts with the correct prefix.
     * 
     * <p>Verifies that the string representation of the error code begins with
     * the expected prefix. If not, reports an error through the messager.</p>
     *
     * @param element The element where the error should be reported
     * @param codeStr The string representation of the error code
     * @param expectedPrefix The prefix that the error code should start with
     * @param fieldName The name of the field containing the error code
     * @param code The original numeric error code value
     * @param messager The messager for reporting validation errors
     */
    private static void validateCodePrefix(Element element, String codeStr, String expectedPrefix,
                                         String fieldName, int code, Messager messager) {
        if (!codeStr.startsWith(expectedPrefix)) {
            String errorMessage = String.format(
                ErrorMessages.ERROR_CODE_PREFIX_MISMATCH,
                fieldName, expectedPrefix, codeStr, code
            );
            messager.printMessage(Diagnostic.Kind.ERROR, errorMessage, element);
        }
    }
    
    /**
     * Report a general error message
     * 
     * @param element The element to report the error on
     * @param message The error message
     * @param messager The messager for reporting errors
     */
    public static void reportError(Element element, String message, Messager messager) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    /**
     * Validates the uniqueness of an error code.
     * 
     * <p>This method checks if the given error code is unique across all processed
     * enum constants. If a duplicate is found, it reports an error message indicating
     * where the error code was previously defined.</p>
     *
     * @param errorCode The error code to validate
     * @param element The element containing the error code
     * @param messager The messager for reporting errors
     * @return true if the error code is unique, false if a duplicate was found
     */
    public static boolean validateErrorCodeUniqueness(String errorCode, Element element, Messager messager) {
        if (!ErrorCodeRegistry.isUniqueErrorCode(errorCode, element)) {
            Element existingElement = ErrorCodeRegistry.getErrorCodeDefinition(errorCode);
            reportError(element, 
                String.format(ErrorMessages.DUPLICATE_ERROR_CODE, 
                    errorCode, 
                    existingElement.getEnclosingElement().getSimpleName() + "." + existingElement.getSimpleName()),
                messager);
            return false;
        }
        return true;
    }

    private ErrorCodeValidator() {
        // This is a utility class and should not be instantiated.
    }
}