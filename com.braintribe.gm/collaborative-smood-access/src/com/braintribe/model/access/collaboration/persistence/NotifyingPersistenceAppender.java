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
package com.braintribe.model.access.collaboration.persistence;

import java.util.function.Consumer;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceAppender;
import com.braintribe.model.processing.session.api.managed.EntityManager;
import com.braintribe.model.processing.session.api.managed.ManipulationMode;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * {@link PersistenceAppender} that also notifies a listener in case the {@link Manipulation} was successfully appended
 * to the delegate.
 */
public class NotifyingPersistenceAppender implements PersistenceAppender {

	private static final Logger log = Logger.getLogger(NotifyingPersistenceAppender.class);

	private final PersistenceAppender delegate;
	private final Consumer<Manipulation> appendedManipulationListener;

	public NotifyingPersistenceAppender(PersistenceAppender delegate, Consumer<Manipulation> appendedManipulationListener) {
		this.delegate = delegate;
		this.appendedManipulationListener = appendedManipulationListener;
	}

	@Override
	public AppendedSnippet[] append(Manipulation manipulation, ManipulationMode mode) throws ManipulationPersistenceException {
		AppendedSnippet[] result = delegate.append(manipulation, mode);

		try {
			appendedManipulationListener.accept(manipulation);
		} catch (Exception e) {
			log.error("Error while notifying listener with appended manipulations.", e);
		}

		return result;
	}

	@Override
	public void append(Resource[] gmmlResources, EntityManager entityManager) {
		delegate.append(gmmlResources, entityManager);
	}

	@Override
	public PersistenceStage getPersistenceStage() {
		return delegate.getPersistenceStage();
	}

}
