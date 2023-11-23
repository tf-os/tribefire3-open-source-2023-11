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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.NullDescriptor;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.validation.ValidationContext;

public class InitializerValidationTask implements ValidationTask {

	private GmProperty property;

	public InitializerValidationTask(GmProperty property) {
		this.property = property;
	}

	@Override
	public void execute(ValidationContext context) {
		Object initializer = property.getInitializer();
		// cannot validate if we are missing type information
		if (property.getType() == null || propertyTypeSignature() == null) {
			return;
		}

		if (initializer instanceof ValueDescriptor) {
			validateWhenValueDescriptor(context, initializer);
		} else {
			GenericModelType initializerType = GMF.getTypeReflection().getType(initializer);
			if (initializerType == null) {
				context.addValidationMessage(property, ERROR, "Initialized to an unsupported value " + initializer
						+ " of type " + initializer.getClass().getName());
				return;
			}
			if (initializerType.isBase()) {
				validateWhenBase(context, initializer);
			} else if (initializerType.isCollection()) {
				validateWhenCollection(context, initializer);
			} else if (initializerType.isScalar()) {
				validateWhenScalar(context, initializer);
			} else {
				context.addValidationMessage(property, ERROR, "Initialized to an unknown type");
			}
		}
	}

	private void validateWhenValueDescriptor(ValidationContext context, Object initializer) {
		if (initializer instanceof NullDescriptor) {
			validateWhenNullDescriptor(context, initializer);
		} else if (initializer instanceof EnumReference) {
			validateWhenEnumReference(context, initializer);
		} else if (initializer instanceof Now) {
			validateWhenNow(context, initializer);
		} else {
			context.addValidationMessage(property, ERROR, "Initialized to an unsupported value " + initializer
					+ " of type " + initializer.getClass().getName());
		}
	}

	private void validateWhenNullDescriptor(ValidationContext context, Object initializer) {
		if (!isPropertyNullable()) {
			context.addValidationMessage(property, ERROR, "Initialized to null but nullable is set to false");
		}
	}

	private void validateWhenEnumReference(ValidationContext context, Object initializer) {
		if (isPropertyOfBaseType()) {
			return;
		}
		EnumReference enumInitializer = (EnumReference) initializer;
		if (isPropertyOfEnumType()) {
			if (equalsPropertyTypeSignature(enumInitializer.getTypeSignature())) {
				return;
			}
		}
		context.addValidationMessage(property, ERROR,
				"Initialized to an enum constant " + enumInitializer.getConstant() + " of type "
						+ enumInitializer.getTypeSignature() + " not matching property type "
						+ propertyTypeSignature());
	}

	private void validateWhenNow(ValidationContext context, Object initializer) {
		if (isPropertyOfBaseType() || isPropertyOfDateType()) {
			return;
		}
		context.addValidationMessage(property, ERROR,
				"Initialized to a current date and time but property is not of date type - " + propertyTypeSignature());
	}

	private void validateWhenBase(ValidationContext context, Object initializer) {
		if (!isPropertyOfBaseType()) {
			context.addValidationMessage(property, ERROR, "Initialized to an object but property not a base type");
		}
	}

	private void validateWhenCollection(ValidationContext context, Object initializer) {
		CollectionType collectionType = GMF.getTypeReflection().getType(initializer);
		if (isPropertyOfBaseType()) {
			return;
		}
		if (!isPropertyOfCollectionType()
				|| !propertyTypeSignature().split("<")[0].equals(collectionType.getTypeSignature().split("<")[0])) {
			context.addValidationMessage(property, ERROR,
					"Initialized to a value " + initializer + " of type "
							+ collectionType.getTypeSignature().split("<")[0] + " not matching property type "
							+ propertyTypeSignature().split("<")[0]);
			return;
		}
		CollectionKind collectionKind = collectionType.getCollectionKind();
		switch (collectionKind) {
		case list:
			validateWhenList(context, initializer);
			break;
		case map:
			validateWhenMap(context, initializer);
			break;
		case set:
			validateWhenSet(context, initializer);
			break;
		}
	}

