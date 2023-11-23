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
package com.braintribe.model.processing.securityservice.basic.verification;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.Map;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.utils.lcd.StringTools;

/**
 * Implementation of the {@link UserSessionAccessVerificationExpert} interface that checks whether the IP address of the
 * current requestor is the same as the one of the user who created the UserSession. This should prevent UserSession
 * hijacking by re-using the Session ID by another client (unless the attacker is on the same machine). <br>
 * <br>
 * By default, this expert is not activated. The runtime property TRIBEFIRE_USERSESSION_IP_VERIFICATION has to be set to
 * true to activate this expert. <br>
 * <br>
 * There might be issues between IPv4 and IPv6 addresses (experienced during local tests). For this purpose, a map of IP
 * address aliases can be configured in the TRIBEFIRE_USERSESSION_IP_VERIFICATION_ALIASES runtime property. The value of
 * this property should contain an even number of strings (key-value pairs).
 */
public class SameOriginUserSessionVerification implements UserSessionAccessVerificationExpert, InitializationAware {

	private static final Logger logger = Logger.getLogger(SameOriginUserSessionVerification.class);

	protected boolean ignoreSourceIpNullValue = true;
	protected boolean allowAccessToUserSessionsWithNoCreationIp = true;
	protected Map<String, String> addressAliases = asMap("127.0.0.1", "0:0:0:0:0:0:0:1");

	private boolean active = false;

	@Override
	public Reason verifyUserSessionAccess(ServiceRequestContext requestContext, UserSession userSession) {

		if (!active) {
			logger.trace(() -> "SameOriginUserSessionVerification is deactivated.");
			return null;
		}

		final String requestorAddress = requestContext.getRequestorAddress();

		if (userSession == null) {
			logger.trace(() -> "No UserSession to compare with. Not checking the requestor IP: " + requestorAddress);
			return null;
		}

		final String sessionId = userSession.getSessionId();

		if (StringTools.isBlank(requestorAddress)) {
			if (this.ignoreSourceIpNullValue) {
				logger.trace(() -> "The request has no valid requestorAddress. Hence, it is an internal request and we allow it to use session "
						+ sessionId);
				return null;
			} else {
				return Reasons.build(Forbidden.T)
						.text("The UserSession " + sessionId + " cannot be accessed as the requestor IP address cannot be determined.").toReason();
			}
		}

		String sessionCreationIp = userSession.getCreationInternetAddress();
		if (StringTools.isBlank(sessionCreationIp)) {
			if (allowAccessToUserSessionsWithNoCreationIp) {
				logger.trace(() -> "The UserSession " + sessionId
						+ " has no valid creation address. Hence, we cannot check whether the requestor from " + requestorAddress + " is valid.");
				return null;
			} else {
				return Reasons.build(Forbidden.T)
						.text("The UserSession " + sessionId + " cannot be accessed as UserSession does not bear a creation IP address.").toReason();
			}
		}

		logger.trace(() -> "Comparing UserSession (" + sessionId + ") IP address '" + sessionCreationIp + "' with requestor IP address '"
				+ requestorAddress + "'");

		if (requestorAddress.equals(sessionCreationIp)) {
			logger.trace(() -> "Comparison checks out. Allowing access to UserSession.");
			return null;
		} else {
			logger.debug(() -> "Comparing UserSession (" + sessionId + ") IP address '" + sessionCreationIp + "' with requestor IP address '"
					+ requestorAddress + "' did not match. Trying aliases now: " + addressAliases);
			if (addressAliases != null && !addressAliases.isEmpty()) {
				final String altRequestorAddressValue = addressAliases.get(requestorAddress);
				if (altRequestorAddressValue != null && sessionCreationIp.equals(altRequestorAddressValue)) {
					logger.debug(() -> "Alias '" + altRequestorAddressValue + "' for requestor IP address '" + requestorAddress
							+ "' matches the creation IP address '" + sessionCreationIp + "'. Allowing access to UserSession.");
					return null;
				}
				final String altCreationAddressValue = addressAliases.get(sessionCreationIp);
				if (altCreationAddressValue != null && requestorAddress.equals(altCreationAddressValue)) {
					logger.debug(() -> "Alias '" + altCreationAddressValue + "' for creation IP address '" + sessionCreationIp
							+ "' matches the requestor IP address '" + requestorAddress + "'. Allowing access to UserSession.");
					return null;
				}
			}
		}

		return Reasons.build(Forbidden.T) //
				.text("Not allowing access to UserSession '" + sessionId + "' from IP address '" + requestorAddress + "'").toReason();
	}

	@Configurable
	public void setIgnoreSourceIpNullValue(boolean ignoreSourceIpNullValue) {
		this.ignoreSourceIpNullValue = ignoreSourceIpNullValue;
	}
	@Configurable
	public void setAllowAccessToUserSessionsWithNoCreationIp(boolean allowAccessToUserSessionsWithNoCreationIp) {
		this.allowAccessToUserSessionsWithNoCreationIp = allowAccessToUserSessionsWithNoCreationIp;
	}
	@Configurable
	public void setAddressAliases(Map<String, String> addressAliases) {
		this.addressAliases = addressAliases;
	}

	@Override
	public void postConstruct() {
		String ipVerification = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_USERSESSION_IP_VERIFICATION);
		if (!StringTools.isBlank(ipVerification) && ipVerification.equalsIgnoreCase("true")) {
			logger.debug(() -> "Activating UserSession IP address verification check. This is controlled by the runtime property: "
					+ TribefireRuntime.ENVIRONMENT_USERSESSION_IP_VERIFICATION);
			active = true;

			Map<String, String> aliases = null;
			final String aliasesString = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_USERSESSION_IP_VERIFICATION_ALIASES);
			if (!StringTools.isBlank(aliasesString)) {

				logger.debug(() -> "Using the following configured aliases: " + aliasesString);

				String[] split = StringTools.splitString(aliasesString, ",");
				if (split != null && split.length > 0) {
					if ((split.length % 2) == 0) {
						for (int i = 0; i < split.length; ++i) {
							split[i] = split[i].trim();
						}
						aliases = CollectionTools2.asMap((Object[]) split);
					} else {
						logger.warn("The values of runtime environment variable " + TribefireRuntime.ENVIRONMENT_USERSESSION_IP_VERIFICATION_ALIASES
								+ " must contain an even number of values. ('" + aliasesString + "'). Using default values.");
					}
				}
				setAddressAliases(aliases);

			} else {
				logger.debug(() -> "Using the default IP address aliases");
			}

		} else {
			logger.debug(() -> "UserSession IP address verification check is deactivated. This is controlled by the runtime property: "
					+ TribefireRuntime.ENVIRONMENT_USERSESSION_IP_VERIFICATION);
			active = false;
		}
	}

}
