package io.github.tcq1007.valid.errorcode;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import io.github.tcq1007.valid.errorcode.constants.ErrorMessages;
import io.github.tcq1007.valid.errorcode.util.EnumFieldFinder;
import io.github.tcq1007.valid.errorcode.util.ErrorCodeExtractor;
import io.github.tcq1007.valid.errorcode.util.ErrorCodeValidator;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

/**
 * Annotation processor for validating error codes in enum classes.
 * 
 * <p>This processor validates error codes defined in enum classes according to
 * specified rules including:
 * <ul>
 *   <li>Format validation (prefix, length)</li>
 *   <li>Uniqueness across the codebase</li>
 *   <li>Type correctness</li>
 * </ul></p>
 *
 * <p>The processor is automatically registered for processing through the
 * {@link AutoService} annotation and processes all enum classes annotated
 * with {@link ValidErrorCode}.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * @ValidErrorCode(prefix = "E", length = 5)
 * public enum ErrorCodes {
 *     E10001("System error"),
 *     E10002("Invalid input");
 *     // ...
 * }
 * }</pre></p>
 *
 * @see ValidErrorCode
 * @see AbstractProcessor
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.tcq1007.valid.errorcode.ValidErrorCode")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ErrorCodeProcessor extends AbstractProcessor {

    /** Messager instance for reporting errors and warnings during processing */
    private Messager messager;

    /** Utility instance for working with program elements */
    private Elements elementUtils;

    /** Utility instance for working with types */
    private Types typeUtils;

    /** Utility instance for working with Abstract Syntax Trees */
    private Trees trees;
    /**
     * Constructs a new ErrorCodeProcessor.
     * 
     * <p>This constructor is required by the annotation processing API and
     * will be called by the Java compiler when initializing the processor.</p>
     */
    public ErrorCodeProcessor() {
    }

    /**
     * Initializes the processor with the processing environment.
     * 
     * <p>This method is called by the Java compiler to initialize the processor
     * with the processing environment. It sets up the required utilities for
     * error reporting, element handling, and AST operations.</p>
     *
     * @param processingEnv The processing environment provided by the compiler,
     *                     containing utilities for the annotation processor.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        System.out.println(ErrorMessages.INITIALIZING_PROCESSOR);
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.trees = Trees.instance(processingEnv);
    }

    /**
     * Processes the annotations found in the source files.
     * @param annotations The set of annotations to process.
     * @param roundEnv The current round environment.
     * @return True if the annotations were processed, false otherwise.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println(ErrorMessages.PROCESSING_ANNOTATIONS);

        // Clear the error code registry at the start of each round
        io.github.tcq1007.valid.errorcode.util.ErrorCodeRegistry.clear();

        for (Element element : roundEnv.getElementsAnnotatedWith(ValidErrorCode.class)) {
            processAnnotatedEnum(element);
        }

        return true;
    }

    /**
     * Process a single enum that is annotated with @ValidErrorCode.
     * 
     * <p>This method handles the validation of a single enum class that has been
     * annotated with the {@link ValidErrorCode} annotation. It verifies that the
     * element is a valid enum class and processes all its enum constants.</p>
     *
     * @param element The element representing the enum class to be processed.
     *               Must be annotated with {@link ValidErrorCode}.
     */
    private void processAnnotatedEnum(Element element) {
        if (!isValidEnumElement(element)) {
            return;
        }

        ValidErrorCode annotation = element.getAnnotation(ValidErrorCode.class);
        ErrorCodeValidationConfig config = createValidationConfig(annotation);

        validateAllEnumConstants(element, config);
    }

    /**
     * Validate that the element is an enum class.
     * 
     * <p>This method checks if the given element represents an enum class. If not,
     * it reports an error using the messager.</p>
     *
     * @param element The element to validate.
     * @return {@code true} if the element is an enum class, {@code false} otherwise.
     */
    private boolean isValidEnumElement(Element element) {
        if (element.getKind() != ElementKind.ENUM) {
            ErrorCodeValidator.reportError(element,
                ErrorMessages.ONLY_ENUM_CLASSES_ALLOWED, messager);
            return false;
        }
        return true;
    }

    /**
     * Create validation configuration from annotation.
     * 
     * <p>Creates a configuration object based on the values specified in the
     * {@link ValidErrorCode} annotation. This configuration is used to validate
     * the error codes in the enum constants.</p>
     *
     * @param annotation The ValidErrorCode annotation from which to create the configuration.
     * @return A new {@link ErrorCodeValidationConfig} instance containing the validation rules.
     */
    private ErrorCodeValidationConfig createValidationConfig(ValidErrorCode annotation) {
        return new ErrorCodeValidationConfig(annotation);
    }

    /**
     * Validate all enum constants in the enum class.
     * 
     * <p>Iterates through all enum constants in the given enum class and validates
     * each one according to the provided configuration. Only processes elements
     * that are actually enum constants.</p>
     *
     * @param enumElement The enum class element containing the constants to validate.
     * @param config The configuration specifying the validation rules to apply.
     */
    private void validateAllEnumConstants(Element enumElement, ErrorCodeValidationConfig config) {
        for (Element enumConstant : enumElement.getEnclosedElements()) {
            if (enumConstant.getKind() == ElementKind.ENUM_CONSTANT) {
                validateSingleEnumConstant(enumConstant, config);
            }
        }
    }

    /**
     * Validate a single enum constant.
     * 
     * <p>Performs comprehensive validation of a single enum constant, including:
     * <ul>
     *   <li>Finding and validating the error code field</li>
     *   <li>Extracting the error code value</li>
     *   <li>Checking if validation should be skipped</li>
     *   <li>Validating the error code format</li>
     * </ul></p>
     *
     * @param enumConstant The enum constant element to validate.
     * @param config The configuration specifying the validation rules to apply.
     */
    private void validateSingleEnumConstant(Element enumConstant, ErrorCodeValidationConfig config) {
        // 首先验证字段是否存在且类型正确
        VariableElement codeField = findAndValidateCodeField(enumConstant, config.getCodeField());
        if (codeField == null) {
            return;
        }

        // 获取错误码的值
        Integer codeValue = extractErrorCodeValue(enumConstant, config.getCodeField());
        if (codeValue == null) {
            reportErrorCodeExtractionFailure(enumConstant, config.getCodeField());
            return;
        }

        String errorCode = String.valueOf(codeValue);

        if (shouldSkipValidation(codeValue, config.getExcludeValues())) {
            return;
        }

        // Validate error code format
        ErrorCodeValidator.validateErrorCodeFormat(
            enumConstant,
            codeValue,
            config.getPrefix(),
            config.getLength(),
            config.getCodeField(),
            messager
        );

        // Check for error code uniqueness
        ErrorCodeValidator.validateErrorCodeUniqueness(errorCode, enumConstant, messager);
    }

    /**
     * Find and validate the error code field in the enum constant.
     * 
     * <p>Attempts to find the specified field in the enum constant and validates
     * that it exists and is of the correct type (integer). Reports appropriate
     * errors if validation fails.</p>
     *
     * @param enumConstant The enum constant element to search in.
     * @param codeFieldName The name of the field to find and validate.
     * @return The {@link VariableElement} representing the error code field if found
     *         and valid, {@code null} otherwise.
     */
    private VariableElement findAndValidateCodeField(Element enumConstant, String codeFieldName) {
        VariableElement codeField = EnumFieldFinder.findErrorCodeField(enumConstant, codeFieldName);

        if (codeField == null) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_FIELD_NOT_FOUND, codeFieldName);
            ErrorCodeValidator.reportError(enumConstant, errorMessage, messager);
            return null;
        }

        if (!EnumFieldFinder.isIntegerField(codeField)) {
            String errorMessage = String.format(ErrorMessages.ERROR_CODE_FIELD_MUST_BE_INT, codeFieldName);
            ErrorCodeValidator.reportError(enumConstant, errorMessage, messager);
            return null;
        }

        return codeField;
    }

    /**
     * Extract error code value from enum constant.
     * 
     * <p>Attempts to extract the numeric error code value from the specified
     * field in the enum constant. Uses the {@link ErrorCodeExtractor} utility
     * class to perform the extraction.</p>
     *
     * @param enumConstant The enum constant element containing the error code.
     * @param codeFieldName The name of the field containing the error code.
     * @return The extracted error code value as an {@link Integer}, or {@code null}
     *         if extraction fails.
     */
    private Integer extractErrorCodeValue(Element enumConstant, String codeFieldName) {
        return ErrorCodeExtractor.extractErrorCodeValue(enumConstant, codeFieldName, trees);
    }

    /**
     * Report error when error code extraction fails.
     * 
     * <p>Reports an error message through the messager when the error code
     * extraction process fails. The error message includes the name of the
     * enum constant where the extraction failed.</p>
     *
     * @param enumConstant The enum constant element where extraction failed.
     * @param codeFieldName The name of the field that failed extraction.
     */
    private void reportErrorCodeExtractionFailure(Element enumConstant, String codeFieldName) {
        String errorMessage = String.format(
            ErrorMessages.UNABLE_TO_EXTRACT_ERROR_CODE,
            enumConstant.getSimpleName()
        );
        ErrorCodeValidator.reportError(enumConstant, errorMessage, messager);
    }

    /**
     * Check if validation should be skipped for this value.
     * 
     * <p>Determines whether a given error code value should be excluded from
     * validation based on the configured exclude values. Uses the
     * {@link ErrorCodeValidator} to perform the check.</p>
     *
     * @param codeValue The error code value to check.
     * @param excludeValues Array of values that should be excluded from validation.
     * @return {@code true} if the value should be excluded from validation,
     *         {@code false} otherwise.
     */
    private boolean shouldSkipValidation(Integer codeValue, int[] excludeValues) {
        return ErrorCodeValidator.shouldExcludeFromValidation(codeValue, excludeValues);
    }

    /**
     * Validate the format of the error code.
     * 
     * <p>Validates that the error code value meets all format requirements as
     * specified in the configuration, including:
     * <ul>
     *   <li>Correct prefix</li>
     *   <li>Required length</li>
     *   <li>Valid numeric format</li>
     * </ul></p>
     *
     * @param enumConstant The enum constant element containing the error code.
     * @param codeValue The numeric value of the error code to validate.
     * @param config The configuration containing the validation rules.
     */
    private void validateErrorCodeFormat(Element enumConstant, Integer codeValue, ErrorCodeValidationConfig config) {
        ErrorCodeValidator.validateErrorCodeFormat(
            enumConstant,
            codeValue,
            config.getPrefix(),
            config.getLength(),
            config.getCodeField(),
            messager
        );
    }

    /**
     * Configuration class for error code validation
     */
}
