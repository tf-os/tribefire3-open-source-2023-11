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
package com.braintribe.model.generic.enhance;

import com.braintribe.model.generic.GenericEntity;

/**
 * 
 * @author peter.gazdik
 */
public class EntityFlags {

	public static final int SHALLOW = 1;

	public static boolean isShallow(GenericEntity ge) {
		return (((EnhancedEntity) ge).flags() & SHALLOW) != 0;
	}

	public static void setShallow(GenericEntity ge, boolean shallow) {
		setFlag((EnhancedEntity) ge, SHALLOW, shallow);
	}

	private static void setFlag(EnhancedEntity ee, int FLAG, boolean flagValue) {
		if (flagValue) {
			ee.assignFlags(ee.flags() | FLAG);
		} else {
			ee.assignFlags(ee.flags() & ~FLAG);
		}
	}

}
