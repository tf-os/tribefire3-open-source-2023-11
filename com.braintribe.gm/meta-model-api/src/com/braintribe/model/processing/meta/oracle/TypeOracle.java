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

import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.data.MetaData;

/**
 * @author peter.gazdik
 */
public interface TypeOracle {

	/** Returns the {@link ModelOracle} from which this {@link TypeOracle} was returned. */
	ModelOracle getModelOracle();

	<T extends GmCustomType> T asGmType();

	<T extends CustomType> T asType();

	/**
	 * @returns all metaData defined for given {@link CustomType}, including overrides - iteration over models is done
	 *          in depth first order.
	 */
	Stream<MetaData> getMetaData();

	/** Qualified version of {@link #getMetaData()}. */
	Stream<QualifiedMetaData> getQualifiedMetaData();

	/**
	 * @return true iff custom type behind this oracle is declared in the model corresponding to the {@link ModelOracle}
	 *         from which our oracle was returned.
	 */
	boolean isDeclared();

}
