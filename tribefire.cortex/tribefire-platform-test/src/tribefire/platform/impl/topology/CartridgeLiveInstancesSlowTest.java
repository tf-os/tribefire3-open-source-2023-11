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
package tribefire.platform.impl.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.service.api.InstanceId;
import com.braintribe.testing.category.VerySlow;

/**
 * {@link CartridgeLiveInstances} slow tests.
 * 
 */
@Category(VerySlow.class)
@Ignore
public class CartridgeLiveInstancesSlowTest extends CartridgeLiveInstancesTestBase  {

	@Test
	public void testSimple() throws Exception {

		InstanceId currentInstanceId = InstanceId.T.create();
		currentInstanceId.setApplicationId("me");
		currentInstanceId.setNodeId("0");
		
		int aliveAge = 8000;
		int heartbeatFrequency = aliveAge / 2;
		int maxHeartbeatAge = aliveAge + heartbeatFrequency;
		int cleanupInterval = aliveAge * 2;

		// the period for which we wait before asserting that instances are considered live
		int liveCheck = heartbeatFrequency + 100;

		// the period for which we wait before asserting that instances are considered dead
		int expirationCheck = aliveAge + 100;

		CartridgeLiveInstances cli = new CartridgeLiveInstances();
		cli.setEnabled(true);
		cli.setAliveAge(aliveAge);
		cli.setMaxHeartbeatAge(maxHeartbeatAge);
		cli.setCleanupInterval(cleanupInterval);
		cli.setCurrentInstanceId(currentInstanceId);

		cli.postConstruct();

		Apps apps = new Apps(cli, heartbeatFrequency);
		apps.addApp("foo", 5);
		apps.addApp("bar", 4);
		apps.addApp("boo", 2);
		apps.addApp("far", 1);

		apps.assertAppsAlive(liveCheck);

		apps.stopAppNode("bar", 1);

		apps.assertAppsAlive(expirationCheck);

		apps.addApp("for", 3);

		apps.assertAppsAlive(liveCheck);

		apps.stopApp("boo");

		apps.assertAppsAlive(expirationCheck);

		apps.stop();

		apps.assertAppsAlive(expirationCheck);

	}

	@Test
	public void testSimpleRestart() throws Exception {

		InstanceId currentInstanceId = InstanceId.T.create();
		currentInstanceId.setApplicationId("me");
		currentInstanceId.setNodeId("0");

		int aliveAge = 2000;
		int heartbeatFrequency = aliveAge / 2;
		int maxHeartbeatAge = aliveAge + heartbeatFrequency;
		int cleanupInterval = aliveAge * 2;

		// the period for which we wait before asserting that instances are considered live
		int liveCheck = heartbeatFrequency + 100;

		// the period for which we wait before asserting that instances are considered dead
		int expirationCheck = aliveAge + 100;

		// the period for which we wait before asserting that the clean up task ran and triggered
		// the onShutdown() on the apps for which all nodes expired
		int cleanupCheck = cleanupInterval * 3;

		CartridgeLiveInstances cli = new CartridgeLiveInstances();
		cli.setEnabled(true);
		cli.setAliveAge(aliveAge);
		cli.setMaxHeartbeatAge(maxHeartbeatAge);
		cli.setCleanupInterval(cleanupInterval);
		cli.setCurrentInstanceId(currentInstanceId);

		TestListener listenerA = new TestListener("a");
		TestListener listenerC = new TestListener("c");

		cli.addListener(listenerA.applicationId, listenerA);
		cli.addListener(listenerC.applicationId, listenerC);

		cli.postConstruct();

		Apps apps = new Apps(cli, heartbeatFrequency);
		apps.addApp("a", 30);
		apps.addApp("b", 30);

		apps.assertAppsAlive(liveCheck);

		listenerA.assertListener(1, 0);

		apps.stopAppNode("b", 1);
		apps.stopAppNode("b", 5);
		apps.stopAppNode("b", 10);

		apps.assertAppsAlive(expirationCheck);

		listenerC.assertListener(0, 0);

		apps.addApp("c", 3);

		apps.assertAppsAlive(liveCheck);

		listenerC.assertListener(1, 0);

		apps.stopApp("a");
		apps.stopApp("b");

		apps.assertAppsAlive(cleanupCheck);

		listenerA.assertListener(1, 1);

		apps.addApp("a", 30);

		apps.assertAppsAlive(liveCheck);
		
		listenerA.assertListener(2, 1);

		apps.stopApp("a");

		apps.assertAppsAlive(cleanupCheck);

		listenerA.assertListener(2, 2);
		listenerC.assertListener(1, 0);

		apps.stop();

		apps.assertAppsAlive(expirationCheck);

	}

