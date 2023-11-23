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
package com.braintribe.model.processing.assembly.sync.impl;

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.firstN;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.VdHolder;
import com.braintribe.model.processing.assembly.sync.api.AssemblyImportContext;
import com.braintribe.model.processing.assembly.sync.api.ImportStatistics;
import com.braintribe.model.processing.assembly.sync.impl.PersistenceIdResolver.PersistenceInfoMaps;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingVisitor;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.api.customize.PropertyTransferExpert;
import com.braintribe.model.processing.traversing.engine.api.skip.Skipper;
import com.braintribe.model.processing.traversing.engine.impl.clone.BasicClonerCustomization;
import com.braintribe.model.processing.traversing.engine.impl.clone.Cloner;
import com.braintribe.model.processing.traversing.engine.impl.clone.MinImpactPropertyTransferExpert;
import com.braintribe.model.processing.traversing.engine.impl.skip.PropertySkippingContext;
import com.braintribe.model.processing.traversing.engine.impl.skip.TransferPropertyRelatedValueSkipper;
import com.braintribe.model.processing.traversing.impl.visitors.ValueTypeOrientedVisitor;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ResourceSpecification;
import com.braintribe.utils.lcd.StringTools;

/**
 * @see #importAssembly(AssemblyImportContext)
 */
public class AssemblyImporter<P> {

	private final AssemblyImportContext<? extends P> context;
	private final PersistenceGmSession contextSession;
	private final P assembly;
	private final ImportStatistics statistics = ImportStatistics.T.create();
	private final Map<EntityType<?>, List<String>> globalIds = newMap();
	private PersistenceInfoMaps maps;

	protected static final Property globalIdP = GenericEntity.T.getProperty(GenericEntity.globalId);
	protected static final Property resourceSourceP = Resource.T.getProperty(Resource.resourceSource);

	/**
	 * Imports data into a {@link PersistenceGmSession}. The input data and the target session, as well as other parameters for the import process,
	 * are all described via given {@link AssemblyImportContext}.
	 * 
	 * @see AssemblyImportContext
	 */
	public static <P> P importAssembly(AssemblyImportContext<? extends P> context) {
		return new AssemblyImporter<P>(context).run();
	}

	private AssemblyImporter(AssemblyImportContext<? extends P> context) {
		this.context = context;
		this.contextSession = context.getSession();
		this.assembly = context.getAssembly();
	}

	private P run() {
		collectGlobalIds();
		loadPersistenceIds();
		return runActualImport();
	}

	private void collectGlobalIds() {
		GMT.traverse().visitor(globalIdCollectingVisitor).doFor(assembly);

		if (!entitiesWithougGlobalId.isEmpty())
			throw new GenericModelException("globalIds required but found " + entitiesWithougGlobalId.size()
					+ " entities without globalId. First few of them are: " + firstN(entitiesWithougGlobalId, 10));
	}

	private final List<GenericEntity> entitiesWithougGlobalId = newList();
	private final GmTraversingVisitor globalIdCollectingVisitor = new ValueTypeOrientedVisitor() {

		@Override
		protected void onEntityEnter(GmTraversingContext traversingContext, GenericEntity entity, TraversingModelPathElement pathElement) {
			if (context.isEnvelope(entity) && !context.includeEnvelope())
				return;

			String globalId = entity.getGlobalId();
			if (!StringTools.isEmpty(globalId))
				acquireList(globalIds, entity.entityType()).add(globalId);

			else if (context.requireAllGlobalIds())
				entitiesWithougGlobalId.add(entity);
		}
	};

	private void loadPersistenceIds() {
		maps = PersistenceIdResolver.resolve(contextSession, globalIds);
	}

	private P runActualImport() {
		PropertyTransferExpert propertyTransferExpert = context.propertyTransferExpert();
		if (propertyTransferExpert == null)
			propertyTransferExpert = minImpactPropertyTransferExpert;

		Cloner cloner = new Cloner();
		cloner.setCustomizer(new ClonerCustomization(propertyTransferExpert));

		GMT.traverse().visitor(globalIdAndIdenfifyingPropertySkipper).visitor(cloner).doFor(assembly);

		if (propertyTransferExpert instanceof MinImpactPropertyTransferExpert)
			// TODO the framework itself should support this event
			((MinImpactPropertyTransferExpert) propertyTransferExpert).onCloningFinished();

		context.notifyImportStatistics(statistics);

		return cloner.getClonedValue();
	}