	private void validateWhenList(ValidationContext context, Object initializer) {
		List<?> list = (List<?>) initializer;
		if (list.isEmpty()) {
			return;
		}
		GmType elementType = ((GmListType) property.getType()).getElementType();
		if (elementType.isGmBase()) {
			return;
		}
		for (Object e : list) {
			GenericModelType initializerElementType = GMF.getTypeReflection().getType(e);
			if (initializerElementType == null
					|| !elementType.getTypeSignature().equals(initializerElementType.getTypeSignature())) {
				context.addValidationMessage(property, ERROR,
						"Initialized to a list containing elements whose type is not "
								+ elementType.getTypeSignature());
				break;
			}
		}
	}

	private void validateWhenMap(ValidationContext context, Object initializer) {
		Map<?, ?> map = (Map<?, ?>) initializer;
		if (map.isEmpty()) {
			return;
		}
		GmType keyElementType = ((GmMapType) property.getType()).getKeyType();
		GmType valueElementType = ((GmMapType) property.getType()).getValueType();
		if (!keyElementType.isGmBase()) {
			for (Object key : map.keySet()) {
				GenericModelType initializerKeyElementType = GMF.getTypeReflection().getType(key);
				if (initializerKeyElementType == null
						|| !keyElementType.getTypeSignature().equals(initializerKeyElementType.getTypeSignature())) {
					context.addValidationMessage(property, ERROR,
							"Initialized to a map containing keys whose type is not "
									+ keyElementType.getTypeSignature());
					break;
				}
			}
		}
		if (!valueElementType.isGmBase()) {
			for (Object value : map.values()) {
				GenericModelType initializerValueElementType = GMF.getTypeReflection().getType(value);
				if (initializerValueElementType == null || !valueElementType.getTypeSignature()
						.equals(initializerValueElementType.getTypeSignature())) {
					context.addValidationMessage(property, ERROR,
							"Initialized to a map containing values whose type is not "
									+ valueElementType.getTypeSignature());
					break;
				}
			}
		}
	}

	private void validateWhenSet(ValidationContext context, Object initializer) {
		Set<?> set = (Set<?>) initializer;
		if (set.isEmpty()) {
			return;
		}
		GmType elementType = ((GmSetType) property.getType()).getElementType();
		if (elementType.isGmBase()) {
			return;
		}
		for (Object e : set) {
			GenericModelType initializerElementType = GMF.getTypeReflection().getType(e);
			if (initializerElementType == null
					|| !elementType.getTypeSignature().equals(initializerElementType.getTypeSignature())) {
				context.addValidationMessage(property, ERROR,
						"Initialized to a set containing elements whose type is not " + elementType.getTypeSignature());
				break;
			}
		}
	}

	private void validateWhenScalar(ValidationContext context, Object initializer) {
		if (isPropertyOfBaseType()) {
			return;
		}
		ScalarType scalarType = GMF.getTypeReflection().getType(initializer);
		if (!equalsPropertyTypeSignature(scalarType.getTypeSignature())) {
			context.addValidationMessage(property, ERROR, "Initialized to a value " + initializer + " of type "
					+ scalarType.getTypeSignature() + " not matching property type " + propertyTypeSignature());
		}
	}

	private String propertyTypeSignature() {
		return property.getType().getTypeSignature();
	}

	private boolean equalsPropertyTypeSignature(String typeSignature) {
		return propertyTypeSignature().equals(typeSignature);
	}

	private boolean isPropertyNullable() {
		return property.getNullable();
	}

	private boolean isPropertyOfBaseType() {
		return property.getType().isGmBase();
	}

	private boolean isPropertyOfEnumType() {
		return property.getType().isGmEnum();
	}

	private boolean isPropertyOfCollectionType() {
		return property.getType().isGmCollection();
	}

	private boolean isPropertyOfDateType() {
		return propertyTypeSignature().equals(SimpleTypes.TYPE_DATE.getTypeSignature());
	}
}
