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

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.braintribe.cartridge.common.api.topology.ApplicationHeartbeatListener;
import com.braintribe.cartridge.common.api.topology.ApplicationLifecycleListener;
import com.braintribe.cartridge.common.api.topology.ApplicationShutdownListener;
import com.braintribe.cartridge.common.api.topology.ApplicationStartupListener;
import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.cartridge.common.api.topology.StandardApplicationLifecycleListenerContext;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.execution.NamedCallable;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.utils.date.NanoClock;

/**
 * <p>
 * An standard {@link LiveInstances} implementation, offering a set representing the peer master and extension cartridge
 * instances most likely to be up and running.
 * 
 */
public class CartridgeLiveInstances implements LiveInstances, Consumer<InstanceId>, LifecycleAware {

	// constants
	private static final Logger log = Logger.getLogger(CartridgeLiveInstances.class);
	public static final int DEFAULT_ALIVE_AGE = 30000; // by default, entries older than 30 seconds are considered expired
	public static final int DEFAULT_HEATBEAT_AGE = 30000; // by default, entries expired for more than 30 seconds are to be purged during cleanup
	public static final int DEFAULT_CLEANUP_INTERVAL = 120000; // by default, clean up minimum interval is 2 minutes

	// configurable
	private InstanceId currentInstanceId;
	private boolean enabled;
	private int aliveAge = DEFAULT_ALIVE_AGE;
	private int maxHeartbeatAge = DEFAULT_HEATBEAT_AGE;
	private int cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
	private ExecutorService executorService;

	// post initialized
	private Map<String, InstanceEntry> heartbeats = new ConcurrentHashMap<>();
	private Map<String, Long> applicationHeartbeats = new ConcurrentHashMap<>();
	private Map<String, Set<ApplicationStartupListener>> startupListeners = new ConcurrentHashMap<>();
	private Map<String, Set<ApplicationShutdownListener>> shutdownListeners = new ConcurrentHashMap<>();
	private Map<String, Set<ApplicationHeartbeatListener>> heartbeatListeners = new ConcurrentHashMap<>();
	private long nextCleanup;
	private Object cleanupMonitor = new Object();
	private volatile boolean shuttingDown = false;
	private String logPrefix = "Application";
	private String instanceIdString;
	private ConcurrentHashMap<String,Instant> heartbeatStartInstants = new ConcurrentHashMap<>();
	
	public CartridgeLiveInstances() {
	}

	@Required
	@Configurable
	public void setCurrentInstanceId(InstanceId currentInstanceId) {
		this.currentInstanceId = currentInstanceId;
		this.instanceIdString = currentInstanceId.toString();
	}

	@Configurable
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * <p>
	 * Sets, in milliseconds, the limit age to determine that an instance is alive.
	 */
	@Configurable
	public void setAliveAge(int aliveAge) {
		if (aliveAge <= 0) {
			throw new IllegalArgumentException("Alive age must be greater than 0. Invalid value: " + aliveAge);
		}
		this.aliveAge = aliveAge;
	}

	/**
	 * <p>
	 * Sets, in milliseconds, the limit age to determine that an instance entry is to be purged from the cache.
	 */
	@Configurable
	public void setMaxHeartbeatAge(int maxHeartbeatAge) {
		if (maxHeartbeatAge < 0) {
			throw new IllegalArgumentException("Max heartbeat age must be a positive integer. Invalid value: " + maxHeartbeatAge);
		}
		this.maxHeartbeatAge = maxHeartbeatAge;
	}

	/**
	 * <p>
	 * Sets, in milliseconds, the interval for purging entries from the cache entries which are expired since more than
	 * the {@link #maxHeartbeatAge}.
	 */
	@Configurable
	public void setCleanupInterval(int cleanupInterval) {
		if (cleanupInterval <= 0) {
			log.warn("Clean up was disabled based on configured clean up interval value: " + cleanupInterval);
			this.cleanupInterval = -1;
		} else {
			this.cleanupInterval = cleanupInterval;
		}
	}

