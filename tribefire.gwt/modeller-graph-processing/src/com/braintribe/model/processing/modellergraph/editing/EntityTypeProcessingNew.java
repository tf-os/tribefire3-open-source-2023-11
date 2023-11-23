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
package com.braintribe.model.processing.modellergraph.editing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.processing.modellergraph.GmModellerMode;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;

public class EntityTypeProcessingNew {
	
private static final String NEW_PROPERTY_NAME = "newProperty";
	
	private static final Set<String> tfKeywords = new HashSet<>(Arrays.asList("partition", "globalId", "id"));
	
	private static final Set<String> javaKeywords = new HashSet<>(Arrays.asList(
		    "abstract",     "assert",        "boolean",      "break",           "byte",
		    "case",         "catch",         "char",         "class",           "const",
		    "continue",     "default",       "do",           "double",          "else",
		    "enum",         "extends",       "false",        "final",           "finally",
		    "float",        "for",           "goto",         "if",              "implements",
		    "import",       "instanceof",    "int",          "interface",       "long",
		    "native",       "new",           "null",         "package",         "private",
		    "protected",    "public",        "return",       "short",           "static",
		    "strictfp",     "super",         "switch",       "synchronized",    "this",
		    "throw",        "throws",        "transient",    "true",            "try",
		    "void",         "volatile",      "while"
		));
	
	private static final String JAVA_CLASS_ATTRIBUTE_NAME_PART_PATTERN = "[A-Za-z_$]+[a-zA-Z0-9_$]*";
	private static final String GENERIC_ENTITIY_TYPE_SIG = "com.braintribe.model.generic.GenericEntity";
	
	public static boolean isValidPropertyName(String text) {
	    for (String part : text.split("\\.")) {
	        if (tfKeywords.contains(part) || javaKeywords.contains(part) || !text.matches(JAVA_CLASS_ATTRIBUTE_NAME_PART_PATTERN)) {
	            return false;
	        }
	    }
	    return text.length() > 0;
	}
	
	public static boolean isNameAvailable(String newName, GmEntityType entityType) {		
		for(GmProperty property : getProperties(entityType)) {
			if(property.getName().toLowerCase().equals(newName.toLowerCase()))
				return false;
		}
		return true;	
	}
		
	public static GmType createType(boolean isEntity, String typeSignature, PersistenceGmSession session, GmMetaModel model) {
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		
		GmType gmType = isEntity ? session.create(GmEntityType.T) : session.create(GmEnumType.T);
		if(isEntity) {
			GmEntityType gmEntityType = (GmEntityType)gmType;		
			gmEntityType.setIsAbstract(false);
			gmEntityType.getSuperTypes().add(genericEntityType(session));
		}else {
			//GmEnumType gmEnumType = (GmEnumType)gmType;			
		}
		gmType.setDeclaringModel(model);
		gmType.setTypeSignature(typeSignature);
		
		model.getTypes().add(gmType);
		nestedTransaction.commit();
		return gmType;
	}
	
	private static GmEntityType genericEntityType(PersistenceGmSession session) {
		GmEntityType type = session.query().entities(EntityQueryBuilder.from(GmEntityType.class).where().property("typeSignature").eq(GENERIC_ENTITIY_TYPE_SIG).done()).first();
		return type;
	}
	
