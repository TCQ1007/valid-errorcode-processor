package io.github.tcq1007.valid.errorcode.constants;

/**
 * Constants for error messages used in error code validation
 */
public final class ErrorMessages {
    
    // Validation error messages
    /**
     * Error message when only enum classes are allowed to use @ValidErrorCode annotation.
     */
    public static final String ONLY_ENUM_CLASSES_ALLOWED =
        "Only enum classes can use @ValidErrorCode annotation";
    
    /**
     * Error message when the error code field is not found.
     */
    public static final String ERROR_CODE_FIELD_NOT_FOUND =
        "Error code field named '%s' not found";
    
    /**
     * Error message when the error code field must be of int type.
     */
    public static final String ERROR_CODE_FIELD_MUST_BE_INT =
        "Error code field '%s' must be of int type";
    
    /**
     * Error message when unable to extract error code value from enum constant.
     */
    public static final String UNABLE_TO_EXTRACT_ERROR_CODE =
        "Unable to extract error code value from enum constant '%s', " +
        "please ensure constructor parameters are integer literals";
    
    /**
     * Error message when the error code length does not match the expected length.
     */
    public static final String ERROR_CODE_LENGTH_MISMATCH =
        "Error code field '%s' length should be %d digits, actual is %d digits (value: %s)";
    
    /**
     * Error message when the error code prefix does not match the expected prefix.
     */
    public static final String ERROR_CODE_PREFIX_MISMATCH =
        "Error code field '%s' must start with %s, actual is %s (value: %d)";
    
    /**
     * Log message for initializing the ErrorCodeProcessor.
     */
    public static final String INITIALIZING_PROCESSOR = "Initializing ErrorCodeProcessor";
    /**
     * Log message for processing ErrorCodeProcessor annotations.
     */
    public static final String PROCESSING_ANNOTATIONS = "Processing ErrorCodeProcessor annotations";
    
    // Prevent instantiation
    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}