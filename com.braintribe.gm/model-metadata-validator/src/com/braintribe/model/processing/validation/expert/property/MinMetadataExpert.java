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
package com.braintribe.model.processing.validation.expert.property;

import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.processing.PropertyValidationContext;
import com.braintribe.model.processing.PropertyValidationExpert;

public class MinMetadataExpert implements PropertyValidationExpert {

	@Override
	public void validate(PropertyValidationContext context) {
		Object propertyValue = context.getPropertyValue();
		if (propertyValue instanceof Number) {
			Min min = context.getPropertyMdResolver().meta(Min.T).exclusive();
			if (min != null) {

				Object limit = min.getLimit();
				boolean constraintViolated;

				if (limit.getClass() != propertyValue.getClass()) {
					context.notifyConstraintViolation("Error while resolving " + Min.T.getShortName() + " metadata: Property has a different class ("
							+ propertyValue.getClass() + ") than its declared limit (" + limit.getClass() + ")");
					return;
				}

				switch (context.getPropertyType().getTypeCode()) {
					case integerType:
						constraintViolated = (int) limit > (int) propertyValue;
						break;
					case longType:
						constraintViolated = (long) limit > (long) propertyValue;
						break;
					case floatType:
						constraintViolated = (float) limit > (float) propertyValue;
						break;
					case doubleType:
						constraintViolated = (double) limit > (double) propertyValue;
						break;
					default:
						throw new IllegalStateException("Found " + min.entityType().getTypeSignature() + " metadata on a property of type "
								+ context.getEntityType().getTypeSignature() + " which is not supported.");
				}

				if (constraintViolated) {
					context.notifyConstraintViolation("Property exceeds its allowed minimum value. Min: " + limit);
				}
			}
		}
	}

}
