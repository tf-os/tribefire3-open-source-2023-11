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

import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;

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
public abstract class AbstractNotificationPoll implements InitializableBean, DisposableBean {
	//private static Logger logger = new Logger(AbstractNotificationPoll.class);
	private List<NotificationListener<?>> listeners = new ArrayList<>();
	
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
	 * This method is used to distribute received {@link Notification} instances to all
	 * registered listeners.
	 * @see #addNotificationListener(NotificationListener)
	 * @see #removeNotificationListener(NotificationListener)
	 */
	@SuppressWarnings("rawtypes")
	protected void notifyListeners(Notification<?> notification) {
		//logger.debug("NotificationPoll - notifying listeners. TargetKey: " + notification.getTargetKey());
		for (NotificationListener listener : listeners) {
			listener.onNotificationReceived(notification);
		}
	}

	public abstract void startPolling();
	public abstract void stopPolling();
	
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
