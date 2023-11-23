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
import static com.braintribe.model.processing.validation.ValidationMode.TRANSITIVE;
import static com.braintribe.utils.lcd.CollectionTools.isEmpty;
import static com.braintribe.utils.lcd.StringTools.isEmpty;
import static java.lang.Integer.parseInt;

import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;

public class SkeletonModelValidationTask implements ValidationTask {

	private GmMetaModel model;

	public SkeletonModelValidationTask(GmMetaModel model) {
		this.model = model;
	}

	@Override
	public void execute(ValidationContext context) {
		if (isEmpty(model.getGlobalId())) {
			context.addValidationMessage(model, ERROR, "Global id is missing");
		}
		if (isEmpty(model.getName())) {
			context.addValidationMessage(model, ERROR, "Name is missing");
		} else {
			// temporary solution - skipping root model validation
			if (model.getName().equals(GenericModelTypeReflection.rootModelName)) {
				return;
			}
			if (!isValidNameFormat(model.getName())) {
				context.addValidationMessage(model, ERROR, "Invalid name format");
			}
		}
		if (isEmpty(model.getVersion())) {
			context.addValidationMessage(model, ERROR, "Version is missing");
		} else {
			if (!isValidVersionFormat(model.getVersion())) {
				context.addValidationMessage(model, ERROR, "Invalid version format");
			}
		}
		if (!isEmpty(model.getTypeOverrides())) {
			context.addValidationMessage(model, ERROR, "Type overrides are not allowed");
		}
		if (!isEmpty(model.getMetaData())) {
			context.addValidationMessage(model, ERROR, "Metadata is not allowed");
		}
		if (!isEmpty(model.getEnumTypeMetaData())) {
			context.addValidationMessage(model, ERROR, "Enum type metadata is not allowed");
		}
		if (!isEmpty(model.getEnumConstantMetaData())) {
			context.addValidationMessage(model, ERROR, "Enum constant metadata is not allowed");
		}
		if (isEmpty(model.getDependencies())) {
			context.addValidationMessage(model, ERROR,
					"No dependencies found. Should at least depend on the root model.");
		}
		if (context.getMode().equals(TRANSITIVE)) {
			if (model.getDependencies().contains(null)) {
				context.addValidationMessage(model, ERROR, "Null values in dependency collection");
			}
			model.getDependencies().stream() //
					.filter(CommonChecks::isNotNull) //
					.map(SkeletonModelValidationTask::new) //
					.forEach(context::addValidationTask);
		}
		if (model.getTypes().contains(null)) {
			context.addValidationMessage(model, ERROR, "Null values in type collection");
		}
		model.getTypes().stream() //
				.filter(CommonChecks::isNotNull) //
				.map(this::declaredTypeValidationTask) //
				.forEach(context::addValidationTask);
	}

	private DeclaredTypeValidationTask declaredTypeValidationTask(GmType type) {
		return new DeclaredTypeValidationTask(model, type);
	}

	private boolean isValidNameFormat(String name) {
		if (name.split(":").length != 2) {
			return false;
		}
		return true;
	}

	private boolean isValidVersionFormat(String version) {
		String[] versionParts = version.split("\\.");
		if (versionParts.length != 3) {
			return false;
		}
		try {
			parseInt(versionParts[0]);
			parseInt(versionParts[1]);
			parseInt(versionParts[2].replace("-pc", ""));
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
