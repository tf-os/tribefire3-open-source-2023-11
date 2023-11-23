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
package com.braintribe.model.processing.itw.synthesis.gm;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TransientProperty;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.InterfaceBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.asm.EnhancedEntityImplementer;
import com.braintribe.model.processing.itw.synthesis.gm.asm.EntityTypeImplementer;
import com.braintribe.model.processing.itw.synthesis.gm.asm.PlainEntityImplementer;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;

public class PreliminaryEntityType implements ItwEntityType {

	public ProtoGmEntityType gmEntityType;
	public String entityTypeName;
	public String shortName;

	public AsmClass entityIface;
	public AsmClass weakInterface;
	public AsmClass plainClass;
	public AsmClass enhancedClass;

	public InterfaceBuilder weakInterfaceImplementer;
	public PlainEntityImplementer plainEntityImplementer;
	public EnhancedEntityImplementer enhancedEntityImplementer;

	/**
	 * The EntityType is built in two phases - first we start building it thus crating the corresponding {@link AsmNewClass} (in
	 * {@link GenericModelTypeSynthesis#startBuildingEntityType(ProtoGmEntityType)}). We then proceed to build
	 */
	public EntityTypeImplementer entityTypeImplementer;
	public AsmClass entityTypeClass;
	public JvmEntityType<GenericEntity> entityType;

	/**
	 * Super-type which is used for super-class purposes of plain/enhanced classes. It is the super-type with the biggest number of properties, so
	 * that we re-use the max amount of already existing implemented getters/setters (and readers/writers).
	 */
	public ProtoGmEntityType implSuperType;
	public PreliminaryEntityType implSuperPreliminaryType;
	public Set<String> implSuperTypeProps;

	public List<ItwEntityType> superTypes = newList();

	public PropertyAnalysis propertyAnalysis;
	public Map<String, PreliminaryProperty> preliminaryProperties = newMap();

	public Map<String, ProtoGmPropertyInfo[]> mergedProtoGmProperties;
	private final Map<String, Property> allProperties = newMap();

	public Map<String, AsmClass> allPreliminaryTransientPropsToImpl;
	public Map<String, AsmClass> preliminaryTransientPropsToImpl;
	private final Map<String, TransientProperty> introducedTransientProperties = newMap();
	private final Map<String, TransientProperty> allTransientProperties = newMap();

	public ToStringInformation toStringAnnotation;
	public SelectiveInformation selectiveInformationAnnotation;

	public PreliminaryEntityType(ProtoGmEntityType gmEntityType) {
		this.gmEntityType = gmEntityType;
		this.entityTypeName = gmEntityType.getTypeSignature();
		this.shortName = entityTypeName.substring(entityTypeName.lastIndexOf('.') + 1);
	}

	public void createPreliminaryProperty(ProtoGmProperty gmProperty, ProtoGmPropertyInfo[] propertyLineage) {
		PreliminaryProperty pp = new PreliminaryProperty(this, gmProperty, propertyLineage);
		preliminaryProperties.put(pp.propertyName, pp);
	}

	public PreliminaryProperty getPreliminaryProperty(String propertyName) {
		return preliminaryProperties.get(propertyName);
	}

	public boolean isAbstract() {
		return Boolean.TRUE.equals(gmEntityType.getIsAbstract());
	}

	/**
	 * @return <tt>true</tt> iff this property is not inherited from the implSuperType (see {@link #implSuperType})
	 */
	public boolean mustImplement(String propertyName) {
		return !implSuperTypeProps.contains(propertyName);
	}

	public void addIntroducedProperty(Property property) {
		allProperties.put(property.getName(), property);
	}

	@Override
	public Property findProperty(String name) {
		Property result = allProperties.get(name);
		if (result != null)
			return result;

		for (ItwEntityType superType : superTypes) {
			result = superType.findProperty(name);
			if (result != null) {
				allProperties.put(name, result);

				return result;
			}
		}

		return null;
	}

	public void addIntroducedTransientProperty(TransientProperty property) {
		introducedTransientProperties.put(property.getName(), property);
		allTransientProperties.put(property.getName(), property);
	}

	@Override
	public TransientProperty findTransientProperty(String name) {
		TransientProperty result = allTransientProperties.get(name);
		if (result != null)
			return result;

		for (ItwEntityType superType : superTypes) {
			result = superType.findTransientProperty(name);
			if (result != null) {
				allTransientProperties.put(name, result);
				return result;
			}
		}

		return null;
	}

	public ProtoGmType getDeclaredEvaluatesTo() {
		return gmEntityType.getEvaluatesTo();
	}

	@Override
	public String toString() {
		return entityTypeName + "(prelim)";
	}

	public String getPropertyClassName(String propertyName) {
		ProtoGmPropertyInfo[] propertyLineage = mergedProtoGmProperties.get(propertyName);
		if (propertyLineage == null)
			throw new RuntimeException("Property '" + propertyName + "' not found for: " + entityTypeName);

		ProtoGmProperty nonOverlayProperty = propertyLineage[0].relatedProperty();
		return GmtsHelper.getPropertyClassName(nonOverlayProperty.getDeclaringType(), propertyName);
	}

}
