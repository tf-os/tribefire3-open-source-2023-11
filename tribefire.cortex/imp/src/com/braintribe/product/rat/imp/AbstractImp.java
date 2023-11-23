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
package com.braintribe.product.rat.imp;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * The base class for all {@link Imp} implementations.
 */
public abstract class AbstractImp<T extends GenericEntity> extends AbstractHasSession implements Imp<T> {

	protected final T instance;

	public AbstractImp(PersistenceGmSession session, T instance) {
		super(session);

		if (instance == null) {
			throw new ImpException("Cannot create imp with an instance == null");
		}

		this.instance = instance;
	}

	@Override
	public T get() {
		return instance;
	}

	@Override
	public void delete() {
		session().deleteEntity(instance);
	}
}
