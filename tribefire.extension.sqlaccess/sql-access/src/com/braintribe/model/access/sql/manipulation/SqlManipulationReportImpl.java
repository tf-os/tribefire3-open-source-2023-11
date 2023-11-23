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
package com.braintribe.model.access.sql.manipulation;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.access.sql.SqlManipulationReport;
import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
public class SqlManipulationReportImpl implements SqlManipulationReport {

	public final List<GenericEntity> newEntities = newList();
	public final List<GenericEntity> existingEntities = newList();

	public final Map<GenericEntity, String> assignedIds = newMap();
	public final Map<GenericEntity, String> assignedGlobalIds = newMap();
	public final Map<GenericEntity, String> assignedPartitions = newMap();

	@Override
	public List<GenericEntity> getNewEntities() {
		return newEntities;
	}

	@Override
	public List<GenericEntity> getExistingEntities() {
		return existingEntities;
	}

	@Override
	public Map<GenericEntity, String> getAssignedIds() {
		return assignedIds;
	}

	@Override
	public Map<GenericEntity, String> getAssignedGlobalIds() {
		return assignedGlobalIds;
	}

	@Override
	public Map<GenericEntity, String> getAssignedPartitions() {
		return assignedPartitions;
	}

}
