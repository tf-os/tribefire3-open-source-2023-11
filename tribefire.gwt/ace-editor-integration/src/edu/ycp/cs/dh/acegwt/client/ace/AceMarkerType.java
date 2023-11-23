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
package edu.ycp.cs.dh.acegwt.client.ace;

/**
 *	This enumeration represents the selection/marker types.
 */
public enum AceMarkerType {
	
	/**
	 * Highlights the whole line. 
	 */
	FULL_LINE("fullLine"),
	
	/**
	 * Highlights the whole screen line.
	 */
	SCREEN_LINE("screenLine"),
	
	/**
	 * Highlights only the range.
	 */
	TEXT("text");

	private final String name;

	private AceMarkerType(final String name) {
		this.name = name;
	}

	/**
	 * @return the marker type name (e.g., "error")
	 */
	public String getName() {
		return name;
	}
}
