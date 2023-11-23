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

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumConstantInfo;

import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(namespace = GmCoreApiInteropNamespaces.model)
@SuppressWarnings("unusable-by-js")
public interface EnumConstantOracle {

	/** Returns the {@link EnumTypeOracle} from which this {@link EnumConstantOracle} was returned. */
	EnumTypeOracle getEnumTypeOracle();

	GmEnumConstant asGmEnumConstant();

	Enum<?> asEnum();

	/**
	 * Returns a list of {@link GmEnumConstantInfo} declared for this constant.
	 */
	List<GmEnumConstantInfo> getGmEnumConstantInfos();

	/** Returns the meta-data from the {@link GmEnumConstantInfo}s returned by {@link #getGmEnumConstantInfos()}. */
	Stream<MetaData> getMetaData();

	/** Qualified version of {@link #getMetaData()}. */
	Stream<QualifiedMetaData> getQualifiedMetaData();

}
