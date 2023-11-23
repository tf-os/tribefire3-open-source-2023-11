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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.meta.oracle.ElementInOracleNotFoundException;
import com.braintribe.model.processing.meta.oracle.EnumConstantOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeConstants;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyEnumTypeOracle extends EmptyTypeOracle implements EnumTypeOracle {

	public EmptyEnumTypeOracle(ModelOracle modelOracle) {
		super(modelOracle);
	}

	protected final EmptyEnumConstantOracle emptyConstantOracle = new EmptyEnumConstantOracle(this);

	@Override
	public GmEnumType asGmEnumType() {
		return null;
	}

	@Override
	public List<GmEnumTypeInfo> getGmEnumTypeInfos() {
		return Collections.emptyList();
	}

	@Override
	public EnumTypeConstants getConstants() {
		return EmptyEnumTypeConstants.INSTANCE;
	}

	@Override
	public EnumConstantOracle getConstant(String constantName) {
		EnumConstantOracle result = findConstant(constantName);
		if (result == null) {
			throw new ElementInOracleNotFoundException("Constant not found: " + constantName);
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
		return emptyConstantOracle;
	}

	@Override
	public EnumConstantOracle findConstant(GmEnumConstant constant) {
		return findConstant(constant.getName());
	}

	@Override
	public EnumConstantOracle findConstant(Enum<?> enumValue) {
		return findConstant(enumValue.name());
	}

	@Override
	public Stream<MetaData> getConstantMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedConstantMetaData() {
		return Stream.empty();
	}

}
