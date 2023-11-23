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
package tribefire.extension.cache.wire.space;

import java.util.HashMap;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.cache.model.deployment.service.CacheAspectConfiguration;
import tribefire.extension.cache.model.deployment.service.cache2k.Cache2kCacheAspectConfiguration;
import tribefire.extension.cache.model.status.CacheAspectStatus;
import tribefire.extension.cache.service.CacheAspect;
import tribefire.extension.cache.service.CacheAspectAdminServiceProcessor;
import tribefire.extension.cache.service.CacheAspectInterface;
import tribefire.extension.cache.service.CacheDemoProcessor;
import tribefire.extension.cache.service.cache2k.Cache2kCacheAspect;
import tribefire.module.wire.contract.PlatformReflectionContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 *
 */
@Managed
public class DeployablesSpace<T extends CacheAspectStatus> implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	@Import
	private PlatformReflectionContract platformReflection;

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public CacheDemoProcessor cacheDemoProcessor(ExpertContext<tribefire.extension.cache.model.deployment.service.demo.CacheDemoProcessor> context) {

		tribefire.extension.cache.model.deployment.service.demo.CacheDemoProcessor deployable = context.getDeployable();

		CacheDemoProcessor bean = new CacheDemoProcessor();
		return bean;
	}

	// -----------------------------------------------------------------------
	// ASPECT
	// -----------------------------------------------------------------------

	@Managed
	public CacheAspect cacheAspect(ExpertContext<tribefire.extension.cache.model.deployment.service.CacheAspect> context) {

		tribefire.extension.cache.model.deployment.service.CacheAspect deployable = context.getDeployable();

		HashMap<EntityType<? extends CacheAspectConfiguration>, CacheAspectInterface<? extends CacheAspectStatus>> expertRegistry = cacheExpertRegistry(
				context);

		CacheAspectConfiguration configuration = deployable.getConfiguration();
		CacheAspectInterface<? extends CacheAspectStatus> expert = expertRegistry.get(configuration.entityType());

		CacheAspect bean = new CacheAspect();
		bean.setDeployable(deployable);
		bean.setExpert(expert);
		bean.setMarshaller(tfPlatform.marshalling().jsonMarshaller());

		return bean;
	}

	@Managed
	private HashMap<EntityType<? extends CacheAspectConfiguration>, CacheAspectInterface<? extends CacheAspectStatus>> cacheExpertRegistry(
			ExpertContext<tribefire.extension.cache.model.deployment.service.CacheAspect> context) {

		HashMap<EntityType<? extends CacheAspectConfiguration>, CacheAspectInterface<? extends CacheAspectStatus>> expertRegistry = new HashMap<>();
		expertRegistry.put(Cache2kCacheAspectConfiguration.T, cache2kCacheAspect(context));

		return expertRegistry;
	}

	@Managed
	private Cache2kCacheAspect cache2kCacheAspect(ExpertContext<tribefire.extension.cache.model.deployment.service.CacheAspect> context) {

		tribefire.extension.cache.model.deployment.service.CacheAspect deployable = context.getDeployable();

		Cache2kCacheAspect bean = new Cache2kCacheAspect();
		bean.setCacheNameSupplier(() -> deployable.getName() + "_" + deployable.getExternalId());
		bean.setConfiguration((Cache2kCacheAspectConfiguration) deployable.getConfiguration());

		return bean;
	}

	@Managed
	public CacheAspectAdminServiceProcessor<T> cacheAspectLocalStatus(
			ExpertContext<tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor> context) {

		tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor deployable = context.getDeployable();

		TimeSpan multicastTimeout = TimeSpan.T.create();
		multicastTimeout.setUnit(TimeUnit.second);
		multicastTimeout.setValue(10);

		InstanceId multicastInstanceId = platformReflection.instanceId();

		CacheAspectAdminServiceProcessor<T> bean = new CacheAspectAdminServiceProcessor<>();
		bean.setDeployable(deployable);
		bean.setCortexSessionProvider(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setMulticastInstanceId(multicastInstanceId);
		bean.setMulticastTimeout(multicastTimeout);

		return bean;
	}

	// -----------------------------------------------------------------------
	// CUSTOM DEPLOYABLES
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

}
