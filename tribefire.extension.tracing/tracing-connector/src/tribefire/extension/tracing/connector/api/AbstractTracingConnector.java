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
package tribefire.extension.tracing.connector.api;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonMarshaller;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.common.attribute.common.impl.BasicUserInfo;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.lcd.CommonTools;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import tribefire.extension.tracing.model.deployment.service.CustomValueAttribute;
import tribefire.extension.tracing.model.deployment.service.DefaultAttribute;
import tribefire.extension.tracing.model.deployment.service.TracingProcessor;

public abstract class AbstractTracingConnector implements TracingConnector, LifecycleAware {

	private final static Logger logger = Logger.getLogger(AbstractTracingConnector.class);

	private Timer timer;

	private Set<DefaultAttribute> defaultAttributes;
	private Map<String, String> customAttributesRegistry;
	private Set<String> entityTypeInclusionsRegistry;
	private Set<String> entityTypeHierarchyInclusionsRegistry;
	private Set<String> entityTypeExclusionsRegistry;
	private Set<String> entityTypeHierarchyExclusionsRegistry;
	private Set<String> userInclusionsRegistry;
	private Set<String> userExclusionsRegistry;

	// TODO: is volatile necessary
	private volatile boolean tracingEnabled;
	private volatile Date disableTracingAt;
	private volatile String componentName;
	private volatile String tenant;
	private List<DefaultAttribute> defaultBeforeAttributes;
	private List<DefaultAttribute> defaultErrorAttributes;
	private List<DefaultAttribute> defaultAfterAttributes;
	private List<Pair<String, String>> customAttributes;
	private Set<String> entityTypeInclusions;
	private Set<String> entityTypeHierarchyInclusions;
	private Set<String> entityTypeExclusions;
	private Set<String> entityTypeHierarchyExclusions;
	private Set<String> userInclusions;
	private Set<String> userExclusions;

	// static information from the tf instance
	private InstanceId instanceId;
	private String hostAddressIPv4 = "unknown";
	private String hostAddressIPv6 = "unknown";

	private Marshaller marshaller;
	private GmSerializationOptions serializationOptions;
	protected String serviceName;
	private Level addAttributesFromNotificationsMessage;
	private Level addAttributesFromNotificationsDetailsMessage;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		InetAddress iPv4NetworkInterface = NetworkTools.getIPv4NetworkInterface();
		if (iPv4NetworkInterface != null) {
			hostAddressIPv4 = iPv4NetworkInterface.getHostAddress();
		}
		InetAddress iPv6NetworkInterface = NetworkTools.getIPv6NetworkInterface();
		if (iPv6NetworkInterface != null) {
			hostAddressIPv6 = iPv6NetworkInterface.getHostAddress();
		}

		marshaller = new JsonMarshaller();

		//@formatter:off
		serializationOptions = GmSerializationOptions.defaultOptions.derive()
			.stabilizeOrder(true)
			.writeAbsenceInformation(false)
			.writeEmptyProperties(true)
			.setOutputPrettiness(OutputPrettiness.high)
			.build();
		//@formatter:on

		// set default configuration
		defaultBeforeAttributes = new CopyOnWriteArrayList<>();
		defaultErrorAttributes = new CopyOnWriteArrayList<>();
		defaultAfterAttributes = new CopyOnWriteArrayList<>();
		customAttributes = new CopyOnWriteArrayList<>();
		entityTypeInclusions = new CopyOnWriteArraySet<>();
		entityTypeHierarchyInclusions = new CopyOnWriteArraySet<>();
		entityTypeExclusions = new CopyOnWriteArraySet<>();
		entityTypeHierarchyExclusions = new CopyOnWriteArraySet<>();
		userInclusions = new CopyOnWriteArraySet<>();
		userExclusions = new CopyOnWriteArraySet<>();

