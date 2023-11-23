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
package tribefire.platform.wire.space.cortex.deployment.deployables;

import java.util.Arrays;

import com.braintribe.model.accessdeployment.DefaultSystemAccess;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.cortex.aspect.CryptoAspect;
import com.braintribe.model.cortex.aspect.FulltextAspect;
import com.braintribe.model.cortex.aspect.IdGeneratorAspect;
import com.braintribe.model.cortex.aspect.SecurityAspect;
import com.braintribe.model.cortex.aspect.StateProcessingAspect;
import com.braintribe.model.cortex.preprocessor.RequestValidatorPreProcessor;
import com.braintribe.model.cortex.processorrules.BidiPropertyStateChangeProcessorRule;
import com.braintribe.model.cortex.processorrules.MetaDataStateChangeProcessorRule;
import com.braintribe.model.deployment.remote.GmWebRpcRemoteServiceProcessor;
import com.braintribe.model.deployment.remote.RemotifyingInterceptor;
import com.braintribe.model.deployment.resource.filesystem.FileSystemBinaryProcessor;
import com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor;
import com.braintribe.model.idgendeployment.NumericUidGenerator;
import com.braintribe.model.idgendeployment.UuidGenerator;
import com.braintribe.model.lockingdeployment.Locking;
import com.braintribe.model.messagingdeployment.Messaging;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.model.deployment.mimetypedetection.MimeTypeDetector;
import tribefire.cortex.model.deployment.usersession.cleanup.AccessCleanupUserSessionsProcessor;
import tribefire.cortex.model.deployment.usersession.cleanup.JdbcCleanupUserSessionsProcessor;
import tribefire.cortex.model.deployment.usersession.service.AccessUserSessionService;
import tribefire.cortex.model.deployment.usersession.service.JdbcUserSessionService;
import tribefire.platform.impl.deployment.ComponentInterfaceBindingsRegistry;
import tribefire.platform.impl.deployment.DenotationBindingsRegistry;
import tribefire.platform.impl.deployment.proxy.SchrodingerBeanCoupler;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.common.TribefireConnectionsSpace;
import tribefire.platform.wire.space.cortex.AccessAspectsSpace;
import tribefire.platform.wire.space.cortex.deployment.StateChangeProcessorsSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.access.CollaborativeSmoodAccessSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.aspect.StateProcessingAspectSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.idgenerator.NumericUidGeneratorSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.idgenerator.UuidGeneratorSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.processor.HttpProcessorSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.processor.PreProcessorSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.streaming.BinaryProcessorsSpace;
import tribefire.platform.wire.space.cortex.services.ClusterSpace;
import tribefire.platform.wire.space.messaging.accesses.TransientMessagingDataAccessSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.security.accesses.AuthAccessSpace;
import tribefire.platform.wire.space.security.accesses.UserSessionsAccessSpace;
import tribefire.platform.wire.space.security.accesses.UserStatisticsAccessSpace;
import tribefire.platform.wire.space.security.services.UserSessionServiceSpace;

@Managed
public class DeployablesSpace implements WireSpace {

	// @formatter:off
	@Import private AccessAspectsSpace accessAspects;
	@Import private AuthAccessSpace authAccess;
	@Import private BinaryProcessorsSpace binaryProcessors;
	@Import private BindersSpace binders;
	@Import private ClusterSpace cluster;
	@Import private CollaborativeSmoodAccessSpace collaborativeSmoodAccess;
	@Import private HttpProcessorSpace httpProcessors;
	@Import private MessagingSpace messaging;
	@Import private ModuleInitializationSpace moduleInitialization;
	@Import private NumericUidGeneratorSpace numericUidGenerator;
	@Import private PreProcessorSpace preProcessor;
	@Import private ResourceProcessingSpace resourceProcessing;
	@Import private StateChangeProcessorsSpace stateChangeProcessors;
	@Import private StateProcessingAspectSpace stateProcessingAspect;
	@Import private TransientMessagingDataAccessSpace transientMessagingDataAccess;
	@Import private TribefireConnectionsSpace tribefireConnections;
	@Import private UserSessionsAccessSpace userSessionsAccess;
	@Import private UserSessionServiceSpace userSessionService;
	@Import private UserStatisticsAccessSpace userStatisticsAccess;
	@Import private UuidGeneratorSpace uuidGenerator;
	// @formatter:on

