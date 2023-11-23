// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.gwt.simplepropertypanel.client.validation;

import java.util.Arrays;
import java.util.Set;

import com.braintribe.gwt.utils.client.FastSet;

public class GmTypeValidator {
	
	private static final Set<String> tfKeywords = new FastSet(Arrays.asList("partition", "globalId", "id"));
	
	private static final Set<String> javaKeywords = new FastSet(Arrays.asList(
		    "abstract",     "assert",        "boolean",      "break",           "byte",
		    "case",         "catch",         "char",         "class",           "const",
		    "continue",     "default",       "do",           "double",          "else",
		    "enum",         "extends",       "false",        "final",           "finally",
		    "float",        "for",           "goto",         "if",              "implements",
		    "import",       "instanceof",    "int",          "interface",       "long",
		    "native",       "new",           "null",         "package",         "private",
		    "protected",    "public",        "return",       "short",           "static",
		    "strictfp",     "super",         "switch",       "synchronized",    "this",
		    "throw",        "throws",        "transient",    "true",            "try",
		    "void",         "volatile",      "while"
		));

//	private static final Pattern JAVA_CLASS_NAME_PART_PATTERN = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*");
//	private static final Pattern JAVA_CLASS_ATTRIBUTE_NAME_PART_PATTERN = Pattern.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*");
	
	private static final String JAVA_CLASS_NAME_PART_PATTERN = "[A-Za-z_$]+[a-zA-Z0-9_$]*";
//	private static final RegExp JAVA_CLASS_NAME_PART_PATTERN = RegExp.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*", "gi");
//	private static final RegExp JAVA_CLASS_ATTRIBUTE_NAME_PART_PATTERN = RegExp.compile("[A-Za-z_$]+[a-zA-Z0-9_$]*", "gi");
	private static final String JAVA_CLASS_ATTRIBUTE_NAME_PART_PATTERN = "[A-Za-z_$]+[a-zA-Z0-9_$]*";
	
	public static boolean isValidTypeName(String text) {
	    for (String part : text.split("\\.")) {
	        if (javaKeywords.contains(part) || !text.matches(JAVA_CLASS_NAME_PART_PATTERN)) {
	            return false;
	        }
	    }
	    return text.length() > 0;
	}
	
	public static boolean isValidPropertyName(String text) {
	    for (String part : text.split("\\.")) {
	        if (tfKeywords.contains(part) || javaKeywords.contains(part) || !text.matches(JAVA_CLASS_ATTRIBUTE_NAME_PART_PATTERN)) {
	            return false;
	        }
	    }
	    return text.length() > 0;
	}

}
