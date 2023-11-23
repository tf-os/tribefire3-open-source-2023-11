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
package tribefire.extension.messaging.model;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Defines the strategy to persist the binaries of a resource
 *
 */
public enum ResourceBinaryPersistence implements EnumBase {
	/**
	 * No resource
	 */
	NONE,
	/**
	 * Only transient resources
	 */
	TRANSIENT,
	/**
	 *  Transient and access based resources
	 */
	ALL;

	public static final EnumType T = EnumTypes.T(ResourceBinaryPersistence.class);

	@Override
	public EnumType type() {
		return T;
	}

	public boolean shouldPersist(boolean isTransient) {
		boolean shouldPersist;
		switch (this) {
			case TRANSIENT -> shouldPersist = isTransient;
			case ALL ->	shouldPersist = true;
			default -> shouldPersist = false;
		}
		return shouldPersist;
	}
}
