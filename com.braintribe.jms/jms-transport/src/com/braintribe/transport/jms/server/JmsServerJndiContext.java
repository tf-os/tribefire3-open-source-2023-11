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
package com.braintribe.transport.jms.server;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.queuecomm.QueueContext;
import com.braintribe.transport.jms.util.JmsExceptionListener;

public class JmsServerJndiContext implements IServer {

	protected static Logger logger = Logger.getLogger(JmsServerJndiContext.class);

	protected String namingContextFactory = null;

	protected String urlPackagePrefix = null;
	protected String providerURL = null;
	protected String connectionFactory = null;

	protected String username = null;
	protected String password = null;

	protected InitialContext ctx = null;
	protected ConnectionFactory qConnFactory = null;

	public void initialize() throws Exception {
		// first, we need a queue connection factory
		try {
			this.createConnectionFactory();
		} catch (Exception e) {
			throw new Exception("Could not initialize naming context.", e);
		}

	}

	protected void createConnectionFactory() throws Exception {
		try {
			Properties properties = new Properties();
			if (!isEmpty(this.namingContextFactory)) {
				properties.put(Context.INITIAL_CONTEXT_FACTORY, this.namingContextFactory);
			}
			if (!isEmpty(this.urlPackagePrefix)) {
				properties.put(Context.URL_PKG_PREFIXES, this.urlPackagePrefix);
			}
			if (!isEmpty(this.providerURL)) {
				properties.put(Context.PROVIDER_URL, this.providerURL);
			}

			this.ctx = new InitialContext(properties);
			this.qConnFactory = (ConnectionFactory) ctx.lookup(this.connectionFactory);
		} catch (Exception e) {
			logger.error("Could not initialize naming context.", e);
			throw e;
		}
	}
	
	protected static boolean isEmpty(String text) {
		if (text == null) {
			return true;
		}
		if (text.trim().length() == 0) {
			return true;
		}
		return false;
	}

	@Override
	public Connection createConnection() throws Exception {
		Connection con = null;

		if ((this.username != null) && (this.password != null)) {
			logger.debug(String.format("creating queue connection for user '%s'", this.username));
			con = this.qConnFactory.createConnection(this.username, this.password);
		} else {
			logger.debug("creating anonymous queue connection");
			con = this.qConnFactory.createConnection();
		}

		con.setExceptionListener(new JmsExceptionListener(logger));

		logger.trace("successfully created queue connection");
		return con;
	}

	@Override
	public QueueContext getQueueContext(String queueName, boolean transactionalSession, int acknowledgeMode)
			throws Exception {
		Destination queue = (Destination) this.ctx.lookup(queueName);
		QueueContext queueContext = new QueueContext(this, queue, queueName, transactionalSession, acknowledgeMode);
		return queueContext;
	}

	public void destroy() throws Exception {
		//Nothing to do here
	}

	public String getNamingContextFactory() {
		return namingContextFactory;
	}

	public void setNamingContextFactory(String namingContextFactory) {
		this.namingContextFactory = namingContextFactory;
	}

	public String getUrlPackagePrefix() {
		return urlPackagePrefix;
	}

	public void setUrlPackagePrefix(String urlPackagePrefix) {
		this.urlPackagePrefix = urlPackagePrefix;
	}

	public String getProviderURL() {
		return providerURL;
	}

	public void setProviderURL(String providerURL) {
		this.providerURL = providerURL;
	}

	public String getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(String connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: JmsServerJndiContext.java 92413 2016-03-15 08:30:06Z roman.kurmanowytsch $";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("JMS/JNDI: ");
		sb.append("URL Package Prefix: "+this.urlPackagePrefix);
		sb.append(", Provider URL: "+this.providerURL);
		sb.append(", Connection Factory: "+this.connectionFactory);
		return sb.toString();
	}

}
