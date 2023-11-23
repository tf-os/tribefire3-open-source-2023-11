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
package com.braintribe.model.processing.validation.expert;

import static com.braintribe.model.processing.validation.ValidationMessageLevel.ERROR;
import static com.braintribe.model.processing.validation.expert.CommonChecks.isNotNull;
import static com.braintribe.utils.lcd.StringTools.isEmpty;
import static java.lang.Character.isLowerCase;

import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.validation.ValidationContext;

public class PropertyValidationTask implements ValidationTask {

	private GmProperty property;

	public PropertyValidationTask(GmProperty property) {
		this.property = property;
	}

	@Override
	public void execute(ValidationContext context) {
		if (isEmpty(property.getName())) {
			context.addValidationMessage(property, ERROR, "Name is missing");
		} else {
			if (!isLowerCase(property.getName().charAt(0))) {
				context.addValidationMessage(property, ERROR, "Name first character must be lowercase");
			}
			if (property.getName().length() > 1) {
				if (!isLowerCase(property.getName().charAt(1))) {
					context.addValidationMessage(property, ERROR, "Name second character must be lowercase");
				}
			}
			// TODO add additional validation (from GME area)
		}
		if (!isNotNull(property.getDeclaringType())) {
			context.addValidationMessage(property, ERROR, "Declaring type is missing");
		}
		if (!isNotNull(property.getType())) {
			context.addValidationMessage(property, ERROR, "Type is missing");
		}
	}
}
