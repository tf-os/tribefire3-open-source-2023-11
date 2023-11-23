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
package com.braintribe.model.platformreflection.disk;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface DiskInfo extends Capacity {

	EntityType<DiskInfo> T = EntityTypes.T(DiskInfo.class);

	String getModel();
	void setModel(String model);

	String getDiskName();
	void setDiskName(String diskName);

	String getSerial();
	void setSerial(String serial);

	List<Partition> getPartitions();
	void setPartitions(List<Partition> partitions);

	long getReadBytes();
	void setReadBytes(long readBytes);

	Double getReadBytesInGb();
	void setReadBytesInGb(Double readBytesInGb);

	long getReads();
	void setReads(long reads);

	long getTimeStamp();
	void setTimeStamp(long timeStamp);

	long getTransferTime();
	void setTransferTime(long transferTime);

	long getWriteBytes();
	void setWriteBytes(long writeBytes);

	Double getWriteBytesInGb();
	void setWriteBytesInGb(Double writeBytesInGb);

	long getWrites();
	void setWrites(long writes);

}