	@Configurable
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void postConstruct() {
		
		if (!enabled) {
			heartbeats = null;
			applicationHeartbeats = null;
			startupListeners = null;
			shutdownListeners = null;
			return;
		}

		if (currentInstanceId == null) { // Meaningful exception in case IoC fails to enforce @Required
			throw new IllegalStateException("Insufficient configuration. currentInstanceId is required");
		}

		logPrefix = "Application " + currentInstanceId;
		
		scheduleNextCleanUp();
	}

	@Override
	public void preDestroy() {

		shuttingDown = true;

		shutdownExecutorService();

	}

	public void acceptCurrentInstance() {
		// add ourself to live instances, because we cannot wait for the first self-heartbeat
		// to do this job.
		accept(currentInstanceId);
	}
	
	public void addListener(String applicationId, ApplicationLifecycleListener listener) {

		if (startupListeners != null && listener instanceof ApplicationStartupListener) {
			addListener(applicationId, (ApplicationStartupListener)listener, startupListeners);
		}

		if (shutdownListeners != null && listener instanceof ApplicationShutdownListener) {
			addListener(applicationId, (ApplicationShutdownListener)listener, shutdownListeners);
		}

		if (heartbeatListeners != null && listener instanceof ApplicationHeartbeatListener) {
			addListener(applicationId, (ApplicationHeartbeatListener)listener, heartbeatListeners);
		}

	}

	private <T extends ApplicationLifecycleListener> void addListener(String applicationId, T listener, Map<String, Set<T>> listeneres) {
		
		Set<T> listeners = listeneres.computeIfAbsent(applicationId, k -> new LinkedHashSet<>());
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void accept(InstanceId heartbeatInstanceId) {

		if (!enabled) {
			return;
		}

		log.pushContext(instanceIdString);
		try {
			long now = System.currentTimeMillis();

			long expiration = now + aliveAge;

			InstanceEntry entry = new InstanceEntry(heartbeatInstanceId, expiration);

			heartbeats.put(entry.id, entry);

			Long previous = applicationHeartbeats.put(entry.applicationId, expiration);

			log.trace(() -> logPrefix + " stored heartbeat for " + heartbeatInstanceId + " received at " + now + ". Current applications: "+applicationHeartbeats);

			cleanUpIfNeeded(now);
			
			notifyHeartbeat(entry.applicationId, entry.nodeId);
			
			if (previous == null) {
				notifyStartup(entry.applicationId, entry.nodeId);
			}
			
		} finally {
			log.popContext();
		}
	}

	@Override
	public Set<String> liveInstances() {
		return liveInstances(null);
	}

	@Override
	public Set<String> liveInstances(InstanceId matching) {

		if (!enabled) {
			return null;
		}

		String applicationId = null, nodeId = null;

		long start = log.isTraceEnabled() ? System.currentTimeMillis() : 0;

		if (matching != null) {
			applicationId = matching.getApplicationId();
			nodeId = matching.getNodeId();
		}

		long now = System.currentTimeMillis();
		Set<String> liveInstances = new HashSet<>();

		// @formatter:off
		for (InstanceEntry entry : heartbeats.values()) {
			if ((entry.expiration > now || entry.id.equals(instanceIdString)) && 
					(
						applicationId == null || 
						applicationId.equals(entry.applicationId)
					) && (
						nodeId == null || 
						nodeId.equals(entry.nodeId)
					)
			) {
				liveInstances.add(entry.id);
			}
		}
		// @formatter:on

		log.trace(() -> logPrefix + " collected live instances" + (matching == null ? "" : " matching " + matching) + " in "
				+ (System.currentTimeMillis() - start) + " ms");

		return liveInstances;

	}

	@Override
	public Set<String> liveApplications() {

		if (!enabled) {
			return null;
		}

		long now = System.currentTimeMillis();

		Set<String> liveApplications = new HashSet<>();

		log.trace(() -> "Selecting live apps from application heartbeats: "+applicationHeartbeats);
		for (Entry<String, Long> entry : applicationHeartbeats.entrySet()) {
			if (entry.getValue() > now) {
				liveApplications.add(entry.getKey());
			} else {
				log.trace(() -> "Excluding entry "+entry.getKey()+" because it's expiry time "+entry.getValue()+" is later than "+now);
			}
		}

		return liveApplications;

	}

	public void cleanUpIfNeeded(long now) {
		if (mustCleanUp(now)) {
			long purgeLimit = now - maxHeartbeatAge;
			cleanUp(purgeLimit);
		}
	}

	protected void notifyStartup(String applicationId, String nodeId) {

		Objects.requireNonNull(applicationId, "applicationId");

		if (log.isDebugEnabled()) {
			if (applicationId.equals(currentInstanceId.getApplicationId())) {
				log.trace(() -> logPrefix + " acknowledged that itself is active");
			} else {
				log.debug(logPrefix + " acknowledged that the peer application " + applicationId + " has become active based on heartbeat from " + nodeId);
			}
		}

		if (startupListeners == null || startupListeners.isEmpty()) {
			return;
		}

		Set<ApplicationStartupListener> listeners = startupListeners.getOrDefault(applicationId, Collections.emptySet());
		Set<ApplicationStartupListener> generalListeners = startupListeners.getOrDefault("*", Collections.emptySet());

		if (listeners.isEmpty() && generalListeners.isEmpty()) {
			log.trace(() -> logPrefix + " acknowledged that " + applicationId + " is active, but there aren't startup listeners registered for it");
			return;
		}

		if (executorService == null) {
			notifyStartup(applicationId, nodeId, listeners);
			notifyStartup(applicationId, nodeId, generalListeners);
		} else if (!shuttingDown) {
			try {
				executorService.submit(new NamedCallable<Void>() {
					@Override
					public Void call() throws Exception {
						notifyStartup(applicationId, nodeId, listeners);
						notifyStartup(applicationId, nodeId, generalListeners);
						return null;
					}

					@Override
					public String getName() {
						return instanceIdString;
					}
				});
			} catch (Exception e) {
				log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
						"Failed to submit task for notifying '" + applicationId + "' startup listeners: " + e.getMessage(), e);
			}
		}


	}

