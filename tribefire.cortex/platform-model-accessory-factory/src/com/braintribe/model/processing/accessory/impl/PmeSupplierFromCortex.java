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
package com.braintribe.model.processing.accessory.impl;

import static com.braintribe.model.processing.accessory.impl.ModelAccessoryHelper.accessTc;
import static com.braintribe.model.processing.accessory.impl.ModelAccessoryHelper.queryCortexModelname;
import static com.braintribe.model.processing.accessory.impl.ModelAccessoryHelper.serviceDomainTc;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.StringTools.isEmpty;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessory.GetAccessModel;
import com.braintribe.model.accessory.GetModelByName;
import com.braintribe.model.accessory.GetServiceModel;
import com.braintribe.model.accessory.ModelRetrievingRequest;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.components.AccessModelExtension;
import com.braintribe.model.meta.data.components.ModelExtension;
import com.braintribe.model.meta.data.components.ServiceModelExtension;
import com.braintribe.model.processing.accessory.api.PlatformModelEssentials;
import com.braintribe.model.processing.accessory.api.PlatformModelEssentialsSupplier;
import com.braintribe.model.processing.accessory.api.PmeKey;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelChangeListener;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;

/**
 * {@link PlatformModelEssentialsSupplier} which takes the actual models directly from the configured {@link #setCortexSupplier(Supplier) cortex
 * access}.
 * 
 * @author peter.gazdik
 */
public class PmeSupplierFromCortex extends PmeSupplierBase implements ServiceProcessor<ModelRetrievingRequest, GmMetaModel>, ModelChangeListener {

	private Supplier<CollaborativeAccess> cortexSupplier;
	private MdPerspectiveRegistry mdPerspectiveRegistry;
	private ModelAccessory munchhausenSafeModelAccessory;

	private final Map<PmeKey, PlatformModelEssentials> pmeCache = new ConcurrentHashMap<>();
	private final MultiMap<String, PlatformModelEssentials> pmesByModelName = new HashMultiMap<>();

	private static final Logger log = Logger.getLogger(PmeSupplierFromCortex.class);

	@Required
	public void setCortexSupplier(Supplier<CollaborativeAccess> cortexSupplier) {
		this.cortexSupplier = cortexSupplier;
		this.munchhausenSafeModelAccessory = newMunchhausenSafeModelAccessory(cortexSupplier);
	}

	@Required
	public void setMdPerspectiveRegistry(MdPerspectiveRegistry mdPerspectiveRegistry) {
		this.mdPerspectiveRegistry = mdPerspectiveRegistry;
	}

	/**
	 * Resolving {@link PlatformModelEssentials} for an access or a service domain requires MD resolution, i.e. it requires some other
	 * {@link ModelAccessory}. This MA, which we call here "munchhausenSafeModelAccessory" has be such that it doesn't require another MA. We achieve
	 * this by resolving the name of the cortex' data model and the resolve a standard {@link PlatformModelEssentials} for this model name.
	 * <p>
	 * The model name is retrieved via a query on the cortex access directly.
	 */
	private ModelAccessory newMunchhausenSafeModelAccessory(Supplier<CollaborativeAccess> cortexSupplier) {
		PlatformModelAccessory result = new PlatformModelAccessory();
		result.setModelEssentialsSupplier(() -> getModelEssentials(cortexSupplier));
		result.setDescription("muchhausen-safe-cortex");

		return result;
	}

	private PlatformModelEssentials getModelEssentials(Supplier<CollaborativeAccess> cortexSupplier) {
		String cortexDataModelName = queryCortexModelname(cortexSupplier.get());
		String perspective = null;

		return getForModelName(cortexDataModelName, perspective);
	}

	// #####################################################
	// ## . . . . . . ServiceProcessor . . . . . . . . . .##
	// #####################################################

