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
package com.braintribe.model.processing.meta.cmd.empty;

import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.MdSelectorResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.empty.EmptyModelOracle;

public class EmptyCmdResolver implements CmdResolver {

	public static final EmptyCmdResolver INSTANCE = new EmptyCmdResolver();

	private EmptyCmdResolver() {
	}

	@Override
	public ModelOracle getModelOracle() {
		return EmptyModelOracle.INSTANCE;
	}

	@Override
	public ModelMdResolver getMetaData() {
		return EmptyModelMdResolver.INSTANCE;
	}

	@Override
	public MdSelectorResolver getMdSelectorResolver() {
		return EmptyMdSelectorResolver.INSTANCE;
	}

	@Override
	public <T extends ScalarType> T getIdType(String typeSignature) {
		return (T) SimpleTypes.TYPE_LONG;
	}

}
