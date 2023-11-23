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

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumTypeInfo;

/**
 * @author peter.gazdik
 */
public interface EnumTypeOracle extends TypeOracle {

	GmEnumType asGmEnumType();

	List<GmEnumTypeInfo> getGmEnumTypeInfos();

	EnumTypeConstants getConstants();

	EnumConstantOracle getConstant(String constantName);

	EnumConstantOracle getConstant(GmEnumConstant constant);

	EnumConstantOracle getConstant(Enum<?> enumValue);

	EnumConstantOracle findConstant(String constantName);

	EnumConstantOracle findConstant(GmEnumConstant constant);

	EnumConstantOracle findConstant(Enum<?> enumValue);

	/** returns all property MD from all GmEnumTypeInfos (those returned via getGmEnumTypeInfos()) */
	Stream<MetaData> getConstantMetaData();

	/** Qualified version of {@link #getConstantMetaData()} */
	Stream<QualifiedMetaData> getQualifiedConstantMetaData();

}
