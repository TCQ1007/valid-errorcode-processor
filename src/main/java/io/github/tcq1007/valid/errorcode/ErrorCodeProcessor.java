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
 * Annotation processor for validating error codes in enums
 * This processor validates that error codes follow specified format rules
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.tcq1007.valid.errorcode.ValidErrorCode")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ErrorCodeProcessor extends AbstractProcessor {
    
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        System.out.println(ErrorMessages.INITIALIZING_PROCESSOR);
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.trees = Trees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println(ErrorMessages.PROCESSING_ANNOTATIONS);
        
        for (Element element : roundEnv.getElementsAnnotatedWith(ValidErrorCode.class)) {
            processAnnotatedEnum(element);
        }
        
        return true;
    }
    
    /**
     * Process a single enum that is annotated with @ValidErrorCode
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
     * Validate that the element is an enum class
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
     * Create validation configuration from annotation
     */
    private ErrorCodeValidationConfig createValidationConfig(ValidErrorCode annotation) {
        return new ErrorCodeValidationConfig(
            annotation.prefix(),
            annotation.length(),
            annotation.codeField(),
            annotation.excludeValues()
        );
    }
    
    /**
     * Validate all enum constants in the enum class
     */
    private void validateAllEnumConstants(Element enumElement, ErrorCodeValidationConfig config) {
        for (Element enumConstant : enumElement.getEnclosedElements()) {
            if (enumConstant.getKind() == ElementKind.ENUM_CONSTANT) {
                validateSingleEnumConstant(enumConstant, config);
            }
        }
    }
    
    /**
     * Validate a single enum constant
     */
    private void validateSingleEnumConstant(Element enumConstant, ErrorCodeValidationConfig config) {
        VariableElement codeField = findAndValidateCodeField(enumConstant, config.getCodeFieldName());
        if (codeField == null) {
            return;
        }
        
        Integer codeValue = extractErrorCodeValue(enumConstant, config.getCodeFieldName());
        if (codeValue == null) {
            reportErrorCodeExtractionFailure(enumConstant, config.getCodeFieldName());
            return;
        }
        
        if (shouldSkipValidation(codeValue, config.getExcludeValues())) {
            return;
        }
        
        validateErrorCodeFormat(enumConstant, codeValue, config);
    }
    
    /**
     * Find and validate the error code field in the enum constant
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
     * Extract error code value from enum constant
     */
    private Integer extractErrorCodeValue(Element enumConstant, String codeFieldName) {
        return ErrorCodeExtractor.extractErrorCodeValue(enumConstant, codeFieldName, trees);
    }
    
    /**
     * Report error when error code extraction fails
     */
    private void reportErrorCodeExtractionFailure(Element enumConstant, String codeFieldName) {
        String errorMessage = String.format(
            ErrorMessages.UNABLE_TO_EXTRACT_ERROR_CODE,
            enumConstant.getSimpleName()
        );
        ErrorCodeValidator.reportError(enumConstant, errorMessage, messager);
    }
    
    /**
     * Check if validation should be skipped for this value
     */
    private boolean shouldSkipValidation(Integer codeValue, int[] excludeValues) {
        return ErrorCodeValidator.shouldExcludeFromValidation(codeValue, excludeValues);
    }
    
    /**
     * Validate the format of the error code
     */
    private void validateErrorCodeFormat(Element enumConstant, Integer codeValue, ErrorCodeValidationConfig config) {
        ErrorCodeValidator.validateErrorCodeFormat(
            enumConstant, 
            codeValue, 
            config.getPrefix(), 
            config.getTotalLength(), 
            config.getCodeFieldName(), 
            messager
        );
    }
    
    /**
     * Configuration class for error code validation
     */
    private static class ErrorCodeValidationConfig {
        private final String prefix;
        private final int totalLength;
        private final String codeFieldName;
        private final int[] excludeValues;
        
        public ErrorCodeValidationConfig(String prefix, int totalLength, String codeFieldName, int[] excludeValues) {
            this.prefix = prefix;
            this.totalLength = totalLength;
            this.codeFieldName = codeFieldName;
            this.excludeValues = excludeValues;
        }
        
        public String getPrefix() { return prefix; }
        public int getTotalLength() { return totalLength; }
        public String getCodeFieldName() { return codeFieldName; }
        public int[] getExcludeValues() { return excludeValues; }
    }
}
