package io.github.tcq1007.valid.errorcode.util;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;

/**
 * Utility class for finding fields in enum classes
 */
public class EnumFieldFinder {
    
    /**
     * Find the error code field in an enum constant by field name
     * 
     * @param enumConstant The enum constant element
     * @param fieldName The name of the field to find
     * @return The variable element representing the field, or null if not found
     */
    public static VariableElement findErrorCodeField(Element enumConstant, String fieldName) {
        TypeElement enumClass = getEnumClassFromConstant(enumConstant);
        if (enumClass == null) {
            return null;
        }

        return findInstanceFieldByName(enumClass, fieldName);
    }
    
    /**
     * Validate that the error code field is of int type
     * 
     * @param codeField The field to validate
     * @return true if the field is of int type, false otherwise
     */
    public static boolean isIntegerField(VariableElement codeField) {
        return codeField != null && codeField.asType().getKind() == TypeKind.INT;
    }
    
    /**
     * Find the index position of a field in the constructor parameters
     * 
     * @param enumConstant The enum constant
     * @param fieldName The field name to find
     * @return The index position, or -1 if not found
     */
    public static int findFieldIndexInConstructor(Element enumConstant, String fieldName) {
        try {
            TypeElement enumClass = getEnumClassFromConstant(enumConstant);
            if (enumClass == null) {
                return -1;
            }
            
            ExecutableElement constructor = findEnumConstructor(enumClass);
            if (constructor == null) {
                return -1;
            }
            
            return findParameterIndexByName(constructor, fieldName);
        } catch (Exception e) {
            return -1;
        }
    }
    
    private static TypeElement getEnumClassFromConstant(Element enumConstant) {
        TypeElement enumClass = (TypeElement) enumConstant.getEnclosingElement();
        if (enumClass == null || !enumClass.getKind().isClass()) {
            return null;
        }
        return enumClass;
    }
    
    private static VariableElement findInstanceFieldByName(TypeElement enumClass, String fieldName) {
        for (Element enclosed : enumClass.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosed;
                if (field.getSimpleName().contentEquals(fieldName) && 
                    !field.getModifiers().contains(Modifier.STATIC)) {
                    return field;
                }
            }
        }
        return null;
    }
    
    private static ExecutableElement findEnumConstructor(TypeElement enumClass) {
        for (Element element : enumClass.getEnclosedElements()) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                return (ExecutableElement) element;
            }
        }
        return null;
    }
    
    private static int findParameterIndexByName(ExecutableElement constructor, String parameterName) {
        for (int i = 0; i < constructor.getParameters().size(); i++) {
            if (constructor.getParameters().get(i).getSimpleName().contentEquals(parameterName)) {
                return i;
            }
        }
        return -1;
    }
}