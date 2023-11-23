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
package com.braintribe.model.processing.query.smart.test.model.accessB;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.accessdeployment.smart.meta.conversion.EnumToSimpleValue;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType_Identity;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType_String;

/**
 * 
 */

public interface EnumEntityB extends StandardIdentifiableB {

	final EntityType<EnumEntityB> T = EntityTypes.T(EnumEntityB.class);

	// @formatter:off
	String getName();
	void setName(String name);

	/** Mapped to {@link ItemType_String} */
	String getEnumAsString();
	void setEnumAsString(String enumAsString);

	/** Mapped to {@link ItemType} */
	ItemTypeB getEnumAsDelegate();
	void setEnumAsDelegate(ItemTypeB enumAsDelegate);

	/** Mapped to {@link ItemType_Identity} */
	ItemType_Identity getEnumIdentity();
	void setEnumIdentity(ItemType_Identity enumIdentity);

	/** Mapped to {@link ItemType} via custom {@link EnumToSimpleValue} conversion */
	Integer getEnumCustomConverted();
	void setEnumCustomConverted(Integer enumCustomConverted);

	Set<String> getEnumAsStringSet();
	void setEnumAsStringSet(Set<String> enumAsStringSet);

	Map<String, String> getEnumAsStringMap();
	void setEnumAsStringMap(Map<String, String> enumAsStringMap);

	Map<String, EnumEntityB> getEnumAsStringMapKey();
	void setEnumAsStringMapKey(Map<String, EnumEntityB> enumAsStringMapKey);

	Map<ItemTypeB, ItemTypeB> getEnumAsDelegateMap();
	void setEnumAsDelegateMap(Map<ItemTypeB, ItemTypeB> enumAsDelegateMap);

	Map<ItemTypeB, EnumEntityB> getEnumAsDelegateMapKey();
	void setEnumAsDelegateMapKey(Map<ItemTypeB, EnumEntityB> enumAsDelegateMapKey);
	// @formatter:on

}
