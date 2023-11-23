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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Partition extends GenericEntity {

	EntityType<Partition> T = EntityTypes.T(Partition.class);

	String getIdentification();
	void setIdentification(String identification);

	String getMountPoint();
	void setMountPoint(String mountPoint);

	String getName();
	void setName(String name);

	long getSize();
	void setSize(long size);

	Double getSizeInGb();
	void setSizeInGb(Double sizeInGb);

	String getType();
	void setType(String type);

	String getUuid();
	void setUuid(String uuid);

	int getMajor();
	void setMajor(int major);

	int getMinor();
	void setMinor(int minor);

}
