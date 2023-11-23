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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.builders.ConstantMdResolver;
import com.braintribe.model.processing.meta.cmd.result.ConstantMdResult;

/**
 * 
 */
public class EmptyConstantMdResolver extends EmptyMdResolver<ConstantMdResolver> implements ConstantMdResolver {

	public static final EmptyConstantMdResolver INSTANCE = new EmptyConstantMdResolver();

	private EmptyConstantMdResolver() {
	}

	@Override
	public GmEnumConstant getGmEnumConstant() {
		throw new UnsupportedOperationException("Method 'EmptyConstantMdResolver.getGmEnumConstant' is not supported!");
	}

	@Override
	public final <M extends MetaData> ConstantMdResult<M> meta(EntityType<M> metaDataType) {
		return EmptyMdResult.EmptyConstantMdResult.singleton();
	}

}
