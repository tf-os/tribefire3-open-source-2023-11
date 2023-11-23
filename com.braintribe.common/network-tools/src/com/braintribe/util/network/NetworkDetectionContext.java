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
package com.braintribe.util.network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class NetworkDetectionContext {

	protected NetworkInterface networkInterface;
	protected Inet4Address inet4Address;
	protected Inet6Address inet6Address;
	protected String macAddress;

	public NetworkDetectionContext() {
	}

	public boolean isLoopback() {
		if (this.inet4Address != null) {
			return this.inet4Address.isLoopbackAddress();
		}
		if (this.inet6Address != null) {
			return this.inet6Address.isLoopbackAddress();
		}
		return true;
	}
	public boolean isLinkLocal4() {
		if (this.inet4Address != null) {
			return this.inet4Address.isLinkLocalAddress();
		}
		return false;
	}
	public boolean isLinkLocal6() {
		if (this.inet6Address != null) {
			return this.inet6Address.isLinkLocalAddress();
		}
		return false;
	}

	public boolean isVmNet() {
		if (this.networkInterface == null) {
			return false;
		}

		String name = this.networkInterface.getName();
		if (name == null) {
			return false;
		}
		if (name.toLowerCase().indexOf("vmnet") != -1) {
			return true;
		}
		return false;
	}
	public boolean isOfType(Class<? extends InetAddress> type) {
		if (type == null) {
			return this.inet4Address != null || this.inet6Address != null;
		}
		if (type.isAssignableFrom(Inet4Address.class)) {
			return this.inet4Address != null;
		}
		if (type.isAssignableFrom(Inet6Address.class)) {
			return this.inet6Address != null;
		}
		throw new IllegalArgumentException("Unsupported InetAddress type " + type);
	}

	public NetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}

	public Inet4Address getInet4Address() {
		return inet4Address;
	}

	public void setInet4Address(Inet4Address inet4Address) {
		this.inet4Address = inet4Address;
	}

	public Inet6Address getInet6Address() {
		return inet6Address;
	}

	public void setInet6Address(Inet6Address inet6Address) {
		this.inet6Address = inet6Address;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.networkInterface != null) {
			sb.append(this.networkInterface.getName());
		} else {
			sb.append("No network interface.");
		}
		sb.append(";");
		if (this.inet4Address != null) {
			sb.append("IP4:");
			sb.append(this.inet4Address.getHostAddress());
			sb.append(";");
		}
		if (this.inet6Address != null) {
			sb.append("IP6:");
			sb.append(this.inet6Address.getHostAddress());
			sb.append(";");
		}
		if (this.macAddress != null) {
			sb.append("mac:");
			sb.append(this.macAddress);
			sb.append(";");
		}
		sb.append("Loopback:");
		sb.append(this.isLoopback());
		sb.append(";");
		sb.append("Link-local(IPv4):");
		sb.append(this.isLinkLocal4());
		sb.append(";");
		sb.append("Link-local(IPv6):");
		sb.append(this.isLinkLocal6());
		return sb.toString();
	}

}