	private void notifyStartup(String applicationId, String nodeId, Set<ApplicationStartupListener> listeners) {

		if (shuttingDown) {
			return;
		}

		try {

			synchronized (listeners) {

				if (listeners.isEmpty() || shuttingDown) {
					return;
				}

				log.trace(() -> logPrefix + " will invoke " + applicationId + " startup listeners");

				Iterator<ApplicationStartupListener> iterator = listeners.iterator();

				while (iterator.hasNext()) {

					if (shuttingDown) {
						return;
					}

					ApplicationStartupListener listener = iterator.next();

					StandardApplicationLifecycleListenerContext context = new StandardApplicationLifecycleListenerContext(applicationId, nodeId,
							(p) -> iterator.remove());

					if (!shuttingDown) {
						listener.onStartup(context);
						log.trace(() -> logPrefix + " invoked " + applicationId + " startup listener: " + listener);
					}

				}

			}

		} catch (Exception e) {
			log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
					"Notification of '" + applicationId + "' startup to listeners has failed: " + e.getMessage(), e);
		}

	}

	protected void notifyShutdown(String applicationId) {

		Objects.requireNonNull(applicationId, "applicationId");

		if (log.isDebugEnabled()) {
			if (applicationId.equals(currentInstanceId.getApplicationId())) {
				log.trace(() -> logPrefix + " acknowledged that itself is no longer active");
			} else {
				log.debug(logPrefix + " acknowledged that the peer application " + applicationId + " is no longer active");
			}
		}

		if (shutdownListeners == null || startupListeners.isEmpty()) {
			return;
		}

		Set<ApplicationShutdownListener> listeners = shutdownListeners.getOrDefault(applicationId, Collections.emptySet());
		Set<ApplicationShutdownListener> generalListeners = shutdownListeners.getOrDefault("*", Collections.emptySet());

		
		if (listeners.isEmpty() && generalListeners.isEmpty()) {
			log.trace(() -> logPrefix + " acknowledged that " + applicationId
					+ " is no longer active, but there aren't shutdown listeners registered for it");
			return;
		}
		
		if (executorService == null) {
			notifyShutdown(applicationId, listeners);
			notifyShutdown(applicationId, generalListeners);
		} else if (!shuttingDown) {
			try {
				executorService.submit(new NamedCallable<Void>() {
					@Override
					public Void call() throws Exception {
						notifyShutdown(applicationId, listeners);
						notifyShutdown(applicationId, generalListeners);
						return null;
					}

					@Override
					public String getName() {
						return instanceIdString;
					}
				});
			} catch (Exception e) {
				log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
						"Failed to submit task for notifying '" + applicationId + "' shutdown listeners: " + e.getMessage(), e);
			}
		}


	}

	private void notifyShutdown(String applicationId, Set<ApplicationShutdownListener> listeners) {

		if (shuttingDown) {
			return;
		}

		try {

			synchronized (listeners) {

				if (listeners.isEmpty() || shuttingDown) {
					return;
				}

				log.trace(() -> logPrefix + " will invoke " + applicationId + " shutdown listeners");

				Iterator<ApplicationShutdownListener> iterator = listeners.iterator();

				while (iterator.hasNext()) {

					if (shuttingDown) {
						return;
					}

					ApplicationShutdownListener listener = iterator.next();

					StandardApplicationLifecycleListenerContext context = new StandardApplicationLifecycleListenerContext(applicationId,
							(p) -> iterator.remove());

					if (!shuttingDown) {
						listener.onShutdown(context);
						log.trace(() -> logPrefix + " invoked " + applicationId + " shutdown listener: " + listener);
					}

				}

			}

		} catch (Exception e) {
			log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
					"Notification of '" + applicationId + "' startup to listeners has failed: " + e.getMessage(), e);
		}

	}
	
	protected void notifyHeartbeat(String applicationId, String nodeId) {

		Objects.requireNonNull(applicationId, "applicationId");

		if (log.isDebugEnabled()) {
			if (applicationId.equals(currentInstanceId.getApplicationId())) {
				log.trace(() -> logPrefix + " acknowledged that itself is active");
			} else {
				log.trace(logPrefix + " acknowledged that the peer application " + applicationId + " is active based on heartbeat from " + nodeId);
			}
		}

		heartbeatStartInstants.computeIfAbsent(applicationId, appId -> {
			log.trace(() -> "Got first heartbeat from "+appId);
			return NanoClock.INSTANCE.instant();	
		});

		if (heartbeatListeners == null || heartbeatListeners.isEmpty()) {
			return;
		}

		Set<ApplicationHeartbeatListener> listeners = heartbeatListeners.getOrDefault(applicationId, Collections.emptySet());
		Set<ApplicationHeartbeatListener> generalListeners = heartbeatListeners.getOrDefault("*", Collections.emptySet());

		if (listeners.isEmpty() && generalListeners.isEmpty()) {
			log.trace(() -> logPrefix + " acknowledged that " + applicationId + " is active, but there aren't heartbeat listeners registered for it");
			return;
		}

		if (executorService == null) {
			notifyHeartbeat(applicationId, nodeId, listeners);
			notifyHeartbeat(applicationId, nodeId, generalListeners);
		} else if (!shuttingDown) {
			try {					
				executorService.submit(new NamedCallable<Void>() {
					@Override
					public Void call() throws Exception {
						notifyHeartbeat(applicationId, nodeId, listeners);
						notifyHeartbeat(applicationId, nodeId, generalListeners);
						return null;
					}

					@Override
					public String getName() {
						return instanceIdString;
					}
				});
			} catch (Exception e) {
				log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
						"Failed to submit task for notifying '" + applicationId + "' heartbeat listeners: " + e.getMessage(), e);
			}
		}

	}

	private void notifyHeartbeat(String applicationId, String nodeId, Set<ApplicationHeartbeatListener> listeners) {

		if (shuttingDown) {
			return;
		}

		try {

			synchronized (listeners) {

				if (listeners.isEmpty() || shuttingDown) {
					return;
				}

				log.trace(() -> logPrefix + " will invoke " + applicationId + " heartbeat listeners");

				Iterator<ApplicationHeartbeatListener> iterator = listeners.iterator();

				while (iterator.hasNext()) {

					if (shuttingDown) {
						return;
					}

					ApplicationHeartbeatListener listener = iterator.next();

					StandardApplicationLifecycleListenerContext context = new StandardApplicationLifecycleListenerContext(applicationId, nodeId,
							(p) -> iterator.remove());

					if (!shuttingDown) {
						listener.onHeartbeat(context);
						log.trace(() -> logPrefix + " invoked " + applicationId + " heartbeat listener: " + listener);
					}

				}

			}

		} catch (Exception e) {
			log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
					"Notification of '" + applicationId + "' heartbeat to listeners has failed: " + e.getMessage(), e);
		}

	}

	

	private boolean mustCleanUp(long now) {
		if (enabled && cleanupInterval != -1 && heartbeats != null && nextCleanup < now) {
			synchronized (cleanupMonitor) {
				if (nextCleanup < now) {
					scheduleNextCleanUp();
					log.trace(() -> logPrefix + " must clean up stale heartbeats now " + now + ". Scheduled next cleanup for " + nextCleanup);
					return true;
				}
			}
		}
		return false;
	}

	private void cleanUp(long purgeLimit) {
		cleanUpApplicationStatus(purgeLimit);
		cleanUpHeartbeats(purgeLimit);
	}

	private void cleanUpApplicationStatus(long purgeLimit) {

		if (shutdownListeners == null || shutdownListeners.isEmpty()) {
			return;
		}

		Set<String> deadApplicationIds = new HashSet<>();

		try {
			Iterator<Entry<String, Long>> it = applicationHeartbeats.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Long> entry = it.next();
				
				String instanceId = entry.getKey();
				// do not remove ourself
				if (instanceId.equals(instanceIdString))
					continue;
				
				if (entry.getValue() < purgeLimit) {
					deadApplicationIds.add(instanceId);
					log.debug(() -> logPrefix + " will remove the status for " + instanceId + " since " + maxHeartbeatAge
							+ " ms has passed since this entry was last considered live.");
					it.remove();
				}
			}
		} catch (Exception e) {
			log.error(logPrefix + " failed to update application status", e);
		}

		if (!deadApplicationIds.isEmpty()) {
			for (String deadApplicationId : deadApplicationIds) {
				notifyShutdown(deadApplicationId);
			}
		}

	}

	private void cleanUpHeartbeats(long purgeLimit) {
		try {
			log.trace(() -> logPrefix + " starting heartbeat clean up task");
			Iterator<Entry<String, InstanceEntry>> it = heartbeats.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, InstanceEntry> entry = it.next();
				
				InstanceEntry instanceEntry = entry.getValue();
				// do not remove ourself
				if (instanceEntry.id.equals(instanceIdString))
					continue;
				
				if (instanceEntry.expiration < purgeLimit) {
					log.debug(() -> logPrefix + " will remove the entry for " + entry.getKey() + " since " + maxHeartbeatAge
							+ " ms has passed since this entry was last considered live.");
					it.remove();
				}
			}
			log.trace(() -> logPrefix + " completed clean up task in " + (System.currentTimeMillis() - purgeLimit) + " ms");
		} catch (Exception e) {
			log.error(logPrefix + " failed to purge stale instances entries", e);
		}
	}

	private void scheduleNextCleanUp() {
		nextCleanup = System.currentTimeMillis() + cleanupInterval;
	}

	private void shutdownExecutorService() {
		if (executorService != null) {
			try {
				executorService.shutdownNow();
				log.debug(() -> "Shut down executor service from " + instanceIdString);
			} catch (Exception e) {
				log.error("Failed to shutdown executor service from " + instanceIdString + ": " + e.getMessage(), e);
			}
		}
	}

	static class InstanceEntry {

		String id;
		String applicationId;
		String nodeId;
		long expiration;

		InstanceEntry(InstanceId instanceId, long expiresAt) {
			applicationId = instanceId.getApplicationId();
			nodeId = instanceId.getNodeId();
			id = applicationId + "@" + nodeId;
			this.expiration = expiresAt;
		}

		@Override
		public String toString() {
			return "[".concat(applicationId).concat(" @ ").concat(nodeId).concat(" /").concat(""+expiration).concat("]");
		}
	}

	public Instant getHeartbeatStartInstant(String applicationId) {
		return heartbeatStartInstants.get(applicationId);
	}

}
