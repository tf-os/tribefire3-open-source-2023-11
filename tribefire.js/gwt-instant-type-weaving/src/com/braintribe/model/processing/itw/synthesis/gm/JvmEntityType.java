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
package com.braintribe.model.processing.itw.synthesis.gm;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityInitializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GmtsEnhancedEntityStub;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.type.custom.AbstractEntityType;

public abstract class JvmEntityType<T extends GenericEntity> extends AbstractEntityType<T> implements ItwEntityType {

	private EntityInitializer[] initializers;
	
	public JvmEntityType() {
		super();
	}

	public void setInitializers(EntityInitializer[] initializers) {
		this.initializers = initializers;
	}

	@Override
	protected EntityInitializer[] getInitializers(){
		return initializers;
	}
	
	@Override
	public T createRaw(PropertyAccessInterceptor pai) {
		T result = createRaw();
		((GmtsEnhancedEntityStub) result).pai = pai;
		return result;
	}

	/** Similar to {@link #isAssignableFrom(EntityType)} */
	@Override
	public boolean isAssignableFrom(GenericModelType type) {
		return this == type || getJavaType().isAssignableFrom(type.getJavaType());
	}

	/**
	 * Semantics is 'isAssibnableFrom' i.e. returns true iff the parameter <tt>entityType</tt> is a sub-type of <tt>this</tt>.
	 */
	@Override
	public boolean isAssignableFrom(EntityType<?> entityType) {
		return this == entityType || getJavaType().isAssignableFrom(entityType.getJavaType());
	}

}
