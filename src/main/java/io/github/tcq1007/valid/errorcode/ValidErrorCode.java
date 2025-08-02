package io.github.tcq1007.valid.errorcode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Custom annotation for marking enums that need error code validation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // Only retained at source level, not included in bytecode
public @interface ValidErrorCode {
    // Configurable prefix, default is 1122
    String prefix() default "1122";
    // Configurable total length, default is 8 digits
    int length() default 8;
    // Specify error code field name, default is "code"
    String codeField() default "code";
    // Excluded values that will skip validation, default excludes 0 (usually represents success/OK)
    int[] excludeValues() default {0};
}

