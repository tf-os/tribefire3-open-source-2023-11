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
import static com.braintribe.model.processing.validation.ValidationMode.DECLARATION;

import java.util.Optional;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;

public class ReferencedTypeValidationTask implements ValidationTask {

	private GmMetaModel model;
	private GmType type;

	public ReferencedTypeValidationTask(GmMetaModel model, GmType type) {
		this.model = model;
		this.type = type;
	}

	@Override
	public void execute(ValidationContext context) {
		if (model.equals(type.getDeclaringModel())) {
			Optional<GmType> typeOptional = model.getTypes().stream() //
					.filter(CommonChecks::isNotNull) //
					.filter(t -> t.equals(type)) //
					.findFirst();
			if (!typeOptional.isPresent()) {
				context.addValidationMessage(type, ERROR, "Not found in declaring model type collection");
			}
		} else {
			Optional<GmMetaModel> depOptional = model.getDependencies().stream() //
					.filter(CommonChecks::isNotNull) //
					.filter(d -> d.equals(type.getDeclaringModel())) //
					.findFirst();
			if (!depOptional.isPresent()) {
				context.addValidationMessage(type, ERROR, "Declaring model not found in model dependency collection");
			}
		}
		if (context.getMode().equals(DECLARATION)) {
			context.addValidationTask(new TypeValidationTask(type));
		}
	}
}
