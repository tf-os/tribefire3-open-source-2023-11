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
package com.braintribe.model.platformreflection.host.tomcat;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface Connector extends GenericEntity {

	EntityType<Connector> T = EntityTypes.T(Connector.class);
	
	String getUriEncoding();
	void setUriEncoding(String uriEncoding);
	
	Integer getAcceptCount();
	void setAcceptCount(Integer acceptCount);
	
	Integer getConnectionLinger();
	void setConnectionLinger(Integer connectionLinger);
	
	Long getConnectionTimeout();
	void setConnectionTimeout(Long connectionTimeout);
	
	Long getKeepAliveTimeout();
	void setKeepAliveTimeout(Long keepAliveTimeout);
	
	Integer getLocalPort();
	void setLocalPort(Integer localPort);
	
	Integer getMaxHeaderCount();
	void setMaxHeaderCount(Integer maxHeaderCount);
	
	Integer getMaxParameterCount();
	void setMaxParameterCount(Integer maxParameterCount);
	
	Integer getMaxPostSize();
	void setMaxPostSize(Integer maxPostSize);
	
	Integer getMaxSavePostSize();
	void setMaxSavePostSize(Integer maxSavePortSize);
	
	Integer getMaxSwallowSize();
	void setMaxSwallowSize(Integer maxSwallowSize);
	
	Integer getMaxThreads();
	void setMaxThreads(Integer maxThreads);
	
	Integer getMinSpareThreads();
	void setMinSpareThreads(Integer minSpareThreads);
	
	Integer getPacketSize();
	void setPacketSize(Integer packetSize);
	
	Integer getPort();
	void setPort(Integer port);
	
	Integer getProcessorCache();
	void setProcessorCache(Integer processorCache);
	
	String getProtocol();
	void setProtocol(String protocol);
	
	String getProxyName();
	void setProxyName(String proxyName);
	
	Integer getProxyPort();
	void setProxyPort(Integer proxyPort);
	
	Integer getRedirectPort();
	void setRedirectPort(Integer redirectPort);
	
	String getScheme();
	void setScheme(String scheme);
	
	Boolean getSecure();
	void setSecure(Boolean secure);
	
	List<String> getSslProtocols();
	void setSslProtocols(List<String> sslProtocols);
	
	String getStateName();
	void setStateName(String stateName);
	
	boolean getTcpNoDelay();
	void setTcpNoDelay(boolean tcpNoDelay);
	
	Integer getThreadPriority();
	void setThreadPriority(Integer threadPriority);
	
	Long getBytesReceived();
	void setBytesReceived(Long bytesReceived);
	
	Long getBytesSent();
	void setBytesSent(Long bytesSent);
	
	Integer getErrorCount();
	void setErrorCount(Integer errorCount);
	
	Long getMaxTime();
	void setMaxTime(Long maxTime);
	
	Long getProcessingTime();
	void setProcessingTime(Long processingTime);
	
	Integer getRequestCount();
	void setRequestCount(Integer requestCount);
	
}
