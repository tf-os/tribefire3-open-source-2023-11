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

import static com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry.DOMAIN_BASIC;
import static com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry.DOMAIN_ESSENTIAL;
import static com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry.PERSPECTIVE_PERSISTENCE_SESSION;
import static com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry.testMdDeclaredInModel;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.gm._BasicMetaModel_;
import com.braintribe.model.accessory.ModelRetrievingRequest;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.processing.accessory.impl.BasicModelAccessorySupplier;
import com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry;
import com.braintribe.model.processing.accessory.impl.PlatformModelAccessoryFactory;
import com.braintribe.model.processing.accessory.impl.PmeSupplierFromCortex;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.securityservice.commons.provider.SessionAuthorizationFromUserSessionProvider;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.SessionFactoryBasedSessionProvider;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.impl.BasicPersistenceGmSessionFactory;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.module.loading.PlatformModelApiSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.module.TribefireWebPlatformSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public class GmSessionsSpace implements WireSpace {

	@Import
	private AccessServiceSpace accessService;

	@Import
	private AuthContextSpace authContext;

	@Import
	private ResourceAccessSpace resourceAccess;

	@Import
	private CortexAccessSpace cortexAccess;

	@Import
	private RpcSpace rpc;

	@Import
	private TribefireWebPlatformSpace tribefireWebPlatform;

	@Managed
	public Supplier<PersistenceGmSession> sessionSupplier(String accessId) {
		SessionFactoryBasedSessionProvider bean = new SessionFactoryBasedSessionProvider();
		bean.setAccessId(accessId);
		bean.setPersistenceGmSessionFactory(sessionFactory());
		return bean;
	}

	@Managed
	public Supplier<PersistenceGmSession> systemSessionSupplier(String accessId) {
		SessionFactoryBasedSessionProvider bean = new SessionFactoryBasedSessionProvider();
		bean.setAccessId(accessId);
		bean.setPersistenceGmSessionFactory(systemSessionFactory());
		return bean;
	}

	@Managed
	public BasicModelAccessorySupplier modelAccessorySupplier() {
		BasicModelAccessorySupplier bean = new BasicModelAccessorySupplier();
		bean.setCacheModelAccessories(true);
		bean.setCmdInitializer(standardCmdInitializer());
		bean.setCortexSessionSupplier(cortexAccess::lowLevelSession);
		return bean;
	}

	@Managed
	public PlatformModelAccessoryFactory userModelAccessoryFactory() {
		PlatformModelAccessoryFactory bean = new PlatformModelAccessoryFactory();
		bean.setModelEssentialsSupplier(modelEssentialsSupplier());
		bean.setCortexSessionSupplier(cortexAccess::lowLevelSession);
		bean.setAccessSessionProvider(sessionAuthorizationProvider());
		bean.setServiceSessionProvider(authContext.currentUser().userSessionSupplier());
		bean.setUserRolesProvider(authContext.currentUser().rolesProvider());
		bean.setCmdInitializer(standardCmdInitializer());
		return bean;
	}

	@Managed
	public PlatformModelAccessoryFactory systemModelAccessoryFactory() {
		PlatformModelAccessoryFactory bean = new PlatformModelAccessoryFactory();
		bean.setModelEssentialsSupplier(modelEssentialsSupplier());
		bean.setCortexSessionSupplier(cortexAccess::lowLevelSession);
		bean.setAccessSessionProvider(systemSessionAuthorizationProvider());
		bean.setServiceSessionProvider(authContext.internalUser().userSessionProvider());
		bean.setUserRolesProvider(authContext.internalUser().rolesProvider());
		bean.setCmdInitializer(standardCmdInitializer());

		return bean;
	}

	/**
	 * Brings CMD-related experts from modules to the core model accessories.
	 * 
	 * @see PlatformModelApiSpace#initializeCmdResolver(CmdResolverBuilder)
	 */
	private Consumer<CmdResolverBuilder> standardCmdInitializer() {
		return tribefireWebPlatform.modelApi()::initializeCmdResolver;
	}

	public ServiceProcessor<ModelRetrievingRequest, GmMetaModel> modelRetrievingProcessor() {
		return modelEssentialsSupplier();
	}

	@Managed
	public PmeSupplierFromCortex modelEssentialsSupplier() {
		PmeSupplierFromCortex bean = new PmeSupplierFromCortex();
		bean.setCortexSupplier(cortexAccess::access);
		bean.setMdPerspectiveRegistry(mdPerspectiveRegistry());

		return bean;
	}

	@Managed
	public MdPerspectiveRegistry mdPerspectiveRegistry() {
		MdPerspectiveRegistry bean = new MdPerspectiveRegistry();
		bean.extendMdDomain(DOMAIN_ESSENTIAL, testMdDeclaredInModel(Mandatory.T.getModel()));
		bean.extendMdDomain(DOMAIN_BASIC, testMdDeclaredInModel(GMF.getTypeReflection().getModel(_BasicMetaModel_.reflection.name())));		
		bean.extendModelPerspective(PERSPECTIVE_PERSISTENCE_SESSION, DOMAIN_ESSENTIAL, DOMAIN_BASIC);

		return bean;
	}

	@Managed
	public BasicPersistenceGmSessionFactory sessionFactory() {
		BasicPersistenceGmSessionFactory bean = new BasicPersistenceGmSessionFactory();
		bean.setSessionAuthorizationProvider(sessionAuthorizationProvider());
		bean.setAccessService(accessService.service());
		bean.setResourceAccessFactory(resourceAccess.dynamicResourceAccessFactory());
		bean.setModelAccessoryFactory(userModelAccessoryFactory());
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		return bean;
	}

	@Managed
	public BasicPersistenceGmSessionFactory systemSessionFactory() {
		BasicPersistenceGmSessionFactory bean = new BasicPersistenceGmSessionFactory();
		bean.setSessionAuthorizationProvider(systemSessionAuthorizationProvider());
		bean.setAccessService(accessService.internalService());
		bean.setResourceAccessFactory(resourceAccess.dynamicResourceAccessFactory());
		bean.setModelAccessoryFactory(systemModelAccessoryFactory());
		bean.setRequestEvaluator(rpc.systemServiceRequestEvaluator());
		return bean;
	}

	@Managed
	public Supplier<SessionAuthorization> sessionAuthorizationProvider() {
		SessionAuthorizationFromUserSessionProvider bean = new SessionAuthorizationFromUserSessionProvider();
		bean.setUserSessionProvider(authContext.currentUser().userSessionSupplier());

		// GSC: remove default session authorizations. no authorizations should be returned if there's no or invalid sessions in context.
		// bean.setDefaultSessionAuthorizationProvider(systemSessionAuthorizationProvider());
		return bean;
	}

	@Managed
	public Supplier<SessionAuthorization> systemSessionAuthorizationProvider() {
		SessionAuthorizationFromUserSessionProvider bean = new SessionAuthorizationFromUserSessionProvider();
		bean.setUserSessionProvider(authContext.internalUser().userSessionProvider());
		return bean;
	}

}
