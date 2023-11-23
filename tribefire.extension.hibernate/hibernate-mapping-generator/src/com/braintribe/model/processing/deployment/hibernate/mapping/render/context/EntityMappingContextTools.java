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
package com.braintribe.model.processing.deployment.hibernate.mapping.render.context;

import java.util.regex.Pattern;

public class EntityMappingContextTools {

	private static final Pattern quotationNeededPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

	/**
	 * Quotes a database identifier with back-ticks, if necessary.
	 * 
	 * @param dbIdentifier
	 *            The database identifier to be quoted
	 */
	public static String quoteIdentifier(String dbIdentifier) {
		if (dbIdentifier == null || (dbIdentifier.startsWith("`") && dbIdentifier.endsWith("`")))
			return dbIdentifier;

		if (!quotationNeededPattern.matcher(dbIdentifier).matches())
			return "`" + dbIdentifier + "`";

		return dbIdentifier;
	}

}
