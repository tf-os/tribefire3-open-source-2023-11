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
package com.braintribe.model.elasticsearchreflection.nodestats;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface MergeStats extends StandardIdentifiable {

	final EntityType<MergeStats> T = EntityTypes.T(MergeStats.class);

	long getTotal();
	void setTotal(long total);

	long getTotalTimeInMillis();
	void setTotalTimeInMillis(long totalTimeInMillis);

	long getTotalNumDocs();
	void setTotalNumDocs(long totalNumDocs);

	long getTotalSizeInBytes();
	void setTotalSizeInBytes(long totalSizeInBytes);

	long getCurrent();
	void setCurrent(long current);

	long getCurrentNumDocs();
	void setCurrentNumDocs(long currentNumDocs);

	long getCurrentSizeInBytes();
	void setCurrentSizeInBytes(long currentSizeInBytes);

	long getTotalStoppedTimeInMillis();
	void setTotalStoppedTimeInMillis(long totalStoppedTimeInMillis);

	long getTotalThrottledTimeInMillis();
	void setTotalThrottledTimeInMillis(long totalThrottledTimeInMillis);

	long getTotalBytesPerSecAutoThrottle();
	void setTotalBytesPerSecAutoThrottle(long totalBytesPerSecAutoThrottle);

}
