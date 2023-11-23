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
package com.braintribe.model.processing.securityservice.commons.provider;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.usersession.UserSession;

/**
 * <p>
 * A {@link UserSession} {@link Supplier} which triggers the creation of a fresh new {@code UserSession} once it
 * identifies that the currently cached {@code UserSession} is no longer valid or has reached a time-to-live calculated
 * based on the optionally configurable purgeAgeFactor.
 * 
 * <p>
 * The cached {@code UserSession} are expected to expire normally when idle, therefore no logout operation is performed
 * by this object.
 * 
 */
public class CachingUserSessionSupplier implements Supplier<UserSession>, Consumer<Object> {

	private static final Logger log = Logger.getLogger(CachingUserSessionSupplier.class);

	// configurable
	private Supplier<UserSession> authenticator;
	private float purgeAgeFactor = 0.0f;

	// internals
	private UserSession userSession;
	private boolean authenticating = false;
	private long purgeTime = -1;
	private ReentrantLock lock = new ReentrantLock();

	public CachingUserSessionSupplier() {
	}

	@Required
	@Configurable
	public void setAuthenticator(Supplier<UserSession> authenticator) {
		this.authenticator = authenticator;
	}

	@Configurable
	public void setPurgeAgeFactor(float purgeAgeFactor) {
		this.purgeAgeFactor = purgeAgeFactor;
	}

	@Override
	public void accept(Object invalidationSignal) {
		log.trace(() -> "Purging current user session [ " + userSession + " ] upon invalidation signal: " + invalidationSignal);
		lock.lock();
		try {
			userSession = null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public UserSession get() {

		if (purgeTime != -1 && purgeTime < System.currentTimeMillis()) {
			accept("Purge time reached");
		}

		lock.lock();
		try {
			if (userSession != null || authenticating) {
				return userSession;
			}

			try {
				authenticating = true;
				userSession = authenticator.get();
				if (purgeAgeFactor > 0) {
					updatePurgeTime(userSession);
				}
				log.trace(() -> "Cached user session [ " + userSession + " ]");
				return userSession;
			} finally {
				authenticating = false;
			}
		} finally {
			lock.unlock();
		}

	}

	private void updatePurgeTime(UserSession userSession) {

		long now = System.currentTimeMillis();
		TimeSpan maxIdleTime = userSession.getMaxIdleTime();
		Date expiryDate = userSession.getExpiryDate();
		long delta = -1;

		if (maxIdleTime != null) {
			delta = Math.round(toMillis(maxIdleTime));
		}

		if (expiryDate != null) {
			long expiryDelta = expiryDate.getTime() - now;
			if (delta < 0 || delta > expiryDelta) {
				delta = expiryDelta;
			}
		}

		if (delta < 1) {
			this.purgeTime = -1;
		} else {
			this.purgeTime = now + Math.round(delta * purgeAgeFactor);
		}

	}

	public static double toMillis(TimeSpan timeSpan) {
		TimeUnit u = Objects.requireNonNull(timeSpan.getUnit(), "TimeSpan's unit must not be null");
		double v = timeSpan.getValue();
		switch (u) {
			case nanoSecond:
				return v * 1000e-9;
			case microSecond:
				return v * 1000e-6;
			case milliSecond:
				return v * 1000e-3;
			case second:
				return v * 1000;
			case minute:
				return v * 60000;
			case hour:
				return v * 60000 * 60;
			case day:
				return v * 60000 * 60 * 24;
			default:
				throw new UnsupportedOperationException("Unsupported time unit: " + u);
		}
	}

}