		populateDefaultAttributes(defaultAttributes);
		populateCustomAttributes(customAttributesRegistry);
		populateEntityTypeInclusions(entityTypeInclusionsRegistry);
		populateEntityTypeHierarchyInclusions(entityTypeHierarchyInclusionsRegistry);
		populateEntityTypeExclusions(entityTypeExclusionsRegistry);
		populateEntityTypeHierarchyExclusions(entityTypeHierarchyExclusionsRegistry);
		populateUserInclusions(userInclusionsRegistry);
		populateUserExclusions(userExclusionsRegistry);

		initialize();
	}

	@Override
	public void preDestroy() {
		closeTracer();
	}

	protected void closeTracer() {
		SdkTracerProvider tracerProvider = tracerProvider();
		if (tracerProvider != null) {
			String tracerType = tracerProvider.get(serviceName).getClass().getName();
			tracerProvider.close();
			logger.info(() -> "Closed existing tracer of type: '" + tracerType + "'");
		}
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	public CheckResultEntry health() {
		CheckResultEntry entry = actualHealth();
		return entry;
	}

	protected abstract CheckResultEntry actualHealth();

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public boolean tracingEnabled() {
		return tracingEnabled;
	}

	@Override
	public Date disableTracingAt() {
		return disableTracingAt;
	}

	@Override
	public boolean tracingActive(ServiceRequest request, ServiceRequestContext requestContext) {
		if (!tracingEnabled()) {
			return false;
		}

		String entityType = request.entityType().getTypeName();
		String user = requestContext.getRequestorUserName();

		if (!entityTypeInclusions.isEmpty() && entityTypeInclusions.contains(entityType)) {
			return true;
		}
		if (!entityTypeHierarchyInclusions.isEmpty()) {
			for (String entityTypeHierarchyInclusion : entityTypeHierarchyInclusions) {
				if (EntityTypes.get(entityTypeHierarchyInclusion).isAssignableFrom(request.entityType())) {
					return true;
				}
			}
		}
		if (!entityTypeExclusions.isEmpty() && entityTypeExclusions.contains(entityType)) {
			return false;
		}
		if (!entityTypeHierarchyExclusions.isEmpty()) {
			for (String entityTypeHierarchyExclusion : entityTypeHierarchyExclusions) {
				if (EntityTypes.get(entityTypeHierarchyExclusion).isAssignableFrom(request.entityType())) {
					return false;
				}
			}
		}
		if (!userInclusions.isEmpty() && userInclusions.contains(user)) {
			return true;
		}
		if (!userExclusions.isEmpty() && userExclusions.contains(user)) {
			return false;
		}

		// if inclusions are not empty and at this point it seems to be still enabling we disable it
		if (!entityTypeInclusions.isEmpty() || !entityTypeHierarchyInclusions.isEmpty()) {
			return false;
		}

		return true;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS FOR ADAPTING CONFIGURATION AT RUNTIME
	// -----------------------------------------------------------------------

	@Override
	public void enableTracing(TimeSpan enableDurationDuration) {
		synchronized (this) {
			initDisableTimer();
			this.tracingEnabled = true;
			if (enableDurationDuration != null) {
				long enableDurationMillis = enableDurationDuration.toLongMillies();
				disableTracingAt = Date.from(Instant.now().plusMillis(enableDurationMillis));

				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						disableTracing();
						logger.info(() -> "Disabled tracing after: '" + enableDurationMillis + "'ms");
					}
				};

				timer.schedule(task, enableDurationMillis);
			} else {
				disableTracingAt = null;
			}
		}
	}

	private void initDisableTimer() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		timer = new Timer(TracingProcessor.class.getName() + "_" + "DisableTracing", true);
	}

	@Override
	public void disableTracing() {
		synchronized (this) {
			this.tracingEnabled = false;
			disableTracingAt = null;
			logger.debug(() -> "Disabled tracing");
		}
	}

	@Override
	public synchronized void changeComponentName(String _componentName) {
		this.componentName = _componentName;
		logger.debug(() -> "Changed componentName: '" + this.componentName + "'");
	}

	@Override
	public synchronized void changeAddAttributesFromNotificationsMessage(Level _addAttributesFromNotificationsMessage) {
		this.addAttributesFromNotificationsMessage = _addAttributesFromNotificationsMessage;
		logger.debug(() -> "Changed addAttributesFromNotificationsMessage: '" + this.addAttributesFromNotificationsMessage + "'");
	}

	@Override
	public synchronized void changeAddAttributesFromNotificationsDetailsMessage(Level _addAttributesFromNotificationsDetailsMessage) {
		this.addAttributesFromNotificationsDetailsMessage = _addAttributesFromNotificationsDetailsMessage;
		logger.debug(() -> "Changed addAttributesFromNotificationsDetailsMessage: '" + this.addAttributesFromNotificationsDetailsMessage + "'");
	}

	@Override
	public synchronized void populateDefaultAttributes(Set<DefaultAttribute> _attributes) {
		defaultBeforeAttributes.clear();
		defaultAfterAttributes.clear();

		// until before/after attribute lists gets filled out the content is not fully filled ('inconsistent'). This is done
		// to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		_attributes.forEach(t -> {
			if (t.label().equals("BEFORE")) {
				defaultBeforeAttributes.add(t);
			} else if (t.label().equals("ERROR")) {
				defaultErrorAttributes.add(t);
			} else if (t.label().equals("AFTER")) {
				defaultAfterAttributes.add(t);
			} else {
				throw new IllegalArgumentException(
						"Got '" + DefaultAttribute.class.getName() + "' with value: '" + t.toString() + "' but this is not supported");
			}
		});
		logger.debug(() -> "Changed defaultBeforeAttributes: '" + this.defaultBeforeAttributes 
				+ "' defaultErrorAttributes: '" + defaultErrorAttributes
				+ "' defaultAfterAttributes: '" + defaultAfterAttributes + "'");
	}

	@Override
	public synchronized void populateCustomAttributes(Map<String, String> _customAttributesRegistry) {
		customAttributes.clear();

		// until before/after attribute lists gets filled out the content is not fully filled ('inconsistent'). This is done
		// to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		_customAttributesRegistry.forEach((k, v) -> {
			customAttributes.add(new Pair<>(k, v));
		});
		logger.debug(() -> "Changed customAttributes: '" + this.customAttributes + "'");
	}

	@Override
	public synchronized void populateEntityTypeInclusions(Set<String> _entityTypeInclusions) {
		entityTypeInclusions.clear();

		// until entityTypeInclusions gets filled out the content is not fully filled ('inconsistent'). This is done
		// to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		entityTypeInclusions.addAll(_entityTypeInclusions);
		logger.debug(() -> "Changed entityTypeInclusions: '" + this.entityTypeInclusions + "'");
	}

	@Override
	public synchronized void populateEntityTypeHierarchyInclusions(Set<String> _entityTypeHierarchyInclusions) {
		entityTypeHierarchyInclusions.clear();

		// until entityTypeHierarchyInclusions gets filled out the content is not fully filled ('inconsistent'). This is
		// done to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		entityTypeHierarchyInclusions.addAll(_entityTypeHierarchyInclusions);
		logger.debug(() -> "Changed entityTypeHierarchyInclusions: '" + this.entityTypeHierarchyInclusions + "'");
	}

	@Override
	public void populateEntityTypeExclusions(Set<String> _entityTypeExclusions) {
		entityTypeExclusions.clear();

		// until entityTypeExclusions gets filled out the content is not fully filled ('inconsistent'). This is done
		// to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		entityTypeExclusions.addAll(_entityTypeExclusions);
		logger.debug(() -> "Changed entityTypeExclusions: '" + this.entityTypeExclusions + "'");
	}

	@Override
	public void populateEntityTypeHierarchyExclusions(Set<String> _entityTypeHierarchyExclusions) {
		entityTypeHierarchyExclusions.clear();

		// until entityTypeHierarchyExclusions gets filled out the content is not fully filled ('inconsistent'). This is
		// done to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		entityTypeHierarchyExclusions.addAll(_entityTypeHierarchyExclusions);
		logger.debug(() -> "Changed entityTypeHierarchyExclusions: '" + this.entityTypeHierarchyExclusions + "'");
	}

	@Override
	public void populateUserInclusions(Set<String> _userInclusions) {
		userInclusions.clear();

		// until userInclusions gets filled out the content is not fully filled ('inconsistent'). This is done
		// to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		userInclusions.addAll(_userInclusions);
		logger.debug(() -> "Changed userInclusions: '" + this.userInclusions + "'");
	}

	@Override
	public void populateUserExclusions(Set<String> _userExclusions) {
		userExclusions.clear();

		// until userExclusions gets filled out the content is not fully filled ('inconsistent'). This is done
		// to avoid a collection with locks. It is optimized for reads because the writes are very rare - only on
		// reconfiguration

		userExclusions.addAll(_userExclusions);
		logger.debug(() -> "Changed userExclusions: '" + this.userExclusions + "'");
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	@Override
	public void addAttributesBeforeExecution(final SpanBuilder spanBuilder, final ServiceRequestContext requestContext, final String type) {
		addAttributesBeforeExecution(spanBuilder, null, requestContext, type);
	}

	@Override
	public void addAttributesBeforeExecution(final SpanBuilder spanBuilder, final ServiceRequest request, 
			final ServiceRequestContext requestContext, final String type) {

		String entityType = null;
		String partition = null;
		if (request != null) {
			entityType = request.entityType().getTypeName();
			partition = request.getPartition();
		}
		String sessionId = requestContext.getRequestorSessionId();
		String domainId = requestContext.getDomainId();
		String user = requestContext.getRequestorUserName();
		Optional<UserSession> userSessionOpt = requestContext.getAspect(UserSessionAspect.class);

		for (DefaultAttribute defaultAttribute : defaultBeforeAttributes) {
			switch (defaultAttribute) {
				case ATTRIBUTE_APPLICATION_ID:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_APPLICATION_ID.toString(), instanceId.getApplicationId());
					break;
				case ATTRIBUTE_DOMAIN_ID:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_DOMAIN_ID.toString(), domainId);
					break;
				case ATTRIBUTE_ENTITY_TYPE:
					if (entityType != null) {
						addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_ENTITY_TYPE.toString(), entityType);
					}
					break;
				case ATTRIBUTE_HOST_ADDRESS_IPV4:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_HOST_ADDRESS_IPV4.toString(), hostAddressIPv4);
					break;
				case ATTRIBUTE_HOST_ADDRESS_IPV6:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_HOST_ADDRESS_IPV6.toString(), hostAddressIPv6);
					break;
				case ATTRIBUTE_INSTANCE_ID:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_INSTANCE_ID.toString(), instanceId.toString());
					break;
				case ATTRIBUTE_NODE_ID:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_NODE_ID.toString(), instanceId.getNodeId());
					break;
				case ATTRIBUTE_PARTITION:
					if (partition != null) {
						addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_PARTITION.toString(), partition);
					}
					break;
				case ATTRIBUTE_REQUEST:
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					marshaller.marshall(baos, request, serializationOptions);
					String serializedRequest = new String(baos.toByteArray());
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_REQUEST.toString(), serializedRequest);
					break;
				case ATTRIBUTE_SESSION_ID:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_SESSION_ID.toString(), sessionId);
					break;
				case ATTRIBUTE_TIMESTAMP:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_TIMESTAMP.toString(), System.currentTimeMillis());
					break;
				case ATTRIBUTE_TIMESTAMP_ISO8601:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_TIMESTAMP_ISO8601.toString(),
							ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
					break;
				case ATTRIBUTE_TYPE:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_TYPE.toString(), type);
					break;
				case ATTRIBUTE_USER:
					addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_USER.toString(), user);
					break;
				case ATTRIBUTE_ROLES:
					if (userSessionOpt.isPresent()) {
						UserSession userSession = userSessionOpt.get();
						if (userSession.getEffectiveRoles() != null && !userSession.getEffectiveRoles().isEmpty()) {
							String roles = String.join(",", userSession.getEffectiveRoles());
							addAttribute(spanBuilder, DefaultAttribute.ATTRIBUTE_ROLES.toString(), roles);
						}
					}
					break;
				default:
					throw new IllegalArgumentException("Default before attribute: '" + defaultAttribute + "' not supported.");
			}
		}

		customAttributes.forEach(e -> {
			addAttribute(spanBuilder, e.first(), e.second());
		});

		if (!CommonTools.isEmpty(componentName)) {
			addAttribute(spanBuilder, CustomValueAttribute.ATTRIBUTE_COMPONENT_NAME.toString(), componentName);
		}
		if (!CommonTools.isEmpty(tenant)) {
			addAttribute(spanBuilder, CustomValueAttribute.ATTRIBUTE_TENANT.toString(), tenant);
		}
	}

	@Override
	public void addAttributesErrorExecution(final Span span, final Throwable t) {
		defaultErrorAttributes.forEach(defaultAttribute -> {
			switch (defaultAttribute) {
				case ATTRIBUTE_STACK:
					String exceptionAsString = Exceptions.stringify(t);
					span.setAttribute(DefaultAttribute.ATTRIBUTE_STACK.toString(), exceptionAsString);
					break;
				case ATTRIBUTE_ERROR:
					span.setAttribute(DefaultAttribute.ATTRIBUTE_ERROR.value(), true);
					break;
				default:
					throw new IllegalArgumentException("Default error attribute: '" + defaultAttribute + "' not supported.");
			}
		});
	}

	@Override
	public void addAttributesAfterExecution(Span span, Long duration, Long tracingOverhead) {
		addAttributesAfterExecution(span, null, duration, tracingOverhead);
	}

	@Override
	public void addAttributesAfterExecution(Span span, Object result, Long serviceDuration, Long tracingOverhead) {
		defaultAfterAttributes.forEach(defaultAttribute -> {
			switch (defaultAttribute) {
				case ATTRIBUTE_RESULT:
					if (result == null) {
						span.setAttribute(DefaultAttribute.ATTRIBUTE_RESULT.toString(), "[null]");
					} else {
						// TODO: needs to be double checked - looked that the marshaller can do everything
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						marshaller.marshall(baos, result, serializationOptions);
						String serializedResult = new String(baos.toByteArray());
						span.setAttribute(DefaultAttribute.ATTRIBUTE_RESULT.toString(), serializedResult);
					}
					break;
				case ATTRIBUTE_SERVICE_DURATION:
					span.setAttribute(DefaultAttribute.ATTRIBUTE_SERVICE_DURATION.toString(), serviceDuration);
					break;
				case ATTRIBUTE_TRACING_OVERHEAD:
					span.setAttribute(DefaultAttribute.ATTRIBUTE_TRACING_OVERHEAD.toString(), tracingOverhead);
					break;
				case ATTRIBUTE_NOTIFICATION_MESSAGE: {
					addNotificationAttribute(span, result, DefaultAttribute.ATTRIBUTE_NOTIFICATION_MESSAGE, n -> n.getMessage(), () -> addAttributesFromNotificationsMessage);
					break;
				}
				case ATTRIBUTE_NOTIFICATION_DETAIL_MESSAGE: {
					addNotificationAttribute(span, result, DefaultAttribute.ATTRIBUTE_NOTIFICATION_DETAIL_MESSAGE, n -> n.getMessage(),
							() -> addAttributesFromNotificationsDetailsMessage);
					break;
				}
				default:
					throw new IllegalArgumentException("Default after attribute: '" + defaultAttribute + "' not supported.");
			}
		});
	}

	private void addNotificationAttribute(Span span, Object result, DefaultAttribute attribute, Function<MessageNotification, String> f,
			Supplier<Level> levelProvider) {
		Level level = levelProvider.get();
		if (level != null) {

			int counter = 0;

			if (result instanceof HasNotifications) {
				HasNotifications hasNotifications = (HasNotifications) result;

				for (Notification notification : hasNotifications.getNotifications()) {
					if (notification instanceof MessageNotification) {
						MessageNotification messageNotification = (MessageNotification) notification;
						Level actualLevel = messageNotification.getLevel();

						int actualOrdinal = actualLevel.ordinal();
						int ordinal = level.ordinal();

						if (actualLevel == Level.ERROR) {
							span.setAttribute(DefaultAttribute.ATTRIBUTE_ERROR.value(), true);
						}

						if (actualOrdinal <= ordinal) {
							String msg = f.apply(messageNotification);

							if (!CommonTools.isEmpty(msg)) {
								span.setAttribute(attribute.toString() + "_" + counter, msg);
							}
						}

						counter++;
					}
				}
			} else {
				logger.trace(
						() -> "Result not type of 'HasNotifications' but of: '" + result.getClass().getName() + "' - skip adding attribute: '" + attribute + "'");
			}
		}
	}

	private void addAttribute(final SpanBuilder spanBuilder, final String key, final String value) {
		spanBuilder.setAttribute(key, value);
	}

	private void addAttribute(final SpanBuilder spanBuilder, final String key, final long value) {
		spanBuilder.setAttribute(key, value);
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setTracingEnabled(boolean tracingEnabled) {
		this.tracingEnabled = tracingEnabled;
	}

	@Configurable
	@Required
	public void setCustomAttributesRegistry(Map<String, String> customAttributesRegistry) {
		this.customAttributesRegistry = customAttributesRegistry;
	}

	@Configurable
	@Required
	public void setEntityTypeInclusionsRegistry(Set<String> entityTypeInclusionsRegistry) {
		this.entityTypeInclusionsRegistry = entityTypeInclusionsRegistry;
	}

	@Configurable
	@Required
	public void setEntityTypeHierarchyInclusionsRegistry(Set<String> entityTypeHierarchyInclusionsRegistry) {
		this.entityTypeHierarchyInclusionsRegistry = entityTypeHierarchyInclusionsRegistry;
	}

	@Configurable
	@Required
	public void setEntityTypeExclusionsRegistry(Set<String> entityTypeExclusionsRegistry) {
		this.entityTypeExclusionsRegistry = entityTypeExclusionsRegistry;
	}

	@Configurable
	@Required
	public void setEntityTypeHierarchyExclusionsRegistry(Set<String> entityTypeHierarchyExclusionsRegistry) {
		this.entityTypeHierarchyExclusionsRegistry = entityTypeHierarchyExclusionsRegistry;
	}

	@Configurable
	@Required
	public void setUserInclusionsRegistry(Set<String> userInclusionsRegistry) {
		this.userInclusionsRegistry = userInclusionsRegistry;
	}

	@Configurable
	@Required
	public void setUserExclusionsRegistry(Set<String> userExclusionsRegistry) {
		this.userExclusionsRegistry = userExclusionsRegistry;
	}

	@Configurable
	@Required
	public void setDefaultAttributes(Set<DefaultAttribute> defaultAttributes) {
		this.defaultAttributes = defaultAttributes;
	}

	@Configurable
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	@Configurable
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	@Configurable
	@Required
	public void setInstanceId(InstanceId instanceId) {
		this.instanceId = instanceId;
	}

	@Configurable
	@Required
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Configurable
	public void setAddAttributesFromNotificationsMessage(Level addAttributesFromNotificationsMessage) {
		this.addAttributesFromNotificationsMessage = addAttributesFromNotificationsMessage;
	}

	@Configurable
	public void setAddAttributesFromNotificationsDetailsMessage(Level addAttributesFromNotificationsDetailsMessage) {
		this.addAttributesFromNotificationsDetailsMessage = addAttributesFromNotificationsDetailsMessage;
	}
}
