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
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.processing.meta.oracle.EnumConstantOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * @author peter.gazdik
 */
public class EmptyEnumConstantOracle implements EnumConstantOracle {

	private final EnumTypeOracle enumTypeOracle;

	public EmptyEnumConstantOracle(EnumTypeOracle enumTypeOracle) {
		this.enumTypeOracle = enumTypeOracle;
	}

	@Override
	public EnumTypeOracle getEnumTypeOracle() {
		return enumTypeOracle;
	}

	@Override
	public GmEnumConstant asGmEnumConstant() {
		return null;
	}

	@Override
	public Enum<?> asEnum() {
		return null;
	}

	@Override
	public List<GmEnumConstantInfo> getGmEnumConstantInfos() {
		return Collections.emptyList();
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return Stream.empty();
	}

}
