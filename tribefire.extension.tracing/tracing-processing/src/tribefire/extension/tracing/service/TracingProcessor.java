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
package tribefire.extension.tracing.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.time.TimeSpan;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import tribefire.extension.tracing.connector.api.TracingConnector;
import tribefire.extension.tracing.model.deployment.connector.JaegerInMemoryTracingConnector;
import tribefire.extension.tracing.model.deployment.service.DefaultAttribute;
import tribefire.extension.tracing.model.service.MulticastTracingRequest;
import tribefire.extension.tracing.model.service.TracingRequest;
import tribefire.extension.tracing.model.service.TracingResult;
import tribefire.extension.tracing.model.service.configuration.ConfigureTracing;
import tribefire.extension.tracing.model.service.configuration.ConfigureTracingResult;
import tribefire.extension.tracing.model.service.configuration.DisableTracing;
import tribefire.extension.tracing.model.service.configuration.DisableTracingResult;
import tribefire.extension.tracing.model.service.configuration.EnableTracing;
import tribefire.extension.tracing.model.service.configuration.EnableTracingResult;
import tribefire.extension.tracing.model.service.configuration.InitializeTracing;
import tribefire.extension.tracing.model.service.configuration.InitializeTracingResult;
import tribefire.extension.tracing.model.service.configuration.local.ConfigureTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.ConfigureTracingLocalResult;
import tribefire.extension.tracing.model.service.configuration.local.DisableTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.DisableTracingLocalResult;
import tribefire.extension.tracing.model.service.configuration.local.EnableTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.EnableTracingLocalResult;
import tribefire.extension.tracing.model.service.configuration.local.InitializeTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.InitializeTracingLocalResult;
import tribefire.extension.tracing.model.service.status.TracingStatus;
import tribefire.extension.tracing.model.service.status.TracingStatusResult;
import tribefire.extension.tracing.model.service.status.local.InMemoryTracingStatusResult;
import tribefire.extension.tracing.model.service.status.local.TracingStatusLocal;
import tribefire.extension.tracing.model.service.status.local.TracingStatusLocalResult;
import tribefire.extension.tracing.service.base.ResponseBuilder;

public class TracingProcessor extends AbstractDispatchingServiceProcessor<TracingRequest, TracingResult> {

	private static final Logger logger = Logger.getLogger(TracingProcessor.class);

	private tribefire.extension.tracing.model.deployment.service.TracingProcessor deployable;

	private InstanceId instanceId;

	private TracingConnector tracingConnector;
	private String tracingConnectorName;

	// -----------------------------------------------------------------------
	// DISPATCHING
	// -----------------------------------------------------------------------

	@Override
	protected void configureDispatching(DispatchConfiguration<TracingRequest, TracingResult> dispatching) {
		// -----------------------------
		// configuration
		// -----------------------------
		// local
		dispatching.register(EnableTracingLocal.T, this::enableTracingLocal);
		dispatching.register(DisableTracingLocal.T, this::disableTracingLocal);
		dispatching.register(ConfigureTracingLocal.T, this::configureTracingLocal);
		dispatching.register(InitializeTracingLocal.T, this::initializeTracingLocal);

		// multicast
		dispatching.register(EnableTracing.T, this::enableTracing);
		dispatching.register(DisableTracing.T, this::disableTracing);
		dispatching.register(ConfigureTracing.T, this::configureTracing);
		dispatching.register(InitializeTracing.T, this::initializeTracing);

		// -----------------------------
		// status
		// -----------------------------
		// local
		dispatching.register(TracingStatusLocal.T, this::tracingStatusLocal);

		// multicast
		dispatching.register(TracingStatus.T, this::tracingStatus);
	}

	// -----------------------------------------------------------------------
	// SERVICE METHODS - CONFIGURATION - LOCAL
	// -----------------------------------------------------------------------

	public EnableTracingLocalResult enableTracingLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext, EnableTracingLocal request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		tracingConnector.enableTracing(request.getEnableDuration());

