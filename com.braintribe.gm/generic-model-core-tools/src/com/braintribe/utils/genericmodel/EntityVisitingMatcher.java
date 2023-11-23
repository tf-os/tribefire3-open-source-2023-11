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
package com.braintribe.utils.genericmodel;

import java.util.Set;

import com.braintribe.common.lcd.Empty;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.utils.lcd.NullSafe;

/**
 * {@link AbstractEntityVisitingMatcher} implementation that doesn't {@link #matchesEntity(GenericEntity) match}
 * entities, except for the {@link #setEntitiesToMatch(Set) entities to match}.
 *
 * @author michael.lafite
 */
public class EntityVisitingMatcher extends AbstractEntityVisitingMatcher {

	private Set<? extends GenericEntity> entitiesToMatch = Empty.set();

	public EntityVisitingMatcher() {
		// nothing to do
	}

	public EntityVisitingMatcher(final Set<? extends GenericEntity> entitiesToMatch) {
		if (entitiesToMatch == null) {
			this.entitiesToMatch = Empty.set();
		} else {
			this.entitiesToMatch = entitiesToMatch;
		}
	}

	public void setEntitiesToMatch(final Set<? extends GenericEntity> entitiesToMatch) {
		this.entitiesToMatch = entitiesToMatch;
	}

	/**
	 * Doesn't match, i.e. returns <code>false</code>, unless the <code>entity</code> is one of the
	 * {@link #entitiesToMatch entities to match}.
	 */
	@Override
	protected boolean matchesEntity(final GenericEntity entity) {
		if (NullSafe.contains(this.entitiesToMatch, entity)) {
			return true;
		}
		return false;
	}

}
