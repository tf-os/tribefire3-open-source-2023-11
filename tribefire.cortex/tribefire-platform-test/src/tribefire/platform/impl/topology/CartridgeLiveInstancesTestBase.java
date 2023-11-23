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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.braintribe.cartridge.common.api.topology.ApplicationLifecycleListenerContext;
import com.braintribe.cartridge.common.api.topology.ApplicationShutdownListener;
import com.braintribe.cartridge.common.api.topology.ApplicationStartupListener;
import com.braintribe.execution.CustomThreadFactory;
import com.braintribe.model.service.api.InstanceId;

public class CartridgeLiveInstancesTestBase {
	
	public static int NODES_PER_APP = 3;

	// ============= //
	// == Commons == //
	// ============= //

	protected void testCartridgeLiveInstances(int aliveAge, boolean withListeners, boolean unregisteringListeners, boolean restartingApps,
			boolean purgeWhenExpired, boolean noCleanup) throws Exception {

		if (aliveAge <= 0) {
			aliveAge = CartridgeLiveInstances.DEFAULT_ALIVE_AGE;
		}

		int heartbeatFrequency = aliveAge / 2;
		int maxHeartbeatAge = purgeWhenExpired ? 0 : aliveAge;
		int cleanupInterval = noCleanup ? -1 : aliveAge * 2;

		// the period for which we wait before asserting that instances are considered live
		int liveCheck = heartbeatFrequency + 100;

		// the period for which we wait before asserting that instances are considered dead
		int expirationCheck = aliveAge + 100;

		// the period for which we wait before asserting that the clean up task ran
		int cleanupCheck = cleanupInterval + cleanupInterval / 2;
		
		
		InstanceId currentInstanceId = InstanceId.T.create();
		currentInstanceId.setApplicationId("me");
		currentInstanceId.setNodeId("0");

		CartridgeLiveInstances cli = new CartridgeLiveInstances();
		cli.setEnabled(true);
		cli.setAliveAge(aliveAge);
		cli.setMaxHeartbeatAge(maxHeartbeatAge);
		cli.setCleanupInterval(cleanupInterval);
		cli.setCurrentInstanceId(currentInstanceId);

		TestListener listenerA = null;
		TestListener listenerB = null;
		if (withListeners) {
			cli.setExecutorService(listenerInvocationService());
			listenerA = new TestListener("alpha", unregisteringListeners);
			listenerB = new TestListener("beta", unregisteringListeners);
			cli.addListener(listenerA.applicationId, listenerA);
			cli.addListener(listenerB.applicationId, listenerB);
		}

		cli.postConstruct();

		try {

			Apps apps = new Apps(cli, heartbeatFrequency);
			apps.addApp("me", 1);
			apps.addApp("foo", NODES_PER_APP);
			apps.addApp("bar", NODES_PER_APP);
			apps.addApp("alpha", NODES_PER_APP);

			apps.assertAppsAlive(liveCheck);

			apps.stopAppNode("bar", NODES_PER_APP - 1);

			apps.assertAppsAlive(expirationCheck);

			if (listenerB != null)
				listenerB.assertListener(0, 0);

			apps.addApp("beta", NODES_PER_APP);

			apps.assertAppsAlive(liveCheck);

			if (listenerB != null)
				listenerB.assertListener(1, 0);

			apps.stopApp("alpha");

			apps.assertAppsAlive(expirationCheck);

			if (listenerA != null) {

				if (!noCleanup) {
					apps.assertAppsAlive(cleanupCheck);
				}

				// if cleanup is disabled, no dead listener will be invoked at all.
				int expectedDeadCallbacks = noCleanup ? 0 : 1;

				listenerA.assertListener(1, expectedDeadCallbacks); // fail with expected =1, actual 0 -> no dead callback 

			}

			if (restartingApps) {

				apps.addApp("alpha", NODES_PER_APP);

				apps.assertAppsAlive(liveCheck);

				if (listenerA != null) {

					// if the listener unregistered itself, the live listener won't be called twice
					// if the cleanup is disabled, the dead callback is never called therefore only 1 live callback
					// occurs.
					int expectedLiveCallbacks = noCleanup || unregisteringListeners ? 1 : 2;

					// if cleanup is disabled, no dead listener will be invoked at all.
					int expectedDeadCallbacks = noCleanup ? 0 : 1;

					if (!noCleanup) {
						apps.assertAppsAlive(cleanupCheck);
					}

					listenerA.assertListener(expectedLiveCallbacks, expectedDeadCallbacks);

				}

				apps.stopApp("alpha");

				apps.assertAppsAlive(expirationCheck);

				if (listenerA != null) {

					// if the listener unregistered itself, the live listener won't be called twice
					// if the cleanup is disabled, the dead callback is never called therefore only 1 live callback
					// occurs.
					int expectedLiveCallbacks = noCleanup || unregisteringListeners ? 1 : 2;

					// if cleanup is disabled, no dead listener will be invoked at all.
					// if the listener unregistered itself, the dead listener won't be called twice
					int expectedDeadCallbacks = noCleanup ? 0 : unregisteringListeners ? 1 : 2;

					if (!noCleanup) {
						apps.assertAppsAlive(cleanupCheck);
					}

					listenerA.assertListener(expectedLiveCallbacks, expectedDeadCallbacks);

				}

			}

			apps.stop();

		} finally {
			cli.preDestroy();
		}

	}

