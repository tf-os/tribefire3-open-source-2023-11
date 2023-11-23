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
package com.braintribe.gwt.utils.genericmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.common.lcd.ConfigurationException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.CommonTools;

/**
 * This {@link EntitiesFinder} is used to combine the result of {@link #setDelegates(List) multiple EntitiesFinders}.
 * For more info see method {@link #findEntities(PersistenceGmSession)}.
 *
 */
public class CompoundEntitiesFinder implements EntitiesFinder {

	private List<EntitiesFinder> delegates;

	/**
	 * Passes the request to the configured {@link #setDelegates(List) delegates} and returns the union of the result
	 * sets.
	 *
	 * @throws ConfigurationException
	 *             if the list of delegates is <code>null</code>.
	 */
	@Override
	public Set<GenericEntity> findEntities(final PersistenceGmSession session) throws ConfigurationException {

		if (this.delegates == null) {
			throw new ConfigurationException("The configured list of delegates must not be null!");
		}

		final Set<GenericEntity> foundEntitiesUnion = new HashSet<GenericEntity>();

		for (final EntitiesFinder entitiesFinder : this.delegates) {
			final Set<GenericEntity> foundEntities = entitiesFinder.findEntities(session);
			if (!CommonTools.isEmpty(foundEntities)) {
				foundEntitiesUnion.addAll(foundEntities);
			}
		}

		return foundEntitiesUnion;
	}

	public List<EntitiesFinder> getDelegates() {
		return this.delegates;
	}

	public void setDelegates(final List<EntitiesFinder> delegates) {
		this.delegates = delegates;
	}
}
