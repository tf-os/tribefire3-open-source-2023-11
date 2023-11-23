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
package com.braintribe.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.utils.MapTools;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see {@link com.braintribe.common.lcd.Constants}.
 */
public final class Constants extends com.braintribe.common.lcd.Constants {

	private Constants() {
		// no instantiation required
	}

	/**
	 * The line separators for Windows on (\r\n), Unix/Linux/MacOS (\n) and old MacOS (\r).
	 */
	public static final List<String> LINE_SEPARATORS = Collections.unmodifiableList(Arrays.asList("\r\n", "\n", "\r"));

	/**
	 * Maps from {@link #LINE_SEPARATORS line separator} to the respective regex.
	 */
	public static final Map<String, String> LINE_SEPARATORS_TO_REGEXES = Collections
			.unmodifiableMap(MapTools.getStringMap("\r\n", "\\r\\n", "\n", "\\n", "\r", "\\r"));
}