	@Managed
	public DenotationBindingsRegistry bindings() {
		DenotationBindingsRegistry bean = new DenotationBindingsRegistry();
		bean.setInterfaceBindings(interfaceBindings());

		bindDefaultPlatformDeployables(bean);

		/* ---- PreProcessors ---- */

		bean.bind(RequestValidatorPreProcessor.T) //
				.component(binders.servicePreProcessor()) //
				.expertSupplier(preProcessor::requestValidatorPreProcessor); //

		/* ---- Incremental accesses ---- */

		bean.bind(CollaborativeSmoodAccess.T) //
				.component(binders.incrementalAccess()).expertFactory(collaborativeSmoodAccess::access) //
				.component(binders.collaborativeAccess()).expertFactory(collaborativeSmoodAccess::access);

		/* ---- User Session Services ---- */
		bean.bind(AccessUserSessionService.T) //
				.component(binders.userSessionService()) //
				.expertSupplier(userSessionService::accessSessionService);

		bean.bind(JdbcUserSessionService.T) //
				.component(binders.userSessionService()) //
				.expertFactory(userSessionService::jdbcService);

		bean.bind(AccessCleanupUserSessionsProcessor.T) //
				.component(binders.serviceProcessor()) //
				.expertSupplier(userSessionService::accessSessionCleanupService);

		bean.bind(JdbcCleanupUserSessionsProcessor.T) //
				.component(binders.serviceProcessor()) //
				.expertFactory(userSessionService::jdbcCleanupService);

		/* ---- Access aspects ---- */

		bean.bind(StateProcessingAspect.T) //
				.component(binders.accessAspect()) //
				.expertFactory(stateProcessingAspect::stateProcessingAspect);

		bean.bind(SecurityAspect.T) //
				.component(binders.accessAspect()) //
				.expertSupplier(accessAspects::security);

		bean.bind(FulltextAspect.T) //
				.component(binders.accessAspect()) //
				.expertSupplier(accessAspects::fulltext);

		bean.bind(IdGeneratorAspect.T) //
				.component(binders.accessAspect()) //
				.expertSupplier(accessAspects::idGenerator);

		bean.bind(CryptoAspect.T) //
				.component(binders.accessAspect()) //
				.expertSupplier(accessAspects::crypto);

		/* ---- Id generators ---- */

		bean.bind(UuidGenerator.T) //
				.component(binders.idGenerator()) //
				.expertFactory(uuidGenerator::uuidGenerator);

		bean.bind(NumericUidGenerator.T) //
				.component(binders.idGenerator()) //
				.expertSupplier(numericUidGenerator::nuidGenerator);

		/* ---- Tribefire connectors ---- */

		tribefireConnections.bindAll(bean);

		/* ---- State change processor rules ---- */

		// @formatter:off
		bean.bind(BidiPropertyStateChangeProcessorRule.T)
			.component(binders.stateChangeProcessorRule())
			.expertSupplier(stateChangeProcessors::bidiProperty);

		bean.bind(MetaDataStateChangeProcessorRule.T)
			.component(binders.stateChangeProcessorRule())
			.expertSupplier(stateChangeProcessors::metadata);
		// @formatter:on

		/* ---- Binary processors ---- */

		bean.bind(FileSystemBinaryProcessor.T) //
				.component(binders.binaryRetrievalProcessor()).expertFactory(binaryProcessors::fileSystem) //
				.component(binders.binaryPersistenceProcessor()).expertFactory(binaryProcessors::fileSystem);

		bean.bind(SqlBinaryProcessor.T) //
				.component(binders.binaryRetrievalProcessor()).expertFactory(binaryProcessors::sql) //
				.component(binders.binaryPersistenceProcessor()).expertFactory(binaryProcessors::sql);

		bean.bind(GmWebRpcRemoteServiceProcessor.T) //
				.component(binders.serviceProcessor()) //
				.expertFactory(httpProcessors::gmWebRpcRemoteServiceProcessor);

		bean.bind(RemotifyingInterceptor.T) //
				.component(binders.serviceAroundProcessor()) //
				.expertFactory(httpProcessors::remotifyingInterceptor);

		return bean;
	}

