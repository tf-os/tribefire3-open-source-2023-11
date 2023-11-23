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
package tribefire.platform.wire.space.cortex;

import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.rootModelName;
import static com.braintribe.model.processing.query.tools.QueryPlanPrinter.print;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cartridge.common.processing.deployment.DeploymentScope;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessapi.ReferencesRequest;
import com.braintribe.model.accessapi.ReferencesResponse;
import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.DeploymentScoping;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.manipulation.marshaller.ManipulationStringifier;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.provider.Holder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.deployment.DcsaSharedStorageLoader;
import tribefire.platform.impl.deployment.WireDeploymentScoping;
import tribefire.platform.impl.session.ProviderBasedStaticModelAccessory;
import tribefire.platform.wire.space.bindings.BindingsSpace;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployablesSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;

/**
 * Space for the so called "PreCortex" components, i.e. ones that are relevant for the deployment of cortex itself (and other hardwired collaborative
 * accesses of course) in the distributed environment. (In a single-node environment there is nothing to deploy dynamically, as the default hardwired
 * CSAs configuration is used.)
 * <p>
 * This space provides classes needed for the {@link DeploymentService} in order to deploy the actual hardwired accesses, and these are based on the
 * configured {@link DcsaSharedStorage}, which is configured via environment variables. See {@link DcsaSharedStorageLoader} for more information.
 * 
 * @see DcsaSharedStorageLoader
 * 
 * @author peter.gazdik
 */
@Managed
public class PreCortexSpace implements WireSpace {

	private static final String PRE_CORTEX_ACCESS_ID = "preCortex";
	private static final String FILENAME_DEFAULT_DCSA_SS = "default-dcsa-shared-storage.yml";

	@Import
	private CartridgeInformationSpace cartridgeInformation;

	@Import
	private MessagingSpace messaging;

	@Import
	private AuthContextSpace authContext;

	@Import
	private DeployablesSpace deployables;

	@Import
	private DeploymentSpace deployment;

	@Import
	private DeploymentScope deploymentScope;

	@Import
	private BindingsSpace bindings;

	@Import
	private EnvironmentSpace environment;

	@Managed
	private DeploymentScoping scoping() {
		WireDeploymentScoping bean = new WireDeploymentScoping();
		bean.setScope(deploymentScope);
		return bean;
	}

	/**
	 * Creates a session backed by a fake, non-deployed {@link IncrementalAccess} called {@value PreCortexSpace#PRE_CORTEX_ACCESS_ID}, which only
	 * exists to satisfy the required dependency of the session. This session gets this {@link #modelAccessory()}.
	 */
	@Managed(Scope.prototype)
	public BasicPersistenceGmSession session() {
		BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setAccessId(PRE_CORTEX_ACCESS_ID);
		session.setModelAccessory(modelAccessory());
		session.setIncrementalAccess(fakePreCortexAccess());

		return session;
	}

	@Managed
	private IncrementalAccess fakePreCortexAccess() {
		return new PotemkinAccess();
	}

	/** PreCortex {@link ModelAccessory} backed by {@link #model()}. */
	@Managed
	public ModelAccessory modelAccessory() {
		ProviderBasedStaticModelAccessory bean = new ProviderBasedStaticModelAccessory();
		bean.setMetaModelProvider(new Holder<>(model()));
		bean.setAccessId(PRE_CORTEX_ACCESS_ID);

		return bean;
	}

	/**
	 * PreCortex model is determined dynamically based on the configured {@link DcsaSharedStorage} (see {@link DcsaSharedStorageLoader}). This model
	 * is simply the minimal model which covers all the instances reachable from said {@linkplain DcsaSharedStorage}. If no shared storage is
	 * configured, this model is an empty extension of the root model.
	 */
	@Managed
	public GmMetaModel model() {
		GmMetaModel bean = MetaModelBuilder.metaModel("tribefire.synthetic:pre-cortex-model");
		bean.getDependencies().addAll(modelDependencies());
		bean.setVersion("1.0");

		return bean;
	}

	private Set<GmMetaModel> modelDependencies() {
		Set<GmMetaModel> result = newLinkedSet();

		dcsaStorageLoader().entities().stream() //
				.map(this::getGmModelOfEntity) //
				.forEach(result::add);

		dcsaStorageLoader().enums().stream() //
				.map(this::getGmModelOfEnum) //
				.forEach(result::add);

		if (result.isEmpty())
			result.add(rootModel().getMetaModel());

		return result;
	}

	private GmMetaModel getGmModelOfEntity(GenericEntity e) {
		return e.entityType().getModel().<GmMetaModel> getMetaModel();
	}

	private GmMetaModel getGmModelOfEnum(Enum<?> e) {
		return GMF.getTypeReflection().getEnumType(e).getModel().<GmMetaModel> getMetaModel();
	}

	private static Model rootModel() {
		return GMF.getTypeReflection().getModel(rootModelName);
	}

	public DcsaSharedStorage dcsaStorage() {
		return dcsaStorageLoader().storage();
	}

	public List<Deployable> dcsaStorageRelatedDeployables() {
		return (List<Deployable>) (List<?>) dcsaStorageLoader().entities().stream() //
				.filter(Deployable.T::isInstance) //
				.collect(Collectors.toList());
	}

	@Managed
	private DcsaSharedStorageLoader dcsaStorageLoader() {
		return new DcsaSharedStorageLoader(defaultDssFile(), legacyDcsaDenotationSupplier());
	}

	private LegacyDcsaDenotationSupplier legacyDcsaDenotationSupplier() {
		LegacyDcsaDenotationSupplier bean = new LegacyDcsaDenotationSupplier();
		bean.setEnvironmentDenotations(environment.environmentDenotations());
		return bean;
	}

	@Managed
	public File defaultDssFile() {
		return new File(TribefireRuntime.getConfigurationDir(), FILENAME_DEFAULT_DCSA_SS);
	}

	private class PotemkinAccess implements IncrementalAccess {

		@Override
		public GmMetaModel getMetaModel() throws GenericModelException {
			return model();
		}

		@Override
		public String getAccessId() {
			return PRE_CORTEX_ACCESS_ID;
		}

		@Override
		public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
			throw new UnsupportedOperationException(
					"This is a fake access just to satisfy the required properties, no querying expected. SelectQuery: " + print(query));
		}

		@Override
		public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
			throw new UnsupportedOperationException(
					"This is a fake access just to satisfy the required properties, no querying expected. EntityQuery: " + print(query));
		}

		@Override
		public PropertyQueryResult queryProperty(PropertyQuery query) throws ModelAccessException {
			throw new UnsupportedOperationException(
					"This is a fake access just to satisfy the required properties, no querying expected. PropertyQuery: " + print(query));
		}

		@Override
		public ManipulationResponse applyManipulation(ManipulationRequest mr) throws ModelAccessException {
			throw new UnsupportedOperationException(
					"This is a fake access just to satisfy the required properties, no manipulations expected. Manipulation: "
							+ ManipulationStringifier.stringify(mr.getManipulation(), true));
		}

		@Override
		public ReferencesResponse getReferences(ReferencesRequest referencesRequest) throws ModelAccessException {
			throw new UnsupportedOperationException(
					"This is a fake access just to satisfy the required properties, no reference resolving expected.");
		}

		@Override
		public Set<String> getPartitions() throws ModelAccessException {
			return asSet(getAccessId());
		}

	}

}
