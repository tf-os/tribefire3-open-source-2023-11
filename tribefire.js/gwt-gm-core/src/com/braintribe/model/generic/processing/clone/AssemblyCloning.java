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
package com.braintribe.model.generic.processing.clone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;

public class AssemblyCloning {
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private static final AbsenceInformation absenceInformation = GMF.absenceInformation();
	private TraversingNode lastNode;
	private Map<GenericEntity, GenericEntity> clones = new IdentityHashMap<GenericEntity, GenericEntity>();
	private boolean absentify = true;
	private boolean propertyOnly = true;
	private boolean daw = true;
	// private boolean dar;
	private TcEvaluation tcEvaluation;
	
	public static AssemblyCloningBuilder builder() {
		final AssemblyCloning cloning = new AssemblyCloning();
		return new AssemblyCloningBuilder() {
			
			@Override
			public AssemblyCloningBuilder withAbsenceInformation(boolean absenceInformation) {
				cloning.absentify = absenceInformation;
				return this;
			}
			
			@Override
			public AssemblyCloningBuilder directPropertyWrite(boolean direct) {
				cloning.daw = direct;
				return this;
			}
			
			@Override
			public AssemblyCloningBuilder directPropertyRead(boolean direct) {
				// cloning.dar = direct; // commented out as unused
				return this;
			}
			
			@Override
			public AssemblyCloningBuilder tc(TraversingCriterion traversingCriterion) {
				cloning.tcEvaluation = traversingCriterion != null? new TcEvaluation(traversingCriterion): null;
				return this;
			}
			
			@Override
			public <T> T clone(T assembly) {
				return (T)cloning.cloneAssembly(typeReflection.getBaseType(), assembly);
			}
			
			@Override
			public <T> T clone(GenericModelType type, T assembly) {
				return (T)cloning.cloneAssembly(type, assembly);
			}
		};
	}
	
	private GenericEntity cloneEntity(GenericEntity entity) {
		GenericEntity clone = clones.get(entity);
		
		if (clone != null)
			return clone;
		
		EntityType<?> entityType = entity.entityType();
		
		clone = entityType.create();
		clones.put(entity, clone);
		
		EntityTraversingNode entityTraversingNode = new EntityTraversingNode(entityType, entity);
		
		push(entityTraversingNode);
		
		try {
			boolean dap = this.daw;
			for (Property property: entityType.getProperties()) {
				
				PropertyTraversingNode node = new PropertyTraversingNode(property, entity);
				
				push(node);
				
				try {
					if (isMatching()) {
						if (absentify)
							property.setAbsenceInformation(clone, absenceInformation);
						
						continue;
					}
					
					Object value = node.getValue();
					
					if (value != null) {
						Object clonedValue = cloneValue(property.getType(), value);
						if (dap)
							property.setDirectUnsafe(clone, clonedValue);
						else
							property.set(clone, clonedValue);
							
					}
					else {
						AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);
						if (absenceInformation != null) {
							property.setAbsenceInformation(clone, absenceInformation);
						}
					}
				}
				finally {
					pop();
				}
			}
		}
		finally {
			pop();
		}
		
		return clone;
	}
	
	private List<Object> cloneList(CollectionType collectionType, List<?> collection) {
		
		List<Object> clone = new ArrayList<Object>(collection.size());
		
		if (collection.isEmpty())
			return clone;
		
		GenericModelType elementType = collectionType.getCollectionElementType();
		
		ListElementTraversingNode node = new ListElementTraversingNode(elementType);
		
		push(node);
		
		try {
			for (Object value: collection) {
				node.setValue(value);
				
				if (!propertyOnly && isMatching())
					continue;
				
				Object clonedValue = cloneValue(elementType, value);
				clone.add(clonedValue);
			}
		}
		finally {
			pop();
		}
		
		return clone;
	}
	
	private Set<Object> cloneSet(CollectionType collectionType, Set<?> set) {
		
		Set<Object> clone = new HashSet<Object>(set.size());
		
		if (set.isEmpty())
			return clone;
		
		GenericModelType elementType = collectionType.getCollectionElementType();
		
		SetElementTraversingNode node = new SetElementTraversingNode(elementType);
		
		push(node);
		
		try {
			for (Object value: set) {
				node.setValue(value);
				
				if (!propertyOnly && isMatching())
					continue;
				
				Object clonedValue = cloneValue(elementType, value);
				clone.add(clonedValue);
			}
		}
		finally {
			pop();
		}
		
		return clone;
	}
	
	private Map<Object, Object> cloneMap(CollectionType collectionType, Map<?, ?> map) {
		
		Map<Object, Object> clone = new HashMap<Object, Object>(map.size());
		
		if (map.isEmpty())
			return clone;
		
		MapTraversingNode mapNode = new MapTraversingNode(map, collectionType);
		MapEntryTraversingNode entryNode = new MapEntryTraversingNode(collectionType);
		
		GenericModelType[] parameterization = collectionType.getParameterization();
		GenericModelType keyType = parameterization[0];
		GenericModelType valueType = parameterization[1];

		MapKeyTraversingNode keyNode = new MapKeyTraversingNode(keyType);
		MapValueTraversingNode valueNode = new MapValueTraversingNode(valueType);
		
		push(mapNode);
		push(entryNode);
		
		try {
			for (Map.Entry<?, ?> entry: map.entrySet()) {
				entryNode.setValue(entry);
				
				if (!propertyOnly && isMatching())
					continue;
				
				// clone key
				Object key = entry.getKey();
				Object clonedKey = null;
				keyNode.setValue(key);
				push(keyNode);
				try {
					clonedKey = cloneValue(keyType, key);
				}
				finally {
					pop();
				}
				
				// clone value
				Object value = entry.getValue();
				Object clonedValue = null;
				valueNode.setValue(value);
				push(valueNode);
				try {
					clonedValue = cloneValue(valueType, value);
				}
				finally {
					pop();
				}
				
				// put cloned pair
				clone.put(clonedKey, clonedValue);
			}
		}
		finally {
			pop();
			pop();
		}
		
		return clone;
	}
	
	public Object cloneAssembly(GenericModelType type, Object value) {
		RootTraversingNode node = new RootTraversingNode(type, value);
		push(node);
		
		try {
			return cloneValue(type, value);
		}
		finally {
			pop();
		}
	}
	
	private Object cloneValue(GenericModelType type, Object value) {
		while (true) {
			switch (type.getTypeCode()) {
			case objectType: type = type.getActualType(value); break;
			case entityType: return cloneEntity((GenericEntity)value);
			case listType: return cloneList((CollectionType)type, (List<?>)value);
			case setType: return cloneSet((CollectionType)type, (Set<?>)value);
			case mapType: return cloneMap((CollectionType)type, (Map<?, ?>)value);
			default: return value;
			}
		}
	}
	
	private void push(TraversingNode node) {
		node.prev = lastNode;
		lastNode = node;
	}
	
	private void pop() {
		lastNode = lastNode.prev;
	}

	private boolean isMatching() {
		return tcEvaluation != null && tcEvaluation.matches(lastNode);
	}
	
}