	private void bindDefaultPlatformDeployables(DenotationBindingsRegistry bean) {
		bindDefaultVitals(bean);
		bindDefaultAccesses(bean);
		bindDefaultOthers(bean);
	}

	private void bindDefaultVitals(DenotationBindingsRegistry bean) {
		bean.bind(Locking.T, ClusterSpace.DEFAULT_LOCKING_EXTERNAL_ID) //
				.component(binders.locking()) //
				.expertSupplier(cluster::defaultLocking); //

		bean.bind(Messaging.T, MessagingSpace.DEFAULT_MESSAGING_EXTERNAL_ID) //
				.component(binders.messaging()) //
				.expertSupplier(messaging::defaultMessagingConnectionSupplier); //
	}

	private void bindDefaultAccesses(DenotationBindingsRegistry bean) {
		bean.bind(CollaborativeSmoodAccess.T, TribefireConstants.ACCESS_AUTH) //
				.component(binders.incrementalAccess()).expertSupplier(authAccess::defaultAccess) //
				.component(binders.collaborativeAccess()).expertSupplier(authAccess::defaultAccess);

		bean.bind(DefaultSystemAccess.T, TribefireConstants.ACCESS_USER_SESSIONS) //
				.component(binders.incrementalAccess()) //
				.expertSupplier(userSessionsAccess::defaultAccess); //

		bean.bind(DefaultSystemAccess.T, TribefireConstants.ACCESS_USER_STATISTICS) //
				.component(binders.incrementalAccess()) //
				.expertSupplier(userStatisticsAccess::defaultAccess); //

		bean.bind(DefaultSystemAccess.T, TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA) //
				.component(binders.incrementalAccess()) //
				.expertSupplier(transientMessagingDataAccess::defaultAccess); //
	}

	private void bindDefaultOthers(DenotationBindingsRegistry bean) {
		bean.bind(MimeTypeDetector.T, ResourceProcessingSpace.DEFAULT_MIME_TYPE_DETECTOR_EXTERNAL_ID) //
				.component(binders.mimeTypeDetector()) //
				.expertSupplier(resourceProcessing::defaultMimeTypeDetector);
	}

	@Managed
	public ComponentInterfaceBindingsRegistry interfaceBindings() {
		ComponentInterfaceBindingsRegistry bean = new ComponentInterfaceBindingsRegistry();
		return bean;
	}

	@Managed
	public SchrodingerBeanCoupler schrodingerBeanCoupler() {
		SchrodingerBeanCoupler bean = new SchrodingerBeanCoupler();
		bean.setBeans(Arrays.asList( //
				authAccess.accessSchrodingerBean(), //
				userSessionsAccess.accessSchrodingerBean(), //
				userStatisticsAccess.accessSchrodingerBean(), //
				transientMessagingDataAccess.accessSchrodingerBean(), //

				userSessionService.serviceSchrodingerBean(), //
				userSessionService.cleanupServiceSchrodingerBean(), //

				resourceProcessing.mimeTypeDetectorSchrodingerBean(), //

				cluster.lockingSchrodingerBean(), //
				messaging.messagingSchrodingerBean() //
		));

		return bean;
	}
}