	static class Apps {

		Map<String, App> apps = new HashMap<>();
		CartridgeLiveInstances liveInstances;
		long heartbeatInitialDelay;
		long heartbeatPeriod;
		TimeUnit heartbeatUnit = TimeUnit.MILLISECONDS;

		Apps(CartridgeLiveInstances cli, long heartbeatPeriod) {
			this(cli, heartbeatPeriod, heartbeatPeriod);
		}
		
		Apps(CartridgeLiveInstances cli, long heartbeatInitialDelay, long heartbeatPeriod) {
			this(cli, heartbeatInitialDelay, heartbeatPeriod, TimeUnit.MILLISECONDS);
		}

		Apps(CartridgeLiveInstances cli, long heartbeatInitialDelay, long heartbeatPeriod, TimeUnit heartbeatUnit) {
			liveInstances = cli;
			this.heartbeatInitialDelay = heartbeatInitialDelay;
			this.heartbeatPeriod = heartbeatPeriod;
			this.heartbeatUnit = heartbeatUnit;
		}

		App addApp(String appId, int nodes) {
			App app = new App(liveInstances, appId, nodes, heartbeatInitialDelay, heartbeatPeriod, heartbeatUnit);
			apps.put(appId, app);
			return app;
		}

		void stop() {
			for (App app : apps.values()) {
				if (app != null) {
					app.stop();
				}
			}
			apps.clear();
		}

		void stopApp(String appId) {
			App app = apps.get(appId);
			if (app != null) {
				app.stop();
				apps.remove(appId);
			}
		}

		void stopAppNode(String appId, int nodeIx) {
			App app = apps.get(appId);
			if (app != null) {
				app.stop(nodeIx);
			}
		}

		void assertAppsAlive(int after) throws Exception {

			Thread.sleep(after);

			Set<String> actual = liveInstances.liveInstances();

			for (App app : apps.values()) {

				InstanceId appInstanceId = InstanceId.T.create();
				appInstanceId.setApplicationId(app.appId);

				Set<String> actualByApp = liveInstances.liveInstances(appInstanceId);
				
				boolean nodeCheck = true;

				for (int i = 0; i < app.appNodes.size(); i++) {
					
					AppNode appNode = app.appNodes.get(i);
					
					if (appNode != null && !appNode.stopped) {

						InstanceId nodeId = appNode.instanceId;
						String nodeIdString = nodeId.toString();

						Assert.assertTrue("Missing expected  " + appNode.instanceId + " from liveInstances() result: " + actual,
								actual.contains(nodeIdString));
						Assert.assertTrue(
								"Missing expected  " + appNode.instanceId + " from liveInstances(" + appInstanceId + ") result: " + actualByApp,
								actualByApp.contains(nodeIdString));
						
						if (nodeCheck) { // 1 node check per app

							Set<String> actualByNode = liveInstances.liveInstances(nodeId);

							Assert.assertTrue("Missing expected  " + appNode.instanceId + " from liveInstances(" + appNode.instanceId + ") result: "
									+ actualByNode, actualByNode.contains(nodeIdString));

							nodeCheck = false;

						}

					}
				}
			}

		}

	}

