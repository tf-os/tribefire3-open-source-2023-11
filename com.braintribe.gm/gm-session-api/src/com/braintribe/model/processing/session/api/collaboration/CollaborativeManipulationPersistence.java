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

import java.io.File;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * @author peter.gazdik
 */
public interface CollaborativeManipulationPersistence extends PersistenceInitializer {

	File getStrogaBase();

	void onCollaborativeAccessInitialized(CollaborativeAccess csa, ManagedGmSession csaSession);

	void setModelOracle(ModelOracle modelOracle);

	Stream<PersistenceStage> getPersistenceStages();

	/** @return the {@link PersistenceStage} of the current appender */
	@Override
	PersistenceStage getPersistenceStage();

	PersistenceAppender getPersistenceAppender() throws ManipulationPersistenceException;

	PersistenceAppender newPersistenceAppender(String name);

	void renamePersistenceStage(String oldName, String newName);

	void mergeStage(String source, String target);

	void reset();

	/** @see CollaborativeAccess#getResourcesForStage(String) */
	Stream<Resource> getResourcesForStage(String name);

	/** @see CollaborativeAccess#getModifiedEntitiesForStage(String) */
	Stream<Supplier<Set<GenericEntity>>> getModifiedEntitiesForStage(String name);

}
