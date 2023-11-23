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

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * Manages the acknowledgement and broadcasting of heartbeats.
 * 
 */
public class HeartbeatManager implements MessageListener, Worker, DestructionAware {

	// constants
	private static final Logger log = Logger.getLogger(HeartbeatManager.class);

	// configurable
	private InstanceId currentInstanceId;
	private String topicName;
	private MessagingSessionProvider messagingSessionProvider;
	private boolean consumptionEnabled;
	private Consumer<InstanceId> heartbeatConsumer;
	private boolean broadcastingEnabled;
	private ScheduledExecutorService broadcastingService;
	private Long broadcastingInitialDelay = 0L;
	private Long broadcastingInterval;
	private java.util.concurrent.TimeUnit broadcastingIntervalUnit;
	private boolean doBroadcasting = false;
	private boolean autoHeartbeatBroadcastingStart = true;

	// post initialized
	private MessageProducer messageProducer;
	private volatile boolean shuttingDown = false;
	private String instanceIdString;

	private long heartbeatBroadcastingManuallyStarted;
	
	private Supplier<Boolean> availabilityChecker = () -> Boolean.TRUE;

	// lazy initialized
	private final LazyInitialized<HearbeatManagerMsg> msg = new LazyInitialized<>(HearbeatManagerMsg::new) ;

	private class HearbeatManagerMsg implements AutoCloseable {
		public final MessagingSession messagingSession;
		public final Topic topic;

		public HearbeatManagerMsg() {
			log.info(() -> "HeartbeatManager post-construct: consumptionEnabled:" + consumptionEnabled + //
					", broadcastingEnabled:" + broadcastingEnabled + //
					", currentInstanceId:" + currentInstanceId + //
					", messagingSessionProvider:" + messagingSessionProvider + //
					", topicName:" + topicName + //
					", heartbeatConsumer:" + heartbeatConsumer + //
					", broadcastingService:" + broadcastingService + //
					", broadcastingInterval:" + broadcastingInterval + //
					", broadcastingIntervalUnit:" + broadcastingIntervalUnit);

			if (!consumptionEnabled || !broadcastingEnabled) {
				log.warn("A no-op " + getClass().getName() + " instance was constructed. Neither consumption nor broadcasting is enabled for "
						+ HeartbeatManager.this);
				messagingSession = null;
				topic = null;
				return;
			}

			// Meaningful exception in case IoC fails to enforce @Required
			checkConfiguration(currentInstanceId, "currentInstanceId");
			checkConfiguration(messagingSessionProvider, "messagingSessionProvider");
			checkConfiguration(topicName, "topicName");

			if (consumptionEnabled && heartbeatConsumer == null)
				throw new IllegalStateException("Insufficient configuration. heartbeatConsumer is required when consumption is enabled");

			messagingSession = messagingSessionProvider.provideMessagingSession();
			topic = messagingSession.createTopic(HeartbeatManager.this.topicName);

			if (consumptionEnabled) {
				MessageConsumer messageConsumer = messagingSession.createMessageConsumer(topic);
				messageConsumer.setMessageListener(HeartbeatManager.this);
			}

			if (broadcastingEnabled && autoHeartbeatBroadcastingStart)
				_startHeartbeatBroadcasting();
		}

		private void checkConfiguration(Object component, String componentName) {
			if (component == null)
				throw new IllegalStateException("Insufficient configuration. " + componentName + " is required");
		}

		@Override
		public void close() throws Exception {
			shuttingDown = true;

			stopHeartbeatBroadcasting();

			if (messagingSession != null)
				try {
					messagingSession.close();
				} catch (Exception e) {
					log.error("Failed to close the messaging session", e);
				}
		}
	}	
	
	@Required
	public void setCurrentInstanceId(InstanceId currentInstanceId) {
		this.currentInstanceId = currentInstanceId;
		instanceIdString = currentInstanceId.toString();
	}

	@Required
	public void setTopicName(String topicName) {
		if (!StringTools.isEmpty(topicName))
			this.topicName = topicName;
	}

