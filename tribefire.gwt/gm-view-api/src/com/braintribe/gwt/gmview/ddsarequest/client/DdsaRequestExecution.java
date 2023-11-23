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
package com.braintribe.gwt.gmview.ddsarequest.client;

import java.util.Collection;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gmrpc.api.client.user.ResourceSupport;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener.TabInformation;
import com.braintribe.gwt.gmview.ddsarequest.client.confirmation.ConfirmationExpert;
import com.braintribe.gwt.gmview.ddsarequest.client.message.MessageExpert;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.NotificationBarEventSource;
import com.braintribe.model.processing.notification.api.NotificationAwareEvalContext;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.service.EnvelopeSessionAspect;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.widget.core.client.Component;

/**
 * Expert responsible for executing a call to a DDSA Service, and handling the response accordingly.
 * 
 * @author michel.docouto
 *
 */
public class DdsaRequestExecution {

	protected static final Logger logger = new Logger(DdsaRequestExecution.class);
	private static boolean executionFinished;

	/**
	 * Executes the {@link ServiceRequest} with the given executionData.
	 */
	public static <T> Future<T> executeRequest(RequestExecutionData data) {
		Future<T> future = new Future<>();

		ConfirmationExpert.checkConfirmation(data) //
				.andThen(checkResult -> {
					if (!checkResult) {
						future.onFailure(new ConfirmationFailedException());
						return;
					}

					execute(data).andThen(result -> future.onSuccess((T) result)).onError(future::onFailure);
				}).onError(future::onFailure);

		return future;
	}

	public static Future<Object> execute(RequestExecutionData data) {
		Future<Object> future = new Future<>();

		if (data.serviceRequest.getMetaData() == null)
			data.serviceRequest.setMetaData(UrlParameters.getRequestMetaParameters());
		else
			data.serviceRequest.getMetaData().putAll(UrlParameters.getRequestMetaParameters());
		
		EvalContext<?> evalContext = data.serviceRequest.eval(data.dataSession).with(ResourceSupport.class, true);
		if (data.sendTransientEnvelope) {
			TransientPersistenceGmSession individualTransientSession = data.transientSessionProvider.get();
			ModelAccessory modelAccessory = data.transientSession.getModelAccessory();
			if (modelAccessory != null)
				individualTransientSession.setModelAccessory(modelAccessory);
			
			evalContext = evalContext.with(EnvelopeSessionAspect.class, individualTransientSession);
		}

		NotificationAwareEvalContext<?> notificationEvalContext = Notifications.makeNotificationAware(evalContext);
		
		executionFinished = false;
		if (data.navigationListener instanceof Component) {
			if (!data.handlingPaging) {
				GlobalState.mask();
				new Timer() {
					@Override
					public void run() {
						if (!executionFinished) {
							GlobalState.unmask();
							MessageExpert.showMessage(data);
						}
					}
				}.schedule(150);
			}
		}

		notificationEvalContext.get(AsyncCallback.of(result -> {
			executionFinished = true;
			if (data.navigationListener instanceof Component && !data.handlingPaging) {
				GlobalState.unmask();
				MessageExpert.hideMessage();
			}

			if (result == null) {
				GlobalState.showSuccess(LocalizedText.INSTANCE.requestExecutedWithoutResponse());
				future.onSuccess(result);
				return;
			}

			if (result instanceof HasNotifications && data.notificationFactorySupplier != null) {
				HasNotifications hn = (HasNotifications) result;
				if (!hn.getNotifications().isEmpty()) {
					handleNotifications((HasNotifications) result, data.notificationFactorySupplier, future);
					
					if (!data.navigateEvenIfHasNotification)
						return;
				}
			}

			navigateToData(result, data.navigationListener, data.handlingPaging);
			future.onSuccess(data.navigationListener == null ? result : null);
		}, e -> {
			executionFinished = true;
			logger.error(e);
			if (data.navigationListener instanceof Component) {
				GlobalState.unmask();
				MessageExpert.hideMessage();
			}

			com.braintribe.model.notification.Notifications notifications = notificationEvalContext.getReceivedNotifications();
			if (notifications != null && data.notificationFactorySupplier != null) {
				handleNotifications(notifications, data.notificationFactorySupplier, future);
				return;
			}

			ErrorDialog.show("Error while executing the selected service request.", e);
			e.printStackTrace();
			future.onSuccess(false);
		}));

		return future;
	}