	private final Skipper globalIdAndIdenfifyingPropertySkipper = new TransferPropertyRelatedValueSkipper() {
		@Override
		protected boolean shouldSkipProperty(PropertySkippingContext context) {
			Property property = context.getProperty();
			return property.isGlobalId() || property.isIdentifying() || property == resourceSourceP;
		}
	};

	private final MinImpactPropertyTransferExpert minImpactPropertyTransferExpert = new MinImpactPropertyTransferExpert();

	private final class ClonerCustomization extends BasicClonerCustomization {

		public ClonerCustomization(PropertyTransferExpert propertyTransferExpert) {
			setPropertyTransferExpert(propertyTransferExpert);
		}

		@Override
		public <T extends GenericEntity> T supplyRawClone(T entity, GmTraversingContext traversingContext, TraversingModelPathElement pathElement,
				EntityType<T> entityType) {

			if (context.isEnvelope(entity))
				if (context.includeEnvelope())
					return newInstance(entity);
				else
					return entityType.create();

			String globalId = entity.getGlobalId();
			if (StringTools.isEmpty(globalId)) {
				return newInstance(entity);
			}

			Object persistenceId = maps.idMap.get(globalId);
			if (persistenceId == null) {
				// TODO optimize - no need to look beyond session cache here
				T result = contextSession.findEntityByGlobalId(globalId);
				if (result != null) {
					/* we might have an entity in the session from a previous run with no commit in the meantime. We do not however know if the
					 * previous run was treating the entity as an external reference, so we sync it here. */
					if (context.isExternalReference(entity))
						traversingContext.skipDescendants(null);

					return result;
				}

				result = newInstance(entity);
				result.setGlobalId(globalId);

				handleResource(globalId, result);
				return result;
			}

			statistics.increaseAffectedExistingEntityCount();

			T result = findLocalOrBuildShallow(entityType, globalId, persistenceId, entity.getPartition());
			if (globalIdNotSet(result))
				setGlobalIdSilently(result, globalId);

			if (context.isExternalReference(entity))
				/* Why null? Well, that is the current skip use-case, and if we set something else, we could influence the current processing, e.g. it
				 * could skip our entity. (I tried using the DefaultSkipUseCase, and the entity disappeared from the assembly) */
				traversingContext.skipDescendants(null);
			else
				handleResource(globalId, result);

			return result;
		}

		private <T extends GenericEntity> T findLocalOrBuildShallow(EntityType<T> entityType, String globalId, Object id, String assemblyPartition) {
			String sessionPartition = maps.partitionMap.get(globalId);

			if (assemblyPartition != null && !assemblyPartition.equals(sessionPartition))
				throw new GenericModelException("Problem with partition for entity with globalId '" + globalId
						+ "'. This entity was found with persistence id '" + id + "' and partition: '" + sessionPartition
						+ "', but the partition value in given assembly is different: " + assemblyPartition);

			return contextSession.query().entity(entityType, id, sessionPartition).findLocalOrBuildShallow();
		}

		private boolean globalIdNotSet(GenericEntity entity) {
			Object globalId = globalIdP.getDirectUnsafe(entity);
			return globalId == null || VdHolder.isVdHolder(globalId);
		}

		private void setGlobalIdSilently(GenericEntity result, String globalId) {
			contextSession.suspendHistory();
			result.setGlobalId(globalId);
			contextSession.resumeHistory();
		}

		private <T extends GenericEntity> T newInstance(T entity) {
			statistics.increaseCreatedEntityCount();
			return contextSession.createRaw(entity.<T> entityType());
		}

		private void handleResource(String globalId, GenericEntity result) {
			if (!(result instanceof Resource))
				return;

			Resource uploadResource = context.findUploadResource(globalId);
			if (uploadResource == null)
				return;

			Resource resource = (Resource) result;

			Resource tmpResource = uploadResource(uploadResource);
			resource.setResourceSource(tmpResource.getResourceSource());
			cleanupResource(tmpResource);
		}

		private void cleanupResource(Resource resource) {
			ResourceSpecification spec = resource.getSpecification();
			if (spec != null)
				contextSession.deleteEntity(spec);

			contextSession.deleteEntity(resource);
		}

		private Resource uploadResource(Resource callResource) {
			return contextSession.resources().create().name("tmp-resource").store(callResource::openStream);
		}

	}

}
