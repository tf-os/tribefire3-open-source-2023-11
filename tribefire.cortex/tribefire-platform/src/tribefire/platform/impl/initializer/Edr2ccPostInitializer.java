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

import static tribefire.module.api.PlatformBindIds.RESOURCES_DB;

import java.util.Map;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.resource.sql.SqlBinaryProcessor;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.module.api.DenotationMorpher;
import tribefire.module.api.DenotationTransformer;
import tribefire.module.api.DenotationTransformerRegistry;
import tribefire.module.api.PlatformBindIds;
import tribefire.platform.impl.denotrans.AccessToAccessCleanupUserSessionsProcessorMorpher;
import tribefire.platform.impl.denotrans.AccessToAccessUserSessionServiceMorpher;
import tribefire.platform.impl.denotrans.CleanupUserSessionsProcessorEnricher;
import tribefire.platform.impl.denotrans.SystemAccessesDenotationEnricher;
import tribefire.platform.impl.denotrans.SystemDeployablesAutoDeployEnsuringEnricher;
import tribefire.platform.impl.denotrans.UserSessionServiceEnricher;
import tribefire.platform.impl.configuration.denotrans.DenotationTransformationExecutor;
import tribefire.platform.wire.space.security.services.UserSessionServiceSpace;

/**
 * Configures system components on {@link CortexConfiguration} (CC) based on {@link EnvironmentDenotationRegistry} (EDR).
 * <p>
 * For each relevant CC property this initializer looks for a EDR entry by a known bindId (see {@link PlatformBindIds}). If an entry exists, the
 * denotation is transformed into a desired type (based on the CortexConfiguration property type) and assigned to the property.
 * 
 * <h3>Supported CC components</h3>
 * 
 * Vitals:
 * <ul>
 * <li>{@link CortexConfiguration#getLocking() Locking} ({@value PlatformBindIds#TRIBEFIRE_LOCK_DB_BIND_ID},
 * {@value PlatformBindIds#TRIBEFIRE_LOCK_MANAGER_BIND_ID})
 * <li>{@link CortexConfiguration#getMessaging() Messaging} ({@value PlatformBindIds#TRANSIENT_MESSAGING_DATA_DB_BIND_ID})
 * </ul>
 * 
 * Accesses (and related):
 * <ul>
 * <li>{@link CortexConfiguration#getAuthenticationAccess() Authentication Access} ({@value PlatformBindIds#AUTH_DB_BIND_ID})
 * <li>{@link CortexConfiguration#getTransientMessagingAccess() Transient Messaging Access}
 * ({@value PlatformBindIds#TRANSIENT_MESSAGING_DATA_DB_BIND_ID})
 * <li>{@link CortexConfiguration#getUserStatisticsAccess() User Statistics Access} ({@value PlatformBindIds#USER_STATISTICS_DB_BIND_ID})
 * <li>{@link CortexConfiguration#getUserSessionsAccess() User Sessions Access} ({@value PlatformBindIds#USER_SESSIONS_DB_BIND_ID}) with:
 * <ul>
 * <li>{@link CortexConfiguration#getUserSessionService() User Sessions Service}
 * <li>{@link CortexConfiguration#getCleanupUserSessionsProcessor() CleanupUserSessionsProcessor}
 * </ul>
 * </ul>
 * 
 * <h3>Denotation Transformation</h3>
 * 
 * The transformation is done by given {@link DenotationTransformationExecutor}, which uses {@link DenotationTransformerRegistry} internally.
 * <p>
 * {@link DenotationTransformer}s in this registry can be registered from extension modules (e.g. a {@link DenotationMorpher} that turns a
 * {@link DatabaseConnectionPool} to a HibernateAccess), but some are registered by the platform.
 * <p>
 * Platform transformers:
 * <ul>
 * <li>{@link SystemAccessesDenotationEnricher}
 * <li>{@link SystemDeployablesAutoDeployEnsuringEnricher}
 * <li>{@link CleanupUserSessionsProcessorEnricher}
 * <li>{@link UserSessionServiceEnricher}
 * <li>{@link AccessToAccessCleanupUserSessionsProcessorMorpher}
 * <li>{@link AccessToAccessUserSessionServiceMorpher}
 * </ul>
 * 
 * For a more general overview of CC initialization see {@link CortexConfigurationPostInitializer}.
 * 
 * @see CortexConfiguration
 * @see CortexConfigurationPostInitializer
 * 
 * @author peter.gazdik
 */
public class Edr2ccPostInitializer extends SimplePersistenceInitializer {

	private DenotationTransformationExecutor transformationExecutor;

	private static final Logger log = Logger.getLogger(Edr2ccPostInitializer.class);

	@Required
	public void setTransformationExecutor(DenotationTransformationExecutor transformationExecutor) {
		this.transformationExecutor = transformationExecutor;
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		new Edr2ccInitialization(context).run();
	}

	private class Edr2ccInitialization {

