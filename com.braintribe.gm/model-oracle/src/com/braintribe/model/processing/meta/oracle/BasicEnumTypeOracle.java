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

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.oracle.empty.EmptyEnumConstantOracle;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumConstant;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumType;

/**
 * @author peter.gazdik
 */
public class BasicEnumTypeOracle extends BasicTypeOracle implements EnumTypeOracle {

	protected final FlatEnumType flatEnumType;
	protected final EnumConstantOraclesIndex enumConstantOracles = new EnumConstantOraclesIndex();

	protected final EmptyEnumConstantOracle emptyConstantOracle = new EmptyEnumConstantOracle(this);

	public BasicEnumTypeOracle(BasicModelOracle modelOracle, FlatEnumType flatEnumType) {
		super(modelOracle);

		this.flatEnumType = flatEnumType;
	}

	class EnumConstantOraclesIndex extends ConcurrentCachedIndex<String, EnumConstantOracle> {
		@Override
		protected EnumConstantOracle provideValueFor(String propertyName) {
			FlatEnumConstant flatEnumConstant = flatEnumType.acquireFlatEnumConstants().get(propertyName);
			return flatEnumConstant != null ? new BasicEnumConstantOracle(BasicEnumTypeOracle.this, flatEnumConstant) : emptyConstantOracle;
		}
	}

	@Override
	public <T extends GmCustomType> T asGmType() {
		return (T) flatEnumType.type;
	}

	@Override
	public GmEnumType asGmEnumType() {
		return flatEnumType.type;
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return getGmEnumTypeInfos().stream().flatMap(gmEnumTypeInfo -> nullSafe(gmEnumTypeInfo.getMetaData()).stream());
	}

	@Override
	public Stream<MetaData> getConstantMetaData() {
		return getGmEnumTypeInfos().stream().flatMap(gmEnumTypeInfo -> nullSafe(gmEnumTypeInfo.getEnumConstantMetaData()).stream());
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return getGmEnumTypeInfos().stream().flatMap(QualifiedMetaDataTools::ownMetaData);
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedConstantMetaData() {
		return getGmEnumTypeInfos().stream().flatMap(QualifiedMetaDataTools::enumConstantMetaData);
	}

	@Override
	public List<GmEnumTypeInfo> getGmEnumTypeInfos() {
		return flatEnumType.infos;
	}

	@Override
	public EnumTypeConstants getConstants() {
		return new BasicEnumTypeConstants(this);
	}

	@Override
	public EnumConstantOracle getConstant(String constantName) {
		EnumConstantOracle result = enumConstantOracles.acquireFor(constantName);
		if (result == emptyConstantOracle) {
			throw new ElementInOracleNotFoundException("Constant '" + constantName + "' of '" + flatEnumType.type.getTypeSignature()
					+ "' not found in model: " + modelOracle.getGmMetaModel().getName());
		}

		return result;
	}

	@Override
	public EnumConstantOracle getConstant(GmEnumConstant constant) {
		return getConstant(constant.getName());
	}

	@Override
	public EnumConstantOracle getConstant(Enum<?> enumValue) {
		return getConstant(enumValue.name());
	}

	@Override
	public EnumConstantOracle findConstant(String constantName) {
		EnumConstantOracle result = enumConstantOracles.acquireFor(constantName);
		return result == emptyConstantOracle ? null : result;
	}

	@Override
	public EnumConstantOracle findConstant(GmEnumConstant constant) {
		return findConstant(constant.getName());
	}

	@Override
	public EnumConstantOracle findConstant(Enum<?> enumValue) {
		return findConstant(enumValue.name());
	}

}
