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

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.validation.ValidationContext;

public class DeclaredPropertyValidationTask implements ValidationTask {

	private GmMetaModel model;
	private GmEntityType declaringType;
	private GmProperty property;

	public DeclaredPropertyValidationTask(GmMetaModel model, GmEntityType declaringType, GmProperty property) {
		this.model = model;
		this.declaringType = declaringType;
		this.property = property;
	}

	@Override
	public void execute(ValidationContext context) {
		if (!declaringType.equals(property.getDeclaringType())) {
			context.addValidationMessage(property, ERROR, "Declaring type is wrong");
		}
		context.addValidationTask(new PropertyValidationTask(property));
		if (isNotNull(property.getType())) {
			context.addValidationTask(new ReferencedTypeValidationTask(model, property.getType()));
		}
	}
}
