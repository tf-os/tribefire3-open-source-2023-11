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
package tribefire.extension.messaging.wire.space;

import java.time.Duration;
import java.util.Optional;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.messaging.connector.api.ConsumerMessagingConnector;
import tribefire.extension.messaging.connector.api.ProducerMessagingConnector;
import tribefire.extension.messaging.model.deployment.event.rule.ConsumerEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.model.deployment.service.MessagingAspect;
import tribefire.extension.messaging.model.meta.MessagingProperty;
import tribefire.extension.messaging.model.meta.MessagingTypeSignature;
import tribefire.extension.messaging.service.HealthCheckProcessor;
import tribefire.extension.messaging.service.MessagingAspectImpl;
import tribefire.extension.messaging.service.MessagingProcessor;
import tribefire.extension.messaging.service.MessagingWorker;
import tribefire.extension.messaging.service.cache.CortexCache;
import tribefire.extension.messaging.service.test.TestGetObjectProcessor;
import tribefire.extension.messaging.service.test.TestReceiveMessagingProcessor;
import tribefire.extension.messaging.service.test.TestUpdateObjectProcessor;
import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.PlatformReflectionContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 *
 */
@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	@Import
	private PlatformReflectionContract platformReflection;

	@Import
	private ModuleReflectionContract module;

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public MessagingProcessor messagingProcessor(ExpertContext<tribefire.extension.messaging.model.deployment.service.MessagingProcessor> context) {
		tribefire.extension.messaging.model.deployment.service.MessagingProcessor deployable = context.getDeployable();

		MessagingProcessor bean = new MessagingProcessor();
		bean.setDeployable(deployable);
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setModuleClassLoader(module.moduleClassLoader());
		bean.setCortexCache(producerCache());
		return bean;
	}

	@Managed
	public CortexCache<String, ProducerMessagingConnector> producerCache() {
		CortexCache<String, ProducerMessagingConnector> cache = new CortexCache<>();
		cache.setExpirationDuration(Duration.ofMinutes(10));
		cache.setExpireAfterWrite(false);
		cache.setRemovalListener((key, value, cause) -> Optional.ofNullable(value).ifPresent(ProducerMessagingConnector::destroy));
		cache.setKeyExtractionFunction(ProducerMessagingConnector::getExternalId);

		cache.initialize();
		return cache;
	}

	@Managed
	public MessagingAspectImpl messagingAspect(ExpertContext<MessagingAspect> context) {
		MessagingAspect deployable = context.getDeployable();

		tribefire.extension.messaging.model.deployment.service.MessagingProcessor messagingProcessor = deployable.getMessagingProcessor();

		MessagingAspectImpl bean = new MessagingAspectImpl();
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setContextName(deployable.getConfigurationName());
		bean.setMessagingProcessor(messagingProcessor);
		bean.setProducerRuleCache(aspectCache());
		bean.setTypeSignatureMdCache(typeSignatureMdCache());
		bean.setPropertyMdCache(propertyMdCache());
		bean.setDeployable(deployable);
		return bean;
	}

	@Managed
	public CortexCache<String, MessagingTypeSignature> typeSignatureMdCache() {
		CortexCache<String, MessagingTypeSignature> cache = new CortexCache<>();
		cache.setExpirationDuration(Duration.ofDays(1));
		cache.setExpireAfterWrite(false);
		cache.setRemovalListener(null);
		cache.setKeyExtractionFunction(null);

		cache.initialize();
		return cache;
	}

	@Managed
	public CortexCache<Property, MessagingProperty> propertyMdCache() {
		CortexCache<Property, MessagingProperty> cache = new CortexCache<>();
		cache.setExpirationDuration(Duration.ofDays(1));
		cache.setExpireAfterWrite(false);
		cache.setRemovalListener(null);
		cache.setKeyExtractionFunction(null);

		cache.initialize();
		return cache;
	}

	@Managed
	public CortexCache<String, ProducerEventRule> aspectCache() {
		CortexCache<String, ProducerEventRule> cache = new CortexCache<>();
		cache.setExpirationDuration(Duration.ofDays(1));
		cache.setExpireAfterWrite(true);
		cache.setRemovalListener(null);
		cache.setKeyExtractionFunction(ProducerEventRule::getGlobalId);

		cache.initialize();
		return cache;
	}

	// -----------------------------------------------------------------------
	// WORKER
	// -----------------------------------------------------------------------

	@Managed
	public MessagingWorker messagingWorker(ExpertContext<tribefire.extension.messaging.model.deployment.service.MessagingWorker> context) {
		tribefire.extension.messaging.model.deployment.service.MessagingWorker deployable = context.getDeployable();

		MessagingWorker bean = new MessagingWorker();
		bean.setDeployable(deployable);
		bean.setFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setModuleClassLoader(module.moduleClassLoader());
		bean.setRulesCache(consumerRuleCache());
		bean.setConnectorsCache(consumerCache());
		return bean;
	}

	@Managed
	public CortexCache<String, ConsumerMessagingConnector> consumerCache() {
		CortexCache<String, ConsumerMessagingConnector> cache = new CortexCache<>();
		cache.setExpirationDuration(Duration.ofDays(1));
		cache.setExpireAfterWrite(true);
		cache.setRemovalListener(null);
		cache.setKeyExtractionFunction(ConsumerMessagingConnector::getExternalId);

		cache.initialize();
		return cache;
	}

	@Managed
	public CortexCache<String, ConsumerEventRule> consumerRuleCache() {
		CortexCache<String, ConsumerEventRule> cache = new CortexCache<>();
		cache.setExpirationDuration(Duration.ofDays(1));
		cache.setExpireAfterWrite(true);
		cache.setRemovalListener(null);
		cache.setKeyExtractionFunction(ConsumerEventRule::getGlobalId);

		cache.initialize();
		return cache;
	}

	// -----------------------------------------------------------------------
	// TEST
	// -----------------------------------------------------------------------

	@Managed
	public TestReceiveMessagingProcessor testReceiveMessagingProcessor(
			ExpertContext<tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor> context) {
		tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor deployable = context.getDeployable();

		TestReceiveMessagingProcessor bean = new TestReceiveMessagingProcessor();
		bean.setDeployable(deployable);
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());

		return bean;
	}

	@Managed
	public TestUpdateObjectProcessor testUpdateObjectProcessor(
			ExpertContext<tribefire.extension.messaging.model.deployment.service.test.TestUpdateObjectProcessor> context) {
		tribefire.extension.messaging.model.deployment.service.test.TestUpdateObjectProcessor deployable = context.getDeployable();
		TestUpdateObjectProcessor processor = new TestUpdateObjectProcessor();
		processor.setFactory(tfPlatform.systemUserRelated().sessionFactory());
		return processor;
	}

	@Managed
	public TestGetObjectProcessor testGetObjectProcessor(
			ExpertContext<tribefire.extension.messaging.model.deployment.service.test.TestGetObjectProcessor> context) {
		tribefire.extension.messaging.model.deployment.service.test.TestGetObjectProcessor deployable = context.getDeployable();
		TestGetObjectProcessor processor = new TestGetObjectProcessor();
		processor.setFactory(tfPlatform.systemUserRelated().sessionFactory());
		return processor;
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Managed
	public HealthCheckProcessor healthCheckProcessor(
			@SuppressWarnings("unused") ExpertContext<tribefire.extension.messaging.model.deployment.service.HealthCheckProcessor> context) {

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		return bean;
	}

}
