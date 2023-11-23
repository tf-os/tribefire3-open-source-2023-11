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

import com.braintribe.model.processing.deployment.hibernate.mapping.HbmXmlGenerationContext;
import com.braintribe.utils.lcd.CommonTools;

/**
 * 
 */
abstract class AbstractDescriptor {

	protected final HbmXmlGenerationContext context;

	public String tag;

	protected AbstractDescriptor(HbmXmlGenerationContext context) {
		this.context = context;
	}

	protected static String simpleName(String fullName) {
		int pos = fullName.lastIndexOf(".");

		if (pos < 0)
			return fullName;
		else
			return fullName.substring(pos + 1);
	}

	protected static String capitalize(String s) {
		return CommonTools.capitalize(s);
	}

	public String getTag() {
		return tag;
	}

	/**
	 * <p>
	 * Applies the table prefix as given by {@link HbmXmlGenerationContext#tablePrefix} to table names configured via
	 * metadata or hints.
	 * 
	 * @param hintedTableName
	 *            Table name configured via metadata or hints.
	 * @return The table name possibly prefixed.
	 */
	protected String prefixTableName(String hintedTableName) {
		String p = context.tablePrefix;
		return p != null ? p + hintedTableName : hintedTableName;
	}

}
