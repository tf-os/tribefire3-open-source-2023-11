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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.net.ssl.SSLSocketFactory;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.queuecomm.QueueContext;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.WMQConstants;

public class JmsServerMq implements IServer {

	protected static Logger logger = Logger.getLogger(JmsServerMq.class);

	protected long destinationExpiry = -1;
	protected String host;
	protected int port = 1414;
	protected String channel;
	protected String queueManager;
	protected String username = null;
	protected String password = null;
	protected int ccsId = -1; // Host likes 1141
	protected int targetClient = -1;
	protected boolean useBindingsModeConnections = false;
	protected String sslCipherSuite = null; 
	protected String sslPeerName = null;
	protected boolean enableTracing = false;

	protected SslSocketFactoryProvider sslSocketFactoryProvider = null;

	public Connection createConnection() throws Exception {
		MQConnectionFactory factory = this.createConnectionFactory();

		if ((this.username != null) && (this.password != null)) {
			return factory.createConnection(this.username, this.password);
		} else {
			return factory.createConnection();
		}
	}

	protected MQConnectionFactory createConnectionFactory() throws Exception {
		MQConnectionFactory factory = new MQConnectionFactory();

		if (this.enableTracing) {
			logger.debug("Enable MQ tracing.");
			MQEnvironment.enableTracing(10);			
		}

		if (this.ccsId != -1) {
			factory.setCCSID(this.ccsId);
		}

		if (sslSocketFactoryProvider != null) {
			logger.debug("Setting a SSLSocketFactory in MQConnectionFactory");
			SSLSocketFactory sslSocketFactory = sslSocketFactoryProvider.provideSSLSocketFactory();
			factory.setSSLSocketFactory(sslSocketFactory);
		}

		logger.debug(String
				.format("creating new MQ connection factory: qmHost=%s:%d, qmChannel=%s, queueManager=%s, useBindingsModeConnections=%s",
						host, port, channel, queueManager, useBindingsModeConnections));

		/* Valid types are
    		WMQConstants.WMQ_CM_BINDINGS
    		WMQConstants.WMQ_CM_CLIENT
    		WMQConstants.WMQ_CM_DIRECT_TCPIP
    		WMQConstants.WMQ_CM_DIRECT_HTTP 
		 */
		if (useBindingsModeConnections) {
			factory.setTransportType(WMQConstants.WMQ_CM_BINDINGS); //0
		} else {
			factory.setTransportType(WMQConstants.WMQ_CM_CLIENT); //1
		}
		factory.setHostName(host);
		factory.setPort(port);
		factory.setChannel(channel);
		factory.setQueueManager(queueManager);
		
		if (this.sslPeerName != null) {
			factory.setSSLPeerName(this.sslPeerName );
		}

		if (this.sslCipherSuite != null) {
			factory.setSSLCipherSuite(this.sslCipherSuite);
		}

		return factory;
	}

	public QueueContext getQueueContext(String queueName, boolean transactionalSession, int acknowledgeMode)
			throws Exception {
		MQQueue d = new MQQueue(queueName);
		if (this.destinationExpiry != -1) {
			logger.trace(String.format("setting destinationExpiry for destination %s to %d", d.getBaseQueueName(),
					destinationExpiry));
			d.setExpiry(this.destinationExpiry);
		}

		if (this.targetClient != -1) {
			int oldValue = d.getTargetClient();
			logger.trace("Replacing target client: " + oldValue + " by " + this.targetClient);
			d.setTargetClient(this.targetClient);
		}

		Destination queue = (Destination) d;
		QueueContext queueContext = new QueueContext(this, queue, queueName, transactionalSession, acknowledgeMode);
		return queueContext;
	}

	public void destroy() throws Exception {
	}

	public long getDestinationExpiry() {
		return destinationExpiry;
	}

	@Configurable
	public void setDestinationExpiry(long destinationExpiry) {
		this.destinationExpiry = destinationExpiry;
	}

	public String getHost() {
		return host;
	}

	@Required
	public void setHost(String qmHost) {
		this.host = qmHost;
	}

	public int getPort() {
		return port;
	}

	@Configurable
	public void setPort(int qmPort) {
		this.port = qmPort;
	}

	public String getChannel() {
		return channel;
	}

	@Required
	public void setChannel(String qmChannel) {
		this.channel = qmChannel;
	}

	public String getQueueManager() {
		return queueManager;
	}

	@Required
	public void setQueueManager(String qmQueueManager) {
		this.queueManager = qmQueueManager;
	}

	public String getUsername() {
		return username;
	}

	@Configurable
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	@Configurable
	public void setPassword(String password) {
		this.password = password;
	}

	@Configurable
	public void setCcsId(int ccsID) {
		this.ccsId = ccsID;
	}

	public int getTargetClient() {
		return targetClient;
	}

	@Configurable
	public void setTargetClient(int targetClient) {
		this.targetClient = targetClient;
	}

	public boolean isUseBindingsModeConnections() {
		return useBindingsModeConnections;
	}

	@Configurable
	public void setUseBindingsModeConnections(boolean useBindingsModeConnections) {
		this.useBindingsModeConnections = useBindingsModeConnections;
	}

	@Configurable
	public void setSslSocketFactoryProvider(SslSocketFactoryProvider sslSocketFactoryProvider) {
		this.sslSocketFactoryProvider = sslSocketFactoryProvider;
	}

	@Configurable
	public void setSslCipherSuite(String sslCipherSuite) {
		this.sslCipherSuite = sslCipherSuite;
	}

	@Configurable
	public void setSslPeerName(String sslPeerName) {
		this.sslPeerName = sslPeerName;
	}

	public boolean isEnableTracing() {
		return enableTracing;
	}
	@Configurable
	public void setEnableTracing(boolean enableTracing) {
		this.enableTracing = enableTracing;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MQ: ");
		sb.append("Host: "+this.host);
		sb.append(", Port: "+this.port);
		sb.append(", Channel: "+this.channel);
		sb.append(", CCSID: "+this.ccsId);
		sb.append(", SSL Cipher Suite: "+this.sslCipherSuite);
		sb.append(", SSL Peer Name: "+this.sslPeerName);
		return sb.toString();
	}
}
