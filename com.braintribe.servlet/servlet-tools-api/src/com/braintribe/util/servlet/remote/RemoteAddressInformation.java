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

import java.util.List;

public class RemoteAddressInformation {

	protected String directClientAddress;
	protected List<String> xForwardedFor;
	protected List<Forwarded> forwarded;
	protected String xRealIp;
	protected List<String> customIpAddresses;
	

	public String getRemoteIp() {
		
		String candidate = this.directClientAddress;
		
		if (xRealIp != null && xRealIp.trim().length() > 0) {
			candidate = xRealIp;
		}
		
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			candidate = xForwardedFor.get(0);
		}
		
		if (forwarded != null && !forwarded.isEmpty()) {
			Forwarded lastForwarded = forwarded.get(0);
			String forAddress = lastForwarded.getForAddress();
			if (forAddress != null) {
				candidate = forAddress;
			}
		}
		
		if (customIpAddresses != null && !customIpAddresses.isEmpty()) {
			candidate = customIpAddresses.get(0);
		}
		
		return candidate;
	}
	
	
	public String getDirectClientAddress() {
		return directClientAddress;
	}
	public void setDirectClientAddress(String directClientAddress) {
		this.directClientAddress = directClientAddress;
	}
	public List<String> getXForwardedFor() {
		return xForwardedFor;
	}
	public void setXForwardedFor(List<String> xForwardedFor) {
		this.xForwardedFor = xForwardedFor;
	}
	public List<Forwarded> getForwarded() {
		return forwarded;
	}
	public void setForwarded(List<Forwarded> forwarded) {
		this.forwarded = forwarded;
	}
	public String getxRealIp() {
		return xRealIp;
	}
	public void setxRealIp(String xRealIp) {
		this.xRealIp = xRealIp;
	}
	public List<String> getCustomIpAddresses() {
		return customIpAddresses;
	}
	public void setCustomIpAddresses(List<String> customIpAddresses) {
		this.customIpAddresses = customIpAddresses;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Direct address: ");
		sb.append(this.directClientAddress);
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			sb.append(", X-Forwarded-For: ");
			sb.append(this.xForwardedFor);
		}
		if (forwarded != null && !forwarded.isEmpty()) {
			sb.append(", Forwarded: ");
			sb.append(this.forwarded);
		}
		if (xRealIp != null) {
			sb.append(", X-Real-IP: ");
			sb.append(this.xRealIp);
		}
		if (customIpAddresses != null && !customIpAddresses.isEmpty()) {
			sb.append(", Custom IPs: ");
			sb.append(this.customIpAddresses);			
		}
		return sb.toString();
	}
}
