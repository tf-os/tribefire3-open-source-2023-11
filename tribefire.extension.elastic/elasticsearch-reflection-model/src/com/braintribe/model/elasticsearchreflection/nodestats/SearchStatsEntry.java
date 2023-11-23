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

public interface SearchStatsEntry extends StandardIdentifiable {

	final EntityType<SearchStatsEntry> T = EntityTypes.T(SearchStatsEntry.class);

	long getQueryCount();
	void setQueryCount(long queryCount);

	long getQueryTimeInMillis();
	void setQueryTimeInMillis(long queryTimeInMillis);

	long getQueryCurrent();
	void setQueryCurrent(long queryCurrent);

	long getFetchCount();
	void setFetchCount(long fetchCount);

	long getFetchTimeInMillis();
	void setFetchTimeInMillis(long fetchTimeInMillis);

	long getFetchCurrent();
	void setFetchCurrent(long fetchCurrent);

	long getScrollCount();
	void setScrollCount(long scrollCount);

	long getScrollTimeInMillis();
	void setScrollTimeInMillis(long scrollTimeInMillis);

	long getScrollCurrent();
	void setScrollCurrent(long scrollCurrent);

	long getSuggestCount();
	void setSuggestCount(long suggestCount);

	long getSuggestTimeInMillis();
	void setSuggestTimeInMillis(long suggestTimeInMillis);

	long getSuggestCurrent();
	void setSuggestCurrent(long suggestCurrent);

}
