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

/**
 * {@link #getSeparator() Provides} the plattform dependent line separator. The main purpose of this class is to separate GWT incompatible code so
 * that it can easily be replaced (i.e. emulated) in GWT. Please just use {@link com.braintribe.utils.lcd.CommonTools#LINE_SEPARATOR} instead.
 *
 * @author michael.lafite
 */
public final class LineSeparatorProvider {

	private static final String SEPARATOR = System.getProperty("line.separator");

	private LineSeparatorProvider() {
		// no instantiation required
	}

	/**
	 * Returns the platform dependent line separator.
	 */
	public static String getSeparator() {
		return SEPARATOR;
	}

}
