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
package com.braintribe.model.processing.garbagecollection;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.gwt.utils.genericmodel.EntitiesFinder;
import com.braintribe.gwt.utils.genericmodel.EntityTypeBasedEntitiesFinder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.cleanup.GarbageCollection;
import com.braintribe.model.meta.data.cleanup.GarbageCollectionKind;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Finds entities based on the {@link GarbageCollection} metadata.
 *
 * @author michael.lafite
 */
public class GarbageCollectionMetaDataBasedEntitiesFinder implements EntitiesFinder {

	private final GmMetaModel metaModel;
	private final String useCase;
	private final GarbageCollectionKind searchedGarbageCollectionKind;

	public GarbageCollectionMetaDataBasedEntitiesFinder(final GmMetaModel metaModel, final String useCase,
			final GarbageCollectionKind searchedGarbageCollectionKind) {
		this.metaModel = metaModel;
		this.useCase = useCase;
		this.searchedGarbageCollectionKind = searchedGarbageCollectionKind;
	}

	@Override
	public Set<GenericEntity> findEntities(final PersistenceGmSession session) {
		final Set<String> entityTypes = findEntityTypes(this.metaModel, this.searchedGarbageCollectionKind,
				this.useCase);

		final EntityTypeBasedEntitiesFinder entityTypeBasedEntitiesFinder = new EntityTypeBasedEntitiesFinder();
		entityTypeBasedEntitiesFinder.setEntityTypeSignatures(entityTypes);

		final Set<GenericEntity> result = entityTypeBasedEntitiesFinder.findEntities(session);

		return result;
	}

	public static Set<String> findEntityTypes(final GmMetaModel metaModel,
			final GarbageCollectionKind searchedGarbageCollectionKind, final String useCase) {

		BasicModelOracle modelOracle = new BasicModelOracle(metaModel);
		final CmdResolver cmdResolver = new CmdResolverImpl(modelOracle);

		final Set<String> entityTypeSignatures = new HashSet<>();

		modelOracle.getTypes().onlyEntities().<GmEntityType> asGmTypes().forEach(entityType -> {
			GarbageCollection metaData = cmdResolver.getMetaData().entityType(entityType).useCase(useCase).meta(GarbageCollection.T).exclusive();

			if (metaData != null) {
				final GarbageCollectionKind kind = metaData.getKind();
				if (kind == null) {
					throw new GarbageCollectionException(GarbageCollection.class.getSimpleName()
							+ " metadata not properly specified for entity type " + entityType.getTypeSignature() + ": "
							+ GarbageCollectionKind.class.getSimpleName() + " missing!");
				}

				if (kind.equals(searchedGarbageCollectionKind)) {
					entityTypeSignatures.add(entityType.getTypeSignature());
				}
			}
		});

		return entityTypeSignatures;
	}

}
