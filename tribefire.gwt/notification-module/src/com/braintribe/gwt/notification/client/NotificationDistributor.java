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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;

/**
 * This implementation of {@link NotificationListener} distributes the {@link Notification} instances
 * based on the {@link Notification#getTargetKey()} property. This is the way how {@link Notification}
 * instances can reach an end point in an application. 
 * @author Dirk
 *
 */
public class NotificationDistributor implements NotificationListener<Object>, DisposableBean {
	private Map<String, NotificationListener<?>> targetListeners;

	@SuppressWarnings("rawtypes")
	@Override
	public void onNotificationReceived(Notification<Object> notification) {
		if (targetListeners != null) {
			NotificationListener listener = targetListeners.get(notification.getTargetKey());
			if (listener != null)
				listener.onNotificationReceived(notification);
		}
	}
	
	/**
	 * Configures the {@link NotificationListener} instances that are associated to
	 * a targetKey that must match with {@link Notification#getTargetKey()} when
	 * a {@link Notification} is being distributed.
	 */
	@Configurable
	public void setTargetListeners(Map<String, NotificationListener<?>> targetListeners) {
		this.targetListeners = targetListeners;
	}
	
	/**
	 * Adds a single {@link NotificationListener} that is associated to
	 * a targetKey that must match with {@link Notification#getTargetKey()} when a 
	 * {@link Notification} is being distributed.
	 */
	@Configurable
	public void addTargetListener(String targetKey, NotificationListener<?> listener) {
		if (targetListeners == null) 
			targetListeners = new HashMap<>();
		
		targetListeners.put(targetKey, listener);
	}
	
	/**
	 * IOC cleanup method
	 */
	@Override
	public void disposeBean() throws Exception {
		if (targetListeners != null) {
			targetListeners.clear();
			targetListeners = null;
		}
	}
}
