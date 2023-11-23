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
package com.braintribe.model.processing.email.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;

import com.braintribe.logging.Logger;
import com.braintribe.model.email.deployment.connection.SmtpConnector;
import com.braintribe.utils.lcd.StringTools;

public class MailerCache {

	private static final Logger logger = Logger.getLogger(MailerCache.class);

	private ConcurrentHashMap<String, MailerContext> mailers = new ConcurrentHashMap<>();

	public MailerContext getMailer(SmtpConnector smtpConnector) {

		if (smtpConnector == null) {
			throw new IllegalArgumentException("The smtp connector must not be null.");
		}
		String externalId = smtpConnector.getExternalId();
		MailerContext mailerContext = mailers.computeIfAbsent(externalId, eId -> {

			String host = smtpConnector.getSmtpHostName();
			String loginUser = smtpConnector.getLoginUser();
			String password = smtpConnector.getPassword();
			int smtpPort = smtpConnector.getSmtpPort();
			String user = smtpConnector.getUser();
			if (StringTools.isBlank(loginUser)) {
				loginUser = user;
			}

			MailerRegularBuilderImpl mailerBuilder = MailerBuilder.withSMTPServer(host, smtpPort, loginUser, password);

			setProxy(smtpConnector, mailerBuilder);

			Integer connectionTimeoutInMs = smtpConnector.getConnectionTimeoutInMs();
			if (connectionTimeoutInMs != null) {
				mailerBuilder.withSessionTimeout(connectionTimeoutInMs);
			}

			setPoolSizes(smtpConnector, mailerBuilder);

			com.braintribe.model.email.deployment.connection.TransportStrategy transportStrategy = smtpConnector.getTransportStrategy();
			if (transportStrategy != null) {
				TransportStrategy strategy = TransportStrategy.valueOf(transportStrategy.name());
				mailerBuilder.withTransportStrategy(strategy);
			}

			Mailer newMailer = mailerBuilder.buildMailer();

			MailerContext context = new MailerContext(newMailer);
			context.setSendAsync(smtpConnector.getSendAsync());

			return context;
		});
		return mailerContext;
	}

	private void setPoolSizes(SmtpConnector smtpConnector, MailerRegularBuilderImpl mailerBuilder) {
		Integer threadPoolSize = smtpConnector.getThreadPoolSize();
		if (threadPoolSize != null && threadPoolSize > 0) {
			mailerBuilder.withThreadPoolSize(threadPoolSize);
		}

		Integer connectionPoolCoreSize = smtpConnector.getConnectionPoolCoreSize();
		if (connectionPoolCoreSize != null && connectionPoolCoreSize >= 0) {
			mailerBuilder.withConnectionPoolCoreSize(connectionPoolCoreSize);
		}

		Integer connectionPoolMaxSize = smtpConnector.getConnectionPoolMaxSize();
		if (connectionPoolMaxSize != null && connectionPoolMaxSize > 0) {
			mailerBuilder.withConnectionPoolMaxSize(connectionPoolMaxSize);
		}
	}

	private void setProxy(SmtpConnector smtpConnector, MailerRegularBuilderImpl mailerBuilder) {
		Integer proxyBridgePort = smtpConnector.getProxyBridgePort();
		if (proxyBridgePort != null && proxyBridgePort > 0) {
			mailerBuilder.withProxyBridgePort(proxyBridgePort);
		}

		String proxyHost = smtpConnector.getProxyHost();
		Integer proxyPort = smtpConnector.getProxyPort();
		String proxyUsername = smtpConnector.getProxyUsername();
		String proxyPassword = smtpConnector.getProxyPassword();
		if (!StringTools.isBlank(proxyHost) && proxyPort != null && proxyPort > 0) {
			mailerBuilder.withProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
		}
	}

	public void removeMailer(String externalId) {
		MailerContext mailerContext = mailers.remove(externalId);
		if (mailerContext != null) {
			try {
				mailerContext.getMailer().shutdownConnectionPool();
			} catch (Exception e) {
				logger.warn("Could not shutdown the connection pool of Mailer for " + externalId, e);
			}
		}
	}
}
