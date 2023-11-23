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
package com.braintribe.model.processing.query.tools;

import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Source;
import com.braintribe.utils.lcd.CommonTools;

/**
 * @author peter.gazdik
 */
public class QueryModelTools {

	public static String printWithAlias(Source source) {
		if (source instanceof From)
			return printWithAlias((From) source);

		if (source instanceof Join)
			return printWithAlias((Join) source);

		if (source != null)
			return source.entityType().getShortName();

		return "null";
	}

	public static String printWithAlias(From from) {
		if (from.getName() != null)
			return from.getName();
		else
			return CommonTools.getClassNameFromFullyQualifiedClassName(from.getEntityTypeSignature());
	}

	public static String printWithAlias(Join join) {
		if (join.getName() != null)
			return join.getName();
		else
			return printWithAlias(join.getSource()) + "." + join.getProperty();
	}

}
