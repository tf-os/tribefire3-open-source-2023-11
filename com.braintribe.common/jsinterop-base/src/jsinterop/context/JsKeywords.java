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
package jsinterop.context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author peter.gazdik
 */
public class JsKeywords {

	/** Actually JS reserved words, but keywords is shorter and cooler */
	public static List<String> jsKeywords = Arrays.asList("arguments", "await", "break", "case", "catch", "class", "const", "continue", "debugger",
			"default", "delete", "do", "else", "enum", "eval", "export", "extends", "false", "finally", "for", "function", "if", "implements",
			"import", "in", "instanceof", "interface", "let", "new", "null", "package", "private", "protected", "prototype", "public", "return",
			"static", "super", "switch", "this", "throw", "true", "try", "typeof", "var", "void", "while", "with", "yield");

	/** Converts java class Name to a valid JS class name within a namespace. Escapes JS reserved words in every part of the package if needed. */
	public static String classNameToJs(String className) {
		int i = className.lastIndexOf('.');
		String packageName = className.substring(0, i);
		String simpleName = className.substring(i + 1);

		return packageToJsNamespace(packageName) + "." + simpleName;
	}

	/** Converts java package name to valid JS namespace name. Escapes JS reserved words in every part of the package if needed. */
	public static String packageToJsNamespace(String p) {
		return Stream.of(p.split("\\.")) //
				.map(JsKeywords::javaIdentifierToJs) //
				.collect(Collectors.joining("."));
	}

	public static String javaIdentifierToJs(String s) {
		String nameBase = removeTrailingUnderscores(s);

		return jsKeywords.contains(nameBase) ? s + "_" : s;
	}

	private static String removeTrailingUnderscores(String s) {
		while (s.endsWith("_"))
			s = s.substring(0, s.length() - 1);

		return s;
	}

}
