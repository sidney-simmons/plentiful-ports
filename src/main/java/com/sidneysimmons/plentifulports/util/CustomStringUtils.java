package com.sidneysimmons.plentifulports.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Custom version of the apache string utils.
 * 
 * @author Sidney Simmons
 */
public class CustomStringUtils extends StringUtils {

    /**
     * Private constructor.
     */
    private CustomStringUtils() {
        // No need to instantiate this
    }

    /**
     * Check if the given strings are equal. Ignores the type of line endings.
     * 
     * @param string1 string 1
     * @param string2 string 2
     * @return true if the strings are equal, false otherwise
     */
    public static Boolean equalsIgnoreLineEnds(String string1, String string2) {
        return normalizeLineEndings(string1).equals(normalizeLineEndings(string2));
    }

    /**
     * Normalize the line endings within a given string. All "\r\n" and "\r" will be replaced with "\n".
     * 
     * @param string a string
     * @return the normalized string
     */
    public static String normalizeLineEndings(String string) {
        return string.replace("\r\n", "\n").replace("\r", "\n");
    }

}
