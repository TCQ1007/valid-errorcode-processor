package io.github.tcq1007.valid.errorcode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for marking enums that need error code validation.
 * This annotation is used to configure how error codes within an enum should be validated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // Only retained at source level, not included in bytecode
public @interface ValidErrorCode {
    /**
     * Configurable prefix for the error code.
     * The default value is "1122".
     * @return The prefix for the error code.
     */
    String prefix() default "1122";
    /**
     * Configurable total length for the error code (in digits).
     * The default value is 8 digits.
     * @return The total length of the error code.
     */
    int length() default 8;
    /**
     * Specifies the name of the field within the enum constant that holds the error code.
     * The default value is "code".
     * @return The name of the error code field.
     */
    String codeField() default "code";
    /**
     * An array of integer values that will skip validation.
     * By default, it excludes 0 (which usually represents success or OK).
     * @return An array of values to exclude from validation.
     */
    int[] excludeValues() default {0};
}

