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
package com.braintribe.model.processing.itw.tools;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.meta.GmTypeKind;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmEnumConstant;
import com.braintribe.model.weaving.ProtoGmEnumType;
import com.braintribe.model.weaving.ProtoGmLinearCollectionType;
import com.braintribe.model.weaving.ProtoGmMapType;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmModelElement;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.override.ProtoGmCustomTypeOverride;
import com.braintribe.model.weaving.override.ProtoGmEntityTypeOverride;
import com.braintribe.model.weaving.override.ProtoGmEnumConstantOverride;
import com.braintribe.model.weaving.override.ProtoGmEnumTypeOverride;
import com.braintribe.model.weaving.override.ProtoGmPropertyOverride;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class MetaModelValidator {

	public static List<String> validate(ProtoGmMetaModel metaModel) {
		return new MetaModelValidator().validateModel(metaModel);
	}

	public static List<String> validate(ProtoGmType gmType) {
		return new MetaModelValidator().validateType(gmType);
	}

	// ############################################
	// ## . . . . . . Implementation . . . . . . ##
	// ############################################

	private final List<String> errors = newList();
	private final Set<ProtoGmModelElement> visitedModelElements = newSet();
	private final Deque<String> path = new ArrayDeque<>();

	private MetaModelValidator() {
		// empty
	}

	// ############################################
	// ## . . . . . . . . Models . . . . . . . . ##
	// ############################################

	private List<String> validateModel(ProtoGmMetaModel metaModel) {
		enter("Root");
		visitModel(metaModel);
		return errors;
	}

	private void visitModel(ProtoGmMetaModel metaModel) {
		if (!visitElement(metaModel, "MetaModel is null"))
			return;

		enter("Model: " + metaModel.getName());
		
		for (ProtoGmMetaModel modelDependency : nullSafe(metaModel.getDependencies()))
			visitModel(modelDependency);

		for (ProtoGmType gmType : nullSafe(metaModel.getTypes()))
			visitType(gmType);

		for (ProtoGmCustomTypeOverride gmTypeOverride : nullSafe(metaModel.getTypeOverrides()))
			visitTypeOverride(gmTypeOverride);

		exit();
	}

	// ############################################
	// ## . . . . . . . . Types . . . . . . . . .##
	// ############################################

	private List<String> validateType(ProtoGmType gmType) {
		enter("Root");
		visitType(gmType);
		return errors;
	}

	private void visitType(ProtoGmType gmType) {
		if (!visitElement(gmType, "ProtoGmType is null"))
			return;

		if (gmType.getTypeSignature() == null) {
			addErrorMessage("ProtoGmType has no signature. Type: " + gmType + ", declared in model: " + gmType.getDeclaringModel());
			return;
		}

		if (gmType.isGmBase() || gmType.isGmSimple())
			return;

		GmTypeKind typeKind = gmType.typeKind();

		enter(typeKind.name().toLowerCase() + " Type: " + gmType.getTypeSignature());

		switch (typeKind) {
			case ENTITY:
				visitEntityType((ProtoGmEntityType) gmType);
				break;
			case ENUM:
				visitEnumType((ProtoGmEnumType) gmType);
				break;
			case LIST:
			case SET:
				visitLinearCollectionType((ProtoGmLinearCollectionType) gmType);
				break;
			case MAP:
				visitMapType((ProtoGmMapType) gmType);
				break;
			default:
				throw new UnknownEnumException(typeKind);
		}

		exit();
	}

	private void visitEntityType(ProtoGmEntityType gmType) {
		for (ProtoGmEntityType superType : nullSafe(gmType.getSuperTypes()))
			visitType(superType);

		for (ProtoGmProperty gmProperty : nullSafe(gmType.getProperties()))
			visitProperty(gmProperty);

		for (ProtoGmPropertyOverride gmPropertyOverride : nullSafe(gmType.getPropertyOverrides()))
			visitPropertyOverride(gmPropertyOverride);

		ProtoGmType evaluatesToType = gmType.getEvaluatesTo();
		if (evaluatesToType != null)
			visitType(evaluatesToType);
	}

	private void visitProperty(ProtoGmProperty gmProperty) {
		if (!visitElement(gmProperty, "ProtoGmProperty is null"))
			return;

		if (StringTools.isBlank(gmProperty.getName())) {
			addErrorMessage("Property has no name");
			return;
		}

		enter("Property: " + gmProperty.getName());

		ProtoGmType propertyType = gmProperty.getType();
		if (propertyType == null)
			addErrorMessage("Property has no type.");

		visitType(propertyType);

		exit();
	}

	private void visitEnumType(ProtoGmEnumType gmEnumType) {
		List<? extends ProtoGmEnumConstant> constants = gmEnumType.getConstants();
		if (isEmpty(constants))
			addErrorMessage("No constants defined for enum type: " + gmEnumType.getTypeSignature());
		else
			for (ProtoGmEnumConstant gmEnumConstant : constants)
				visitEnumConstant(gmEnumConstant);
	}

	private void visitEnumConstant(ProtoGmEnumConstant gmEnumConstant) {
		if (!visitElement(gmEnumConstant, "ProtoGmEnumConstant is null"))
			return;

		if (gmEnumConstant.getName() == null)
			addErrorMessage("Enum constant has no name");
	}

	private void visitLinearCollectionType(ProtoGmLinearCollectionType gmLinearCollectionType) {
		visitType(gmLinearCollectionType.getElementType());
	}

	private void visitMapType(ProtoGmMapType gmMapType) {
		visitType(gmMapType.getKeyType());
		visitType(gmMapType.getValueType());
	}

	// ############################################
	// ## . . . . . . TypeOverrides . . . . . . .##
	// ############################################

	private void visitTypeOverride(ProtoGmCustomTypeOverride gmTypeOverride) {
		if (!visitElement(gmTypeOverride, "ProtoGmTypeOverride is null"))
			return;

		if (gmTypeOverride.isGmEntityOverride()) {
			visitEntityTypeOverride((ProtoGmEntityTypeOverride) gmTypeOverride);
		} else {
			visitEnumTypeOverride((ProtoGmEnumTypeOverride) gmTypeOverride);
		}
	}

	private void visitEntityTypeOverride(ProtoGmEntityTypeOverride gmTypeOverride) {
		ProtoGmEntityType entityType = gmTypeOverride.getEntityType();
		if (entityType == null) {
			addErrorMessage("EntityTypeOverride doesn't reference an EntityType");
			return;
		}

		enter("EntityTypeOverride: " + entityType.getTypeSignature());
		visitType(entityType);

		for (ProtoGmPropertyOverride gmPropertyOverride : nullSafe(gmTypeOverride.getPropertyOverrides()))
			visitPropertyOverride(gmPropertyOverride);

		exit();
	}

	private void visitPropertyOverride(ProtoGmPropertyOverride gmPropertyOverride) {
		if (!visitElement(gmPropertyOverride, "ProtoGmPropertyOverride is null"))
			return;

		if (gmPropertyOverride.getProperty() == null)
			addErrorMessage("PropertyOverride doesn't reference a Property");
	}

	private void visitEnumTypeOverride(ProtoGmEnumTypeOverride gmTypeOverride) {
		ProtoGmEnumType enumType = gmTypeOverride.getEnumType();
		if (enumType == null) {
			addErrorMessage("EnumTypeOverride doesn't reference an EnumType");
			return;
		}

		enter("EnumTypeOverride: " + enumType.getTypeSignature());
		visitType(enumType);

		for (ProtoGmEnumConstantOverride gmEnumConstantOverride : gmTypeOverride.getConstantOverrides())
			visitEnumConstantOverride(gmEnumConstantOverride);

		exit();
	}

	private void visitEnumConstantOverride(ProtoGmEnumConstantOverride gmEnumConstantOverride) {
		if (!visitElement(gmEnumConstantOverride, "ProtoGmEnumConstantOverride is null"))
			return;

		if (gmEnumConstantOverride.getEnumConstant() == null)
			addErrorMessage("EnumConstantOverride doesn't reference a Constant");
	}

	// ############################################
	// ## . . . . . . . . Common . . . . . . . . ##
	// ############################################

	private void enter(String string) {
		path.push(string);
	}

	private void exit() {
		path.pop();
	}

	/** @return true iff we are visiting this element for the first time */
	private boolean visitElement(ProtoGmModelElement modelElement, String nullErrorMessage) {
		if (modelElement != null) {
			return visitedModelElements.add(modelElement);

		} else {
			addErrorMessage(nullErrorMessage);
			return false;
		}
	}

	private boolean addErrorMessage(String msg) {
		return errors.add(msg + " Path: " + path);
	}

}
