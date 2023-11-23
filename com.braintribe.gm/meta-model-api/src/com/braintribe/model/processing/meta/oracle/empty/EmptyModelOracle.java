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
package com.braintribe.model.processing.meta.oracle.empty;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
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
import com.braintribe.model.processing.meta.oracle.ElementInOracleNotFoundException;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelDependencies;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelTypes;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.TypeOracle;

/**
 * @author peter.gazdik
 */
public class EmptyModelOracle implements ModelOracle {

	public static final EmptyModelOracle INSTANCE = new EmptyModelOracle();

	protected final EntityTypeOracle emptyEntityTypeOracle = new EmptyEntityTypeOracle(this);
	protected final EnumTypeOracle emptyEnumTypeOracle = new EmptyEnumTypeOracle(this);

	protected EmptyModelOracle() {
		// let's allow sub-types
	}

	// @formatter:off
	@Override public GmMetaModel getGmMetaModel() { return cannotGet("metaModel"); }

	@Override public GmBaseType getGmBaseType() { return cannotGet("base type"); }
	
	@Override public GmStringType getGmStringType(){ return cannotGet("string type"); }
	@Override public GmFloatType getGmFloatType(){ return cannotGet("float type"); }
	@Override public GmDoubleType getGmDoubleType(){ return cannotGet("double type"); }
	@Override public GmBooleanType getGmBooleanType(){ return cannotGet("boolean type"); }
	@Override public GmIntegerType getGmIntegerType(){ return cannotGet("integer type"); }
	@Override public GmLongType getGmLongType(){ return cannotGet("long type"); }
	@Override public GmDateType getGmDateType(){ return cannotGet("date type"); }
	@Override public GmDecimalType getGmDecimalType(){ return cannotGet("decimal type"); }

	@Override public List<GmSimpleType> getGmSimpleTypes() { return cannotGet("simple types"); }
	// @formatter:on

	private <T> T cannotGet(String whatWeCannotGet) {
		throw new GenericModelException("Cannot get " + whatWeCannotGet + " from empty ModelOracle.");
	}

	@Override
	public <T extends GmType> T findGmType(GenericModelType type) {
		return null;
	}

	@Override
	public <T extends GmType> T findGmType(String typeSignature) {
		return null;
	}

	@Override
	public <T extends GmCustomType> List<T> findGmTypeBySimpleName(String simpleTypeName) {
		return emptyList();
	}

	@Override
	public ModelTypes getTypes() {
		return EmptyModelTypes.INSTANCE;
	}

	@Override
	public ModelDependencies getDependencies() {
		return EmptyModelDependencies.INSTANCE;
	}

	@Override
	public <T extends TypeOracle> T getTypeOracle(String customTypeSignature) {
		throw new ElementInOracleNotFoundException("Custom type not found: " + customTypeSignature);
	}

	@Override
	public <T extends TypeOracle> T getTypeOracle(CustomType type) {
		if (type.isEntity())
			return (T) getEntityTypeOracle(type.getTypeSignature());
		else
			return (T) getEnumTypeOracle(type.getTypeSignature());
	}

	@Override
	public <T extends TypeOracle> T getTypeOracle(GmCustomType type) {
		if (type.isGmEntity())
			return (T) getEntityTypeOracle(type.getTypeSignature());
		else
			return (T) getEnumTypeOracle(type.getTypeSignature());
	}

	@Override
	public EntityTypeOracle getEntityTypeOracle(String typeSignature) {
		return emptyEntityTypeOracle;
	}

	@Override
	public EntityTypeOracle getEntityTypeOracle(GmEntityType type) {
		return getEntityTypeOracle(type.getTypeSignature());
	}

	@Override
	public EntityTypeOracle getEntityTypeOracle(EntityType<?> type) {
		return getEntityTypeOracle(type.getTypeSignature());
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle(String typeSignature) {
		return emptyEnumTypeOracle;
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle(GmEnumType type) {
		return getEnumTypeOracle(type.getTypeSignature());
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle(EnumType type) {
		return getEnumTypeOracle(type.getTypeSignature());
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle(Class<? extends Enum<?>> enumClass) {
		return getEnumTypeOracle(enumClass.getName());
	}

	@Override
	public <T extends TypeOracle> T findTypeOracle(String customTypeSignature) {
		return null;
	}

	@Override
	public <T extends TypeOracle> T findTypeOracle(CustomType type) {
		return null;
	}

	@Override
	public <T extends TypeOracle> T findTypeOracle(GmCustomType type) {
		return null;
	}

	@Override
	public EntityTypeOracle findEntityTypeOracle(String typeSignature) {
		return emptyEntityTypeOracle;
	}

	@Override
	public EntityTypeOracle findEntityTypeOracle(GmEntityType type) {
		return findEntityTypeOracle(type.getTypeSignature());
	}

	@Override
	public EntityTypeOracle findEntityTypeOracle(EntityType<?> type) {
		return findEntityTypeOracle(type.getTypeSignature());
	}

	@Override
	public EnumTypeOracle findEnumTypeOracle(String typeSignature) {
		return emptyEnumTypeOracle;
	}

	@Override
	public EnumTypeOracle findEnumTypeOracle(GmEnumType type) {
		return findEnumTypeOracle(type.getTypeSignature());
	}

	@Override
	public EnumTypeOracle findEnumTypeOracle(EnumType type) {
		return findEnumTypeOracle(type.getTypeSignature());
	}

	@Override
	public EnumTypeOracle findEnumTypeOracle(Class<? extends Enum<?>> enumClass) {
		return findEnumTypeOracle(enumClass.getName());
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<MetaData> getEnumMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<MetaData> getEnumConstantMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedEnumMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedEnumConstantMetaData() {
		return Stream.empty();
	}

}
