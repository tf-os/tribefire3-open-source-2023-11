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
package com.braintribe.model.processing.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Max;
import com.braintribe.model.generic.annotation.meta.MaxLength;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Pattern;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ToStringInformation("|${#type_short}: ${name}|")
public interface SelfContaining extends GenericEntity {
	EntityType<SelfContaining> T = EntityTypes.T(SelfContaining.class);

	void setChild(SelfContaining selfContaining);
	SelfContaining getChild();

	void setListOfChildren(List<SelfContaining> list);
	List<SelfContaining> getListOfChildren();

	void setSetOfChildren(Set<SelfContaining> list);
	Set<SelfContaining> getSetOfChildren();

	void setMapOfChildren(Map<SelfContaining, SelfContaining> map);
	Map<SelfContaining, SelfContaining> getMapOfChildren();

	@Mandatory
	@MaxLength(23)
	@MinLength(6)
	@Pattern("child.*")
	String getName();
	void setName(String name);

	@Max("130L")
	@Min("0L")
	Long getAge();
	void setAge(Long age);

	@Deprecated
	@Max("1.2345f")
	float getPoints();
	void setPoints(float points);
}
