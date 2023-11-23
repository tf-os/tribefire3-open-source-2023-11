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
package com.braintribe.gwt.gmview.client;

import com.braintribe.model.query.EntityQuery;

public class SelectionTabConfig {

	private EntityQuery entityQuery;
	private String tabName;

	public SelectionTabConfig(EntityQuery entityQuery) {
		this(entityQuery, null);
	}

	public SelectionTabConfig(EntityQuery entityQuery, String tabName) {
		super();
		this.tabName = tabName;
		this.entityQuery = entityQuery;
	}

	public EntityQuery getEntityQuery() {
		return this.entityQuery;
	}

	public void setEntityQuery(EntityQuery entityQuery) {
		this.entityQuery = entityQuery;
	}

	public String getTabName() {
		return this.tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}
}
