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
package com.braintribe.model.processing.session.impl.persistence.eagerloader.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
@ToStringInformation("${name}")
public interface EagerOwner extends GenericEntity {

	EntityType<EagerOwner> T = EntityTypes.T(EagerOwner.class);

	String getName();
	void setName(String name);

	EagerItem getEntity();
	void setEntity(EagerItem entity);

	List<String> getStringList();
	void setStringList(List<String> stringList);

	Set<String> getStringSet();
	void setStringSet(Set<String> stringSet);

	Map<Integer, String> getIntegerStringMap();
	void setIntegerStringMap(Map<Integer, String> integerStringMap);

	List<EagerItem> getEntityList();
	void setEntityList(List<EagerItem> entityList);

	Set<EagerItem> getEntitySet();
	void setEntitySet(Set<EagerItem> entitySet);

	Map<EagerItem, EagerItem> getEntityMap();
	void setEntityMap(Map<EagerItem, EagerItem> entityMap);

}
