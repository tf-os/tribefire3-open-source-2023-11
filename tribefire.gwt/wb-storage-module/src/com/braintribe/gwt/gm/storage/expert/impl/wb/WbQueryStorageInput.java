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
package com.braintribe.gwt.gm.storage.expert.impl.wb;

import com.braintribe.gwt.gm.storage.api.ColumnData;
import com.braintribe.gwt.gm.storage.expert.api.QueryStorageInput;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.Query;

public interface WbQueryStorageInput extends QueryStorageInput {

	final EntityType<WbQueryStorageInput> T = EntityTypes.T(WbQueryStorageInput.class);

	// @formatter:off
	Query getQuery();
	void setQuery(Query query);

	String getQueryString();
	void setQueryString(String queryString);
	
	ColumnData getColumnData();
	void setColumnData(ColumnData columnData);
	// @formatter:on

}
