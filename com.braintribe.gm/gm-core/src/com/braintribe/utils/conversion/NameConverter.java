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
package com.braintribe.utils.conversion;

import com.braintribe.model.meta.data.display.NameConversionStyle;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class NameConverter {

	public static String convert(String camelCase, NameConversionStyle style) {
		switch (style) {
			case screamingSnakeCase:
				return StringTools.camelCaseToScreamingSnakeCase(camelCase);
			case snakeCase:
				return StringTools.camelCaseToSnakeCase(camelCase);
			default:
				throw new UnsupportedOperationException("This converter does not support the style: " + style);

		}
	}

}
