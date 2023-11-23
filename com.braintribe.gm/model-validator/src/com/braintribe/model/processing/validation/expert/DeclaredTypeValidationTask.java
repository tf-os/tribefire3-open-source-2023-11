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

import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;

public class DeclaredTypeValidationTask implements ValidationTask {

	private GmMetaModel declaringModel;
	private GmType type;

	public DeclaredTypeValidationTask(GmMetaModel declaringModel, GmType type) {
		this.declaringModel = declaringModel;
		this.type = type;
	}

	@Override
	public void execute(ValidationContext context) {
		if (!declaringModel.equals(type.getDeclaringModel())) {
			context.addValidationMessage(type, ERROR, "Declaring model is wrong");
		}
		if (type instanceof GmEntityType) {
			context.addValidationTask(new DeclaredEntityTypeValidationTask(declaringModel, (GmEntityType) type));
		} else if (type instanceof GmEnumType) {
			context.addValidationTask(new DeclaredEnumTypeValidationTask((GmEnumType) type));
		} else if (!(type instanceof GmCustomType)) {
			context.addValidationMessage(type, ERROR, "Not a custom type");
		}
		context.addValidationTask(new TypeValidationTask(type));
	}
}
