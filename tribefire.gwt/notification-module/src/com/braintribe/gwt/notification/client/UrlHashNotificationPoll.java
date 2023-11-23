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

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.logging.client.Logger;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

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
public class UrlHashNotificationPoll extends AbstractNotificationPoll {
	private static Logger logger = new Logger(UrlHashNotificationPoll.class);

	private Timer timer;
	private int pollIntervalInMillies = 250;
	private String lastHash = "";
	private Codec<Notification<?>, String> notificationCodec = null;
	
	/**
	 * Configures the codec that will be used to decode {@link Notification} instances from the url.
	 */
	@Configurable
	public void setNotificationCodec(Codec<Notification<?>, String> notificationCodec) {
		this.notificationCodec = notificationCodec;
	}
	
	public Codec<Notification<?>, String> getNotificationCodec() {
		if (notificationCodec == null) {
			notificationCodec = new UrlNotificationCodec();
		}

		return notificationCodec;
	}
	
	@Configurable
	public void setPollIntervalInMillies(int pollIntervalInMillies) {
		this.pollIntervalInMillies = pollIntervalInMillies;
	}
	
	public Timer getTimer() {
		if (timer == null) {
			timer = new Timer() {
				@Override
				public void run() {
					checkForUrlHashChanges();
				}
			};
		}

		return timer;
	}
	
	protected void checkForUrlHashChanges() {
		String currentHash = Window.Location.getHash();
		
		// detect hash changes
		if (!currentHash.equals(lastHash)) {
			// hash changed so decode it
			lastHash = currentHash;
			UrlParameters hashParameters = new UrlParameters(lastHash);
			String encodedNotification = hashParameters.getParameter("notify");
			if (encodedNotification != null) {
				try {
					Notification<?> notification = getNotificationCodec().decode(encodedNotification);
					notifyListeners(notification);
				} catch (CodecException e) {
					logger.error("exception while decoding notification from url", e);
				}
			}
		}
	}
	
	/**
	 * Starts a the timer of this poll that examines the url for changes of the hash (#) part
	 */
	@Override
	public void startPolling() {
		getTimer().scheduleRepeating(pollIntervalInMillies);
	}

	/**
	 * Stops the timer of this poll 
	 */
	@Override
	public void stopPolling() {
		getTimer().cancel();
	}
}
