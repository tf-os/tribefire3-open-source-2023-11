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

import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;

public class TypeValidationTask implements ValidationTask {

	private GmType type;

	public TypeValidationTask(GmType type) {
		this.type = type;
	}

	@Override
	public void execute(ValidationContext context) {
		if (isEmpty(type.getGlobalId())) {
			context.addValidationMessage(type, ERROR, "Global id is missing");
		}
		if (!isNotNull(type.getDeclaringModel())) {
			context.addValidationMessage(type, ERROR, "Declaring model is missing");
		}
		if (isEmpty(type.getTypeSignature())) {
			context.addValidationMessage(type, ERROR, "Type signature is missing");
		} else {
			try {
				EntityTypes.get(type.getTypeSignature());
			} catch (GenericModelException e) {
				context.addValidationMessage(type, ERROR,
						"Type signature points to unexisting type");
			}
		}
	}
}
