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

import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.TypeOracle;

/**
 * @author peter.gazdik
 */
public abstract class EmptyTypeOracle implements TypeOracle {

	protected final ModelOracle modelOracle;

	public EmptyTypeOracle(ModelOracle modelOracle) {
		this.modelOracle = modelOracle;
	}

	@Override
	public ModelOracle getModelOracle() {
		return modelOracle;
	}

	@Override
	public <T extends GmCustomType> T asGmType() {
		return null;
	}

	@Override
	public <T extends CustomType> T asType() {
		return null;
	}

	@Override
	public Stream<MetaData> getMetaData() {
		return Stream.empty();
	}

	@Override
	public Stream<QualifiedMetaData> getQualifiedMetaData() {
		return Stream.empty();
	}

	@Override
	public boolean isDeclared() {
		return false;
	}

}
