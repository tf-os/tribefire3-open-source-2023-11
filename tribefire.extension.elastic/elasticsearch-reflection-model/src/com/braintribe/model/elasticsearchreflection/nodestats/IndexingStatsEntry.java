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

public interface IndexingStatsEntry extends StandardIdentifiable {

	final EntityType<IndexingStatsEntry> T = EntityTypes.T(IndexingStatsEntry.class);

	long getIndexCount();
	void setIndexCount(long indexCount);

	long getIndexTimeInMillis();
	void setIndexTimeInMillis(long indexTimeInMillis);

	long getIndexCurrent();
	void setIndexCurrent(long indexCurrent);

	long getIndexFailedCount();
	void setIndexFailedCount(long indexFailedCount);

	long getDeleteCount();
	void setDeleteCount(long deleteCount);

	long getDeleteTimeInMillis();
	void setDeleteTimeInMillis(long deleteTimeInMillis);

	long getDeleteCurrent();
	void setDeleteCurrent(long deleteCurrent);

	long getNoopUpdateCount();
	void setNoopUpdateCount(long noopUpdateCount);

	long getThrottleTimeInMillis();
	void setThrottleTimeInMillis(long throttleTimeInMillis);

	boolean getIsThrottled();
	void setIsThrottled(boolean isThrottled);

}