	@Override
	public GmMetaModel process(ServiceRequestContext requestContext, ModelRetrievingRequest request) {
		requireNonNull(request, "request cannot be null");

		if (request instanceof GetAccessModel) {
			GetAccessModel r = (GetAccessModel) request;
			return getForAccess(r.getExternalId(), r.getPerspective(), r.getExtended()).getModel();
		}

		if (request instanceof GetServiceModel) {
			GetServiceModel r = (GetServiceModel) request;
			return getForServiceDomain(r.getExternalId(), r.getPerspective(), r.getExtended()).getModel();
		}

		if (request instanceof GetModelByName) {
			GetModelByName r = (GetModelByName) request;
			return getForModelName(r.getName(), r.getPerspective()).getModel();
		}

		throw new UnsupportedOperationException("Unknown ModelRetrievingRequest " + request + " of type " + request.entityType().getTypeSignature());
	}

	// #####################################################
	// ## . . . . . . . . . Access . . . . . . . . . . . .##
	// #####################################################

	@Override
	public PlatformModelEssentials getForAccess(String accessId, String perspective, boolean extended) {
		/* Accessing the CMD resolver might trigger cortex deployment, which then, in it's post construct, accesses the MA for cortex access.
		 * Therefore we want to trigger this first, rather than have it triggered in the middle of PME creation for some access. This is especially
		 * dangerous if the first PME ever requested was for cortex access, as that would mean re-entering the super-implementation with the same
		 * parameter and thus ending up getting stuck in the ConcurrentHashmap.computeIfAbsent() */
		ensureCmdResolver();

		return super.getForAccess(accessId, perspective, extended);
	}

	@Override
	protected PlatformModelEssentials createNewAccessPme(String accessId, String perspective, boolean extended) {
		return getComponentModelEssentials(session -> readAccesModelNameAndExtensions(session, accessId, perspective, extended));
	}

	private PmeData readAccesModelNameAndExtensions(ManagedGmSession cortexSession, String accessId, String perspective, boolean extended) {
		IncrementalAccess access = resolveByExternalId(cortexSession, IncrementalAccess.T, accessId, accessTc());
		if (access == null)
			throw new IllegalArgumentException("No access found with externalId: " + accessId);

		GmMetaModel dataModel = access.getMetaModel();
		if (dataModel == null)
			throw new IllegalArgumentException("No model configured on access with externalId: " + accessId);

		List<AccessModelExtension> extensions = resolveExtensions(access, AccessModelExtension.T, extended);

		// we have to query the model again anyway, this time detached and fully loaded
		return new PmeData(dataModel.getName(), perspective, extensions, access);
	}

	// #####################################################
	// ## . . . . . . . . . Service . . . . . . . . . . . ##
	// #####################################################

	@Override
	protected PlatformModelEssentials createNewServiceDomainPme(String serviceDomainId, String perspective, boolean extended) {
		return getComponentModelEssentials(session -> readSdModelNameAndExtensions(session, serviceDomainId, perspective, extended));
	}

	private PmeData readSdModelNameAndExtensions(ManagedGmSession cortexSession, String serviceDomainId, String perspective, boolean extended) {
		ServiceDomain serviceDomain = resolveByExternalId(cortexSession, ServiceDomain.T, serviceDomainId, serviceDomainTc());
		if (serviceDomain == null)
			throw new IllegalArgumentException("No domain found with externalId: " + serviceDomainId);

		List<ServiceModelExtension> extensions = resolveExtensions(serviceDomain, ServiceModelExtension.T, extended);

		GmMetaModel serviceModel = serviceDomain.getServiceModel();
		if (serviceModel == null)
			serviceModel = ServiceRequest.T.getModel().getMetaModel();

		// we have to query the model again anyway, this time detached and fully loaded
		// we do not pass serviceDomain, as that information is not needed and we want to prevent creation of unnecessary instances
		return new PmeData(serviceModel.getName(), perspective, extensions, null);
	}

