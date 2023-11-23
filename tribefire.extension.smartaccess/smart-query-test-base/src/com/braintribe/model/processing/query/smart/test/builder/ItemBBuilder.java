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
package com.braintribe.model.processing.query.smart.test.builder;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemB;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemTypeB;

/**
 * 
 */
public class ItemBBuilder extends AbstractBuilder<ItemB, ItemBBuilder> {

	public static ItemBBuilder newInstance(SmartDataBuilder dataBuilder) {
		return new ItemBBuilder(dataBuilder.repoDriver());
	}

	public ItemBBuilder(RepositoryDriver repoDriver) {
		super(ItemB.class, repoDriver);
	}

	public ItemBBuilder nameB(String value) {
		instance.setNameB(value);
		return this;
	}

	public ItemBBuilder type(ItemTypeB type) {
		instance.setItemType(type);
		return this;
	}

	public ItemBBuilder types(ItemTypeB... types) {
		instance.setItemTypes(asSet(types));
		return this;
	}

	public ItemBBuilder singleOwnerName(String value) {
		instance.setSingleOwnerName(value);
		return this;
	}

	public ItemBBuilder multiOwnerName(String value) {
		instance.setMultiOwnerName(value);
		return this;
	}

	public ItemBBuilder sharedOwnerNames(String... value) {
		instance.setSharedOwnerNames(asSet(value));
		return this;
	}

	public ItemBBuilder multiSharedOwnerNames(String... value) {
		instance.setMultiSharedOwnerNames(asSet(value));
		return this;
	}

	public ItemBBuilder localizedName(String... os) {
		RepositoryDriver lsDriver = repoDriver.newRepoDriver();

		LocalizedString ls = lsDriver.newInstance(LocalizedString.class);
		ls.setLocalizedValues(asMap((Object[]) os));
		lsDriver.commit();

		instance.setLocalizedNameB(ls);

		return this;
	}

}
