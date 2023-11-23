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
package com.braintribe.model.processing.query.smart.test.model.smart;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;

/**
 * Mapped to {@link ItemB}
 */
@ToStringInformation("SmartItem[${nameB}]")
public interface SmartItem extends StandardSmartIdentifiable, BasicSmartEntity {
	
	EntityType<SmartItem> T = EntityTypes.T(SmartItem.class);

	String getNameB();
	void setNameB(String nameB);

	LocalizedString getLocalizedNameB();
	void setLocalizedNameB(LocalizedString localizedNameB);

	SmartPersonB getOwnerB();
	void setOwnerB(SmartPersonB ownerB);

	ItemType getItemType();
	void setItemType(ItemType itemType);

	Set<ItemType> getItemTypes();
	void setItemTypes(Set<ItemType> itemTypes);

	Map<ItemType, Integer> getItemTypeWeights();
	void setItemTypeWeights(Map<ItemType, Integer> itemTypeWeights);

}
