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
package tribefire.platform.wire.space.common;

import javax.sql.DataSource;

import com.braintribe.cartridge.common.processing.deployment.ReflectBeansForDeployment;
import com.braintribe.model.accessdeployment.CollaborativeAccess;
import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionInfoProvider;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.extensiondeployment.StateChangeProcessorRule;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.leadershipdeployment.LeadershipManager;
import com.braintribe.model.lockingdeployment.LockManager;
import com.braintribe.model.lockingdeployment.Locking;
import com.braintribe.model.marshallerdeployment.Marshaller;
import com.braintribe.model.messagingdeployment.Messaging;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.PlainComponentBinder;
import com.braintribe.model.processing.idgenerator.api.IdGenerator;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.model.deployment.mimetypedetection.MimeTypeDetector;
import tribefire.cortex.model.deployment.usersession.service.UserSessionService;
import tribefire.module.wire.contract.WebPlatformBindersContract;
import tribefire.platform.api.binding.ComponentBinders;
import tribefire.platform.impl.binding.AbstractSessionFactoryBasedBinder;
import tribefire.platform.impl.binding.AccessRequestProcessorBinder;
import tribefire.platform.impl.binding.BinaryPersistenceProcessorBinder;
import tribefire.platform.impl.binding.BinaryRetrievalProcessorBinder;
import tribefire.platform.impl.binding.CheckProcessorBinder;
import tribefire.platform.impl.binding.MarshallerBinder;
import tribefire.platform.impl.binding.MasterIncrementalAccessBinder;
import tribefire.platform.impl.binding.MessagingBinder;
import tribefire.platform.impl.binding.ParameterizedAccessCheckProcessorBinder;
import tribefire.platform.impl.binding.ParameterizedCheckProcessorBinder;
import tribefire.platform.impl.binding.ResourceEnricherProcessorBinder;
import tribefire.platform.impl.binding.ServiceAroundProcessorBinder;
import tribefire.platform.impl.binding.ServicePostProcessorBinder;
import tribefire.platform.impl.binding.ServicePreProcessorBinder;
import tribefire.platform.impl.binding.ServiceProcessorBinder;
import tribefire.platform.impl.binding.WebTerminalBinder;
import tribefire.platform.impl.binding.WorkerBinder;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.access.IncrementalAccessListenerSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;
import tribefire.platform.wire.space.security.servlets.SecurityServletSpace;
import tribefire.platform.wire.space.system.servlets.SystemServletsSpace;

@Managed
public class BindersSpace implements WebPlatformBindersContract, ReflectBeansForDeployment, ComponentBinders  {

	// @formatter:off
	@Import	private GmSessionsSpace gmSessions;
	@Import	private DeploymentSpace deployment;
	@Import private IncrementalAccessListenerSpace incrementalAccess;
	@Import	private MarshallingSpace marshalling;
	@Import	private SecurityServletSpace securityServlets;
	@Import	private SystemServletsSpace systemServlets;
	@Import	private WorkerSpace worker;
	// @formatter:on

	@Override
	public MasterIncrementalAccessBinder incrementalAccess() {
		return incrementalAccess.incrementalAccessBinder();
	}

	@Override
	@Managed
	public ComponentBinder<com.braintribe.model.extensiondeployment.AccessAspect, AccessAspect> accessAspect() {
		return binder(com.braintribe.model.extensiondeployment.AccessAspect.T, AccessAspect.class);
	}

	@Managed
	public ComponentBinder<com.braintribe.model.extensiondeployment.IdGenerator, IdGenerator<?>> idGenerator() {
		return binder(com.braintribe.model.extensiondeployment.IdGenerator.T, IdGenerator.class);
	}

	@Override
	public ServiceProcessorBinder serviceProcessor() {
		return ServiceProcessorBinder.INSTANCE;
	}

	@Override
	public ServicePreProcessorBinder servicePreProcessor() {
		return ServicePreProcessorBinder.INSTANCE;
	}

	@Override
	public ServiceAroundProcessorBinder serviceAroundProcessor() {
		return ServiceAroundProcessorBinder.INSTANCE;
	}

	@Override
	public ServicePostProcessorBinder servicePostProcessor() {
		return ServicePostProcessorBinder.INSTANCE;
	}

	@Override
	@Managed
	public ComponentBinder<Marshaller, com.braintribe.codec.marshaller.api.Marshaller> marshaller() {
		MarshallerBinder bean = new MarshallerBinder();
		bean.setDeployRegistry(deployment.registry());
		bean.setMarshallerRegistry(marshalling.registry());

		return bean;
	}

	@Override
	@Managed
	public ComponentBinder<Messaging, MessagingConnectionProvider<?>> messaging() {
		return new MessagingBinder();
	}

	@Override
	@Managed
	public ComponentBinder<MimeTypeDetector, com.braintribe.mimetype.MimeTypeDetector> mimeTypeDetector() {
		return binder(MimeTypeDetector.T, com.braintribe.mimetype.MimeTypeDetector.class);
	}

