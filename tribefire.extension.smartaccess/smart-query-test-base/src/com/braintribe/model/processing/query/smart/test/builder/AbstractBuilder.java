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
package com.braintribe.model.processing.query.smart.test.builder;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;

/**
 * 
 */
public class AbstractBuilder<T extends GenericEntity, B extends AbstractBuilder<T, B>> {

	protected final B self;
	protected final RepositoryDriver repoDriver;
	protected final Class<T> clazz;
	protected final T instance;

	protected AbstractBuilder(Class<T> clazz, RepositoryDriver repoDriver) {
		this.clazz = clazz;
		this.repoDriver = repoDriver;
		this.instance = repoDriver.newInstance(clazz);
		this.self = (B) this;
	}

	public T create() {
		repoDriver.commit();

		return instance;
	}

	public void commitChanges() {
		repoDriver.commit();
	}

}
