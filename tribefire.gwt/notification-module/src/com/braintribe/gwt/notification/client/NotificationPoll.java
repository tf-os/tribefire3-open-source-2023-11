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
package com.braintribe.gwt.notification.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The NotificationPoll uses the {@link CrossDomainJsonRequest} to
 * poll {@link Notification} instances from a server. After a request
 * has transported one {@link Notification} or was been closed due to
 * timeout or error a new one will be started.
 * 
 * The NotificationPoll will start requests when it is initialized and will stop
 * the requests when it is disposed. 
 * 
 * The NotificationPoll will also stop with the automatic request series
 * after a configurable maximal amount of failures in sequence have happened.
 * 
 * @author Dirk
 *
 */
public class NotificationPoll implements InitializableBean, DisposableBean {
	private static Logger logger = new Logger(NotificationPoll.class);
	private List<NotificationListener<?>> listeners = new ArrayList<>();
	private Codec<Notification<?>, JavaScriptObject> notificationCodec = null;
	private Supplier<String> recipientProvider;
	private Future<JavaScriptObject> pollFuture;
	
	private int maxErrorsInSeriesToStop = 5;
	private int errorsInSeries = 0;
	private Supplier<String> servletUrlProvider;
	
	/**
	 * The provider will be used to query for a string that is used as recipient
	 * key for the NotificationBridge (for example the session id is a good candidate)
	 */
	@Configurable @Required
	public void setRecipientProvider(Supplier<String> recipientProvider) {
		this.recipientProvider = recipientProvider;
	}
	
	/**
	 * Configures the codec that will be used to decode {@link Notification} instances.
	 */
	@Configurable
	public void setNotificationCodec(Codec<Notification<?>, JavaScriptObject> notificationCodec) {
		this.notificationCodec = notificationCodec;
	}
	
	/**
	 * Configures the provider that provides the url for the servlet that
	 * will be polled.
	 */
	@Configurable @Required
	public void setServletUrlProvider(Supplier<String> servletUrlProvider) {
		this.servletUrlProvider = servletUrlProvider;
	}
	
	/**
	 * Adds a {@link NotificationListener} that will be informed when {@link Notification}
	 * are received by the NotificationPoll
	 */
	public void addNotificationListener(NotificationListener<?> listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a registered {@link NotificationListener}. This the given listener
	 * will no longer be informed when {@link Notification} instances are received.
	 */
	public void removeNotificationListener(NotificationListener<?> listener) {
		listeners.remove(listener);
	}
	
	/**
	 * @return the recipient that should be used to identify this poll against the
	 * NotificationBridge
	 * @see #setRecipientProvider(Supplier)
	 */
	protected String getRecipient() {
		try {
			return recipientProvider.get();
		} catch (RuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Builds up the url that will be used to run a request with the {@link CrossDomainJsonRequest}
	 * class.
	 */
	protected String getPollingUrl() {
		try {
			String url = servletUrlProvider.get() + "?recipient=" + getRecipient();
			//logger.debug("NotificationPoll - pollingUrl: " + url);
			return url;
		} catch (RuntimeException e) {
			logger.error("error while getting servlet url from configured provider", e);
			e.printStackTrace();
			throw new RuntimeException("error while getting servlet url from configured provider", e);
		}
	}
	
	/**
	 * @see #setNotificationCodec(Codec)
	 * @return the configured codec that decodes {@link Notification} instances.
	 */
	public Codec<Notification<?>, JavaScriptObject> getNotificationCodec() {
		if (notificationCodec == null) {
			notificationCodec = new NotificationCodec();
		}

		return notificationCodec;
	}
	
	/**
	 * This method is used to distribute received {@link Notification} instances to all
	 * registered listeners.
	 * @see #addNotificationListener(NotificationListener)
	 * @see #removeNotificationListener(NotificationListener)
	 */
	@SuppressWarnings("rawtypes")
	protected void notifyListeners(Notification<?> notification) {
		//logger.debug("NotificationPoll - notifying listeners. TargetKey: " + notification.getTargetKey());
		for (NotificationListener listener: listeners) {
			listener.onNotificationReceived(notification);
		}
	}
	
	/**
	 * This callback listens to the results of any started {@link CrossDomainJsonRequest}
	 * and triggers new requests if a request has been finished (successful or unsuccessful).
	 * If a maximum number of errors in a row is received no new request will be automatically 
	 * started.
	 */
	private AsyncCallback<JavaScriptObject> pollCallback = new AsyncCallback<JavaScriptObject>() {
		@Override
		public void onSuccess(JavaScriptObject encodedNotification) {
			try {
				//logger.debug("NotificationPoll - pollCallback onSuccess");
				stopPolling();
				errorsInSeries = 0;
				Notification<?> notification = getNotificationCodec().decode(encodedNotification);
				notifyListeners(notification);
				startPolling();
			} catch (CodecException e) {
				logger.error("error while decoding notification message after successful polling", e);
				e.printStackTrace();
			}
		}
		
		@Override
		public void onFailure(Throwable caught) {
			stopPolling();
			logger.error("error while polling notification message", caught);
			errorsInSeries++;
			if (errorsInSeries <= maxErrorsInSeriesToStop)
				startPolling();
			else {
				logger.warn("notification polling stopped after error occuring more than " + maxErrorsInSeriesToStop + " time(s) in series", caught);
			}
		}
	};
	
	/**
	 * Starts a series of requests to receive {@link Notification} instances.
	 */
	public void startPolling() {
		//logger.debug("NotificationPoll - startPolling");
		if (pollFuture != null) throw new IllegalStateException("there's already a poll in action");
		pollFuture = new CrossDomainJsonRequest(getPollingUrl()).execute();
		pollFuture.get(pollCallback);
	}

	/**
	 * Stops a currently running polling and also the automatic request series. 
	 */
	public void stopPolling() {
		//logger.debug("NotificationPoll - stopPolling");
		if (pollFuture != null) { 
			pollFuture.remove(pollCallback);
			pollFuture = null;
		}
	}
	
	@Override
	public void intializeBean() throws Exception {
		startPolling();
	}
	
	/**
	 * IOC cleanup method
	 */
	@Override
	public void disposeBean() throws Exception {
		stopPolling();
		listeners.clear();
	}
	
}
