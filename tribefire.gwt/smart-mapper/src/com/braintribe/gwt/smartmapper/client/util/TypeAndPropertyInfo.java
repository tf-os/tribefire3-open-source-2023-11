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
package com.braintribe.gwt.smartmapper.client.util;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.gwt.smartmapper.client.PropertyAssignmentContext;
import com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

public class TypeAndPropertyInfo {
	
	private static Set<String> ignoredProperties;
	
	static {
		ignoredProperties = new HashSet<>();
		ignoredProperties.add(GenericEntity.globalId);
		ignoredProperties.add(GenericEntity.partition);
//		ignoredProperties.add(GenericEntity.id);
	}
	
	public static String getTypeSignature(GmEntityTypeInfo typeInfo){
		return typeInfo == null ? null : typeInfo.addressedType().getTypeSignature();
	}
	
	public static String getTypeName(GmEntityTypeInfo typeInfo){
		String typeSignature = getTypeSignature(typeInfo);
		return typeSignature.substring(typeSignature.lastIndexOf(".")+1, typeSignature.length());
	}
	
	public static GmEntityType getEntityType(GmEntityTypeInfo info){
		return info == null ? null : info.addressedType();
	}
	
	public static GmPropertyInfo getDirectProperty(GmEntityTypeInfo entityTypeInfo, String propertyName){
		for(GmPropertyOverride override : entityTypeInfo.getPropertyOverrides()){
			if(override.getProperty().getName().equals(propertyName))
				return override;
		}
		
		GmEntityType entityType = entityTypeInfo.addressedType();
		
		for(GmProperty gmProperty : entityType.getProperties()){
			if(gmProperty.getName().equals(propertyName))
				return gmProperty;
		}
		
		for(GmPropertyOverride override : entityType.getPropertyOverrides()){
			if(override.getProperty().getName().equals(propertyName))
				return override;
		}
		
		return null;
	}
	
	public static GmProperty getProperty(GmEntityTypeInfo entityTypeInfo, String propertyName){
		GmEntityType entityType = getEntityType(entityTypeInfo);
		
		return getAllProperties(entityType).stream() //
			.filter((property) -> property.getName().equalsIgnoreCase(propertyName)) //
			.findFirst().get();
	}
	
	public static Set<GmProperty> getAllProperties(GmEntityTypeInfo entityTypeInfo){
		GmEntityType entityType = getEntityType(entityTypeInfo);
		
		Set<GmProperty> gmProperties = new TreeSet<>((o1, o2) -> getPropertyName(o1).compareTo(getPropertyName(o2)));
		if(entityType != null){
			if(entityType.getProperties() != null){
				entityType.getProperties().stream() //
					.filter((gmProperty) -> !ignoredProperties.contains(gmProperty.getName())) //
					.collect(Collectors.toCollection(() -> gmProperties));
			}
			if(entityType.getSuperTypes() != null){
				for(GmEntityType superType : entityType.getSuperTypes()){
					gmProperties.addAll(getAllProperties(superType));
				}
			}
		}
		return gmProperties;
	}
	
	public static String getPropertyName(GmPropertyInfo property){
		return property == null ? null : property.relatedProperty().getName();
	}
	
	public static GmType getPropertyType(GmPropertyInfo property){
		return property == null ? null : property.relatedProperty().getType();
	}
	
	public static boolean isAsIsAvailable(PropertyAssignmentContext pac){
//		String propertyName = getPropertyName(pac.parentProperty);
		
		Optional<GmProperty> op = getAllProperties(pac.mappedToEntityType).stream()
			.filter(propertyAsIsFilter(pac)).findAny();
		
		return op.isPresent();
	}
	
	private static Predicate<GmProperty> propertyAsIsFilter(final PropertyAssignmentContext pac) {
		return new Predicate<GmProperty>() {			
			@Override
			public boolean test(GmProperty t) {
				boolean sameType = true;	
				try {
					GmType gmtype1 = pac.parentProperty.relatedProperty().getType();
					GmType gmtype2 = t.relatedProperty().getType();
					GenericModelType type1 = GMF.getTypeReflection().getType(gmtype1.getTypeSignature());
					GenericModelType type2 = GMF.getTypeReflection().getType(gmtype2.getTypeSignature());
					sameType = type1.isAssignableFrom(type2);
					/*
					if(type1.isGmSimple() && type2.isGmSimple())
						sameType = type1.typeKind() == type2.typeKind();
					else if(type1.isGmEntity() && type2.isGmEntity()) {
						GmEntityType entityType1 = (GmEntityType) type1;
						GmEntityType entityType2 = (GmEntityType) type2;
						sameType = entityType1.
					}
					*/
				}catch(Exception ex) {
					sameType = true;	
				}
				return t.relatedProperty().getName().equalsIgnoreCase(pac.propertyName) && sameType;
			}
		};
	}
	
	public static boolean isSimpleOrEnumType(GmType gmType){
		if(gmType instanceof GmLinearCollectionType)
			return isSimpleOrEnumType(((GmLinearCollectionType)gmType).getElementType());
		return gmType instanceof GmSimpleType || gmType instanceof GmEnumType;
	}
	
	public static boolean isEntityType(GmType gmType){
		if(gmType instanceof GmLinearCollectionType)
			return isEntityType(((GmLinearCollectionType)gmType).getElementType());
		return gmType instanceof GmEntityType;
	}
	
	public static GmEntityType getMappedEntityTypeOfProperty(ModelMdResolver resolver, GmType gmType){
		if(gmType instanceof GmEntityType){
			GmEntityType gmEntityType = (GmEntityType)gmType;
			
			QualifiedEntityAssignment qea = resolver.entityType(gmEntityType).meta(QualifiedEntityAssignment.T).exclusive();
			if(qea != null)
				return qea.getEntityType();
			else 
				return gmEntityType;
			
		}else if(gmType instanceof GmLinearCollectionType){
			return getMappedEntityTypeOfProperty(resolver, ((GmLinearCollectionType)gmType).getElementType());
		}
		return null;
	}
	
}
