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
package com.braintribe.model.processing.findrefs;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;

/**
 * A {@link MapCandidateProperty} is a {@link CandidateProperty} for a Map-property that also contains information where
 * in the parameterization of the Map references might be located
 * 
 * 
 */
public class MapCandidateProperty extends CandidateProperty {

	/**
	 * Contains the possible combinations where references could be located
	 * 
	 * 
	 */
	public enum MapRefereeType {
		KEY_REF, VALUE_REF, BOTH, NONE;

		public static MapRefereeType getType(boolean keyReferencePossible, boolean valueReferencePossible) {
			if (keyReferencePossible && valueReferencePossible) {
				return BOTH;
			} else if (keyReferencePossible) {
				return KEY_REF;
			} else if (valueReferencePossible) {
				return VALUE_REF;
			} else {
				return NONE;
			}
		}
	}

	private MapRefereeType refereeType;

	public MapCandidateProperty(GmEntityType entityType, GmProperty property, MapRefereeType refereeType) {
		super(entityType, property);
		this.refereeType = refereeType;
	}

	public MapRefereeType getRefereeType() {
		return refereeType;
	}
}
