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
package com.braintribe.model.platformreflection.hotthreads;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformreflection.request.PlatformReflectionResponse;


public interface HotThreads extends PlatformReflectionResponse {

	EntityType<HotThreads> T = EntityTypes.T(HotThreads.class);
	
	Date getTimestamp();
	void setTimestamp(Date timestamp);
	
	long getIntervalInMs();
	void setIntervalInMs(long intervalInMs);
	
	int getNoOfBusiestThreads();
	void setNoOfBusiestThreads(int noOfBusiestThreads);
	
	boolean getIgnoreIdleThreads();
	void setIgnoreIdleThreads(boolean ignoreIdleThreads);
	
	String getType();
	void setType(String type);

	int getThreadElementsSnapshotCount();
	void setThreadElementsSnapshotCount(int threadElementsSnapshotCount);
	
	long getThreadElementsSnapshotDelayInMs();
	void setThreadElementsSnapshotDelayInMs(long threadElementsSnapshotDelayInMs);

	List<HotThread> getHotThreadList();
	void setHotThreadList(List<HotThread> hotThreadList);
	
}
