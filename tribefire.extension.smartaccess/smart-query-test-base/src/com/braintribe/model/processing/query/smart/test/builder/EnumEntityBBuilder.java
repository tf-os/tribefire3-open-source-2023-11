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

import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessB.EnumEntityB;
import com.braintribe.model.processing.query.smart.test.model.accessB.ItemTypeB;
import com.braintribe.model.processing.query.smart.test.model.smart.ItemType_Identity;

/**
 * 
 */
public class EnumEntityBBuilder extends AbstractBuilder<EnumEntityB, EnumEntityBBuilder> {

	public static EnumEntityBBuilder newInstance(SmartDataBuilder dataBuilder) {
		return new EnumEntityBBuilder(dataBuilder.repoDriver());
	}

	public EnumEntityBBuilder(RepositoryDriver repoDriver) {
		super(EnumEntityB.class, repoDriver);
	}

	public EnumEntityBBuilder name(String value) {
		instance.setName(value);
		return this;
	}

	public EnumEntityBBuilder enumIdentity(ItemType_Identity value) {
		instance.setEnumIdentity(value);
		return this;
	}

	public EnumEntityBBuilder enumAsString(String value) {
		instance.setEnumAsString(value);
		return this;
	}

	public EnumEntityBBuilder enumAsDelegate(ItemTypeB value) {
		instance.setEnumAsDelegate(value);
		return this;
	}

	public EnumEntityBBuilder enumCustomConverted(Integer value) {
		instance.setEnumCustomConverted(value);
		return this;
	}

	public EnumEntityBBuilder enumAsStringSet(String... values) {
		instance.setEnumAsStringSet(asSet(values));
		return this;
	}

	public EnumEntityBBuilder enumAsStringMap(String... values) {
		instance.setEnumAsStringMap(asMap((Object[]) values));
		return this;
	}

	public EnumEntityBBuilder enumAsStringMapKey(Object... values) {
		instance.setEnumAsStringMapKey(asMap(values));
		return this;
	}

	public EnumEntityBBuilder enumAsDelegateMap(ItemTypeB... values) {
		instance.setEnumAsDelegateMap(asMap((Object[]) values));
		return this;
	}

	public EnumEntityBBuilder enumAsDelegateMapKey(Object... values) {
		instance.setEnumAsDelegateMapKey(asMap(values));
		return this;
	}

}
