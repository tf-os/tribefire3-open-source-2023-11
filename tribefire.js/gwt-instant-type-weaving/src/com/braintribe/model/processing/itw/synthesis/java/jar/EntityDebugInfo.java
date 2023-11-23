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
package com.braintribe.model.processing.itw.synthesis.java.jar;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.braintribe.model.processing.itw.asm.DebugInfoProvider;

/**
 * Provides debug information for entity source code given as a {@link EntityDebugInfo#EntityDebugInfo(String)
 * constructor} argument.
 * 
 * @see DebugInfoProvider
 */
class EntityDebugInfo {

	private final Map<String, Integer> methodLines = new HashMap<String, Integer>();
	private final Map<String, String> setterParameters = new HashMap<String, String>();

	private static final String VAR = "[^\\s(){};]+";
	private static final String S = "\\s*";
	private static final String Sp = "\\s+";
	private static final String LINE_WITH_GETTER = "public" + Sp + VAR + Sp + "get" + VAR + S + "\\(" + S + "\\).*";
	private static final String LINE_WITH_SETTER = "public\\s+void\\s+set" + VAR + S + "+\\(" + S + VAR + Sp + VAR + S + "\\).*";

	public EntityDebugInfo(String source) {
		int counter = 0;

		Scanner scanner = new Scanner(source);
		while (scanner.hasNextLine()) {
			process(scanner.nextLine(), ++counter);
		}

		scanner.close();
	}

	private void process(String line, int num) {
		line = line.trim();
		if (line.matches(LINE_WITH_GETTER)) {
			processGetter(line, num);

		} else if (line.matches(LINE_WITH_SETTER)) {
			processSetter(line, num);
		}

	}

	private void processGetter(String line, int num) {
		registerMethod(line, num);
	}

	private void processSetter(String line, int num) {
		String setterName = registerMethod(line, num);
		registerSetterParameter(setterName, line);
	}

	private String registerMethod(String line, int num) {
		int pos = line.indexOf("(");
		line = line.substring(0, pos);

		pos = line.lastIndexOf(" ");
		line = line.substring(pos + 1);

		methodLines.put(line, num + 1); // we want the next line

		return line;
	}

	private void registerSetterParameter(String setterName, String line) {
		int pos = line.indexOf(")");
		line = line.substring(0, pos).trim();

		pos = line.lastIndexOf(" ");
		line = line.substring(pos + 1);

		setterParameters.put(setterName, line);
	}

	public Integer getMethodLine(String methodName) {
		return methodLines.get(methodName);
	}

	public String getSetterParameterName(String setterName) {
		return setterParameters.get(setterName);
	}

}
