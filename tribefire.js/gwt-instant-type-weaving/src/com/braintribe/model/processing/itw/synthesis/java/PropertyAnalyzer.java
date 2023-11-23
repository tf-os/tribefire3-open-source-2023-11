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
package com.braintribe.model.processing.itw.synthesis.java;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.synthesis.MdaSynthesis;
import com.braintribe.model.processing.itw.InitializerTools;
import com.braintribe.model.processing.itw.asm.AsmAnnotationInstance;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmMethod;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.TypeBuilder;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.PropertyDescription;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.SetterGetterAchievement;
import com.braintribe.model.processing.itw.tools.ItwTools;
import com.braintribe.model.weaving.ProtoGmCollectionType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmSimpleType;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;

/**
 * 
 */
public class PropertyAnalyzer {

	private final JavaTypeSynthesis jts;

	public PropertyAnalyzer(JavaTypeSynthesis jts) {
		super();
		this.jts = jts;
	}

	public PropertyAnalysis analyzeProperties(TypeBuilder typeBuilder, Map<String, ProtoGmPropertyInfo[]> properties)
			throws JavaTypeSynthesisException {

		return analyzeProperties(typeBuilder.getPreliminaryClass(), properties);
	}

	public PropertyAnalysis analyzeProperties(AsmClass asmClass, Map<String, ProtoGmPropertyInfo[]> properties) throws JavaTypeSynthesisException {

		PropertyAnalysis result = new PropertyAnalysis();

		int propertyIndex = 0;
		for (ProtoGmPropertyInfo[] propertyLineage : properties.values()) {
			ProtoGmPropertyInfo property = propertyLineage[0];
			PropertyDescription description = getPropertyDescription(asmClass, property);
			result.propertyDescriptions.add(description);
			result.propertyNames.add(property.relatedProperty().getName());
		}

		result.numberOfProperties = propertyIndex;

		return result;
	}

	private PropertyDescription getPropertyDescription(AsmClass asmClass, ProtoGmPropertyInfo propertyInfo) throws JavaTypeSynthesisException {

		ProtoGmProperty property = propertyInfo.relatedProperty();
		ProtoGmType propertyType = property.getType();
		AsmClass actualPropertyClass = jts.ensureClass(propertyType);
		AsmClass accessPropertyClass = actualPropertyClass;

		String getterName = ItwTools.getGetterName(property);
		String setterName = ItwTools.getSetterName(property);

		AsmMethod getterMethod = asmClass.getMethod(getterName, accessPropertyClass);

		if (getterMethod == null) {
			// if simple type try primitive variation
			if (propertyType instanceof ProtoGmSimpleType) {
				AsmClass primitiveClass = getPrimitiveClass((ProtoGmSimpleType) propertyType);
				if (primitiveClass != null) {
					getterMethod = asmClass.getMethod(getterName, primitiveClass);

					if (getterMethod != null || !property.getNullable())
						accessPropertyClass = primitiveClass;
				}
			}
		}

		PropertyDescription description = new PropertyDescription();
		if (propertyType instanceof ProtoGmCollectionType) {
			description.actualPropertyClass = jts.ensureClassAsGenericIfNeeded(propertyType);
			description.accessPropertyClass = description.actualPropertyClass;

		} else {
			description.actualPropertyClass = actualPropertyClass;
			description.accessPropertyClass = accessPropertyClass;
		}
		description.property = property;
		description.fieldName = ItwTools.getFieldName(property.getName());
		description.getterName = getterName;
		description.setterName = setterName;
		description.achievement = SetterGetterAchievement.missing;

		if (getterMethod != null) {
			AsmMethod setterMethod = asmClass.getMethod(setterName, AsmClassPool.voidType, accessPropertyClass);

			if (setterMethod == null) {
				throw new JavaTypeSynthesisException("getter/setter inconsistency: No suitable setter for existing getter of property "
						+ asmClass.getName() + "." + property.getName());
			}

			description.achievement = SetterGetterAchievement.declared;
		}

		// if getter inherited (i.e. super type exists, but our type doesn't)
		ProtoGmPropertyInfo p = propertyInfo;
		if ((asmClass instanceof AsmNewClass)
				&& (asmClass.getName().equals(p.declaringTypeInfo().addressedType().getTypeSignature()) || p.getInitializer() != null))
			description.annotations = getAnnotations(p);

		return description;
	}

	private List<AsmAnnotationInstance> getAnnotations(ProtoGmPropertyInfo property) {
		List<AsmAnnotationInstance> result = newList();

		Object initializer = property.getInitializer();
		if (initializer != null) {
			String initializerString = InitializerTools.stringifyInitializer(initializer);
			result.add(new AsmAnnotationInstance(AsmClassPool.initializerType, "value", initializerString));
		}

		// Annotations resulting from configured meta-data
		for (AnnotationDescriptor annotationDescriptor : MdaSynthesis.synthesizeMetaDataAnnotations(property.getMetaData()))
			result.add(jts.toAsmAnnotationInstance(annotationDescriptor));

		return result.isEmpty() ? Collections.<AsmAnnotationInstance> emptyList() : result;
	}

	public AsmClass getPrimitiveClass(ProtoGmSimpleType gmSimpleType) {
		switch (gmSimpleType.getTypeSignature()) {
			case "integer":
				return AsmClassPool.intType;
			case "boolean":
				return AsmClassPool.booleanType;
			case "long":
				return AsmClassPool.longType;
			case "float":
				return AsmClassPool.floatType;
			case "double":
				return AsmClassPool.doubleType;
			default:
				return null;
		}
	}

}
