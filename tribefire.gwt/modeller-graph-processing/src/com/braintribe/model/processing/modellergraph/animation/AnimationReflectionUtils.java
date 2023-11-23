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
package com.braintribe.model.processing.modellergraph.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;



import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.modellergraph.graphics.Color;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.modellergraph.graphics.Point;

public class AnimationReflectionUtils {

	private static Map<EntityType<?>, List<Property[]>> entTypeCache = new HashMap<EntityType<?>, List<Property[]>>();
//	private static Set<String> entTypeCache2 = new HashSet<String>();
	private static Set<Class<?>> relevantClasses = new HashSet<Class<?>>();
	
	static{
		relevantClasses.add(Node.class);
		relevantClasses.add(Edge.class);
		relevantClasses.add(Color.class);
		relevantClasses.add(Point.class);
	}
	
	public static List<Property[]> scanEntityForAnimatableProperties(EntityType<? extends GenericEntity> entityType){
//		entTypeCache2.clear();
		if(!entTypeCache.containsKey(entityType)){
			List<Property[]> propertyPaths = new ArrayList<Property[]>();
			scanEntityForAnimatableProperties(entityType, "", entityType, new Property[0],propertyPaths);
			entTypeCache.put(entityType, propertyPaths);
		}
		return entTypeCache.get(entityType);
	}
	
	public static void scanEntityForAnimatableProperties(EntityType<? extends GenericEntity> parentType, @SuppressWarnings("unused") String propName,
			EntityType<? extends GenericEntity> entityType, Property[] propertyPath, List<Property[]> propertyPaths) {
//		if(!entTypeCache2.contains(parentType.getTypeSignature() + propName)){
//			entTypeCache2.add(parentType.getTypeSignature() + propName);
			for(Property property : entityType.getProperties()){
				GenericModelType propertyType = property.getType();
				switch (propertyType.getTypeCode()) {
				case doubleType:
				{
					Property[] newPropertyPath = new Property[propertyPath.length+1];
					System.arraycopy(propertyPath, 0, newPropertyPath, 0, propertyPath.length);
					newPropertyPath[propertyPath.length] = property;
					propertyPaths.add(newPropertyPath);
					break;
				}
				case entityType:
				{
					if(relevantClasses.contains(propertyType.getJavaType())){
						Property[] newPropertyPath = new Property[propertyPath.length+1];
						System.arraycopy(propertyPath, 0, newPropertyPath, 0, propertyPath.length);
						newPropertyPath[propertyPath.length] = property;
						scanEntityForAnimatableProperties(parentType, property.getName(), (EntityType<GenericEntity>) propertyType, newPropertyPath, propertyPaths);
					}
					break;
				}
				default:
					break;
				}						
			}
//			return propertyPaths;
//		}
	}
	
	public static Object getProperyValue(Property [] propertyPath, GenericEntity entity){
		Object value = null;
		for (Property property: propertyPath) {
			value = property.get(entity);
			if(value == null)
				break;
			if (value instanceof GenericEntity)
				entity = (GenericEntity)value;
			
		}
		
		return value;
	}
	public static void setProperyValue(Property [] propertyPath, GenericEntity entity, Object value){
		for (int i = 0; i <= propertyPath.length - 2; i ++) {
			Property property = propertyPath[i];
			entity = (GenericEntity)property.get(entity);
			if(entity == null)
				break;
		}
		
		Property property = propertyPath[propertyPath.length - 1];
		if(entity != null){
			//EntityType entityType = GMF.getTypeReflection().getEntityType(entity);
			//System.err.println("setting " + value + " " + property.getPropertyName() + " of " + entityType.getSelectiveInformation(entity));
			property.set(entity, value);
		}
	}
	
}