	static class App {

		List<AppNode> appNodes = new ArrayList<>();
		String appId;

		App(CartridgeLiveInstances liveInstances, String appId, int totalNodes, long initialDelay, long period, TimeUnit unit) {
			this.appId = appId;
			for (int i = 0; i < totalNodes; i++) {
				appNodes.add(i, new AppNode(liveInstances, appId, "" + i, initialDelay, period, unit));
			}
		}

		void stop(int nodeIx) {
			AppNode heartbeatSender = appNodes.get(nodeIx);
			if (heartbeatSender != null) {
				heartbeatSender.stop();
			}
		}

		void stop() {
			for (AppNode appNode : appNodes) {
				if (appNode != null) {
					appNode.stop();
				}
			}
		}

	}

	static class AppNode implements Runnable {

		private final CartridgeLiveInstances cli;
		private final InstanceId instanceId;
		private ScheduledExecutorService executor;
		private volatile boolean stopped = false;

		AppNode(CartridgeLiveInstances liveInstances, String applicationId, String nodeId, long initialDelay, long period, TimeUnit unit) {
			this.cli = liveInstances;
			instanceId = InstanceId.T.create();
			instanceId.setApplicationId(applicationId);
			instanceId.setNodeId(nodeId);
			run();
			executor = Executors.newScheduledThreadPool(1);
			executor.scheduleAtFixedRate(this, initialDelay, period, unit);
		}

		@Override
		public void run() {
			cli.accept(instanceId);
		}

		public void stop() {
			if (executor != null) {
				executor.shutdown();
				try {
					executor.awaitTermination(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					System.err.println(e + " thrown. Not terminated after 10 seconds: " + e.getMessage());
				}
				executor = null;
				stopped = true;
			}
		}

	}

	static class TestListener implements ApplicationStartupListener, ApplicationShutdownListener {

		protected String applicationId;
		protected boolean unregister = false;
		protected List<String> live = new ArrayList<>();
		protected List<String> dead = new ArrayList<>();

		public TestListener(String applicationId) {
			this.applicationId = applicationId;
		}

		public TestListener(String applicationId, boolean unregister) {
			this.applicationId = applicationId;
			this.unregister = unregister;
		}

		@Override
		public void onStartup(ApplicationLifecycleListenerContext context) {
			live.add(context.applicationId());
			if (unregister) {
				context.unsubscribe();
			}
		}
		
		@Override
		public void onShutdown(ApplicationLifecycleListenerContext context) {
			dead.add(context.applicationId());
			if (unregister) {
				context.unsubscribe();
			}
		}

		public void assertListener(int live, int dead) {
			Assert.assertEquals("LiveListener for " + applicationId + " was called  " + this.live.size() + " times instead of the expected " + live,
					live, this.live.size());
			Assert.assertEquals("DeadListener for " + applicationId + " was called  " + this.dead.size() + " times instead of the expected " + dead,
					dead, this.dead.size());
			for (String liveApp : this.live) {
				Assert.assertEquals("LiveListener for " + applicationId + " was called with wrong application id: " + liveApp, applicationId,
						liveApp);
			}
			for (String deadApp : this.dead) {
				Assert.assertEquals("DeadListener for " + applicationId + " was called with wrong application id: " + deadApp, applicationId,
						deadApp);
			}
		}


	}

	private ExecutorService listenerInvocationService() {

		// @formatter:off
		ThreadPoolExecutor threadPoolExecutor = 
				new ThreadPoolExecutor(
					20, 				// corePoolSize
					Integer.MAX_VALUE, 	// maxPoolSize
					60, 				// keepAliveTime
					TimeUnit.SECONDS, 	// keepAliveTimeUnit
					new LinkedBlockingQueue<>(Integer.MAX_VALUE),
					CustomThreadFactory
						.create()
						.namePrefix("tribefire.heartbeat.events[test]-")
				);
		// @formatter:on

		return threadPoolExecutor;
	}

}
