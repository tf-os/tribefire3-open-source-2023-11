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
package tribefire.platform.wire.space.cortex.deployment.deployables.access;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.braintribe.model.access.collaboration.CollaborativeAccessManager;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.persistence.BasicManipulationPersistence;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.csa.CollaborativeSmoodConfiguration;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.cortex.priming.TfEnvCsaPriming;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeManipulationPersistence;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.SystemAccessCommonsSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployableBaseSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public class CollaborativeSmoodAccessSpace extends DeployableBaseSpace {

	@Import
	private MarshallingSpace marshalling;

	@Import
	private ModuleInitializationSpace moduleInitialization;

	@Import
	private SystemAccessCommonsSpace systemAccessCommons;

	@Import
	private CortexAccessSpace cortex;

	@Import
	private ResourceAccessSpace resourceAccess;

	@Import
	private ResourceProcessingSpace resourceProcessing;

	public com.braintribe.model.access.collaboration.CollaborativeSmoodAccess access(ExpertContext<CollaborativeSmoodAccess> context) {
		return rawSmoodAccess(context);
	}

	/** deployables.smood.RawSmoodAccess */
	@Managed
	public com.braintribe.model.access.collaboration.CollaborativeSmoodAccess rawSmoodAccess(ExpertContext<CollaborativeSmoodAccess> context) {
		CollaborativeSmoodAccess deployable = context.getDeployable();
		String externalId = deployable.getExternalId();

		com.braintribe.model.access.collaboration.CollaborativeSmoodAccess bean = systemAccessCommons.newRegularOrDistributedCsa(externalId);
		bean.setReadWriteLock(new ReentrantReadWriteLock());
		bean.setManipulationPersistence(manipulationPersistence(bean, deployable));
		bean.setMetaModel(deployable.getMetaModel());
		bean.setCollaborativeRequestProcessor(collaborativeAccessManager(context));
		bean.setInitializerAttributes(asMap("cortex.session.provider", cortex.sessionProvider()));

		return bean;
	}

	@Managed
	private CollaborativeAccessManager collaborativeAccessManager(ExpertContext<CollaborativeSmoodAccess> context) {
		String externalId = context.getDeployable().getExternalId();

		CollaborativeAccessManager bean = new CollaborativeAccessManager();
		bean.setAccess(rawSmoodAccess(context));
		bean.setCsaStatePersistence(csaStatePersistence(externalId));
		bean.setSourcePathResolver(resourceAccess.accessPathResolver().pathResolverForDomain(externalId));
		bean.setGmmlErrorHandler(systemAccessCommons.gmmlErrorHandler(externalId));
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		return bean;
	}

	private CollaborativeManipulationPersistence manipulationPersistence(CollaborativeAccess access, CollaborativeSmoodAccess deployable) {
		File storageDir = new File(deployable.getStorageDirectory());
		String accessId = deployable.getExternalId();

		BasicManipulationPersistence bean = new BasicManipulationPersistence();
		bean.setStorageBase(storageDir);
		bean.setStaticPostInitializers(TfEnvCsaPriming.getEnvironmentInitializersFor(accessId));
		bean.setStaticInitializers(TfEnvCsaPriming.getEnvironmentInitializersFor(accessId, true));
		bean.setCsaStatePersistence(csaStatePersistence(accessId));
		bean.setGmmlErrorHandler(systemAccessCommons.gmmlErrorHandler(accessId));
		bean.setCustomInitializerResolver(moduleInitialization.moduleBoundInitializerResolverFor(accessId));

		if (TribefireRuntime.getPlatformSetupSupport())
			bean.setAppendedManipulationListener(m -> deployment.platformSetupManager().notifyAppendedManipulation(access, m));

		overwriteConfigurationPersistenceIfConfigured(deployable);

		return bean;
	}

	private void overwriteConfigurationPersistenceIfConfigured(CollaborativeSmoodAccess deployable) {
		// TODO make these static, rather than part of config.json
		CollaborativeSmoodConfiguration csaConfiguration = deployable.getCsaConfiguration();
		if (csaConfiguration != null) {
			CsaStatePersistence statePersistence = csaStatePersistence(deployable.getExternalId());
			statePersistence.overwriteOriginalConfiguration(csaConfiguration);
		}
	}

	private CsaStatePersistence csaStatePersistence(String externalId) {
		return systemAccessCommons.csaStatePersistence(externalId);
	}

}
