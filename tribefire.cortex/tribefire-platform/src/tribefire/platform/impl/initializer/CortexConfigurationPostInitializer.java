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
package tribefire.platform.impl.initializer;

import static com.braintribe.model.generic.reflection.Model.modelGlobalId;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_AUTH;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_AUTH_NAME;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_AUTH;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_TRANSIENT_MESSAGING_DATA;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_SESSIONS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_MODEL_USER_STATISTICS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA_NAME;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_SESSIONS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_SESSIONS_NAME;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_STATISTICS;
import static com.braintribe.model.processing.tfconstants.TribefireConstants.ACCESS_USER_STATISTICS_NAME;
import static tribefire.platform.impl.initializer.DefaultDeployableInitializer.defaultDeployableGlobalId;
import static tribefire.platform.wire.space.common.MessagingSpace.DEFAULT_MESSAGING_EXTERNAL_ID;
import static tribefire.platform.wire.space.common.ResourceProcessingSpace.DEFAULT_MIME_TYPE_DETECTOR_EXTERNAL_ID;
import static tribefire.platform.wire.space.cortex.accesses.DefaultDeployablesSpace.CRYPTO_ASPECT_EXTERNAL_ID;
import static tribefire.platform.wire.space.cortex.accesses.DefaultDeployablesSpace.SECURITY_ASPECT_EXTERNAL_ID;
import static tribefire.platform.wire.space.cortex.services.ClusterSpace.DEFAULT_LOCKING_EXTERNAL_ID;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.DefaultSystemAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.lockingdeployment.Locking;
import com.braintribe.model.messagingdeployment.Messaging;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.cortex.model.deployment.mimetypedetection.MimeTypeDetector;
import tribefire.cortex.model.deployment.usersession.cleanup.AccessCleanupUserSessionsProcessor;
import tribefire.cortex.model.deployment.usersession.cleanup.CleanupUserSessionsProcessor;
import tribefire.cortex.model.deployment.usersession.service.AccessUserSessionService;
import tribefire.cortex.model.deployment.usersession.service.UserSessionService;
import tribefire.platform.impl.denotrans.CleanupUserSessionsProcessorEnricher;
import tribefire.platform.impl.denotrans.UserSessionServiceEnricher;

/**
 * Final initializer that configures default deployables on {@link CortexConfiguration} (CC) where none has bean configured by any initializer or
 * {@link Edr2ccPostInitializer}.
 * <p>
 * In other words, ensures system components on {@link CortexConfiguration} are not <tt>null</tt>.
 * <p>
 * <h3>Supported CC components</h3>
 * 
 * Vitals:
 * <ul>
 * <li>{@link CortexConfiguration#getLeadershipManager() Leadership Manager}
 * <li>{@link CortexConfiguration#getLockManager() Lock Manager}
 * <li>{@link CortexConfiguration#getMessaging() Messaging}
 * </ul>
 * 
 * Accesses (and related):
 * <ul>
 * <li>{@link CortexConfiguration#getAuthenticationAccess() Authentication Access}
 * <li>{@link CortexConfiguration#getTransientMessagingAccess() Transient Messaging Access}
 * <li>{@link CortexConfiguration#getUserStatisticsAccess() User Statistics Access}
 * <li>{@link CortexConfiguration#getUserSessionsAccess() User Sessions Access} with:
 * <ul>
 * <li>{@link CortexConfiguration#getUserSessionService() User Sessions Service}
 * <li>{@link CortexConfiguration#getCleanupUserSessionsProcessor() CleanupUserSessionsProcessor}
 * </ul>
 * </ul>
 * 
 * Other:
 * <ul>
 * <li>{@link CortexConfiguration#getMimeTypeDetector() MimeTypeDetector}
 * </ul>
 *
 * @see Edr2ccPostInitializer
 * 
 * @author peter.gazdik
 */
public class CortexConfigurationPostInitializer extends SimplePersistenceInitializer {

