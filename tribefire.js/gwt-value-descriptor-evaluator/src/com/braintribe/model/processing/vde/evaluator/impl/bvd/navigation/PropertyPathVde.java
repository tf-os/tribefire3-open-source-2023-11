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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.navigation;

import com.braintribe.model.bvd.navigation.PropertyPath;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link PropertyPath}
 * 
 */
public class PropertyPathVde implements ValueDescriptorEvaluator<PropertyPath> {

	@Override
	public VdeResult evaluate(VdeContext context, PropertyPath valueDescriptor) throws VdeRuntimeException {

		String[] propertyNames = valueDescriptor.getPropertyPath().split("\\.");

		Object result = context.evaluate(valueDescriptor.getEntity());
		GenericModelType type = GMF.getTypeReflection().getType(result);

		for (String propertyName : propertyNames) {
			if (result == null) {
				return new VdeResultImpl(null, false);
			}

			GenericEntity ge = (GenericEntity) result;
			EntityType<?> et = (EntityType<?>) type;

			Property property = et.findProperty(propertyName);
			
			if (property != null) {
				result = property.get(ge);
				type = property.getType();
			}

		}

		return new VdeResultImpl(result, false);
	}

}
