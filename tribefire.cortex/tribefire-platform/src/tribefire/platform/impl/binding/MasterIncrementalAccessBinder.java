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
package tribefire.platform.impl.binding;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.collectionType;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.entityType;
import static com.braintribe.model.processing.deployment.utils.StringVariableEvaluator.magicReplaceVariables;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.impl.aop.AopAccess;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.processing.access.service.api.registry.RegistryBasedAccessService;
import com.braintribe.model.processing.aop.api.service.AopIncrementalAccess;
import com.braintribe.model.processing.deployment.api.DeploymentContext;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DirectComponentBinder;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.UndeploymentContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;

import tribefire.platform.api.MasterIncrementalAccess;
import tribefire.platform.impl.resourceaccess.MasterInternalizingPersistenceProcessor;

/**
 * <p>
 * Master cartridge dedicated {@link com.braintribe.model.processing.deployment.api.ComponentBinder ComponentBinder} of
 * {@link com.braintribe.model.accessdeployment.IncrementalAccess} components.
 */
public class MasterIncrementalAccessBinder
		implements DirectComponentBinder<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> {

	private Function<DeploymentContext<? extends com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess>, AopAccess> aopAccessFactory;
	private Function<DeploymentContext<? extends com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess>, IncrementalAccess> simulatedAccessFactory;
	private Supplier<ResourceAccessFactory<? super PersistenceGmSession>> resourceAccessFactorySupplier;
	private RegistryBasedAccessService accessService;

	private String dataFilePathPattern = "{accessId}/data/current.xml";
	private File defaultFolder;

	@Required
	public void setAopAccessFactory(
			Function<DeploymentContext<? extends com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess>, AopAccess> aopAccessFactory) {
		this.aopAccessFactory = aopAccessFactory;
	}

	@Required
	public void setSimulatedAccessFactory(
			Function<DeploymentContext<? extends com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess>, IncrementalAccess> simulatedAccessFactory) {
		this.simulatedAccessFactory = simulatedAccessFactory;
	}

	@Required
	public void setResourceAccessFactorySupplier(Supplier<ResourceAccessFactory<? super PersistenceGmSession>> resourceAccessFactorySupplier) {
		this.resourceAccessFactorySupplier = resourceAccessFactorySupplier;
	}

	@Required
	public void setAccessService(RegistryBasedAccessService accessService) {
		this.accessService = accessService;
	}

	@Configurable
	public void setDataFilePathPattern(String dataFilePathPattern) {
		this.dataFilePathPattern = dataFilePathPattern;
	}

	@Required
	@Configurable
	public void setDefaultFolder(String defaultFolder) {
		this.defaultFolder = new File(defaultFolder);

		if (this.defaultFolder.exists() && !this.defaultFolder.isDirectory()) {
			throw new IllegalArgumentException(defaultFolder + " is a file, not a directory.");
		}
	}

	@Override
	public EntityType<com.braintribe.model.accessdeployment.IncrementalAccess> componentType() {
		return com.braintribe.model.accessdeployment.IncrementalAccess.T;
	}

	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { MasterIncrementalAccess.class };
	}

	@Override
	public IncrementalAccess bind(MutableDeploymentContext<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> context) {
		AopIncrementalAccess actualAccess = resolveActualAccess(context);

		IncrementalAccess internalizing = new MasterInternalizingPersistenceProcessor(actualAccess, resourceAccessFactorySupplier.get());

		accessService.registerAccess(context.getDeployable(), internalizing);

		return internalizing;
	}

	private AopIncrementalAccess resolveActualAccess(
			MutableDeploymentContext<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> context) {

		com.braintribe.model.accessdeployment.IncrementalAccess deployable = context.getDeployable();

		if (deployable instanceof HardwiredAccess)
			return context.getInstanceToBeBound();

		if (deployable.getSimulated())
			context.setInstanceToBeBoundSupplier(() -> simulatedAccessFactory.apply(context));

		GmMetaModel model = getModel(deployable);

		// Pre-fetch model
		if (!context.isDeployableFullyFetched())
			model = prefetchAccessModel(context.getSession(), model);

		ensureGmTypes(model);

		// Ensure data file path
		ensureDataFilePath(deployable);

		GmMetaModel serviceModel = deployable.getServiceModel();
		if (serviceModel != null) {
			if (!context.isDeployableFullyFetched())
				serviceModel = prefetchAccessModel(context.getSession(), serviceModel);
			ensureGmTypes(serviceModel);
		}

		// Wrap as AOP access
		return aopAccessFactory.apply(context);
	}

	private GmMetaModel getModel(com.braintribe.model.accessdeployment.IncrementalAccess deployable) {
		return requireNonNull(deployable.getMetaModel(), () -> "Access does not reference a model: " + deployable);
	}

	@Override
	public void unbind(UndeploymentContext<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> context) {
		accessService.unregisterAccess(context.getDeployable());
	}

	private GmMetaModel prefetchAccessModel(PersistenceGmSession gmSession, GmMetaModel model) {
		try {
			return gmSession.query().entity(model).withTraversingCriterion(modelTc()).refresh();

		} catch (Exception e) {
			throw new DeploymentException("Error while prefetching passed model", e);
		}
	}

	/* Filter all entity/collection properties of everything that is not a GmModelElement */
	private static TraversingCriterion modelTc() {
		// @formatter:off
		return TC.create()
				.pattern()
					.negation()
						.typeCondition(isAssignableTo(GmModelElement.T))
					.conjunction()
						.property()
						.typeCondition(or(isKind(collectionType), isKind(entityType)))
					.close()
				.close()
			.done();
		// @formatter:on
	}

	private void ensureGmTypes(GmMetaModel model) {
		try {
			GMF.getTypeReflection().deploy(model);

		} catch (Exception e) {
			throw new DeploymentException("Failed to ensure gm types of: " + model, e);
		}
	}

	private void ensureDataFilePath(com.braintribe.model.accessdeployment.IncrementalAccess deployable) {
		if (deployable instanceof CollaborativeSmoodAccess) {
			CollaborativeSmoodAccess access = (CollaborativeSmoodAccess) deployable;
			if (access.getStorageDirectory() == null)
				access.setStorageDirectory(resolveDataFile(deployable).getParent());
		}
	}

	private File resolveDataFile(com.braintribe.model.accessdeployment.IncrementalAccess access) {
		try {
			Map<String, String> vars = asMap("accessId", access.getExternalId());
			String relativeDataFilePath = magicReplaceVariables(dataFilePathPattern, vars);

			return new File(defaultFolder, relativeDataFilePath);

		} catch (Exception e) {
			throw new DeploymentException("Error while building path based on pattern: " + dataFilePathPattern, e);
		}
	}

}