	private PlatformModelEssentials getComponentModelEssentials(Function<ManagedGmSession, PmeData> pmeReadingFunction) {
		PmeData pmeData = cortexSupplier.get().readWithCsaSession(pmeReadingFunction);

		return getModelEssentials(pmeData.modelName, pmeData.perspective, pmeData.ownerType, pmeData.extensions);
	}

	/** This is a result of reading the model and extension from cortex based on accessId or serviceDomainId. */
	private static class PmeData {
		public final String modelName;
		public final String perspective;
		public final List<? extends ModelExtension> extensions;
		public final String ownerType;

		public PmeData(String modelName, String perspective, List<? extends ModelExtension> extensions, HasExternalId owner) {
			this.modelName = modelName;
			this.perspective = perspective;
			this.extensions = extensions;
			this.ownerType = owner == null ? null : owner.entityType().getTypeSignature();
		}
	}

	// #####################################################
	// ## . . . . . . . . . Just Model . . . . . . . . . .##
	// #####################################################

	@Override
	public PlatformModelEssentials getForModelName(String modelName, String perspective) {
		return getModelEssentials(modelName, perspective, null, emptyList());
	}

	private PlatformModelEssentials getModelEssentials(String modelName, String perspective, String ownerType,
			List<? extends ModelExtension> extensions) {

		PmeKey key = PmeKey.create(modelName, perspective, ownerType, extensions);

		if (ownerType != null)
			return getOwnerAwareModelEssentials(modelName, perspective, ownerType, extensions, key);
		else
			return pmeCache.computeIfAbsent(key, k -> newPme(key, extensions));
	}

	private PlatformModelEssentials getOwnerAwareModelEssentials( //
			String modelName, String perspective, String ownerType, List<? extends ModelExtension> extensions, PmeKey key) {

		PlatformModelEssentials result = pmeCache.get(key);
		if (result != null)
			return result;

		PlatformModelEssentials purePme = getModelEssentials(modelName, perspective, null, extensions);

		return pmeCache.computeIfAbsent(key, k -> newOwnerPme(modelName, ownerType, purePme, key));
	}

	private PlatformModelEssentials newOwnerPme(String modelName, String ownerType, PlatformModelEssentials purePme, PmeKey key) {
		OwnerAwarePme result = new OwnerAwarePme(purePme, ownerType, key);
		onNewPme(modelName, result);

		return result;
	}

	private PlatformModelEssentials newPme(PmeKey key, List<? extends ModelExtension> extensions) {
		logNewPme(key);

		GmMetaModel model = queryModel(key.modelName);

		BasicPme result = new ExtendedPmeBuilder(model, extensions, key).build();
		onNewPme(key.modelName, result);

		return result;
	}

	private GmMetaModel queryModel(String modelName) {
		return ModelAccessoryHelper.queryModelForMa((com.braintribe.model.access.IncrementalAccess) cortexSupplier.get(), modelName);
	}