	public static void addRelation(GmEntityType fromType, GmType toType, boolean aggregation, AggregationKind aggregationKind, PersistenceGmSession gmSession, ModelGraphConfigurationsNew config) {
						
		if(aggregation){
			config.modellerMode = GmModellerMode.detailed;
			
			if(config.currentLeftDetailType == null)
				config.currentLeftDetailType = fromType.getTypeSignature();
			if(config.currentRightDetailType == null)
				config.currentRightDetailType = toType.getTypeSignature();
			
			if(!config.currentLeftDetailType.equals(toType.getTypeSignature()) && !config.currentRightDetailType.equals(fromType.getTypeSignature())) {
				config.currentLeftDetailType = fromType.getTypeSignature();
				config.currentRightDetailType  = toType.getTypeSignature();
			}
			
			NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
			GmProperty gmProperty = gmSession.create(GmProperty.T);
			
			if(containsNewPropertyName(fromType))
				gmProperty.setName(NEW_PROPERTY_NAME + "_" + getNewPropertyCount(fromType));
			else
				gmProperty.setName(NEW_PROPERTY_NAME);
			
			gmProperty.setDeclaringType(fromType);
			
			GmType gmType = null;
			switch(aggregationKind){
			case simple_aggregation:
				gmType = toType;
				break;
			case ordered_aggregation:
				String typeSig = "list<" + toType.getTypeSignature() + ">";
				String globalId = "type:"+typeSig;
				gmType = gmSession.findEntityByGlobalId(globalId);
				if(gmType == null){
					gmType = gmSession.create(GmListType.T);
					GmListType gmListType = (GmListType)gmType;
					gmListType.setElementType(toType);
					gmListType.setTypeSignature(typeSig);
					gmListType.setGlobalId(globalId);
				}				
				break;
			case unordered_aggregation:
				typeSig = "set<" + toType.getTypeSignature() + ">";
				globalId = "type:" + typeSig;
				gmType = gmSession.findEntityByGlobalId(globalId);
				if(gmType == null){
					gmType = gmSession.create(GmSetType.T);
					GmSetType gmSetType = (GmSetType)gmType;
					gmSetType.setElementType(toType);
					gmSetType.setTypeSignature(typeSig);
					gmSetType.setGlobalId(globalId);
				}		
				break;
			case key_association:
				gmType = gmSession.create(GmMapType.T);
				GmMapType gmMapType = (GmMapType)gmType;
				gmMapType.setKeyType(toType);
				gmMapType.setTypeSignature("map<" + toType.getTypeSignature() + ",?>");
				String name = toType.getTypeSignature().contains(".") ? toType.getTypeSignature().substring(
						toType.getTypeSignature().lastIndexOf(".")+1,toType.getTypeSignature().length()) : toType.getTypeSignature();
				gmProperty.setName(name + "to?");
				break;
			case value_association:
				gmType = gmSession.create(GmMapType.T);
				gmMapType = (GmMapType)gmType;
				gmMapType.setValueType(toType);
				gmMapType.setTypeSignature("map<?," + toType.getTypeSignature() + ">");
				name = toType.getTypeSignature().contains(".") ? toType.getTypeSignature().substring(
						toType.getTypeSignature().lastIndexOf(".")+1,toType.getTypeSignature().length()) : toType.getTypeSignature();
				gmProperty.setName("?to" +  name);
				break;
			default:
				break;
			}
			gmProperty.setType(gmType);
			config.currentAddedProperty = gmProperty;
			fromType.getProperties().add(gmProperty);			
			nestedTransaction.commit();
		}else{
			boolean canBeAdded = true;
			for(GmEntityType superType : fromType.getSuperTypes()){
				if(superType.getTypeSignature().equals(toType.getTypeSignature())){
					canBeAdded = false;
					break;
				}
			}
			if(((GmEntityType) toType).getSuperTypes() != null && !((GmEntityType) toType).getSuperTypes().isEmpty()){
				for(GmEntityType superType : ((GmEntityType) toType).getSuperTypes()){
					if(superType.getTypeSignature().equals(fromType.getTypeSignature())){
						canBeAdded = false;
						break;
					}
				}
			}
			if(canBeAdded)
				fromType.getSuperTypes().add((GmEntityType) toType);
		}
		
	}

	private static int getNewPropertyCount(GmEntityType gmEntityType) {
		int i = 0;
		
		for(GmProperty p : getProperties(gmEntityType))
			if(p.getName().toLowerCase().startsWith(NEW_PROPERTY_NAME.toLowerCase()))
				i++;
		
		return i;
	}
	
	private static boolean containsNewPropertyName(GmEntityType gmEntityType) {
		for(GmProperty p : getProperties(gmEntityType))
			if(p.getName().equalsIgnoreCase(NEW_PROPERTY_NAME))
				return true;
		
		return false;
	}
	
	private static Set<GmProperty> getProperties(GmEntityType gmEntityType){
		Set<GmProperty> properties = new HashSet<>();
		if(gmEntityType.getProperties() != null)
			properties.addAll(gmEntityType.getProperties());
		if(gmEntityType.getSuperTypes() != null && !gmEntityType.getSuperTypes().isEmpty()){
			for(GmEntityType superType : gmEntityType.getSuperTypes())
				properties.addAll(getProperties(superType));
		}
		return properties;
	}
}
