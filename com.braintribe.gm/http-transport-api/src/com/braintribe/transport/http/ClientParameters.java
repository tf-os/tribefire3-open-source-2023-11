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
package com.braintribe.transport.http;

public class ClientParameters {

	protected Integer maxTotal = null;
	protected Integer maxPerRoute = null;
	protected Integer socketTimeout = null;
	protected Boolean tracePooling = null;
	protected Long poolTimeToLive = null;
	protected Long validateAfterInactivity = null;
	protected Long maxIdleConnectionTtl = null;
	
	public static final ClientParameters emptyParameters = new ClientParameters();
	
	public ClientParameters() {
		//Nothing to do
	}

	public ClientParameters(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}
	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}
	public Integer getMaxPerRoute() {
		return maxPerRoute;
	}
	public void setMaxPerRoute(Integer maxPerRoute) {
		this.maxPerRoute = maxPerRoute;
	}
	public Integer getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public Boolean getTracePooling() {
		return tracePooling;
	}
	public void setTracePooling(Boolean tracePooling) {
		this.tracePooling = tracePooling;
	}
	public Long getPoolTimeToLive() {
		return poolTimeToLive;
	}
	public void setPoolTimeToLive(Long poolTimeToLive) {
		this.poolTimeToLive = poolTimeToLive;
	}
	public Long getValidateAfterInactivity() {
		return validateAfterInactivity;
	}
	public void setValidateAfterInactivity(Long validateAfterInactivity) {
		this.validateAfterInactivity = validateAfterInactivity;
	}
	public Long getMaxIdleConnectionTtl() {
		return maxIdleConnectionTtl;
	}
	public void setMaxIdleConnectionTtl(Long maxIdleConnectionTtl) {
		this.maxIdleConnectionTtl = maxIdleConnectionTtl;
	}

	@Override
	public String toString() {
		return "ClientParameters: maxTotal: "+maxTotal+", maxPerRoute: "+maxPerRoute+", socketTimeout: "+socketTimeout+", tracePooling: "+tracePooling+", poolTimeToLive: "+poolTimeToLive+", validateAfterInactivity: "+validateAfterInactivity;
	}
}
