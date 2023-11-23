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
package com.braintribe.model.processing.meta.oracle.flat;

import static com.braintribe.utils.lcd.CollectionTools2.index;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.ReflectionTools.getSimpleName;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;

/**
 * @author peter.gazdik
 */
public class FlatModel {

	public final GmMetaModel model;
	public final Set<GmMetaModel> allModels = newLinkedSet();
	public final Map<GmMetaModel, Integer> modelIndex = newMap();

	public GmBaseType gmBaseType;
	public GmStringType gmStringType;
	public GmFloatType gmFloatType;
	public GmDoubleType gmDoubleType;
	public GmBooleanType gmBooleanType;
	public GmIntegerType gmIntegerType;
	public GmLongType gmLongType;
	public GmDateType gmDateType;
	public GmDecimalType gmDecimalType;
	public List<GmSimpleType> simpleTypes = newList();

	public final Map<GmEntityType, Set<GmEntityType>> subTypes = newMap();
	public final Map<GmEntityType, Set<GmEntityType>> superTypes = newLinkedMap();

	public final Map<String, GmType> allTypes = newMap();
	public final Map<String, FlatCustomType<?, ?>> flatCustomTypes = newMap();

	private Map<String, List<GmCustomType>> customTypesBySimpleTypeName;

	public FlatModel(GmMetaModel model) {
		this.model = model;
	}

	public <F extends FlatCustomType<?, ?>> F getFlatCustomType(String typeSignature) {
		return (F) flatCustomTypes.get(typeSignature);
	}

	public <T extends GmCustomType> List<T> getFlatCustomTypesBySimpleName(String simpleTypeName) {
		if (customTypesBySimpleTypeName == null)
			customTypesBySimpleTypeName = mapCustomTypesBySimpleName();

		List<GmCustomType> result = customTypesBySimpleTypeName.get(simpleTypeName);

		return result != null ? (List<T>) result : emptyList();
	}

	private Map<String, List<GmCustomType>> mapCustomTypesBySimpleName() {

		return index(flatCustomTypes.values().stream().<GmCustomType> map(fct -> fct.type)) //
				.by(FlatModel::getSimpleTypeName) //
				.multi().please();
	}

	private static String getSimpleTypeName(GmCustomType type) {
		return getSimpleName(type.getTypeSignature());
	}

	@Override
	public String toString() {
		return "FlatModel:".concat(model != null ? model.toString() : "<null>");
	}
}
