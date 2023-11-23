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

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType (namespace = GmCoreApiInteropNamespaces.metadata)
@SuppressWarnings("unusable-by-js")
public interface EnumTypeMetaDataEditor {

	/** return the underlying {@link GmEnumType} */
	GmEnumType getEnumType();

	/** Configures MD for current enum type. */
	EnumTypeMetaDataEditor configure(Consumer<GmEnumTypeInfo> consumer);

	/** Configures MD for given constant. */
	@JsMethod (name = "configureByConstant")
	EnumTypeMetaDataEditor configure(String constant, Consumer<GmEnumConstantInfo> consumer);

	/** Configures MD for given constant. */
	@JsMethod (name = "configureByEnum")
	EnumTypeMetaDataEditor configure(Enum<?> constant, Consumer<GmEnumConstantInfo> consumer);

	/** Configures MD for given constant. */
	@JsMethod (name = "configureByEnumInfo")
	EnumTypeMetaDataEditor configure(GmEnumConstantInfo constant, Consumer<GmEnumConstantInfo> consumer);

	/** Adds MD to {@link GmEnumTypeInfo#getMetaData()} */
	EnumTypeMetaDataEditor addMetaData(MetaData... mds);

	/** Removes MD from {@link GmEnumTypeInfo#getMetaData()} */
	EnumTypeMetaDataEditor removeMetaData(Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmEnumTypeInfo#getEnumConstantMetaData()} */
	EnumTypeMetaDataEditor addConstantMetaData(MetaData... mds);

	/** Removes MD from {@link GmEnumTypeInfo#getEnumConstantMetaData()} */
	EnumTypeMetaDataEditor removeConstantMetaData(Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmEnumConstantInfo#getMetaData()}, for constant/constantOverride of this enum. */
	@JsMethod (name = "addConstantMetaDataByConstant")
	EnumTypeMetaDataEditor addConstantMetaData(String constant, MetaData... mds);

	/** Removes MD from {@link GmEnumConstantInfo#getMetaData()}, for constant/constantOverride of this enum. */
	@JsMethod (name = "removeConstantMetaDataByConstant")
	EnumTypeMetaDataEditor removeConstantMetaData(String constant, Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmEnumConstantInfo#getMetaData()}, for constant/constantOverride of this enum. */
	@JsMethod (name = "addConstantMetaDataByEnum")
	EnumTypeMetaDataEditor addConstantMetaData(Enum<?> constant, MetaData... mds);

	/** Removes MD from {@link GmEnumConstantInfo#getMetaData()}, for constant/constantOverride of this enum. */
	@JsMethod (name = "removeConstantMetaDataByEnum")
	EnumTypeMetaDataEditor removeConstantMetaData(Enum<?> constant, Predicate<? super MetaData> filter);

	/** Adds MD to {@link GmEnumConstantInfo#getMetaData()}, for constant/constantOverride of this enum. */
	@JsMethod (name = "addConstantMetaDataByEnumInfo")
	EnumTypeMetaDataEditor addConstantMetaData(GmEnumConstantInfo gmConstantInfo, MetaData... mds);

	/** Removes MD from {@link GmEnumConstantInfo#getMetaData()}, for constant/constantOverride of this enum. */
	@JsMethod (name = "removeConstantMetaDataByEnumInfo")
	EnumTypeMetaDataEditor removeConstantMetaData(GmEnumConstantInfo gmConstantInfo, Predicate<? super MetaData> filter);

}