		EnableTracingLocalResult result = EnableTracingLocalResult.T.create();
		return result;
	}

	public DisableTracingLocalResult disableTracingLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			DisableTracingLocal request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		tracingConnector.disableTracing();

		DisableTracingLocalResult result = DisableTracingLocalResult.T.create();
		return result;
	}

	public ConfigureTracingLocalResult configureTracingLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			ConfigureTracingLocal request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		// reset values
		if (request.getResetEntityTypeInclusions()) {
			tracingConnector.populateEntityTypeInclusions(new HashSet<>());
		}
		if (request.getResetEntityTypeHierarchyInclusions()) {
			tracingConnector.populateEntityTypeHierarchyInclusions(new HashSet<>());
		}
		if (request.getResetEntityTypeExclusions()) {
			tracingConnector.populateEntityTypeExclusions(new HashSet<>());
		}
		if (request.getResetEntityTypeHierarchyExclusions()) {
			tracingConnector.populateEntityTypeHierarchyExclusions(new HashSet<>());
		}
		if (request.getResetUserInclusions()) {
			tracingConnector.populateUserInclusions(new HashSet<>());
		}
		if (request.getResetUserExclusions()) {
			tracingConnector.populateUserExclusions(new HashSet<>());
		}
		if (request.getResetDefaultAttributes()) {
			tracingConnector.populateDefaultAttributes(new HashSet<>());
		}
		if (request.getResetCustomAttributes()) {
			tracingConnector.populateCustomAttributes(new HashMap<>());
		}
		if (request.getResetComponentName()) {
			tracingConnector.changeComponentName(null);
		}
		if (request.getResetAddAttributesFromNotificationsMessage()) {
			tracingConnector.changeAddAttributesFromNotificationsMessage(null);
		}
		if (request.getResetAddAttributesFromNotificationsDetailsMessage()) {
			tracingConnector.changeAddAttributesFromNotificationsDetailsMessage(null);
		}

		// set values
		Set<String> entityTypeInclusions = request.getEntityTypeInclusions();
		if (entityTypeInclusions != null) {
			tracingConnector.populateEntityTypeInclusions(entityTypeInclusions);
		}
		Set<String> entityTypeHierarchyInclusions = request.getEntityTypeHierarchyInclusions();
		if (entityTypeHierarchyInclusions != null) {
			tracingConnector.populateEntityTypeHierarchyInclusions(entityTypeHierarchyInclusions);
		}
		Set<String> entityTypeExclusions = request.getEntityTypeExclusions();
		if (entityTypeExclusions != null) {
			tracingConnector.populateEntityTypeExclusions(entityTypeExclusions);
		}
		Set<String> entityTypeHierarchyExclusions = request.getEntityTypeHierarchyExclusions();
		if (entityTypeHierarchyExclusions != null) {
			tracingConnector.populateEntityTypeHierarchyExclusions(entityTypeHierarchyExclusions);
		}
		Set<String> userInclusions = request.getUserInclusions();
		if (userInclusions != null) {
			tracingConnector.populateUserInclusions(userInclusions);
		}
		Set<String> userExclusions = request.getUserExclusions();
		if (userExclusions != null) {
			tracingConnector.populateUserExclusions(userExclusions);
		}

		String componentName = request.getComponentName();
		if (componentName != null) {
			tracingConnector.changeComponentName(componentName);
		}
		Set<DefaultAttribute> defaultAttributes = request.getDefaultAttributes();
		if (defaultAttributes != null) {
			tracingConnector.populateDefaultAttributes(defaultAttributes);
		}
		Map<String, String> customAttributes = request.getCustomAttributes();
		if (customAttributes != null) {
			tracingConnector.populateCustomAttributes(customAttributes);
		}
		Level addAttributesFromNotificationsMessage = request.getAddAttributesFromNotificationsMessage();
		if (addAttributesFromNotificationsMessage != null) {
			tracingConnector.changeAddAttributesFromNotificationsMessage(addAttributesFromNotificationsMessage);
		}
		Level addAttributesFromNotificationsDetailsMessage = request.getAddAttributesFromNotificationsDetailsMessage();
		if (addAttributesFromNotificationsDetailsMessage != null) {
			tracingConnector.changeAddAttributesFromNotificationsDetailsMessage(addAttributesFromNotificationsDetailsMessage);
		}

		ConfigureTracingLocalResult result = ConfigureTracingLocalResult.T.create();
		return result;
	}

	public InitializeTracingLocalResult initializeTracingLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			InitializeTracingLocal request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		tracingConnector.initialize();

		InitializeTracingLocalResult result = InitializeTracingLocalResult.T.create();
		return result;
	}

	// -----------------------------------------------------------------------
	// SERVICE METHODS - CONFIGURATION - MULTICAST
	// -----------------------------------------------------------------------

	public EnableTracingResult enableTracing(ServiceRequestContext requestContext, EnableTracing request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		TimeSpan enableDuration = request.getEnableDuration();

		EnableTracingResult result = EnableTracingResult.T.create();

		enrichMulticastResult(requestContext, request, EnableTracingLocal.T, (localRequest) -> {
			localRequest.setEnableDuration(enableDuration);
		}, (sender, localResult) -> {
			result.getResults().put(sender, (EnableTracingLocalResult) localResult);
		});

		return result;
	}

	public DisableTracingResult disableTracing(ServiceRequestContext requestContext, DisableTracing request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		DisableTracingResult result = DisableTracingResult.T.create();

		enrichMulticastResult(requestContext, request, DisableTracingLocal.T, null, (sender, localResult) -> {
			result.getResults().put(sender, (DisableTracingLocalResult) localResult);
		});

		return result;
	}

	public ConfigureTracingResult configureTracing(ServiceRequestContext requestContext, ConfigureTracing request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		ConfigureTracingResult result = ConfigureTracingResult.T.create();

		enrichMulticastResult(requestContext, request, ConfigureTracingLocal.T, null, (sender, localResult) -> {
			result.getResults().put(sender, (ConfigureTracingLocalResult) localResult);
		});

		return result;
	}

	public InitializeTracingResult initializeTracing(ServiceRequestContext requestContext, InitializeTracing request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		InitializeTracingResult result = InitializeTracingResult.T.create();

		enrichMulticastResult(requestContext, request, InitializeTracingLocal.T, null, (sender, localResult) -> {
			result.getResults().put(sender, (InitializeTracingLocalResult) localResult);
		});

		return result;
	}

	// -----------------------------------------------------------------------
	// SERVICE METHODS - STATUS - LOCAL
	// -----------------------------------------------------------------------

	public TracingStatusLocalResult tracingStatusLocal(@SuppressWarnings("unused") ServiceRequestContext requestContext, TracingStatusLocal request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		TracingStatusLocalResult result;
		if (deployable.getTracingConnector() instanceof JaegerInMemoryTracingConnector) {
			InMemorySpanExporter spanExporter = tracingConnector.spanExporter();

			List<SpanData> spans = spanExporter.getFinishedSpanItems();
			InMemoryTracingStatusResult _result = InMemoryTracingStatusResult.T.create();
			spans.forEach(spanData -> {
				tribefire.extension.tracing.model.service.status.local.Span _span = tribefire.extension.tracing.model.service.status.local.Span.T
						.create();
				
				_span.setDuration((spanData.getEndEpochNanos() - spanData.getStartEpochNanos()) / 1000);
				_span.setOperationName(spanData.getName());
				_span.setServiceName(spanData.getResource().getAttribute(AttributeKey.stringKey("service.name")));
				_span.setStart(spanData.getStartEpochNanos());
				_span.setAttributes(spanData.getAttributes().asMap().entrySet().stream()
						.collect(Collectors.toMap(e -> e.getKey().getKey(), Map.Entry::getValue)));
				_result.getSpans().add(_span);
				/*
				 * TODO: Replace this code with openTelemetry equivalent
				List<LogData> logs = null;//record.getLogs();
				if (!CommonTools.isEmpty(logs)) {
					logs.forEach(logData -> {
						tribefire.extension.tracing.model.service.status.local.LogData _logData = tribefire.extension.tracing.model.service.status.local.LogData.T
								.create();
						_span.getLogs().add(_logData);
						_logData.setMessage(logData.getMessage());
						_logData.setTime(logData.getTime());
					});
				}*/
			});
			result = _result;
		} else {
			result = TracingStatusLocalResult.T.create();
		}

		result.setTracingEnabled(tracingConnector.tracingEnabled());
		result.setName(tracingConnectorName);
		result.setConnectorConfiguration(deployable.getTracingConnector());
		result.setDisableTracingAt(tracingConnector.disableTracingAt());

		return result;
	}

	// -----------------------------------------------------------------------
	// SERVICE METHODS - STATUS
	// -----------------------------------------------------------------------

	public TracingStatusResult tracingStatus(ServiceRequestContext requestContext, TracingStatus request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		TracingStatusResult result = TracingStatusResult.T.create();

		enrichMulticastResult(requestContext, request, TracingStatusLocal.T, null, (sender, localResult) -> {
			result.getResults().put(sender, (TracingStatusLocalResult) localResult);
		});

		return result;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private <T extends TracingRequest> void enrichMulticastResult(ServiceRequestContext requestContext, MulticastTracingRequest request,
			EntityType<T> localRequestEntityType, Consumer<T> requestEnricher, BiConsumer<InstanceId, TracingResult> f) {
		T localRequest = localRequestEntityType.create();
		localRequest.setServiceId(request.getServiceId());
		localRequest.setSendNotifications(request.getSendNotifications());

		if (requestEnricher != null) {
			requestEnricher.accept(localRequest);
		}

		MulticastRequest mc = MulticastRequest.T.create();
		mc.setAddressee(instanceId);
		mc.setTimeout(request.getMaxDuration());
		mc.setServiceRequest(localRequest);

		EvalContext<? extends MulticastResponse> eval = mc.eval(requestContext.getEvaluator());
		MulticastResponse multicastResponse = eval.get();

		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
		for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {
			InstanceId sender = entry.getKey();
			String senderString = MulticastTools.getInstanceIdString(sender);
			ServiceResult serviceResult = entry.getValue();
			ResponseEnvelope responseEnvelope = serviceResult.asResponse();

			if (responseEnvelope != null) {

				TracingResult localResult = (TracingResult) responseEnvelope.getResult();

				f.accept(sender, localResult);

				logger.debug(() -> "Attached local result: '" + localResult + "' from: '" + sender + "'");
			} else {
				throw new IllegalStateException(
						"Could not get any information from sender: '" + senderString + "' serviceResult: '" + serviceResult + "'");
			}
		}
	}

	// -----------------------------------------------------------------------
	// FOR NOTIFICATIONS
	// -----------------------------------------------------------------------

	protected <T extends HasNotifications> ResponseBuilder<T> responseBuilder(EntityType<T> responseType, TracingRequest request) {

		return new ResponseBuilder<T>() {
			private List<Notification> localNotifications = new ArrayList<>();
			private boolean ignoreCollectedNotifications = false;
			private Consumer<T> enricher;
			private NotificationsBuilder notificationsBuilder = null;
			private List<Notification> notifications = new ArrayList<>();

			@Override
			public ResponseBuilder<T> notifications(Supplier<List<Notification>> notificationsSupplier) {
				notifications = notificationsSupplier.get();
				return this;
			}
			@Override
			public ResponseBuilder<T> notifications(Consumer<NotificationsBuilder> consumer) {
				this.notificationsBuilder = Notifications.build();
				consumer.accept(notificationsBuilder);
				return this;
			}

			@Override
			public ResponseBuilder<T> ignoreCollectedNotifications() {
				this.ignoreCollectedNotifications = true;
				return this;
			}

			@Override
			public ResponseBuilder<T> responseEnricher(Consumer<T> enricher) {
				this.enricher = enricher;
				return this;
			}

			@Override
			public T build() {

				T response = responseType.create();
				if (enricher != null) {
					this.enricher.accept(response);
				}
				if (request.getSendNotifications()) {
					response.setNotifications(localNotifications);
					if (!ignoreCollectedNotifications) {

						if (notificationsBuilder != null) {
							notifications.addAll(notificationsBuilder.list());
						}

						Collections.reverse(notifications);
						response.getNotifications().addAll(notifications);
					}
				}
				return response;
			}
		};

	}

	protected <T extends HasNotifications> T prepareSimpleNotification(EntityType<T> responseEntityType, TracingRequest request, Level level,
			String msg) {

		//@formatter:off
		T result = responseBuilder(responseEntityType, request)
				.notifications(builder -> 
					builder	
					.add()
						.message()
							.level(level)
							.message(msg)
						.close()
					.close()
				).build();
		//@formatter:on
		return result;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.tracing.model.deployment.service.TracingProcessor deployable) {
		this.deployable = deployable;
	}

	@Required
	@Configurable
	public void setInstanceId(InstanceId instanceId) {
		this.instanceId = instanceId;
	}

	@Configurable
	@Required
	public void setTracingConnector(TracingConnector tracingConnector) {
		this.tracingConnector = tracingConnector;
	}

	@Configurable
	@Required
	public void setTracingConnectorName(String tracingConnectorName) {
		this.tracingConnectorName = tracingConnectorName;
	}

}
