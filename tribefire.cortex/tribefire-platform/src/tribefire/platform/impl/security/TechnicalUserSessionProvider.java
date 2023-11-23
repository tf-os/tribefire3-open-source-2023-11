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
package tribefire.platform.impl.security;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;

/**
 * <p>
 * 
 * NOTE: This provider invokes {@link ValidateUserSession} in {@link #setRevalidationInterval(long) configured interval}
 * before providing a {@link UserSession}. If one needs a {@link Supplier} of {@code UserSession}(s) to use in
 * conjunction with GM RPC client, please use
 * {@link com.braintribe.model.processing.securityservice.commons.provider.AuthenticatingUserSessionProvider} instead,
 * which since tribefire 2.0, does not invoke {@link ValidateUserSession} when providing as it is capable of
 * re-authenticate based on authorization failures caught by the RPC client.
 */
public class TechnicalUserSessionProvider<T extends Credentials> implements Supplier<UserSession>, DestructionAware {

	private static Logger logger = Logger.getLogger(TechnicalUserSessionProvider.class);

	protected Evaluator<ServiceRequest> evaluator;
	protected T credentials;

	private UserSession session;
	private boolean authentification;
	private boolean sessionCheck;

	protected boolean blockUntilServerAvailable = false;
	protected Long blockTimeout = null;
	protected Long retryInterval = 1000L;
	protected boolean stopBlocking = false;
	protected long lastValidation = 0;
	protected long revalidationInterval = Numbers.MILLISECONDS_PER_MINUTE;

	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Required
	@Configurable
	public void setCredentials(T credentials) {
		this.credentials = credentials;
	}

	/**
	 * Configures the interval in ms in which a revalidation of a once acuired UserSession should happen during
	 * {@link #get()}
	 */
	@Configurable
	public void setRevalidationInterval(long revalidationInterval) {
		this.revalidationInterval = revalidationInterval;
	}

	private boolean isSessionValid(String sessionId) {
		long now = System.currentTimeMillis();

		if (now - lastValidation <= revalidationInterval)
			return true;

		ValidateUserSession validateUserSession = ValidateUserSession.T.create();
		validateUserSession.setSessionId(sessionId);
		Maybe<? extends UserSession> maybe = validateUserSession.eval(evaluator).getReasoned();

		lastValidation = now;

		return maybe.isSatisfied();
	}

	@Override
	public UserSession get() throws RuntimeException {

		if (this.session != null) {
			// if we're called while we're checking the session (by the security service)
			// we return the session ID that we have
			if (this.sessionCheck) {
				return this.session;
			}
			try {
				sessionCheck = true;
				boolean valid = isSessionValid(this.session.getSessionId());
				if (!valid) {
					this.session = null;
				}
			} catch (Throwable t) {
				String msg = "Error while trying to validate session ID " + this.session + ": " + t.getMessage();
				logger.debug(msg);
				logger.trace(msg, t);
				this.session = null;
			} finally {
				sessionCheck = false;
			}
		}

		if (session == null) {
			// if we're called while we're authentificating (by the security service) we don't return
			// anything (and break the circular call sequence)
			if (authentification) {
				return null;
			}
			try {
				authentification = true;

				long start = System.currentTimeMillis();

				while (!this.stopBlocking) {

					try {

						Maybe<? extends OpenUserSessionResponse> responseMaybe = openUserSession();

						if (responseMaybe.isSatisfied()) {

							session = responseMaybe.get().getUserSession();
							lastValidation = System.currentTimeMillis();
							if (session != null) {
								logger.debug("Received session " + session + " from server.");
								break;
							} else {
								logger.debug("Received an empty session from the server. Retrying.");
								session = null;
							}

						} else if (responseMaybe.isUnsatisfiedBy(AuthenticationFailure.T)) {
							break;
						} else {
							throw new ReasonException(responseMaybe.whyUnsatisfied());
						}
					} catch (Exception commException) {
						logger.debug("Could not connect to tribefire services. Retrying after " + this.retryInterval + " ms: "
								+ this.blockUntilServerAvailable, commException);

						if (this.blockUntilServerAvailable) {

							if ((this.blockTimeout != null) && (this.blockTimeout >= 0)) {
								long now = System.currentTimeMillis();
								long span = now - start;
								if (span > this.blockTimeout) {
									logger.info("Reached timeout " + this.blockTimeout + "ms while waiting for tribefire services.");
									throw commException;
								}
							}

							if (this.retryInterval != null) {
								try {
									synchronized (this) {
										Thread.sleep(this.retryInterval);
									}
								} catch (InterruptedException ignore) {
									// Ignore
								}
							}
						} else {
							throw commException;
						}
					}

				}

			} finally {
				authentification = false;
			}

			if (session == null) {
				throw new RuntimeException("retrieved session is invalid");
			}
		}
		return session;
	}

	@Override
	public void preDestroy() {
		this.stopBlocking = true;

		if (session != null) {
			try {
				Logout logout = Logout.T.create();
				logout.setSessionId(session.getSessionId());
				logout.eval(evaluator).getReasoned();
			} catch (Throwable e) {
				if (logger.isDebugEnabled()) {
					logger.debug("session couldn't be explicitly logged out: " + session);
				}
			}
		}
	}

	@Configurable
	public void setBlockUntilServerAvailable(boolean blockUntilServerAvailable) {
		this.blockUntilServerAvailable = blockUntilServerAvailable;
	}

	@Configurable
	public void setBlockTimeout(Long blockTimeout) {
		this.blockTimeout = blockTimeout;
	}

	@Configurable
	public void setRetryInterval(Long retryInterval) {
		this.retryInterval = retryInterval;
	}

	protected Maybe<? extends OpenUserSessionResponse> openUserSession() {

		OpenUserSession request = OpenUserSession.T.create();
		request.setCredentials(this.credentials);

		return request.eval(evaluator).getReasoned();
	}

}