	@Required
	public void setMessagingSessionProvider(MessagingSessionProvider messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	/** If set to {@code true}, enables the consumption of heartbeats from other instances.	 */
	@Configurable
	public void setConsumptionEnabled(boolean consumptionEnabled) {
		this.consumptionEnabled = consumptionEnabled;
	}

	/** Required if consumption is enabled (see {@link #setConsumptionEnabled(boolean)}).*/
	@Configurable
	public void setHeartbeatConsumer(Consumer<InstanceId> heartbeatConsumer) {
		this.heartbeatConsumer = heartbeatConsumer;
	}

	/**If set to {@code true}, enables the broadcasting of heartbeats.*/
	@Configurable
	public void setBroadcastingEnabled(boolean broadcastingEnabled) {
		this.broadcastingEnabled = broadcastingEnabled;
	}

	/**Required if broadcasting is enabled (see {@link #setBroadcastingEnabled(boolean)}).*/
	@Configurable
	public void setBroadcastingService(ScheduledExecutorService broadcastingService) {
		this.broadcastingService = broadcastingService;
	}

	@Configurable
	public void setBroadcastingInitialDelay(Long broadcastingInitialDelay) {
		this.broadcastingInitialDelay = requireNonNull(broadcastingInitialDelay, "broadcastingInitialDelay cannot be set to null");
	}

	/**Required if broadcasting is enabled (see {@link #setBroadcastingEnabled(boolean)}).*/
	@Configurable
	public void setBroadcastingInterval(Long broadcastingInterval) {
		this.broadcastingInterval = broadcastingInterval;
	}

	/**Required if broadcasting is enabled (see {@link #setBroadcastingEnabled(boolean)}).*/
	@Configurable
	public void setBroadcastingIntervalUnit(java.util.concurrent.TimeUnit broadcastingIntervalUnit) {
		this.broadcastingIntervalUnit = broadcastingIntervalUnit;
	}

	/**If set to {@code true}, the heartbeat broadcasting starts automatically. Default is set to {@code true}.*/
	@Configurable
	public void setAutoHeartbeatBroadcastingStart(boolean autoHeartbeatBroadcastingStart) {
		this.autoHeartbeatBroadcastingStart = autoHeartbeatBroadcastingStart;
	}

	@Configurable
	public void setAvailabilityChecker(Supplier<Boolean> availabilityChecker) {
		this.availabilityChecker = availabilityChecker;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		msg.get();
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		msg.close();
	}

	@Override
 	public void preDestroy() {
		msg.close();
	}


	@Override
	public void onMessage(Message message) throws MessagingException {
		Object body = message.getBody();

		log.pushContext(instanceIdString);
		try {
			consumeBody(body);
			
		} finally {
			log.popContext();
		}
	}

	private void consumeBody(Object body) {
		if (body instanceof InstanceId)
			consumeInstanceIdBody((InstanceId) body);
		else
			log.warn(() -> "Unexpected message body consumed from " + topicName + ": " + body);
	}

	private void consumeInstanceIdBody(InstanceId instanceId) {
		try {
			heartbeatConsumer.accept(instanceId);
		} catch (Exception e) {
			log.error("Failed to consume heartbeat from [" + instanceId + "]", e);
		}
	}

	private Message createHeartbeatMessage() throws MessagingException {
		HearbeatManagerMsg hbMsg = msg.get();

		Message message = hbMsg.messagingSession.createMessage();
		message.setDestination(hbMsg.topic);
		message.setBody(currentInstanceId);
		message.setTimeToLive(timeToLiveInMillis());

		return message;
	}

	private long timeToLiveInMillis() {
		return 2 * TimeUnit.MILLISECONDS.convert(broadcastingInterval, broadcastingIntervalUnit);
	}

	/**Provides possibility to manually trigger the broadcasting also from outside */
	public void startHeartbeatBroadcasting() {
		heartbeatBroadcastingManuallyStarted = System.currentTimeMillis();
		_startHeartbeatBroadcasting();
	}
	
	private void _startHeartbeatBroadcasting() {
		messageProducer = msg.get().messagingSession.createMessageProducer();
		scheduleHeartbeatBroadcasting();
	}
	
	/**Submits or resubmits the task responsible for broadcasting heartbeats periodically.*/
	private void scheduleHeartbeatBroadcasting() {
		if (!broadcastingEnabled) {
			log.warn(() -> "Suppressed the heartbeat broadcasting task start-up as the broadcastingEnabled is set to " + broadcastingEnabled);
			return;
		}

		checkBroadcastingConfiguration(broadcastingService, "broadcastingService");
		checkBroadcastingConfiguration(broadcastingInterval, "broadcastingInterval");
		checkBroadcastingConfiguration(broadcastingIntervalUnit, "broadcastingIntervalUnit");

		try {
			if (broadcastingService.isShutdown()) {
				log.warn(() -> "Suppressed the heartbeat broadcasting task start-up as the executor is shutting down");
				return;
			}

			// @formatter:off
			broadcastingService.scheduleWithFixedDelay(
					this::broadcastHeartbeat, 
					broadcastingInitialDelay, 
					broadcastingInterval,
					broadcastingIntervalUnit
				);
			// @formatter:on

			log.info(() -> "Scheduled " + instanceIdString + " heartbeat broadcasting task");
			log.debug(() -> "Scheduled " + instanceIdString + " heartbeat broadcasting task to run every " + broadcastingInterval + " "
					+ broadcastingIntervalUnit.toString().toLowerCase());

		} catch (Exception e) {
			log.error("Failed to schedule heartbeat broadcasting task: " + e.getMessage(), e);
		}
	}

	private void checkBroadcastingConfiguration(Object component, String componentName) {
		if (component == null)
			throw new IllegalStateException("Insufficient configuration. broadcasting is enabled but no " + componentName + " was set");
	}

	private void stopHeartbeatBroadcasting() {
		if (broadcastingService != null) {
			try {
				broadcastingService.shutdownNow();
				log.debug(() -> "Shut down heartbeat broadcasting service from " + instanceIdString);
			} catch (Exception e) {
				log.error("Failed to shutdown heartbeat broadcasting service from " + instanceIdString + ": " + e.getMessage(), e);
			}
		}
	}

	private void broadcastHeartbeat() {
		if (!doBroadcasting) {
			boolean initializationCompleted = initializationCompleted();
			if (!initializationCompleted) {
				log.trace(() -> "The heartbeat will be postponed until the initialization has been completed.");
				return;
			}

			boolean available = checkAvailability();
			if (!available) {
				log.trace(() -> "The heartbeat will be postponed until the availability has been established.");
				return;
			}
			
			doBroadcasting = true;
			
			log.debug(() -> "Beginning to broadcast heartbeat messages now.");
		}
		
		try {
			log.trace(() -> "Heartbeat broadcasting for '" + instanceIdString + "' now. Interval: " + broadcastingInterval + " "
					+ broadcastingIntervalUnit.toString().toLowerCase());
			Message message = createHeartbeatMessage();
			if (!shuttingDown) {
				messageProducer.sendMessage(message, message.getDestination());
				log.trace(() -> "Heartbeat broadcasted for '" + instanceIdString + "'");
			}
		} catch (Throwable e) {
			log.log(shuttingDown ? LogLevel.TRACE : LogLevel.ERROR,
					"Heartbeat broadcasting for '" + instanceIdString + "' has failed: " + e.getMessage(), e);
		}
	}

	private boolean checkAvailability() {
		
		String initializationCompleted = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_STARTUP_STATE);
		if (initializationCompleted == null) {
			log.trace(() -> "There is no agent that defines the property "+TribefireRuntime.ENVIRONMENT_STARTUP_STATE);
			return availabilityChecker.get();
		}
		long statusTimestamp = 0;
		int index = initializationCompleted.indexOf("-");
		if (index > 0) {
			try {
				statusTimestamp = Long.parseLong(initializationCompleted.substring(index+1));
			} catch(NumberFormatException nfe) {
				log.debug(() -> "Unexpected timestamp value in "+initializationCompleted, nfe);
			}
		}
		Date statusDate = new Date(statusTimestamp);
		
		if (initializationCompleted.startsWith("pending-")) {
			log.debug(() -> "Startup in progress since "+statusDate);
			return false;
		} else if (initializationCompleted.startsWith("started-")) {
			log.debug(() -> "Startup reportedly finshed at "+statusDate);
			return true;			
		} else {
			log.debug(() -> "Startup status unknown: "+initializationCompleted);
			return true;
		}
	}

