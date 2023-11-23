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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CustomType;

/**
 * @author peter.gazdik
 */
public abstract class BasicTypeOracle implements TypeOracle {

	protected final BasicModelOracle modelOracle;

	public BasicTypeOracle(BasicModelOracle modelOracle) {
		this.modelOracle = modelOracle;
	}

	@Override
	public ModelOracle getModelOracle() {
		return modelOracle;
	}

	@Override
	public final <T extends CustomType> T asType() {
		return GMF.getTypeReflection().<T> getType(asGmType().getTypeSignature());
	}

	@Override
	public boolean isDeclared() {
		return modelOracle.flatModel.model == asGmType().getDeclaringModel();
	}

}
