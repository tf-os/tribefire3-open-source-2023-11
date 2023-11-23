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
package com.braintribe.model.generic.builder.meta;

import java.util.List;

import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.meta.restriction.GmTypeRestriction;

/**
 * @author peter.gazdik
 */
public class MetaModelBuilder {

	public static GmMetaModel metaModel() {
		return GmMetaModel.T.create();
	}

	public static GmMetaModel metaModel(String name) {
		GmMetaModel result = GmMetaModel.T.create();
		result.setName(name);
		result.setGlobalId(Model.modelGlobalId(name));

		return result;
	}

	public static GmEntityType entityType(String typeSignature) {
		GmEntityType result = GmEntityType.T.create();
		result.setTypeSignature(typeSignature);

		return result;
	}

	public static GmEntityType entityType(String typeSignature, List<GmEntityType> superTypes) {
		GmEntityType result = entityType(typeSignature);
		result.setSuperTypes(superTypes);

		return result;
	}

	public static GmEnumType enumType(String typeSignature) {
		GmEnumType result = GmEnumType.T.create();
		result.setTypeSignature(typeSignature);

		return result;
	}

	public static GmEnumConstant enumConstant(GmEnumType enumType, String name) {
		GmEnumConstant result = GmEnumConstant.T.create();
		result.setDeclaringType(enumType);
		result.setName(name);

		return result;
	}

	public static GmSimpleType simpleType(SimpleType simpleType) {
		switch (simpleType.getTypeCode()) {
			case booleanType:
				return booleanType();
			case dateType:
				return dateType();
			case decimalType:
				return decimalType();
			case doubleType:
				return doubleType();
			case floatType:
				return floatType();
			case integerType:
				return integerType();
			case longType:
				return longType();
			case stringType:
				return stringType();
			default:
				throw new RuntimeException("Unsupported simple type: " + simpleType);
		}
	}

	public static GmBooleanType booleanType() {
		GmBooleanType result = GmBooleanType.T.create();
		result.setTypeSignature("boolean");
		return result;
	}

	public static GmDateType dateType() {
		GmDateType result = GmDateType.T.create();
		result.setTypeSignature("date");
		return result;
	}

	public static GmDecimalType decimalType() {
		GmDecimalType result = GmDecimalType.T.create();
		result.setTypeSignature("decimal");
		return result;
	}

	public static GmDoubleType doubleType() {
		GmDoubleType result = GmDoubleType.T.create();
		result.setTypeSignature("double");
		return result;
	}

	public static GmFloatType floatType() {
		GmFloatType result = GmFloatType.T.create();
		result.setTypeSignature("float");
		return result;
	}

	public static GmIntegerType integerType() {
		GmIntegerType result = GmIntegerType.T.create();
		result.setTypeSignature("integer");
		return result;
	}

	public static GmLongType longType() {
		GmLongType result = GmLongType.T.create();
		result.setTypeSignature("long");
		return result;
	}

	public static GmStringType stringType() {
		GmStringType result = GmStringType.T.create();
		result.setTypeSignature("string");
		return result;
	}

	public static GmListType listType() {
		return GmListType.T.create();
	}

	public static GmSetType setType() {
		return GmSetType.T.create();
	}

	public static GmMapType mapType() {
		return GmMapType.T.create();
	}

	public static GmListType listType(GmType elementType) {
		GmListType result = listType();
		result.setElementType(elementType);
		result.setTypeSignature(CollectionType.TypeSignature.forList(elementType.getTypeSignature()));

		return result;
	}

	public static GmSetType setType(GmType elementType) {
		GmSetType result = setType();
		result.setElementType(elementType);
		result.setTypeSignature(CollectionType.TypeSignature.forSet(elementType.getTypeSignature()));

		return result;
	}

	public static GmMapType mapType(GmType keyType, GmType valueType) {
		GmMapType result = mapType();
		result.setKeyType(keyType);
		result.setValueType(valueType);
		result.setTypeSignature(CollectionType.TypeSignature.forMap(keyType.getTypeSignature(), valueType.getTypeSignature()));

		return result;
	}

	public static GmBaseType baseType() {
		GmBaseType result = GmBaseType.T.create();
		result.setTypeSignature("object");

		return result;
	}

	public static GmProperty property(GmEntityType entityType, String name, GmType type) {
		return property(entityType, name, type, null);
	}

	public static GmProperty property(GmEntityType entityType, String name, GmType type, GmTypeRestriction typeRestriction) {
		GmProperty result = GmProperty.T.create();
		result.setDeclaringType(entityType);
		result.setName(name);
		result.setType(type);
		result.setTypeRestriction(typeRestriction);

		return result;
	}

	public static GmTypeRestriction typeRestriction(List<GmType> types, List<GmType> keyTypes, boolean allowVd, boolean allowKeyVd) {
		GmTypeRestriction result = GmTypeRestriction.T.create();
		result.setTypes(types);
		result.setKeyTypes(keyTypes);
		result.setAllowVd(allowVd);
		result.setAllowKeyVd(allowKeyVd);

		return result;
	}

	public static QualifiedProperty qualifiedProperty(GmEntityType entityType, GmProperty property) {
		QualifiedProperty result = QualifiedProperty.T.create();
		result.setEntityType(entityType);
		result.setProperty(property);

		return result;
	}

}