	@Test @Ignore
	public void testCartridgeLiveInstancesParallel() throws Throwable {
		
		long start = System.currentTimeMillis();
		
		List<Callable<AssertionError>> tasks = new ArrayList<>();
		
		int[] ages = {CartridgeLiveInstances.DEFAULT_ALIVE_AGE, 15000, 2000};
		boolean[] yn = {true, false};

		for (int age : ages) {
			for (boolean withListeners : yn) {
				for (boolean unregisteringListeners : yn) {
					if (!withListeners && unregisteringListeners) {
						continue;
					}
					for (boolean restarting : yn) {
						for (boolean purgeExpired : yn) {
							for (boolean noCleanup : yn) {
								tasks.add(new Callable<AssertionError>() {
									@Override
									public AssertionError call() throws Exception {
										try {
											testCartridgeLiveInstances(age, withListeners, unregisteringListeners, restarting, purgeExpired,
													noCleanup);
										} catch (Exception e) {
											return new AssertionError("Test failed [alive age: " + age + "; with listeners: " + withListeners
													+ "; unregistering listeners: " + unregisteringListeners + "; restarting:  " + restarting + "; purging when expired: " + purgeExpired
													+ "; with no cleanup: " + noCleanup + " ] due to: " + e.getMessage(), e);
										}
										return null;
									}
								});
							}
						}
					}
				}
			}
		}

		int totalTests = tasks.size();

		System.out.println("Trigerring " + totalTests + " tests");

		ExecutorService service = Executors.newFixedThreadPool(totalTests);

		List<Future<AssertionError>> results = service.invokeAll(tasks);

		System.out.println("All " + totalTests + " tests completed with succes in " + (System.currentTimeMillis() - start));

		List<Throwable> failures = new ArrayList<>();
		
		for (Future<AssertionError> result : results) {
			AssertionError error = result.get();
			if (error != null) {
				failures.add(error);
			}
		}
		
		if (!failures.isEmpty()) {
			if (failures.size() == 1) {
				throw failures.iterator().next();
			} else {
				AssertionError error = new AssertionError(failures.size()+" of "+tasks.size()+" parallel tests failed.");
				failures.forEach(error::addSuppressed);
				throw error;
			}
		}

		System.out.println("All "+tasks.size()+" tests completed with success");

	}

	// ===========================================================================//
	// Node is considered dead if latest heartbeat is older than 30 seconds
	// (CartridgeLiveInstances.DEFAULT_ALIVE_AGE)
	// ===========================================================================//

