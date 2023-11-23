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
package com.braintribe.utils.genericmodel.entity.lcd;

import com.braintribe.common.lcd.ClassInstantiator;
import com.braintribe.model.generic.GenericEntity;

/**
 * Super class for {@link ClassInstantiator}s that can create {@link GenericEntity}s. The purpose of this interface is
 * to avoid dependencies on <code>GmSession</code> or <code>GMF</code> (and thus the GmCore) if the only thing the code
 * needs to do is instantiating entities.
 *
 * @author michael.lafite
 */
public abstract class EntityInstantiator implements ClassInstantiator<GenericEntity> {

	@Override
	public <U extends GenericEntity> U instantiate(Class<U> clazz) {
		try {
			return instantiateWithoutExceptionHandling(clazz);
		} catch (Exception e) {
			throw new ClassInstantiationException(
					"Error while trying to create a new entity instance of type " + clazz.getName() + "!", e);
		}
	}

	@Override
	public GenericEntity instantiate(String className) throws ClassInstantiationException {
		throw new UnsupportedOperationException("instantiate(" + className + ") should not be called in a GWT environment");
	}

	/**
	 * Creates a new instance of the specified entity class. The method may propagate any exceptions.
	 */
	protected abstract <U extends GenericEntity> U instantiateWithoutExceptionHandling(Class<U> entityClass)
			throws Exception;

}
