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
package com.braintribe.model.access.hibernate;

/**
 * A simple helper class that keeps tracks on manipulations statistics.
 * @author gunther.schenk
 *
 */
public class HibernateApplyStatistics {

	private int creations = 0;
	private int deletions = 0;
	private int valueChanges = 0;
	
	public void increaseCreations() {
		creations++;
	}
	
	public void increaseDeletions() {
		deletions++;
	}

	public void increaseValueChanges() {
		valueChanges++;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("applyStatistics: %d creations done, %d deletions done, %d updates done", creations, deletions, valueChanges);
	}
	public static String getBuildVersion() {

		return "$Build_Version$ $Id: HibernateApplyStatistics.java 86391 2015-05-28 14:25:17Z roman.kurmanowytsch $";
	}
}
