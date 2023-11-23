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
package com.braintribe.devrock.api.ui.properties;

import com.braintribe.devrock.api.ui.editors.DirectoryEditor;

/**
 * a specialized interface for virtual env support for the editors,
 * notably only the {@link DirectoryEditor} for now
 * 
 * @author pit
 *
 */
public interface VirtualPropertyResolver {
	/**
	 * @param key
	 * @return
	 */
	String getSystemProperty(String key);
	/**
	 * @param key
	 * @return
	 */
	String getEnvironmentProperty( String key);
	
	/**
	 * parses an expression and resolves all variables within it 
	 * @param expression - the expression to parse
	 * @return - the parsed result
	 */
	String resolve(String expression);
}
