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
package com.braintribe.util.servlet.remote;

/**
 * See https://tools.ietf.org/html/rfc7239
 * 
 * @author roman.kurmanowytsch
 */
public class Forwarded {

	protected String forAddress;
	protected String proto;
	protected String byAddress;
	protected String host;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getForAddress() {
		return forAddress;
	}
	public void setForAddress(String forAddress) {
		this.forAddress = forAddress;
	}
	public String getProto() {
		return proto;
	}
	public void setProto(String proto) {
		this.proto = proto;
	}
	public String getByAddress() {
		return byAddress;
	}
	public void setByAddress(String byAddress) {
		this.byAddress = byAddress;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if (this.forAddress != null) {
			sb.append("For: ");
			sb.append(this.forAddress);
		}
		if (this.byAddress != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append(", By: ");
			sb.append(this.byAddress);
		}
		if (this.proto != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append("Proto: ");
			sb.append(this.proto);
		}
		if (this.host != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append("Host: ");
			sb.append(this.host);
		}
		
		sb.append("}");
		return sb.toString();
	}
}