		private final ManagedGmSession session;
		private final CortexConfiguration cortexConfiguration;
		private final Map<String, GenericEntity> edrEntries;

		public Edr2ccInitialization(PersistenceInitializationContext context) {
			session = context.getSession();
			cortexConfiguration = session.getEntityByGlobalId(CortexConfiguration.CORTEX_CONFIGURATION_GLOBAL_ID);

			EnvironmentDenotationRegistry edr = session.getEntityByGlobalId(EnvironmentDenotationRegistry.ENVIRONMENT_DENOTATION_REGISTRY__GLOBAL_ID);
			edrEntries = edr.getEntries();
		}

		public void run() {
			// Vitals
			handle(PlatformBindIds.TRIBEFIRE_LOCKING_BIND_ID, CortexConfiguration.locking);
			handle(PlatformBindIds.TRIBEFIRE_MQ_BIND_ID, CortexConfiguration.messaging);

			// Accesses
			handle(PlatformBindIds.AUTH_DB_BIND_ID, CortexConfiguration.authenticationAccess);
			handle(PlatformBindIds.USER_SESSIONS_DB_BIND_ID, CortexConfiguration.userSessionsAccess);
			handle(PlatformBindIds.USER_STATISTICS_DB_BIND_ID, CortexConfiguration.userStatisticsAccess);
			handle(PlatformBindIds.TRANSIENT_MESSAGING_DATA_DB_BIND_ID, CortexConfiguration.transientMessagingAccess);

			handleUserSessionServices();
			handleSqlBinaryProcessor();
		}

		private void handleUserSessionServices() {
			if (findEntry(PlatformBindIds.USER_SESSIONS_DB_BIND_ID) == null) {
				log.info("Edr2cc: Not EDR entry found for bindId [" + PlatformBindIds.USER_SESSIONS_DB_BIND_ID + "], skipping CC properties: ["
						+ CortexConfiguration.userSessionService + ", " + CortexConfiguration.cleanupUserSessionsProcessor + "]");
				return;
			}

			IncrementalAccess access = cortexConfiguration.getUserSessionsAccess();
			handle(UserSessionServiceSpace.USER_SESSION_SERVICE_ID, access, CortexConfiguration.userSessionService);
			handle(UserSessionServiceSpace.CLEANUP_USER_SESSIONS_PROCESSOR_ID, access, CortexConfiguration.cleanupUserSessionsProcessor);
		}

		private void handle(String bindId, String propertyName) {
			GenericEntity denotation = findEntry(bindId);
			if (denotation == null) {
				log.info("Edr2cc: Not EDR entry found for bindId [" + bindId + "]. Skipping CC property: " + propertyName);
				return;
			}

			handle(bindId, denotation, propertyName);
		}

		private void handle(String bindId, GenericEntity denotation, String propertyName) {
			Property p = CortexConfiguration.T.getProperty(propertyName);
			EntityType<?> propertyType = (EntityType<?>) p.getType();

			GenericEntity finalEntity = transformOrThrowException(bindId, denotation, propertyName, propertyType);

			GenericEntity previousEntity = p.get(cortexConfiguration);
			if (previousEntity != null)
				log.warn("Edr2cc: OVERWRITING CortextConfiguration." + propertyName + ". NEW: " + finalEntity + ", OLD: " + previousEntity);

			p.set(cortexConfiguration, finalEntity);
		}

		private void handleSqlBinaryProcessor() {
			GenericEntity denotation = findEntry(RESOURCES_DB);
			if (denotation == null) {
				log.info("Edr2cc: Not EDR entry found for bindId [" + RESOURCES_DB + "].");
				return;
			}

			transformOrThrowException(RESOURCES_DB, denotation, null, SqlBinaryProcessor.T);
		}

		private GenericEntity transformOrThrowException(String bindId, GenericEntity denotation, String propertyName, EntityType<?> propertyType) {

			Maybe<? extends GenericEntity> transformResult = transformationExecutor.transform(bindId, session, denotation, propertyType);
			if (!transformResult.isSatisfied())
				throw new IllegalStateException(edr2ccContext(bindId, propertyName, "FAILED") + "Denotation instance: [" + denotation + "]. "
						+ "Transformation error:\n" + transformResult.whyUnsatisfied().stringify());

			GenericEntity finalEntity = transformResult.get();
			log.info(edr2ccContext(bindId, propertyName, "SUCCEEDED") + "Transformation result: " + finalEntity);

			return finalEntity;
		}

		private GenericEntity findEntry(String bindId) {
			return edrEntries.get(bindId);
		}

		private String edr2ccContext(String bindId, String propertyName, String failedOrSucceeded) {
			return "Edr2cc: Transformation of environment denotation instance with bindId [" + bindId + "] " + failedOrSucceeded + "."
					+ (propertyName == null ? "" : " Relevant CC property: [" + propertyName + "]. ");
		}

	}

}
