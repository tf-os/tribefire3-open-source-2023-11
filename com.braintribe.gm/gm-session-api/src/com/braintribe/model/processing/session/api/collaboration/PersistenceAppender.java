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
package com.braintribe.model.processing.session.api.collaboration;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * Component of {@link CollaborativeAccess} which is responsible for persisting applied manipulations.
 */
public interface PersistenceAppender {

	PersistenceStage getPersistenceStage();

	/**
	 * Appends given {@link Manipulation} to the underlying persistence.
	 * 
	 * @return array of {@link AppendedSnippet}s - one for data and for model. This object provides a stream with data which correspond to the
	 *         persisted manipulation, and when wrapped into a {@link Resource} is compatible with the {@link #append(Resource[], EntityManager)}
	 *         method.
	 */
	AppendedSnippet[] append(Manipulation manipulation, ManipulationMode mode);

	/**
	 * {@link Resource} based alternative to {@link #append(Manipulation, ManipulationMode)}. See also the description of what this other method
	 * returns.
	 */
	void append(Resource[] gmmlResources, EntityManager entityManager);

	static interface AppendedSnippet extends InputStreamProvider {

		long sizeInBytes();

	}

}
