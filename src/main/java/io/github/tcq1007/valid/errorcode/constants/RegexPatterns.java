package io.github.tcq1007.valid.errorcode.constants;

/**
 * Constants for regex patterns used in error code extraction
 */
public final class RegexPatterns {
    
    /**
     * Pattern for matching enum constructor with the first integer parameter.
     * Matches patterns like "ENUM_NAME(123, "message")".
     */
    public static final String ENUM_CONSTRUCTOR_FIRST_INT = "%s\\s*\\(\\s*(\\d+)";
    
    /**
     * Pattern for extracting constructor arguments.
     * Matches content within parentheses: (arg1, arg2, arg3).
     */
    public static final String CONSTRUCTOR_ARGUMENTS = "\\(([^)]+)\\)";
    
    /**
     * Pattern for removing non-digit characters except the minus sign.
     * Used to clean parameter strings and extract only numeric values.
     */
    public static final String NON_DIGIT_EXCEPT_MINUS = "[^\\d-]";
    
    // Prevent instantiation
    private RegexPatterns() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}