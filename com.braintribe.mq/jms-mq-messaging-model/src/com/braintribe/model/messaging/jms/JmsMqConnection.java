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
package com.braintribe.model.messaging.jms;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface JmsMqConnection extends JmsConnection {

	final EntityType<JmsMqConnection> T = EntityTypes.T(JmsMqConnection.class);

	String getHost();
	void setHost(String hostAddress);
	
	@Initializer("false")
	boolean getEnableTracing();
	void setEnableTracing(boolean enableTracing);
	
	@Initializer("-1")
	int getCcsId();
	void setCcsId(int ccsId);
	
	@Initializer("false")
	boolean getUseBindingsModeConnections();
	void setUseBindingsModeConnections(boolean useBindingsModeConnections);
	
	String getChannel();
	void setChannel(String channel);
	
	String getQueueManager();
	void setQueueManager(String queueManager);
	
	@Initializer("1414")	
	int getPort();
	void setPort(int port);
	
	String getSslPeerName();
	void setSslPeerName(String sslPeerName);
	
	String getSslCipherSuite();
	void setSslCipherSuite(String sslCipherSuite);
	
	String getTargetClient();
	void setTargetClient(String targetClient);
	
	@Initializer("-1l")	
	long getDestinationExpiry();
	void setDestinationExpiry(long destinationExpiry);
}
