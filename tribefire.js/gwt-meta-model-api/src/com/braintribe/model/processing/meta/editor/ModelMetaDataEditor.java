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
package com.braintribe.model.processing.meta.editor;

import java.util.function.Predicate;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * This is a simple interface for adding {@link MetaData} to a {@link GmMetaModel} programatically. Implementation of
 * this interface takes care of navigation through the underlying {@link GmMetaModel} and creating the appropriate model
 * element overrides.
 * 
 * @see EntityTypeMetaDataEditor
 * @see EnumTypeMetaDataEditor
 * 
 * @author peter.gazdik
 */
@JsType(namespace = GmCoreApiInteropNamespaces.metadata)
@SuppressWarnings("unusable-by-js")
public interface ModelMetaDataEditor {

	/** return the underlying {@link GmMetaModel} */
	GmMetaModel getMetaModel();

	/** Adds MD to {@link GmMetaModel#getMetaData()} */
	void addModelMetaData(MetaData... mds);

	/** Removes MD from {@link GmMetaModel#getMetaData()} */
	void removeModelMetaData(Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmMetaModel#getEnumTypeMetaData()} */
	void addEnumMetaData(MetaData... mds);

	/** Removes MD from {@link GmMetaModel#getEnumTypeMetaData()} */
	void removeEnumMetaData(Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmMetaModel#getEnumConstantMetaData()} */
	void addConstantMetaData(MetaData... mds);

	/** Removes MD from {@link GmMetaModel#getEnumConstantMetaData()} */
	void removeConstantMetaData(Predicate<? super MetaData> filter);

	@JsMethod(name="onEntityTypeViaTypeSignature")
	EntityTypeMetaDataEditor onEntityType(String typeSignature);

	EntityTypeMetaDataEditor onEntityType(EntityType<?> entityType);

	@JsMethod(name="onEntityTypeViaInfo")
	EntityTypeMetaDataEditor onEntityType(GmEntityTypeInfo gmEntityTypeInfo);

	/** Shortcut for {@code onEntityType(gmPropertyInfo.declaringTypeInfo).addPropertyMetaData(gmPropertyInfo, mds)} */
	EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, MetaData... mds);

	@JsMethod(name="onEnumTypeViaTypeSignature")
	EnumTypeMetaDataEditor onEnumType(String typeSignature);
	
	EnumTypeMetaDataEditor onEnumType(EnumType enumType);

	@JsMethod(name="onEnumTypeViaTypeInfo")
	EnumTypeMetaDataEditor onEnumType(GmEnumTypeInfo gmEnumTypeInfo);

	@JsMethod(name="onEnumTypeViaClass")
	EnumTypeMetaDataEditor onEnumType(Class<? extends Enum<?>> enumClass);

	/** Shortcut for {@code onEnumType(enumType).addConstantMetaData(mds)} */
	@JsMethod(name="addConstantMetaDataViaClass")
	EnumTypeMetaDataEditor addConstantMetaData(Class<? extends Enum<?>> enumType, MetaData... mds);

	/** Shortcut for {@code onEnumType(constant.getDeclaringClass()).addConstantMetaData(constant, mds)} */
	@JsMethod(name="addConstantMetaDataViaEnum")
	EnumTypeMetaDataEditor addConstantMetaData(Enum<?> constant, MetaData... mds);

	/** Shortcut for {@code onEnumType(gmConstantInfo.declaringTypeInfo()).addConstantMetaData(gmConstantInfo, mds)} */
	@JsMethod(name="addConstantMetaDataViaInfo")
	EnumTypeMetaDataEditor addConstantMetaData(GmEnumConstantInfo gmConstantInfo, MetaData... mds);

}