	/* package */ class ExtendedPmeBuilder extends BasicPmeBuilder {

		private final List<? extends ModelExtension> extensions;

		public ExtendedPmeBuilder(GmMetaModel model, List<? extends ModelExtension> extensions, PmeKey key) {
			super(model, key);

			this.extensions = extensions;
		}

		@Override
		protected GmMetaModel prepareModel() {
			GmMetaModel mergedModel = prepareModelWithPossibleExtensions();

			if (key.perspective != null)
				applyPerspective(mergedModel);

			return mergedModel;
		}

		private GmMetaModel prepareModelWithPossibleExtensions() {
			GmMetaModel mergedModel = merge(model);
			if (extensions.isEmpty())
				return mergedModel;
			else
				return new ExtendedModelResolver(mergedModel).result;
		}

		private void applyPerspective(GmMetaModel result) {
			new MdPerspectiveApplier(result, key.perspective).run();
		}

		private class ExtendedModelResolver {

			private final GmMetaModel mergedModel;
			private Set<String> skeletonModelNames;

			public final GmMetaModel result;

			public ExtendedModelResolver(GmMetaModel mergedModel) {
				this.mergedModel = mergedModel;
				this.result = createExtendedModel(mergedModel);

				extendModel();
			}

			private GmMetaModel createExtendedModel(GmMetaModel model) {
				String modelName = "enriched::" + model.getName();

				GmMetaModel result = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
				result.setName(modelName);
				result.setVersion(model.getVersion());

				return result;
			}

			private GmMetaModel extendModel() {
				List<GmMetaModel> deps = result.getDependencies();
				deps.add(mergedModel);

				for (ModelExtension me : extensions)
					for (GmMetaModel modelToAdd : me.getModels())
						if (canUseExtension(me, modelToAdd))
							deps.add(merge(modelToAdd));

				return result;
			}

			private boolean canUseExtension(ModelExtension me, GmMetaModel modelToAdd) {
				if (me.allowTypeExtension())
					return true;
				else
					return modelContainsAllTypesOf(modelToAdd);
			}

			private boolean modelContainsAllTypesOf(GmMetaModel modelToAdd) {
				return skeletonModelNames().containsAll(skeletonModelNamesOf(modelToAdd));
			}

			private Set<String> skeletonModelNames() {
				if (skeletonModelNames == null)
					skeletonModelNames = skeletonModelNamesOf(mergedModel);
				return skeletonModelNames;
			}

			private Set<String> skeletonModelNamesOf(GmMetaModel modelToAdd) {
				return traverseAndCollect(Collections.singleton(modelToAdd), GmMetaModel::getDependencies).stream() //
						.filter(this::isSkeletonModel) //
						.map(GmMetaModel::getName) //
						.collect(Collectors.toSet());
			}

			private boolean isSkeletonModel(GmMetaModel m) {
				return !isEmpty(m.getTypes());
			}

			private <N> Set<N> traverseAndCollect(Iterable<? extends N> nodes, Function<N, ? extends Iterable<? extends N>> neighborFunction) {
				Set<N> visited = newSet();

				visit(nodes, neighborFunction, visited);

				return visited;
			}

			private <N> void visit(Iterable<? extends N> nodes, Function<N, ? extends Iterable<? extends N>> neighborFunction, Set<N> visited) {
				for (N n : nodes) {
					if (!visited.add(n))
						continue;

					Iterable<? extends N> neighbors = neighborFunction.apply(n);
					if (neighbors != null)
						visit(neighbors, neighborFunction, visited);
					continue;
				}
			}

		}
	}

	private class MdPerspectiveApplier {

		private final GmMetaModel model;
		private final String perspective;

		private final Set<MetaData> mdToDeleteFromSession = newSet();

		public MdPerspectiveApplier(GmMetaModel model, String perspective) {
			this.model = model;
			this.perspective = perspective;
		}

		public void run() {
			removeAllIrrelevantMd();
			checkWhichMdAreStillSomehowReferenced();
			deleteNonReferencedMd();
		}

		private TraversingContext removeAllIrrelevantMd() {
			return GmMetaModel.T.traverse(model, this::matches, null);
		}

		private boolean matches(TraversingContext tc) {
			CriterionType criterionType = tc.getCurrentCriterionType();
			if (criterionType == CriterionType.ENTITY) {
				// Do not traverse into non-model elements
				Object entity = tc.getObjectStack().peek();
				return !(entity instanceof GmModelElement);
			}

			if (criterionType != CriterionType.PROPERTY)
				return false;

			PropertyCriterion pc = (PropertyCriterion) tc.getTraversingStack().peek();
			if (!pc.getTypeSignature().endsWith("MetaData>"))
				// traverse properties that are not meta-data
				return false;

			Collection<MetaData> mds = (Collection<MetaData>) tc.getObjectStack().peek();
			if (isEmpty(mds))
				// skip empty MD
				return true;

			Iterator<MetaData> it = mds.iterator();
			while (it.hasNext())
				if (remove(it.next()))
					it.remove();

			return true;
		}

