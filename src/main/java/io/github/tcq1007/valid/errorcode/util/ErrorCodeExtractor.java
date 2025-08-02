package io.github.tcq1007.valid.errorcode.util;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import io.github.tcq1007.valid.errorcode.constants.RegexPatterns;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting error code values from enum constants
 */
public class ErrorCodeExtractor {
    
    /**
     * Extract error code value from enum constant using multiple strategies
     * 
     * @param enumConstant The enum constant element
     * @param codeFieldName The name of the error code field
     * @param trees The Trees instance for syntax tree analysis
     * @return The extracted error code value, or null if extraction fails
     */
    public static Integer extractErrorCodeValue(Element enumConstant, String codeFieldName, Trees trees) {
        // Try syntax tree method first
        Integer value = extractFromSyntaxTree(enumConstant, codeFieldName, trees);
        if (value != null) {
            return value;
        }
        
        // Fall back to string parsing methods
        return extractFromSourceString(enumConstant, codeFieldName);
    }
    
    /**
     * Extract error code value using syntax tree analysis
     * This is the most reliable method when available
     */
    private static Integer extractFromSyntaxTree(Element enumConstant, String codeFieldName, Trees trees) {
        try {
            TreePath path = trees.getPath(enumConstant);
            if (path == null) {
                return null;
            }

            Tree leaf = path.getLeaf();
            if (!(leaf instanceof VariableTree)) {
                return null;
            }

            VariableTree varTree = (VariableTree) leaf;
            ExpressionTree initializer = varTree.getInitializer();
            
            if (!(initializer instanceof NewClassTree)) {
                return null;
            }

            NewClassTree newClassTree = (NewClassTree) initializer;
            List<? extends ExpressionTree> arguments = newClassTree.getArguments();
            
            if (arguments.isEmpty()) {
                return null;
            }

            int codeFieldIndex = EnumFieldFinder.findFieldIndexInConstructor(enumConstant, codeFieldName);
            if (codeFieldIndex == -1 || codeFieldIndex >= arguments.size()) {
                return null;
            }

            ExpressionTree argTree = arguments.get(codeFieldIndex);
            
            if (argTree instanceof LiteralTree) {
                LiteralTree literal = (LiteralTree) argTree;
                Object value = literal.getValue();
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract error code value from string representation of enum constant
     * This is a fallback method when syntax tree analysis fails
     */
    private static Integer extractFromSourceString(Element enumConstant, String codeFieldName) {
        try {
            String enumConstantStr = enumConstant.toString();
            
            // Try regex pattern matching first
            Integer value = extractUsingRegexPattern(enumConstant, enumConstantStr);
            if (value != null) {
                return value;
            }
            
            // Try constructor parameter order method
            return extractFromConstructorParameterOrder(enumConstant, codeFieldName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract error code using regex pattern matching
     * Matches patterns like "ENUM_NAME(123, "message")"
     */
    private static Integer extractUsingRegexPattern(Element enumConstant, String enumConstantStr) {
        try {
            String patternString = String.format(RegexPatterns.ENUM_CONSTRUCTOR_FIRST_INT, enumConstant.getSimpleName());
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(enumConstantStr);
            
            if (matcher.find()) {
                String numberStr = matcher.group(1);
                return Integer.parseInt(numberStr);
            }
        } catch (Exception e) {
            // Ignore and try next method
        }
        return null;
    }
    
    /**
     * Extract error code based on constructor parameter order
     */
    private static Integer extractFromConstructorParameterOrder(Element enumConstant, String codeFieldName) {
        try {
            int codeFieldIndex = EnumFieldFinder.findFieldIndexInConstructor(enumConstant, codeFieldName);
            if (codeFieldIndex == -1) {
                return null;
            }
            
            String enumStr = enumConstant.toString();
            Pattern pattern = Pattern.compile(RegexPatterns.CONSTRUCTOR_ARGUMENTS);
            Matcher matcher = pattern.matcher(enumStr);
            
            if (matcher.find()) {
                String argsStr = matcher.group(1);
                String[] args = argsStr.split(",");
                
                if (codeFieldIndex < args.length) {
                    String arg = args[codeFieldIndex].trim();
                    // Remove quotes and other characters, keep only digits
                    arg = arg.replaceAll(RegexPatterns.NON_DIGIT_EXCEPT_MINUS, "");
                    if (!arg.isEmpty()) {
                        return Integer.parseInt(arg);
                    }
                }
            }
            
        } catch (Exception e) {
            // Parsing failed
        }
        
        return null;
    }
}