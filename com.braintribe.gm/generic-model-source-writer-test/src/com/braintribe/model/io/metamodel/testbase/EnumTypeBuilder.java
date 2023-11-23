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
package com.braintribe.model.io.metamodel.testbase;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;

/**
 * 
 */
public class EnumTypeBuilder {

	private final GmEnumType type;
	private final Map<String, GmEnumConstant> constantByName = newMap();

	public EnumTypeBuilder(String typeSignature) {
		type = GmEnumType.T.create("type:" + typeSignature);
		type.setTypeSignature(typeSignature);
	}

	public EnumTypeBuilder addMd(MetaData... md) {
		type.getMetaData().addAll(Arrays.asList(md));
		return this;
	}

	public EnumTypeBuilder setConstants(String... values) {
		List<GmEnumConstant> constants = new ArrayList<GmEnumConstant>();

		for (String s : values) {
			GmEnumConstant c = GmEnumConstant.T.create("enum:" + type.getTypeSignature() + "/" + s);
			c.setName(s);
			c.setDeclaringType(type);

			constants.add(c);
			constantByName.put(s, c);
		}

		type.setConstants(constants);

		return this;
	}

	public EnumTypeBuilder addConstantMd(String constName, MetaData... md) {
		constantByName.get(constName).getMetaData().addAll(asList(md));
		return this;
	}

	public GmEnumType create() {
		return type;
	}

}