	private boolean initializationCompleted() {
		long startupTime = 0L;

		if (autoHeartbeatBroadcastingStart) {
			String initializationCompleted = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_INITIALIZATION_COMPLETED);
			if (StringTools.isBlank(initializationCompleted)) {
				log.trace(() -> "Postponing the heartbeat broadcast until "+TribefireRuntime.ENVIRONMENT_INITIALIZATION_COMPLETED+" is available.");
				return false;
			}
			try {
				startupTime = Long.parseLong(initializationCompleted);
			} catch(NumberFormatException nfe) {
				throw new RuntimeException("The value "+initializationCompleted+" for "+TribefireRuntime.ENVIRONMENT_INITIALIZATION_COMPLETED+" is not valid. Expected a number.", nfe);
			}
			
		} else {
			startupTime = heartbeatBroadcastingManuallyStarted;
		}
		
		long msSinceStartup = -1;
		
		msSinceStartup = System.currentTimeMillis() - startupTime;
		long initialDelayInMs = broadcastingIntervalUnit.toMillis(broadcastingInitialDelay);

		if (msSinceStartup < initialDelayInMs) {
			log.trace(() -> "Postponing the heartbeat broadcast until the initial delay of "+initialDelayInMs+" ms has passed.");
			return false;				
		}
		
		return true;
	}

}