	private static final Logger log = Logger.getLogger(CortexConfigurationPostInitializer.class);

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		new CortexConfigurationPostInitialization(context).run();
	}

	static class CortexConfigurationPostInitialization {

		private final ManagedGmSession session;
		private final CortexConfiguration cortexConfig;

		public CortexConfigurationPostInitialization(PersistenceInitializationContext context) {
			this.session = context.getSession();
			this.cortexConfig = session.getEntityByGlobalId(CortexConfiguration.CORTEX_CONFIGURATION_GLOBAL_ID);
		}

		public void run() {
			// Vitals
			locking();
			messaging();

			// Accesses
			authAccess();
			userSessionsAccess();
			userSessionsServices();
			userStatisticsAccess();
			transientMessagingAccess();

			// Other
			mimeTypeDetector();
		}

		// ################################################
		// ## . . . . . . . . Locking . . . . . . . . . ##
		// ################################################

		private void locking() {
			if (cortexConfig.getLocking() == null)
				cortexConfig.setLocking(defaultLocking());
		}

		private Locking defaultLocking() {
			Locking bean = session.create(Locking.T, "default:locking/" + DEFAULT_LOCKING_EXTERNAL_ID);
			bean.setExternalId(DEFAULT_LOCKING_EXTERNAL_ID);
			bean.setName("DMB Locking (Default)");
			
			return logDefaultCcComponent(bean, "locking");
		}
		
		// ################################################
		// ## . . . . . . . . Messaging . . . . . . . . .##
		// ################################################

		private void messaging() {
			if (cortexConfig.getMessaging() == null)
				cortexConfig.setMessaging(defaultMessaging());
		}

		private Messaging defaultMessaging() {
			Messaging bean = session.create(Messaging.T, "default:messaging/" + DEFAULT_MESSAGING_EXTERNAL_ID);
			bean.setExternalId(DEFAULT_MESSAGING_EXTERNAL_ID);
			bean.setName("GmDmbMq Messaging (Default)");

			return logDefaultCcComponent(bean, "messaging");
		}

		// ################################################
		// ## . . . . . Authentication Access . . . . . .##
		// ################################################

		private void authAccess() {
			IncrementalAccess access = cortexConfig.getAuthenticationAccess();

			if (access == null)
				cortexConfig.setAuthenticationAccess(authAccessDenotation());

			else if (access.getAspectConfiguration() == null)
				access.setAspectConfiguration(authAspectConfiguration());
		}

		private CollaborativeSmoodAccess authAccessDenotation() {
			CollaborativeSmoodAccess bean = session.create(CollaborativeSmoodAccess.T, "default:access/" + ACCESS_AUTH);
			bean.setExternalId(ACCESS_AUTH);
			bean.setName(ACCESS_AUTH_NAME);
			bean.setAspectConfiguration(authAspectConfiguration());
			bean.setMetaModel(session.getEntityByGlobalId(modelGlobalId(ACCESS_MODEL_AUTH)));
			bean.setWorkbenchAccess(session.getEntityByGlobalId("hardwired:access/auth.wb"));

			return logDefaultCcComponent(bean, "authAccess");
		}

		private AspectConfiguration authAspectConfiguration() {
			return aspectConfiguration(ACCESS_AUTH, SECURITY_ASPECT_EXTERNAL_ID, CRYPTO_ASPECT_EXTERNAL_ID);
		}

		// ################################################
		// ## . . . . . . . User Sessions . . . . . . . .##
		// ################################################

		private void userSessionsAccess() {
			IncrementalAccess access = cortexConfig.getUserSessionsAccess();

			if (access == null) {
				cortexConfig.setUserSessionsAccess(userSessionsAccessDenotation());
				return;
			}

			if (access.getAspectConfiguration() == null)
				access.setAspectConfiguration(userSessionsAspectConfiguration());

			if (access.getWorkbenchAccess() == null)
				access.setWorkbenchAccess(userSessionsWorkbench());
		}

		private DefaultSystemAccess userSessionsAccessDenotation() {
			DefaultSystemAccess bean = session.create(DefaultSystemAccess.T, "default:access/" + ACCESS_USER_SESSIONS);
			bean.setExternalId(ACCESS_USER_SESSIONS);
			bean.setName(ACCESS_USER_SESSIONS_NAME);
			bean.setAspectConfiguration(userSessionsAspectConfiguration());
			bean.setMetaModel(session.getEntityByGlobalId(modelGlobalId(ACCESS_MODEL_USER_SESSIONS)));
			bean.setWorkbenchAccess(userSessionsWorkbench());

			return logDefaultCcComponent(bean, "userSessionsAccess");
		}

		private AspectConfiguration userSessionsAspectConfiguration() {
			return aspectConfiguration(ACCESS_USER_SESSIONS, SECURITY_ASPECT_EXTERNAL_ID);
		}

		private IncrementalAccess userSessionsWorkbench() {
			return session.getEntityByGlobalId("hardwired:access/user-sessions.wb");
		}

		private void userSessionsServices() {
			UserSessionService usService = cortexConfig.getUserSessionService();
			if (usService == null)
				cortexConfig.setUserSessionService(userSessionServiceDenotation());

			CleanupUserSessionsProcessor cleanupProcessor = cortexConfig.getCleanupUserSessionsProcessor();
			if (cleanupProcessor == null)
				cortexConfig.setCleanupUserSessionsProcessor(cleanupProcessorDenotation());
		}

		private AccessUserSessionService userSessionServiceDenotation() {
			AccessUserSessionService bean = session.create(AccessUserSessionService.T);
			UserSessionServiceEnricher.fill(bean);
			return bean;
		}

		private AccessCleanupUserSessionsProcessor cleanupProcessorDenotation() {
			AccessCleanupUserSessionsProcessor bean = session.create(AccessCleanupUserSessionsProcessor.T);
			CleanupUserSessionsProcessorEnricher.fill(bean);
			return bean;
		}

		// ################################################
		// ## . . . . . . User Statistics . . . . . . . .##
		// ################################################

		private void userStatisticsAccess() {
			IncrementalAccess access = cortexConfig.getUserStatisticsAccess();

			if (access == null) {
				cortexConfig.setUserStatisticsAccess(userStatisticsAccessDenotation());
				return;
			}

			if (access.getAspectConfiguration() == null)
				access.setAspectConfiguration(userStatisticsAspectConfiguration());

			if (access.getWorkbenchAccess() == null)
				access.setWorkbenchAccess(userStatisticsWorkbench());
		}

		private DefaultSystemAccess userStatisticsAccessDenotation() {
			DefaultSystemAccess bean = session.create(DefaultSystemAccess.T, "default:access/" + ACCESS_USER_STATISTICS);
			bean.setExternalId(ACCESS_USER_STATISTICS);
			bean.setName(ACCESS_USER_STATISTICS_NAME);
			bean.setAspectConfiguration(userStatisticsAspectConfiguration());
			bean.setMetaModel(session.getEntityByGlobalId(modelGlobalId(ACCESS_MODEL_USER_STATISTICS)));
			bean.setWorkbenchAccess(session.getEntityByGlobalId("hardwired:access/user-statistics.wb"));

			return logDefaultCcComponent(bean, "userStatisticsAccess");
		}

		private AspectConfiguration userStatisticsAspectConfiguration() {
			return aspectConfiguration(ACCESS_USER_STATISTICS, SECURITY_ASPECT_EXTERNAL_ID);
		}

		private IncrementalAccess userStatisticsWorkbench() {
			return session.getEntityByGlobalId("hardwired:access/user-statistics.wb");
		}

		// ################################################
		// ## . . . . . Transient Messaging . . . . . . .##
		// ################################################

		private void transientMessagingAccess() {
			IncrementalAccess access = cortexConfig.getTransientMessagingAccess();

			if (access == null) {
				cortexConfig.setTransientMessagingAccess(transientMessagingAccessDenotation());
				return;
			}

			if (access.getAspectConfiguration() == null)
				access.setAspectConfiguration(transientMessagingAspectConfiguration());

			if (access.getWorkbenchAccess() == null)
				access.setWorkbenchAccess(transientMessagingWorkbench());
		}

		private DefaultSystemAccess transientMessagingAccessDenotation() {
			DefaultSystemAccess bean = session.create(DefaultSystemAccess.T, "default:access/" + ACCESS_TRANSIENT_MESSAGING_DATA);
			bean.setExternalId(ACCESS_TRANSIENT_MESSAGING_DATA);
			bean.setName(ACCESS_TRANSIENT_MESSAGING_DATA_NAME);
			bean.setAspectConfiguration(transientMessagingAspectConfiguration());
			bean.setMetaModel(session.getEntityByGlobalId(modelGlobalId(ACCESS_MODEL_TRANSIENT_MESSAGING_DATA)));
			bean.setWorkbenchAccess(transientMessagingWorkbench());

			return logDefaultCcComponent(bean, "transientMessagingAccess");
		}

		private AspectConfiguration transientMessagingAspectConfiguration() {
			return aspectConfiguration(ACCESS_TRANSIENT_MESSAGING_DATA, SECURITY_ASPECT_EXTERNAL_ID);
		}

		private IncrementalAccess transientMessagingWorkbench() {
			return session.getEntityByGlobalId("hardwired:access/transient-messaging-data.wb");
		}

		// ################################################
		// ## . . . . . MIME Type Detector . . . . . . . ##
		// ################################################

		private void mimeTypeDetector() {
			if (cortexConfig.getMimeTypeDetector() == null)
				cortexConfig.setMimeTypeDetector(defaultMimeTypeDetector());
		}

		private MimeTypeDetector defaultMimeTypeDetector() {
			MimeTypeDetector bean = session.create(MimeTypeDetector.T, "default:mimeTypeDetector/" + DEFAULT_MIME_TYPE_DETECTOR_EXTERNAL_ID);
			bean.setExternalId(DEFAULT_MIME_TYPE_DETECTOR_EXTERNAL_ID);
			bean.setName("MIME Type Detector (Default)");

			return logDefaultCcComponent(bean, "mimeTypeDetector");
		}

		// ################################################
		// ## . . . . . . . . Helpers . . . . . . . . . .##
		// ################################################

		private AspectConfiguration aspectConfiguration(String accessId, String... aspectExternalIds) {
			AspectConfiguration bean = session.create(AspectConfiguration.T, "default:aspectConfig/" + accessId);
			for (String aspectExternalId : aspectExternalIds)
				bean.getAspects().add(session.getEntityByGlobalId(defaultDeployableGlobalId(aspectExternalId)));

			return bean;
		}

		private <T extends Deployable> T logDefaultCcComponent(T bean, String component) {
			log.info("CortexConfiguration." + component + " set to default deployable: " + bean);

			return bean;
		}

	}

}
