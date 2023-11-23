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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.processing.itw.asm.AsmAnnotationInstance;
import com.braintribe.model.processing.itw.asm.AsmExistingClass;
import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmType;
import com.braintribe.model.weaving.ProtoGmCollectionType;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmProperty;

public class PropertyAnalysis implements Iterable<PropertyAnalysis.PropertyDescription> {

	public int numberOfProperties;
	public List<PropertyDescription> propertyDescriptions = new ArrayList<PropertyDescription>();
	public Set<String> propertyNames = new HashSet<String>();

	@Override
	public Iterator<PropertyDescription> iterator() {
		return propertyDescriptions.iterator();
	}

	public static enum SetterGetterAchievement {
		missing,
		declared,
	}

	public boolean isEmpty() {
		return propertyDescriptions.isEmpty();
	}

	public static class PropertyDescription {
		public AsmType accessPropertyClass; // this may be a primitive (if class exists and it's property is primitive)
		public AsmType actualPropertyClass;
		public ProtoGmProperty property;
		public String fieldName;
		public String setterName;
		public String getterName;
		
		public SetterGetterAchievement achievement;

		public AsmField plainPropertyField;
		public AsmField enhancedPropertyField;
		public List<AsmAnnotationInstance> annotations;

		public boolean isCollection() {
			return property.getType() instanceof ProtoGmCollectionType;
		}

		public boolean isIntroducedFor(ProtoGmEntityType gmEntityType) {
			return property.getDeclaringType() == gmEntityType;
		}

		public String getName() {
			return property.getName();
		}

		public String getFieldName() {
			return fieldName;
		}

		public AsmType getPropertyType() {
			return accessPropertyClass;
		}

		public boolean isPrimitive() {
			return accessPropertyClass.isPrimitive();
		}

		public Object defaultValue() {
			Class<?> existingClass = ((AsmExistingClass) accessPropertyClass).getExistingClass();
			return GMF.getTypeReflection().getSimpleType(existingClass).getDefaultValue();
		}
	}

}
