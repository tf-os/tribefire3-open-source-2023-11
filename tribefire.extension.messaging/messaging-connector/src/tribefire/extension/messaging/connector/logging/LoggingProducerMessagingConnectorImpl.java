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
package tribefire.extension.messaging.connector.logging;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.logging.LogLevel;

import tribefire.extension.messaging.connector.api.AbstractProducerMessagingConnector;

/**
 * A simple implementation which is logging the message
 */
public class LoggingProducerMessagingConnectorImpl extends AbstractProducerMessagingConnector {

	private static final Logger logger = Logger.getLogger(LoggingProducerMessagingConnectorImpl.class);

	private LogLevel logLevel;
	private boolean logTags;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	public CheckResultEntry actualHealth() {
		return null;
	}

	// -----------------------------------------------------------------------
	// PRODUCE
	// -----------------------------------------------------------------------

	@Override
	protected void deliverMessageString(byte[] message, Set<String> topics) {
		logger.log(Logger.LogLevel.valueOf(logLevel.name()), "New message: '" + new String(message, StandardCharsets.UTF_8) + "'");
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Configurable
	public void setLogTags(boolean logTags) {
		this.logTags = logTags;
	}

	@Override
	public String getExternalId() {
		return null; // TODO This is a stub
	}

	public void destroy() {
		logger.info("Logging connector closed!");
	}
}