		private boolean remove(MetaData md) {
			if (mdPerspectiveRegistry.perspectiveContainsMd(perspective, md))
				return false;

			mdToDeleteFromSession.add(md);
			return true;
		}

		private TraversingContext checkWhichMdAreStillSomehowReferenced() {
			return GmMetaModel.T.traverse(model, null, this::ifMdMarkNotToBeRemoved);
		}

		private void ifMdMarkNotToBeRemoved(TraversingContext tc) {
			CriterionType criterionType = tc.getCurrentCriterionType();
			if (criterionType != CriterionType.ENTITY)
				return;

			Object entity = tc.getObjectStack().peek();
			if (entity instanceof MetaData)
				mdToDeleteFromSession.remove(entity);
		}

		private void deleteNonReferencedMd() {
			BasicManagedGmSession session = (BasicManagedGmSession) model.session();
			Smood smood = session.getBackup();

			for (MetaData removedMd : mdToDeleteFromSession)
				smood.deleteEntity(removedMd, DeleteMode.ignoreReferences);
		}

	}

	private void logNewPme(PmeKey key) {
		StringBuilder sb = new StringBuilder();
		sb.append("[TMP] Creating new PME, cache size so far: " + pmeCache.size());
		sb.append(". New and cached keys:\n\t");
		sb.append(formatPmeKey(key));
		for (PmeKey cachedKey : pmeCache.keySet()) {
			sb.append("\n\t");
			sb.append(formatPmeKey(cachedKey));
		}

		log.trace(sb.toString());
	}

	private String formatPmeKey(PmeKey key) {
		return "M: " + key.modelName + ", P: " + key.perspective + ", O: " + key.ownerType + ", D: " + key.depModelNames;
	}

	// ###########################################
	// ## . . . . . ModelChangeListener . . . . ##
	// ###########################################

	@Override
	public void onModelChange(String modelName) {
		log.debug(() -> "Received onModelChange notification for " + modelName);

		if (isEmpty(modelName))
			return;

		Collection<PlatformModelEssentials> pmes = removeCachedPmes(modelName);
		for (PlatformModelEssentials pme : pmes)
			if (pme instanceof BasicPme)
				pme.outdated();
	}

	/**
	 * The model name might differ from pme's model as that model might be enriched. But we want to index the PME by the original model, as we need
	 * that when handling notifications on model changes.
	 */
	private void onNewPme(String modelName, PlatformModelEssentials pme) {
		synchronized (pmesByModelName) {
			pmesByModelName.put(modelName, pme);
		}
	}

	private Collection<PlatformModelEssentials> removeCachedPmes(String modelName) {
		synchronized (pmesByModelName) {
			Collection<PlatformModelEssentials> pmes = pmesByModelName.removeAll2(modelName);
			for (PlatformModelEssentials pme : pmes)
				pmeCache.remove(pme.key());

			return pmes;
		}
	}

	// ###########################################
	// ## . . . . . . . . Helpers . . . . . . . ##
	// ###########################################

	private static <E extends HasExternalId> E resolveByExternalId(ManagedGmSession cortexSession, EntityType<E> componentType, String externalId,
			TraversingCriterion tc) {

		return ModelAccessoryHelper.queryComponentForMa(cortexSession, componentType, externalId, tc);
	}

	private <M extends ModelExtension> List<M> resolveExtensions(GenericEntity component, EntityType<M> mdType, boolean really) {
		return really ? theCmdResolver().getMetaData().entity(component).meta(mdType).list() : emptyList();
	}

	private void ensureCmdResolver() {
		theCmdResolver();
	}

	private CmdResolver theCmdResolver() {
		return munchhausenSafeModelAccessory.getCmdResolver();
	}

}
