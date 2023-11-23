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
package com.braintribe.model.processing.meta.oracle;

import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;

/**
 * Provides handy interface for getting information about the model skeleton, like sub/super types of entity types.
 * 
 * {@link ModelOracle}, as well as all the related components like {@link EntityTypeOracle} or
 * {@link ModelDependencies}, are meant to be thread safe.
 * 
 * NOTE: Currently only information about sub/super types can be retrieved. If you need something different, let us
 * (PGA, DSC) know and we can add some features.
 * 
 * @author peter.gazdik
 */
public interface ModelOracle {

	GmMetaModel getGmMetaModel();

	GmBaseType getGmBaseType();

	GmStringType getGmStringType();
	GmFloatType getGmFloatType();
	GmDoubleType getGmDoubleType();
	GmBooleanType getGmBooleanType();
	GmIntegerType getGmIntegerType();
	GmLongType getGmLongType();
	GmDateType getGmDateType();
	GmDecimalType getGmDecimalType();

	List<GmSimpleType> getGmSimpleTypes();

	<T extends GmType> T findGmType(GenericModelType type);

	<T extends GmType> T findGmType(String typeSignature);

	<T extends GmCustomType> List<T> findGmTypeBySimpleName(String simpleTypeName);

	/** @see ModelTypes */
	ModelTypes getTypes();

	/** @see ModelDependencies */
	ModelDependencies getDependencies();

	// ################################################
	// ## . . . . . . . Get TypeOracle . . . . . . . ##
	// ################################################

	/** @return {@link TypeOracle} for a custom type given by it's type signature. */
	<T extends TypeOracle> T getTypeOracle(String customTypeSignature);

	/** @return {@link TypeOracle} for given {@link CustomType}. */
	<T extends TypeOracle> T getTypeOracle(CustomType type);

	/** @return {@link TypeOracle} for given {@link GmCustomType}. */
	<T extends TypeOracle> T getTypeOracle(GmCustomType type);

	/** Same as {@link #getTypeOracle(String)}. */
	EntityTypeOracle getEntityTypeOracle(String typeSignature);

	/** Same as {@link #getTypeOracle(GmCustomType)}. */
	EntityTypeOracle getEntityTypeOracle(GmEntityType type);

	/** Same as {@link #getTypeOracle(CustomType)}. */
	EntityTypeOracle getEntityTypeOracle(EntityType<?> type);

	/** Same as {@link #getTypeOracle(String)}. */
	EnumTypeOracle getEnumTypeOracle(String typeSignature);

	/** Same as {@link #getTypeOracle(GmCustomType)}. */
	EnumTypeOracle getEnumTypeOracle(GmEnumType type);

	/** Same as {@link #getTypeOracle(CustomType)}. */
	EnumTypeOracle getEnumTypeOracle(EnumType type);

	/** @return {@link EnumTypeOracle} for given {@link Enum} class. */
	EnumTypeOracle getEnumTypeOracle(Class<? extends Enum<?>> enumClass);

	// ################################################
	// ## . . . . . . . Find TypeOracle . . . . . . .##
	// ################################################

	/** @return {@link TypeOracle} for a custom type given by it's type signature or <code>null</code>. */
	<T extends TypeOracle> T findTypeOracle(String customTypeSignature);

	/** @return {@link TypeOracle} for given {@link CustomType} or <code>null</code>. */
	<T extends TypeOracle> T findTypeOracle(CustomType type);

	/** @return {@link TypeOracle} for given {@link GmCustomType} or <code>null</code>. */
	<T extends TypeOracle> T findTypeOracle(GmCustomType type);

	/** Same as {@link #findTypeOracle(String)}. */
	EntityTypeOracle findEntityTypeOracle(String typeSignature);

	/** Same as {@link #findTypeOracle(GmCustomType)}. */
	EntityTypeOracle findEntityTypeOracle(GmEntityType type);

	/** Same as {@link #findTypeOracle(CustomType)}. */
	EntityTypeOracle findEntityTypeOracle(EntityType<?> type);

	/** Same as {@link #findTypeOracle(String)}. */
	EnumTypeOracle findEnumTypeOracle(String typeSignature);

	/** Same as {@link #findTypeOracle(GmCustomType)}. */
	EnumTypeOracle findEnumTypeOracle(GmEnumType type);

	/** Same as {@link #findTypeOracle(CustomType)}. */
	EnumTypeOracle findEnumTypeOracle(EnumType type);

	/** @return {@link EnumTypeOracle} for given {@link Enum} class. */
	EnumTypeOracle findEnumTypeOracle(Class<? extends Enum<?>> enumClass);

	// ################################################
	// ## . . . . . . . . MetaData . . . . . . . . . ##
	// ################################################

	/**
	 * @return {@link Stream} of all {@link MetaData} defined for this model and all it's dependencies, where
	 *         dependencies are processed in the breadth-first order.
	 */
	Stream<MetaData> getMetaData();

	Stream<MetaData> getEnumMetaData();

	Stream<MetaData> getEnumConstantMetaData();

	Stream<QualifiedMetaData> getQualifiedMetaData();

	Stream<QualifiedMetaData> getQualifiedEnumMetaData();

	Stream<QualifiedMetaData> getQualifiedEnumConstantMetaData();
}
