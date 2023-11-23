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
package tribefire.extension.cache.integration.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;

import tribefire.extension.cache.model.service.admin.CacheStatusResult;
import tribefire.extension.cache.model.service.demo.CacheDemo;
import tribefire.extension.cache.model.status.ContextualizedCacheAspectStatus;
import tribefire.extension.cache.model.status.cache2k.Cache2kCacheAspectStatus;

public class SimpleCache2kCacheTest extends AbstractCacheTest {

	// -----------------------------------------------------------------------
	// TEST - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	@Override
	public void before() throws Exception {
		super.before();

		deleteAll();
		testCacheAspect();
		testCacheAdminServiceProcessor();
		testCacheDemoProcessor();
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testCacheStatus() {
		CacheStatusResult cacheStatus = cacheStatus();
		ContextualizedCacheAspectStatus statusBefore = cacheStatus.getStatusSet().iterator().next();
		assertThat(statusBefore).isNotNull();
		assertThat(statusBefore.getCacheAspectExternalId()).isEqualTo("cache.test.aspect");
		assertThat(statusBefore.getInstanceId()).isNotNull();
		assertThat(statusBefore.getStatus()).isNotNull();
		assertThat(Cache2kCacheAspectStatus.T.isAssignableFrom(statusBefore.getStatus().entityType()));
		assertThat(((Cache2kCacheAspectStatus) statusBefore.getStatus()).getName()).isEqualTo("TestCacheAspect_cache.test.aspect");
		assertThat(((Cache2kCacheAspectStatus) statusBefore.getStatus()).getSize()).isEqualTo(0);

		CacheDemo request = CacheDemo.T.create();
		request.eval(cortexSession).get();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
			return ((Cache2kCacheAspectStatus) cacheStatus().getStatusSet().iterator().next().getStatus()).getSize() == 1;
		});

	}

	@Test
	public void testCache1() {

		Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
			return ((Cache2kCacheAspectStatus) cacheStatus().getStatusSet().iterator().next().getStatus()).getSize() == 0;
		});

		CacheDemo request = CacheDemo.T.create();
		request.eval(cortexSession).get();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
			return ((Cache2kCacheAspectStatus) cacheStatus().getStatusSet().iterator().next().getStatus()).getSize() == 1;
		});

		request.eval(cortexSession).get();

		Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
			return ((Cache2kCacheAspectStatus) cacheStatus().getStatusSet().iterator().next().getStatus()).getSize() == 1;
		});
	}

}
