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
package tribefire.extension.cache.test.wire.space;

import com.braintribe.codec.marshaller.json.JsonMarshaller;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.cache.model.deployment.service.cache2k.Cache2kCacheAspectConfiguration;
import tribefire.extension.cache.model.deployment.service.cache2k.SimpleConstantExpiration;
import tribefire.extension.cache.model.service.demo.CacheDemo;
import tribefire.extension.cache.service.CacheAspect;
import tribefire.extension.cache.service.CacheAspectAdminServiceProcessor;
import tribefire.extension.cache.service.CacheAspectInterface;
import tribefire.extension.cache.service.CacheDemoProcessor;
import tribefire.extension.cache.service.cache2k.Cache2kCacheAspect;
import tribefire.extension.cache.test.wire.contract.CacheTestContract;

@Managed
public class CacheTestSpace extends AbstractCacheTestSpace implements CacheTestContract {

	@Override
	protected void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.register(CacheDemo.T, cacheDemoProcessor());
		bean.registerInterceptor("test-aspect-interceptor").registerForType(CacheDemo.T, cacheAspect());

	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public CacheDemoProcessor cacheDemoProcessor() {
		CacheDemoProcessor bean = new CacheDemoProcessor();
		return bean;
	}

	@Managed
	public CacheAspect cacheAspect() {

		CacheAspectInterface expert = cache2kCacheAspect();

		tribefire.extension.cache.model.deployment.service.CacheAspect deployable = tribefire.extension.cache.model.deployment.service.CacheAspect.T
				.create();

		CacheAspect bean = new CacheAspect();
		bean.setDeployable(deployable);
		bean.setExpert(expert);
		bean.setMarshaller(new JsonMarshaller());

		return bean;
	}

	@Managed
	private Cache2kCacheAspect cache2kCacheAspect() {

		Cache2kCacheAspect bean = new Cache2kCacheAspect();
		bean.setCacheNameSupplier(() -> "test");

		SimpleConstantExpiration simpleConstantExpiration = SimpleConstantExpiration.T.create();

		Cache2kCacheAspectConfiguration configuration = Cache2kCacheAspectConfiguration.T.create();
		configuration.setExpiration(simpleConstantExpiration);
		bean.setConfiguration(configuration);

		return bean;
	}

	@Managed
	public CacheAspectAdminServiceProcessor cacheAspectLocalStatus() {

		TimeSpan multicastTimeout = TimeSpan.T.create();
		multicastTimeout.setUnit(TimeUnit.second);
		multicastTimeout.setValue(10);

		InstanceId multicastInstanceId = InstanceId.T.create();
		multicastInstanceId.setNodeId("testNodeId");
		multicastInstanceId.setApplicationId("testApplicationId");

		tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor deployable = tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor.T
				.create();

		CacheAspectAdminServiceProcessor bean = new CacheAspectAdminServiceProcessor<>();
		bean.setDeployable(deployable);
		// bean.setCortexSessionProvider(cortexSessionSupplier());
		// bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setMulticastInstanceId(multicastInstanceId);
		bean.setMulticastTimeout(multicastTimeout);

		return bean;
	}

}
