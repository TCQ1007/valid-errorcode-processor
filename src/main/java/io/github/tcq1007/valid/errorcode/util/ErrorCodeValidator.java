package io.github.tcq1007.valid.errorcode.util;

import io.github.tcq1007.valid.errorcode.constants.ErrorMessages;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.annotation.processing.Messager;

/**
 * Utility class for validating error code format and rules
 */
public class ErrorCodeValidator {
    
    /**
     * Validate error code format according to specified rules
     * 
     * @param element The element to report errors on
     * @param code The error code value to validate
     * @param prefix The required prefix
     * @param totalLength The required total length
     * @param fieldName The name of the error code field
     * @param messager The messager for reporting errors
     */
    public static void validateErrorCodeFormat(Element element, int code, String prefix, 
                                             int totalLength, String fieldName, Messager messager) {
        String codeStr = String.valueOf(code);
        
        validateCodeLength(element, codeStr, totalLength, fieldName, messager);
        validateCodePrefix(element, codeStr, prefix, fieldName, code, messager);
    }
    
    /**
     * Check if a value should be excluded from validation
     * 
     * @param value The value to check
     * @param excludeValues Array of values to exclude
     * @return true if the value should be excluded, false otherwise
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
     * Validate that error code has the correct length
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
     * Validate that error code starts with the correct prefix
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
}