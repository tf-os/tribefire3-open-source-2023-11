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
package com.braintribe.model.access.hibernate.interceptor;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

/**
 * Interceptor which provides correct instances for {@link GenericEntity}s and resolves their names.
 */
public class GmAdaptionInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = -5221026406788869293L;
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	@Override
	public String getEntityName(Object object) {
		if (object instanceof GenericEntity) {
			return ((GenericEntity) object).entityType().getTypeSignature();

		} else {
			return super.getEntityName(object);
		}
	}

	@Override
	public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
		if (entityMode == EntityMode.POJO) {
			EntityType<?> entityType = typeReflection.getType(entityName);

			GenericEntity ge = entityType.createPlain();
			ge.setId(id);

			return ge;

		} else {
			return super.instantiate(entityName, entityMode, id);
		}

	}

}
