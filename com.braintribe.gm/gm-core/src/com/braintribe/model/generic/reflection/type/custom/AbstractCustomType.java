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
package com.braintribe.model.generic.reflection.type.custom;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.Model;

/**
 * @author peter.gazdik
 */
public abstract class AbstractCustomType extends AbstractGenericModelType implements CustomType {

	protected AbstractCustomType(Class<?> javaType) {
		super(javaType);
	}

	@Override
	public final Model getModel() {
		return GMF.getTypeReflection().getModelForType(getTypeName());
	}

	@Override
	public final String getTypeName() {
		return getJavaType().getName();
	}

	@Override
	public final String getShortName() {
		return getJavaType().getSimpleName();
	}

}
