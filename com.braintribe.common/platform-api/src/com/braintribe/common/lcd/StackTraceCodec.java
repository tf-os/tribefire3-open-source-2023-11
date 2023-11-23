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
package com.braintribe.common.lcd;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.exception.Exceptions;

/**
 * Please refrain from using this class for encoding only. Use {@link Exceptions#stringify(Throwable)} instead, as this codec, for example, fails to
 * deal with the target exception of {@link InvocationTargetException}
 */
public class StackTraceCodec {

	public final static StackTraceCodec INSTANCE = new StackTraceCodec();

	private static final char vs = ':';
	private static final char ls = '\n';

	private StackTraceCodec() {
	}

	public String encode(StackTraceElement[] stackTraceElems) {

		if (stackTraceElems == null || stackTraceElems.length == 0) {
			return null;
		}

		StringBuilder result = new StringBuilder(stackTraceElems.length * 200);

		for (StackTraceElement elem : stackTraceElems) {

			if (elem == null || elem.getClassName() == null || elem.getMethodName() == null) {
				continue;
			}

			// @formatter:off
			result
				.append(elem.getClassName())
				.append(vs)
				.append(elem.getMethodName())
				.append(vs)
				.append(elem.getFileName())
				.append(vs)
				.append(elem.getLineNumber())
				.append(ls);
			// @formatter:on

		}

		return result.toString();
	}

	public StackTraceElement[] decode(String string) {

		if (string == null || string.isEmpty()) {
			return null;
		}

		String[] stringElems = string.split(String.valueOf(ls));

		if (stringElems == null || stringElems.length == 0) {
			return null;
		}

		List<StackTraceElement> resultElems = new ArrayList<>(stringElems.length);

		String elemPattern = "\\" + vs;

		for (String elem : stringElems) {

			if (elem == null) {
				continue;
			}

			String[] elemParts = elem.split(elemPattern);

			if (elemParts.length < 4) {
				continue;
			}

			resultElems.add(new StackTraceElement(elemParts[0], elemParts[1], convertFileName(elemParts[2]), convertLineNumber(elemParts[3])));

		}

		if (!resultElems.isEmpty()) {
			return resultElems.toArray(new StackTraceElement[resultElems.size()]);
		}

		return null;

	}

	private static String convertFileName(String string) {

		// the file name is the only property of a StackTraceElement which can be null.

		if ("null".equals(string)) {
			return null;
		}

		return string;

	}

	private static int convertLineNumber(String string) {

		if (string == null || string.trim().isEmpty()) {
			return -1;
		}

		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return -1;
		}

	}

}
