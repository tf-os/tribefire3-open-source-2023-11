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
package com.braintribe.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Improved version of {@link com.braintribe.utils.lcd.CustomToStringBuilder} that also supports arrays with {@link Class#getComponentType() primitive
 * component type} (not GWT compatible).
 *
 * @author michael.lafite
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class CustomToStringBuilder extends com.braintribe.utils.lcd.CustomToStringBuilder {

	private static final CustomToStringBuilder DEFAULT_INSTANCE = new CustomToStringBuilder();

	public CustomToStringBuilder() {
		this(null);
	}

	public CustomToStringBuilder(final CustomStringRepresentationProvider customStringRepresentationProvider) {
		super(customStringRepresentationProvider);
	}

	public static CustomToStringBuilder instance() {
		return DEFAULT_INSTANCE;
	}

	/**
	 * (Also) properly handles arrays with a primitive component types.
	 */
	@Override
	protected String arrayToStringAfterNullCheck(final Object array) {
		CommonTools.assertIsArray(array);
		return collectionToStringAfterNullCheck(CommonTools.arrayToList(array));
	}
}
