package io.github.tcq1007.valid.errorcode;

/**
 * Configuration class for error code validation
 */
public class ErrorCodeValidationConfig {
    private final String prefix;
    private final int length;
    private final String codeField;
    private final int[] excludeValues;

    public ErrorCodeValidationConfig(ValidErrorCode annotation) {
        this.prefix = annotation.prefix();
        this.length = annotation.length();
        this.codeField = annotation.codeField();
        this.excludeValues = annotation.excludeValues();
    }

    public String getPrefix() {
        return prefix;
    }

    public int getLength() {
        return length;
    }

    public String getCodeField() {
        return codeField;
    }

    public int[] getExcludeValues() {
        return excludeValues;
    }
}