	@Test @Ignore
	public void testDefaultHeartbeatAge() throws Exception {
		testCartridgeLiveInstances(-1, false, false, false, false, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeWithListeners() throws Exception {
		testCartridgeLiveInstances(-1, true, false, false, false, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(-1, true, true, false, false, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingApps() throws Exception {
		testCartridgeLiveInstances(-1, false, false, true, false, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithListeners() throws Exception {
		testCartridgeLiveInstances(-1, true, false, true, false, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(-1, true, true, true, false, false);
	}

	@Test @Ignore
	public void testDefaultHeartbeatAgePurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(-1, false, false, false, true, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(-1, true, false, false, true, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(-1, true, true, false, true, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(-1, false, false, true, true, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(-1, true, false, true, true, false);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(-1, true, true, true, true, false);
	}

	@Test @Ignore
	public void testDefaultHeartbeatAgeWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(-1, false, false, false, false, true);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(-1, true, false, false, false, true);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(-1, true, true, false, false, true);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(-1, false, false, true, false, true);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(-1, true, false, true, false, true);
	}
	
	@Test @Ignore
	public void testDefaultHeartbeatAgeRestartingAppsWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(-1, true, true, true, false, true);
	}

	// =====================================================================//
	// Node is considered dead if latest heartbeat is older than 15 seconds
	// =====================================================================//

	@Test @Ignore
	public void testLongLived() throws Exception {
		testCartridgeLiveInstances(60000, false, false, false, false, false);
	}
	
	@Test @Ignore
	public void testLongLivedWithListeners() throws Exception {
		testCartridgeLiveInstances(60000, true, false, false, false, false);
	}
	
	@Test @Ignore
	public void testLongLivedWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(60000, true, true, false, false, false);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingApps() throws Exception {
		testCartridgeLiveInstances(60000, false, false, true, false, false);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithListeners() throws Exception {
		testCartridgeLiveInstances(60000, true, false, true, false, false);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(60000, true, true, true, false, false);
	}

	@Test @Ignore
	public void testLongLivedPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(60000, false, false, false, true, false);
	}
	
	@Test @Ignore
	public void testLongLivedWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(60000, true, false, false, true, false);
	}
	
	@Test @Ignore
	public void testLongLivedWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(60000, true, true, false, true, false);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(60000, false, false, true, true, false);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(60000, true, false, true, true, false);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(60000, true, true, true, true, false);
	}

	@Test @Ignore
	public void testLongLivedWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(60000, false, false, false, false, true);
	}
	
	@Test @Ignore
	public void testLongLivedWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(60000, true, false, false, false, true);
	}
	
	@Test @Ignore
	public void testLongLivedWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(60000, true, true, false, false, true);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(60000, false, false, true, false, true);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(60000, true, false, true, false, true);
	}
	
	@Test @Ignore
	public void testLongLivedRestartingAppsWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(60000, true, true, true, false, true);
	}

	// =====================================================================//
	// Node is considered dead if latest heartbeat is older than 15 seconds
	// =====================================================================//

	@Test @Ignore
	public void testHalfLived() throws Exception {
		testCartridgeLiveInstances(15000, false, false, false, false, false);
	}
	
	@Test @Ignore
	public void testHalfLivedWithListeners() throws Exception {
		testCartridgeLiveInstances(15000, true, false, false, false, false);
	}
	
	@Test @Ignore
	public void testHalfLivedWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(15000, true, true, false, false, false);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingApps() throws Exception {
		testCartridgeLiveInstances(15000, false, false, true, false, false);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithListeners() throws Exception {
		testCartridgeLiveInstances(15000, true, false, true, false, false);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(15000, true, true, true, false, false);
	}

	@Test @Ignore
	public void testHalfLivedPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(15000, false, false, false, true, false);
	}
	
	@Test @Ignore
	public void testHalfLivedWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(15000, true, false, false, true, false);
	}
	
	@Test @Ignore
	public void testHalfLivedWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(15000, true, true, false, true, false);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(15000, false, false, true, true, false);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(15000, true, false, true, true, false);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(15000, true, true, true, true, false);
	}

	@Test @Ignore
	public void testHalfLivedWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(15000, false, false, false, false, true);
	}
	
	@Test @Ignore
	public void testHalfLivedWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(15000, true, false, false, false, true);
	}
	
	@Test @Ignore
	public void testHalfLivedWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(15000, true, true, false, false, true);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(15000, false, false, true, false, true);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(15000, true, false, true, false, true);
	}
	
	@Test @Ignore
	public void testHalfLivedRestartingAppsWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(15000, true, true, true, false, true);
	}

	// =====================================================================//
	// Node is considered dead if latest heartbeat is older than 2 seconds
	// =====================================================================//

	@Test @Ignore
	public void testShortLived() throws Exception {
		testCartridgeLiveInstances(2000, false, false, false, false, false);
	}
	
	@Test @Ignore
	public void testShortLivedWithListeners() throws Exception {
		testCartridgeLiveInstances(2000, true, false, false, false, false);
	}
	
	@Test @Ignore
	public void testShortLivedWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(2000, true, true, false, false, false);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingApps() throws Exception {
		testCartridgeLiveInstances(2000, false, false, true, false, false);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithListeners() throws Exception {
		testCartridgeLiveInstances(2000, true, false, true, false, false);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithUnregisteringListeners() throws Exception {
		testCartridgeLiveInstances(2000, true, true, true, false, false);
	}

	@Test @Ignore
	public void testShortLivedPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(2000, false, false, false, true, false);
	}
	
	@Test @Ignore
	public void testShortLivedWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(2000, true, false, false, true, false);
	}
	
	@Test @Ignore
	public void testShortLivedWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(2000, true, true, false, true, false);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(2000, false, false, true, true, false);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(2000, true, false, true, true, false);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithUnregisteringListenersPurgingWhenExpired() throws Exception {
		testCartridgeLiveInstances(2000, true, true, true, true, false);
	}

	@Test @Ignore
	public void testShortLivedWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(2000, false, false, false, false, true);
	}
	
	@Test @Ignore
	public void testShortLivedWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(2000, true, false, false, false, true);
	}
	
	@Test @Ignore
	public void testShortLivedWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(2000, true, true, false, false, true);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(2000, false, false, true, false, true);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(2000, true, false, true, false, true);
	}
	
	@Test @Ignore
	public void testShortLivedRestartingAppsWithUnregisteringListenersWithoutCleanup() throws Exception {
		testCartridgeLiveInstances(2000, true, true, true, false, true);
	}

}
