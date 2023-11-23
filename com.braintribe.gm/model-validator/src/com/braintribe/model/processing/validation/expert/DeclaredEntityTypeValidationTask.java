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
import static com.braintribe.model.processing.validation.expert.CommonChecks.areValuesUnique;
import static com.braintribe.model.processing.validation.expert.CommonChecks.isNotNull;
import static com.braintribe.utils.lcd.CollectionTools.isEmpty;
import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.util.stream.Collectors;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.processing.validation.ValidationContext;

public class DeclaredEntityTypeValidationTask implements ValidationTask {

	private GmMetaModel model;
	private GmEntityType type;

	public DeclaredEntityTypeValidationTask(GmMetaModel model, GmEntityType type) {
		this.model = model;
		this.type = type;
	}

	@Override
	public void execute(ValidationContext context) {
		if (isEmpty(type.getGlobalId())) {
			context.addValidationMessage(type, ERROR, "Global id is missing");
		}
		if (!areValuesUnique(type.getProperties().stream().map(GmProperty::getName).collect(Collectors.toList()))) {
			context.addValidationMessage(type, ERROR, "Duplicate property names");
		}
		if (type.getProperties().contains(null)) {
			context.addValidationMessage(type, ERROR, "Null values in property collection");
		}
		type.getProperties().stream() //
				.filter(CommonChecks::isNotNull) //
				.map(this::declaredPropertyValidationTask) //
				.forEach(context::addValidationTask);
		if (isNotNull(type.getEvaluatesTo())) {
			context.addValidationTask(referencedTypeValidationTask(type.getEvaluatesTo()));
		}
		if (type.getMetaData().contains(null)) {
			context.addValidationMessage(type, ERROR, "Null values in meta data collection");
		}
		type.getMetaData().stream() //
				.filter(CommonChecks::isNotNull) //
				.map(CoreMetaDataValidationTask::new) //
				.forEach(context::addValidationTask);
		if (type.getPropertyMetaData().contains(null)) {
			context.addValidationMessage(type, ERROR, "Null values in property meta data collection");
		}
		type.getPropertyMetaData().stream() //
				.filter(CommonChecks::isNotNull) //
				.map(CoreMetaDataValidationTask::new) //
				.forEach(context::addValidationTask);
		if (type.getPropertyOverrides().contains(null)) {
			context.addValidationMessage(type, ERROR, "Null values in property override collection");
		}
		type.getPropertyOverrides().stream() //
				.filter(CommonChecks::isNotNull) //
				.map(this::declaredPropertyOverrideValidationTask) //
				.forEach(context::addValidationTask);
		if (isEmpty(type.getSuperTypes())) {
			context.addValidationMessage(type, ERROR, "No super types found");
		} else {
			if (type.getSuperTypes().contains(null)) {
				context.addValidationMessage(type, ERROR, "Null values in super type collection");
			}
			type.getSuperTypes().stream() //
					.filter(CommonChecks::isNotNull) //
					.map(this::referencedTypeValidationTask) //
					.forEach(context::addValidationTask);
		}
	}

	private ReferencedTypeValidationTask referencedTypeValidationTask(GmType type) {
		return new ReferencedTypeValidationTask(model, type);
	}

	private DeclaredPropertyValidationTask declaredPropertyValidationTask(GmProperty property) {
		return new DeclaredPropertyValidationTask(model, type, property);
	}

	private DeclaredPropertyOverrideValidationTask declaredPropertyOverrideValidationTask(
			GmPropertyOverride propertyOverride) {
		return new DeclaredPropertyOverrideValidationTask(model, type, propertyOverride);
	}
}
