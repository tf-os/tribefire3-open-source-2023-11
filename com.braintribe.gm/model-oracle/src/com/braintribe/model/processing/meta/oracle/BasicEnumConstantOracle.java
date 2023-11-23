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

import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumConstant;

/**
 * @author peter.gazdik
 */
public class BasicEnumConstantOracle implements EnumConstantOracle {

	private final BasicEnumTypeOracle enumOracle;
	private final FlatEnumConstant flatEnumConstant;

	public BasicEnumConstantOracle(BasicEnumTypeOracle entityOracle, FlatEnumConstant flatProperty) {
		this.enumOracle = entityOracle;
		this.flatEnumConstant = flatProperty;
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle() {
		return enumOracle;
	}

	@Override
	public GmEnumConstant asGmEnumConstant() {
		return flatEnumConstant.gmEnumConstant;
	}

	@Override
	public Enum<?> asEnum() {
		return enumOracle.<EnumType> asType().getEnumValue(flatEnumConstant.gmEnumConstant.getName());
	}

	@Override
	public List<GmEnumConstantInfo> getGmEnumConstantInfos() {
		return flatEnumConstant.infos;
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return getGmEnumConstantInfos().stream().flatMap(gmEnumConstatntInfo -> nullSafe(gmEnumConstatntInfo.getMetaData()).stream());
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return getGmEnumConstantInfos().stream().flatMap(QualifiedMetaDataTools::ownMetaData);
	}

}