	private static void handleNotifications(HasNotifications notifications, Supplier<? extends NotificationFactory> notificationFactorySupplier,
			Future<Object> future) {
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		notificationFactory.broadcast(notifications.getNotifications(), notificationFactory.createEventSource(NotificationBarEventSource.T));
		future.onSuccess(notifications);
	}

	private static void navigateToData(Object data, ModelPathNavigationListener navigationListener, boolean handlingPaging) {
		if (navigationListener == null)
			return;

		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(getGenericModelType(data), data));
		
		TabInformation tabInformation = new TabInformation() {
			@Override
			public String getTabName() {
				return LocalizedText.INSTANCE.executionResponse();
			}

			@Override
			public ImageResource getTabIcon() {
				return null;
			}

			@Override
			public String getTabDescription() {
				return null;
			}
		};
		
		if (handlingPaging)
			navigationListener.onAddModelPath(modelPath);
		else
			navigationListener.onOpenModelPath(modelPath, tabInformation);
	}

	/**
	 * In case of collections, this will return the type correctly when every entry have the same type.
	 */
	private static GenericModelType getGenericModelType(Object data) {
		GenericModelType type = GMF.getTypeReflection().getType(data);
		if (!type.isCollection() || !(data instanceof Collection))
			return type;

		Collection<Object> collection = (Collection<Object>) data;
		EntityType<?> collectionEntityType = null;
		for (Object element : collection) {
			GenericModelType elementType = GMF.getTypeReflection().getType(element);
			if (!elementType.isEntity())
				return type;

			if (collectionEntityType != null && collectionEntityType != elementType)
				return type;

			if (collectionEntityType == null)
				collectionEntityType = (EntityType<?>) elementType;
		}

		if (collectionEntityType == null)
			return type;

		CollectionType collectionType = (CollectionType) type;
		if (collectionType.getCollectionKind().equals(CollectionKind.list))
			return GMF.getTypeReflection().getListType(collectionEntityType);
		
		return GMF.getTypeReflection().getSetType(collectionEntityType);
	}

	public static class RequestExecutionData {

		public ServiceRequest serviceRequest;
		public PersistenceGmSession dataSession;
		public TransientPersistenceGmSession transientSession;
		public ModelPathNavigationListener navigationListener;
		public Supplier<? extends TransientPersistenceGmSession> transientSessionProvider;
		public Supplier<? extends NotificationFactory> notificationFactorySupplier;
		public Template template;
		public boolean handlingPaging;
		public boolean sendTransientEnvelope = true;
		public boolean navigateEvenIfHasNotification;
		
		public RequestExecutionData(ServiceRequest serviceRequest, PersistenceGmSession dataSession, TransientPersistenceGmSession transientSession,
				ModelPathNavigationListener navigationListener, Supplier<? extends TransientPersistenceGmSession> transientSessionProvider,
				Supplier<? extends NotificationFactory> notificationFactorySupplier) {
			this(serviceRequest, dataSession, transientSession, navigationListener, transientSessionProvider, notificationFactorySupplier, false);
		}

		public RequestExecutionData(ServiceRequest serviceRequest, PersistenceGmSession dataSession, TransientPersistenceGmSession transientSession,
				ModelPathNavigationListener navigationListener, Supplier<? extends TransientPersistenceGmSession> transientSessionProvider,
				Supplier<? extends NotificationFactory> notificationFactorySupplier, boolean handlingPaging) {
			this.serviceRequest = serviceRequest;
			this.dataSession = dataSession;
			this.transientSession = transientSession;
			this.navigationListener = navigationListener;
			this.transientSessionProvider = transientSessionProvider;
			this.notificationFactorySupplier = notificationFactorySupplier;
			this.handlingPaging = handlingPaging;
		}

		public void setTemplate(Template template) {
			this.template = template;
		}
		
		/**
		 * Configures whether a {@link EnvelopeSessionAspect} should be attached to the {@link ServiceRequest} execution context.
		 * It defaults to true.
		 */
		public void setSendTransientEnvelope(boolean sendTransientEnvelope) {
			this.sendTransientEnvelope = sendTransientEnvelope;
		}
		
		/**
		 * Configures whether we will navigate to the data even when the response is a {@link HasNotifications} instance.
		 */
		public void setNavigateEvenIfHasNotification(boolean navigateEvenIfHasNotification) {
			this.navigateEvenIfHasNotification = navigateEvenIfHasNotification;
		}

	}

}