	@Override
	@Managed
	@Deprecated
	public ComponentBinder<LockManager, com.braintribe.model.processing.lock.api.LockManager> lockingManager() {
		return binder(LockManager.T, com.braintribe.model.processing.lock.api.LockManager.class);
	}

	@Override
	@Managed
	public ComponentBinder<Locking, com.braintribe.model.processing.lock.api.Locking> locking() {
		return binder(Locking.T, com.braintribe.model.processing.lock.api.Locking.class);
	}

	@Override
	@Managed
	public ComponentBinder<LeadershipManager, tribefire.cortex.leadership.api.LeadershipManager> leadershipManager() {
		return binder(LeadershipManager.T, tribefire.cortex.leadership.api.LeadershipManager.class);
	}

	@Override
	@Managed
	public ComponentBinder<DcsaSharedStorage, com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage> dcsaSharedStorage() {
		return binder(DcsaSharedStorage.T, com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage.class);
	}

	@Override
	public ComponentBinder<UserSessionService, com.braintribe.model.processing.securityservice.api.UserSessionService> userSessionService() {
		return binder(UserSessionService.T, com.braintribe.model.processing.securityservice.api.UserSessionService.class);
	}

	@Override
	@Managed
	public WorkerBinder worker() {
		WorkerBinder bean = new WorkerBinder();
		bean.setWorkerManager(worker.manager());

		return bean;
	}

	@Override
	@Managed
	public AccessRequestProcessorBinder accessRequestProcessor() {
		AccessRequestProcessorBinder bean = new AccessRequestProcessorBinder();
		config(bean);
		return bean;
	}

	@Override
	@Managed
	public ComponentBinder<com.braintribe.model.extensiondeployment.StateChangeProcessor, StateChangeProcessor<?, ?>> stateChangeProcessor() {
		return binder(com.braintribe.model.extensiondeployment.StateChangeProcessor.T, StateChangeProcessor.class);
	}

	@Override
	@Managed
	public ComponentBinder<StateChangeProcessorRule, com.braintribe.model.processing.sp.api.StateChangeProcessorRule> stateChangeProcessorRule() {
		return binder(StateChangeProcessorRule.T, com.braintribe.model.processing.sp.api.StateChangeProcessorRule.class);
	}

	@Override
	public CheckProcessorBinder checkProcessor() {
		return CheckProcessorBinder.INSTANCE;
	}

	@Override
	@Managed
	public ParameterizedCheckProcessorBinder parameterizedCheckProcessor() {
		ParameterizedCheckProcessorBinder bean = new ParameterizedCheckProcessorBinder();
		config(bean);
		return bean;
	}

	@Override
	@Managed
	public ParameterizedAccessCheckProcessorBinder parameterizedAccessCheckProcessor() {
		ParameterizedAccessCheckProcessorBinder bean = new ParameterizedAccessCheckProcessorBinder();
		config(bean);
		return bean;
	}

	private void config(AbstractSessionFactoryBasedBinder bean) {
		bean.setSystemSessionFactory(gmSessions.systemSessionFactory());
		bean.setRequestSessionFactory(gmSessions.sessionFactory());
	}

	@Override
	@Managed
	public BinaryPersistenceProcessorBinder binaryPersistenceProcessor() {
		return new BinaryPersistenceProcessorBinder();
	}

	@Override
	@Managed
	public BinaryRetrievalProcessorBinder binaryRetrievalProcessor() {
		return new BinaryRetrievalProcessorBinder();
	}

	@Override
	@Managed
	public ResourceEnricherProcessorBinder resourceEnricherProcessor() {
		return new ResourceEnricherProcessorBinder();
	}

	@Override
	@Managed
	public ComponentBinder<DatabaseConnectionPool, DataSource> databaseConnectionPool() {
		return binder(DatabaseConnectionPool.T, DataSource.class);
	}

	@Override
	@Managed
	public ComponentBinder<DatabaseConnectionInfoProvider, tribefire.module.api.DatabaseConnectionInfoProvider> databaseConnectionInfoProvider() {
		return binder(DatabaseConnectionInfoProvider.T, tribefire.module.api.DatabaseConnectionInfoProvider.class);
	}

	@Override
	@Managed
	public WebTerminalBinder webTerminal() {
		WebTerminalBinder bean = new WebTerminalBinder();
		bean.setDispatcherServlet(systemServlets.componentServlet());
		bean.setAuthFilter(securityServlets.authFilterLenient());
		return bean;
	}

	@Override
	@Managed
	public ComponentBinder<CollaborativeAccess, com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess> collaborativeAccess() {
		return new PlainComponentBinder<>(CollaborativeAccess.T, com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess.class);
	}

	static <D extends Deployable, A> ComponentBinder<D, A> binder(EntityType<D> componentType, Class<?> apiType) {
		return new PlainComponentBinder<>(componentType, (Class<A>) apiType);
	}
}
