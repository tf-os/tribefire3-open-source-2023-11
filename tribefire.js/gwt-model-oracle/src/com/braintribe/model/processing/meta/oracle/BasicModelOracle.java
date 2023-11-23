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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
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
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.oracle.empty.EmptyModelOracle;
import com.braintribe.model.processing.meta.oracle.flat.FlatCustomType;
import com.braintribe.model.processing.meta.oracle.flat.FlatEntityType;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumType;
import com.braintribe.model.processing.meta.oracle.flat.FlatModel;
import com.braintribe.model.processing.meta.oracle.flat.FlatModelFactory;

/**
 * A standard {@link ModelOracle} implementation.
 * 
 * This class and all the other that implement the model oracle API are thread safe and made with concurrency in mind.
 * 
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class BasicModelOracle extends EmptyModelOracle {

	public static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	protected final FlatModel flatModel;

	protected final EntityOraclesIndex entityTypeOracles = new EntityOraclesIndex();
	protected final EnumOraclesIndex enumTypeOracles = new EnumOraclesIndex();

	// #################################################
	// ## . . . . . . . Constructors . . . . . . . . .##
	// #################################################

	/**
	 * @throws GenericModelException
	 *             in case given model is not correct, e.g. the model or one of it's dependencies has no dependency and
	 *             is not a RootModel.
	 */
	public BasicModelOracle(GmMetaModel model) {
		if (model == null) {
			throw new IllegalArgumentException("Model cannot be null!");
		}

		this.flatModel = FlatModelFactory.buildFor(model);
	}

	// #################################################
	// ## . . . . . . . Basic type stuff . . . . . . .##
	// #################################################

	public EntityTypeOracle getEmptyEntityTypeOracle() {
		return emptyEntityTypeOracle;
	}

	public EnumTypeOracle getEmptyEnumTypeOracle() {
		return emptyEnumTypeOracle;
	}

	@Override
	public GmMetaModel getGmMetaModel() {
		return flatModel.model;
	}

	@Override
	public GmBaseType getGmBaseType() {
		return flatModel.gmBaseType;
	}

	@Override
	public GmStringType getGmStringType() {
		return flatModel.gmStringType;
	}

	@Override
	public GmFloatType getGmFloatType() {
		return flatModel.gmFloatType;
	}

	@Override
	public GmDoubleType getGmDoubleType() {
		return flatModel.gmDoubleType;
	}

	@Override
	public GmBooleanType getGmBooleanType() {
		return flatModel.gmBooleanType;
	}

	@Override
	public GmIntegerType getGmIntegerType() {
		return flatModel.gmIntegerType;
	}

	@Override
	public GmLongType getGmLongType() {
		return flatModel.gmLongType;
	}

	@Override
	public GmDateType getGmDateType() {
		return flatModel.gmDateType;
	}

	@Override
	public GmDecimalType getGmDecimalType() {
		return flatModel.gmDecimalType;
	}

	@Override
	public <T extends GmType> T findGmType(GenericModelType type) {
		return findGmType(type.getTypeSignature());
	}

	@Override
	public <T extends GmType> T findGmType(String typeSignature) {
		return (T) flatModel.allTypes.get(typeSignature);
	}

	@Override
	public <T extends GmCustomType> List<T> findGmTypeBySimpleName(String simpleTypeName) {
		return flatModel.getFlatCustomTypesBySimpleName(simpleTypeName);
	}

	@Override
	public List<GmSimpleType> getGmSimpleTypes() {
		return flatModel.simpleTypes;
	}

	// #################################################
	// ## . . . . . . Entity/Enum Oracles . . . . . . ##
	// #################################################

	class EntityOraclesIndex extends ConcurrentCachedIndex<String, EntityTypeOracle> {
		@Override
		protected EntityTypeOracle provideValueFor(String typeSignature) {
			FlatEntityType flatEntityType = findFlatCustomType(typeSignature);
			return flatEntityType != null ? new BasicEntityTypeOracle(BasicModelOracle.this, flatEntityType) : emptyEntityTypeOracle;
		}
	}

	class EnumOraclesIndex extends ConcurrentCachedIndex<String, EnumTypeOracle> {
		@Override
		protected EnumTypeOracle provideValueFor(String typeSignature) {
			FlatEnumType flatEnumType = findFlatCustomType(typeSignature);
			return flatEnumType != null ? new BasicEnumTypeOracle(BasicModelOracle.this, flatEnumType) : emptyEnumTypeOracle;
		}
	}

	// #################################################
	// ## . . . . . . . Get TypeOracle . . . . . . . .##
	// #################################################

	@Override
	public <T extends TypeOracle> T getTypeOracle(String customTypeSignature) {
		T result = findTypeOracle(customTypeSignature);
		if (result == emptyEntityTypeOracle || result == emptyEnumTypeOracle) {
			throw new ElementInOracleNotFoundException("Custom type '" + customTypeSignature + "' not found in model: " + getGmMetaModel().getName());
		}

		return result;
	}

	@Override
	public EntityTypeOracle getEntityTypeOracle(String typeSignature) {
		EntityTypeOracle result = entityTypeOracles.acquireFor(typeSignature);
		if (result == emptyEntityTypeOracle) {
			throw new ElementInOracleNotFoundException("Entity type '" + typeSignature + "' not found in model: " + getGmMetaModel().getName());
		}

		return result;
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle(String typeSignature) {
		EnumTypeOracle result = enumTypeOracles.acquireFor(typeSignature);
		if (result == emptyEnumTypeOracle) {
			throw new ElementInOracleNotFoundException("Enum type '" + typeSignature + "' not found in model: " + getGmMetaModel().getName());
		}

		return result;
	}

	// #################################################
	// ## . . . . . . . Find TypeOracle . . . . . . . ##
	// #################################################

	@Override
	public <T extends TypeOracle> T findTypeOracle(String customTypeSignature) {
		GmCustomType gmCustomType = findGmCustomType(customTypeSignature);
		return gmCustomType == null ? null : findTypeOracle(gmCustomType);
	}

	@Override
	public <T extends TypeOracle> T findTypeOracle(CustomType type) {
		if (type.isEntity()) {
			return (T) findEntityTypeOracle(type.getTypeSignature());
		} else {
			return (T) findEnumTypeOracle(type.getTypeSignature());
		}
	}

	@Override
	public <T extends TypeOracle> T findTypeOracle(GmCustomType type) {
		if (type.isGmEntity()) {
			return (T) findEntityTypeOracle(type.getTypeSignature());
		} else {
			return (T) findEnumTypeOracle(type.getTypeSignature());
		}
	}

	@Override
	public EntityTypeOracle findEntityTypeOracle(String typeSignature) {
		EntityTypeOracle result = entityTypeOracles.acquireFor(typeSignature);
		return result == emptyEntityTypeOracle ? null : result;
	}

	@Override
	public EnumTypeOracle findEnumTypeOracle(String typeSignature) {
		EnumTypeOracle result = enumTypeOracles.acquireFor(typeSignature);
		return result == emptyEnumTypeOracle ? null : result;
	}

	// #################################################
	// ## . . . . . Find CustomType Helpers . . . . . ##
	// #################################################

	protected <T extends GmCustomType> T findGmCustomType(String customTypeSignature) {
		return (T) flatModel.allTypes.get(customTypeSignature);
	}

	protected <T extends FlatCustomType<?, ?>> T findFlatCustomType(String customTypeSignature) {
		return (T) flatModel.flatCustomTypes.get(customTypeSignature);
	}

	// #################################################
	// ## . . . . FOR NOW, THIS IS THE REST . . . . . ##
	// #################################################

	protected final Set<GmEntityType> getDirectSuper(GmEntityType gmEntityType) {
		Set<GmEntityType> result = flatModel.superTypes.get(gmEntityType);

		if (result == null) {
			result = Collections.emptySet();
			flatModel.superTypes.put(gmEntityType, result);
		}

		return result;
	}

	protected final Set<GmEntityType> getDirectSub(GmEntityType gmEntityType) {
		Set<GmEntityType> result = flatModel.subTypes.get(gmEntityType);

		if (result == null) {
			result = Collections.emptySet();
			flatModel.subTypes.put(gmEntityType, result);
		}

		return result;
	}

	@Override
	public ModelTypes getTypes() {
		return new BasicModelTypes(this);
	}

	@Override
	public ModelDependencies getDependencies() {
		return new BasicModelDependencies(this);
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return flatModel.allModels.stream().map(GmMetaModel::getMetaData).flatMap(Collection::stream);
	}

	@Override
	public Stream<MetaData> getEnumMetaData() {
		return flatModel.allModels.stream().map(GmMetaModel::getEnumTypeMetaData).flatMap(Collection::stream);
	}

	@Override
	public Stream<MetaData> getEnumConstantMetaData() {
		return flatModel.allModels.stream().map(GmMetaModel::getEnumConstantMetaData).flatMap(Collection::stream);
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return flatModel.allModels.stream().flatMap(QualifiedMetaDataTools::ownMetaData);
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedEnumMetaData() {
		return flatModel.allModels.stream().flatMap(QualifiedMetaDataTools::modelEnumMetaData);
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedEnumConstantMetaData() {
		return flatModel.allModels.stream().flatMap(QualifiedMetaDataTools::modelConstantMetaData);
	}

}
