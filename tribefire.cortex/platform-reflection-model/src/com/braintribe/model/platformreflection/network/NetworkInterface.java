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
package com.braintribe.model.platformreflection.network;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface NetworkInterface extends GenericEntity {

	EntityType<NetworkInterface> T = EntityTypes.T(NetworkInterface.class);

	String getDisplayName();
	void setDisplayName(String displayName);

	List<String> getIPv4Addresses();
	void setIPv4Addresses(List<String> iPv4Addresses);

	List<String> getIPv6Addresses();
	void setIPv6Addresses(List<String> iPv6Addresses);

	String getMacAddress();
	void setMacAddress(String macAddress);

	long getMtu();
	void setMtu(long mtu);

	String getName();
	void setName(String name);

	long getSpeed();
	void setSpeed(long speed);

	String getSpeedDisplay();
	void setSpeedDisplay(String speedDisplay);

	long getBytesRecv();
	void setBytesRecv(long bytesRecv);

	Double getBytesRecvInGb();
	void setBytesRecvInGb(Double bytesRecvInGb);

	long getBytesSent();
	void setBytesSent(long bytesSent);

	Double getBytesSentInGb();
	void setBytesSentInGb(Double bytesSentInGb);

	long getPacketsRecv();
	void setPacketsRecv(long packetsRecv);

	long getPacketsSent();
	void setPacketsSent(long packetsSent);

}
