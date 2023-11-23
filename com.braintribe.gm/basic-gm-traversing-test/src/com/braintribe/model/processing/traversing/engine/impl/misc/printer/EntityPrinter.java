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
package com.braintribe.model.processing.traversing.engine.impl.misc.printer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.JavaStringLiteralEscape;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;

public class EntityPrinter {
	
	private  Map<Long,String> entityIdMap;
	private  int entityIdCounter;
	private  Map<Integer,String> hashIdMap;
	private  int hashIdCounter;
	
	private boolean hashIdExistsPreviously;
	private List<String> visistedIdhash;
	private List<String> originalVisistedIdhash;
	
	public EntityPrinter(){
		entityIdMap = new HashMap<Long, String>();
		entityIdCounter = 1;
		hashIdMap = new HashMap<Integer, String>();
		hashIdCounter = 1;
	}

	private String getEntityId(Long id) {
		String result = entityIdMap.get(id);

		if (result == null || result == "") {
			result = "entityId_" + entityIdCounter++;
			entityIdMap.put(id, result);
		}
		return result;
	}
	
	private String getHashId(int id) {
		String result = hashIdMap.get(id);

		if (result == null || result == "") {
			result = "hashId_" + hashIdCounter++;
			hashIdMap.put(id, result);
		}
		else{
			setHashIdExistsPreviously(true);
			visistedIdhash.add(result);
			getOriginalVisistedIdhash().add(result);
		}
		return result;
	}
	
	public String buildToString(GenericEntity genericEntity, EntityType<?> entityType) {
		if (entityType == null)
			return "";
		if (genericEntity == null){
			return "null";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(entityType.getShortName());

		builder.append('[');
		builder.append('@');
		builder.append(getHashId(System.identityHashCode(genericEntity)));
		for (Property property: entityType.getProperties()) {
			builder.append(',');
			if (property.isIdentifier()){				
				builder.append(getEntityId((Long) property.get(genericEntity)));
			}
			else{
				renderProperty(builder, genericEntity, property);
			}
		}
		builder.append(']');

		return builder.toString();
	}

	protected void renderProperty(StringBuilder builder, GenericEntity genericEntity, Property property) {
		String propertyName = property.getName();

		builder.append(propertyName);
		builder.append('=');

		if (property.isAbsent(genericEntity)) {
			builder.append("absent");
		} else {
			GenericModelType type = property.getType();
			Object value = property.get(genericEntity);

			if (value == null) {
				builder.append("null");
			} else {
				if (type instanceof BaseType) {
					type = guessRealTypeFromValue(value);
				} else if (type instanceof EntityType) {
					EntityType<?> valueEntityType = (EntityType<?>) type;
					builder.append(valueEntityType.getShortName());

//					Property idProperty = valueEntityType.getIdProperty();
//					builder.append('[');
//					builder.append('@');
//					builder.append(System.identityHashCode(value));
//					if (idProperty != null) {
//						builder.append(',');
//						renderProperty(builder, (GenericEntity) value, null, idProperty);
//					}
//					builder.append(']');
					builder.append(buildToString((GenericEntity) value, valueEntityType));
				} else if (type instanceof EnumType) {
					String prefix = type.getJavaType().getSimpleName();
					builder.append(prefix + "." + value);
				} else if (type instanceof CollectionType) {
					CollectionType collectionType = (CollectionType) type;
					CollectionKind collectionKind = collectionType.getCollectionKind();
					builder.append(collectionKind);
					builder.append("[size=");
					switch (collectionKind) {
						case list:
						case set:
							builder.append(((Collection<?>) value).size() + (((Collection<?>) value).size() > 0? ",": "") );
							GenericModelType collectionElementType = collectionType.getCollectionElementType();
							if(collectionElementType instanceof SimpleType){
								boolean firstEntry = true;
								for(Object item : (Collection<?>) value){
									builder.append(firstEntry ? "":",");
									builder.append(item);
									firstEntry = false;
								}
							}
							else{
								EntityType<?> valueEntityType = (EntityType<?>) collectionType.getCollectionElementType();
								for(Object item : (Collection<?>) value){
									builder.append(buildToString((GenericEntity) item, valueEntityType));
								}
							}
							break;
						case map:
							Map<?, ?> currentMap = (Map<?, ?>) value;
							builder.append(currentMap.size() + (currentMap.size() > 0? ",": "") );
							if(currentMap.size() > 0){
								GenericModelType[] parameterization = collectionType.getParameterization();
								GenericModelType mapKeyType = parameterization[0];
								GenericModelType mapValueType = parameterization[1];
								
								boolean firstEntry = true;
								for (Map.Entry<?, ?> entry: currentMap.entrySet()) {
									builder.append(firstEntry ? "":",");
									builder.append("Key:");
									if(mapKeyType instanceof SimpleType){
										builder.append(entry.getKey());	
									}
									else{
										builder.append(buildToString((GenericEntity) entry.getKey(),(EntityType<?>)  mapKeyType));
									}
									builder.append(",Value:");
									if(mapValueType instanceof SimpleType){
										builder.append(entry.getValue());	
									}
									else{
										builder.append(buildToString((GenericEntity) entry.getValue(),(EntityType<?>)  mapValueType));
									}
									firstEntry = false;
								}
							}
							break;
					}
					builder.append(']');
				} else {
					if (value instanceof String) {
						value = '"' + JavaStringLiteralEscape.escape((String) value) + '"';
					}
					builder.append(value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static GenericModelType guessRealTypeFromValue(Object value) {
		if (value instanceof List<?>) {
			return GMF.getTypeReflection().getType("list<object>");
		} else if (value instanceof Set<?>) {
			return GMF.getTypeReflection().getType("set<object>");
		} else if (value instanceof Map<?, ?>) {
			return GMF.getTypeReflection().getType("map<object,object>");
		} else if (value instanceof GenericEntity) {
			return ((GenericEntity) value).entityType();
		} else if (value instanceof Enum<?>) {
			return GMF.getTypeReflection().getEnumType((Class<Enum<?>>) value.getClass());
		} else
			return GMF.getTypeReflection().getSimpleType(value.getClass());
	}

	
	public void resetForClone(){
		setHashIdExistsPreviously(false);
		setVisistedIdhash(new ArrayList<String>());
	}
	public void resetForOriginal(){
		setOriginalVisistedIdhash(new ArrayList<String>());
	}
	
	public boolean isHashIdExistsPreviously() {
		return hashIdExistsPreviously;
	}

	public void setHashIdExistsPreviously(boolean hashIdExistsPreviously) {
		this.hashIdExistsPreviously = hashIdExistsPreviously;
	}

	public List<String> getVisistedIdhash() {
		return visistedIdhash;
	}

	public void setVisistedIdhash(List<String> visistedIdhash) {
		this.visistedIdhash = visistedIdhash;
	}

	public List<String> getOriginalVisistedIdhash() {
		return originalVisistedIdhash;
	}

	public void setOriginalVisistedIdhash(List<String> originalVisistedIdhash) {
		this.originalVisistedIdhash = originalVisistedIdhash;
	}
}
