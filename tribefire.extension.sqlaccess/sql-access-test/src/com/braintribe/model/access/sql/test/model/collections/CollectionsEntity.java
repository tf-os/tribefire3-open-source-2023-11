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
package com.braintribe.model.access.sql.test.model.collections;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.access.sql.test.model.SqlAccessEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface CollectionsEntity extends SqlAccessEntity {

	EntityType<CollectionsEntity> T = EntityTypes.T(CollectionsEntity.class);

	String stringList = "stringList";
	String stringSet = "stringSet";
	String integerStringMap = "integerStringMap";
	String entityList = "entityList";
	String entitySet = "entitySet";
	String integerEntityMap = "integerEntityMap";
	String entityIntegerMap = "entityIntegerMap";

	List<String> getStringList();
	void setStringList(List<String> stringList);

	Set<String> getStringSet();
	void setStringSet(Set<String> stringSet);

	Map<Integer, String> getIntegerStringMap();
	void setIntegerStringMap(Map<Integer, String> integerStringMap);

	List<CollectionsEntity> getEntityList();
	void setEntityList(List<CollectionsEntity> entityList);

	Set<CollectionsEntity> getEntitySet();
	void setEntitySet(Set<CollectionsEntity> entitySet);

	Map<Integer, CollectionsEntity> getIntegerEntityMap();
	void setIntegerEntityMap(Map<Integer, CollectionsEntity> integerEntityMap);

	Map<CollectionsEntity, Integer> getEntityIntegerMap();
	void setEntityIntegerMap(Map<CollectionsEntity, Integer> entityIntegerMap);

}
