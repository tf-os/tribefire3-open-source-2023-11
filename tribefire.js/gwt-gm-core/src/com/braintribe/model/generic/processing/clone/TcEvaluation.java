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

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.DisjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.ListElementCriterion;
import com.braintribe.model.generic.pr.criteria.MapCriterion;
import com.braintribe.model.generic.pr.criteria.MapEntryCriterion;
import com.braintribe.model.generic.pr.criteria.MapKeyCriterion;
import com.braintribe.model.generic.pr.criteria.MapValueCriterion;
import com.braintribe.model.generic.pr.criteria.NegationCriterion;
import com.braintribe.model.generic.pr.criteria.PatternCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyTypeCriterion;
import com.braintribe.model.generic.pr.criteria.RecursionCriterion;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.pr.criteria.ValueConditionCriterion;
import com.braintribe.model.generic.pr.criteria.typematch.AnyTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.CollectionTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeStrategy;
import com.braintribe.model.generic.pr.criteria.typematch.EnumTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.SimpleTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.TypeMatch;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.typecondition.TypeCondition;

@SuppressWarnings("deprecation")
public class TcEvaluation {
	private TraversingCriterion criterion;
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public TcEvaluation(TraversingCriterion criterion) {
		super();
		this.criterion = criterion;
	}

	public void setCriterion(TraversingCriterion criterion) {
		this.criterion = criterion;
	}

	public TraversingCriterion getCriterion() {
		return criterion;
	}
	
	public boolean matches(TraversingNode node) {
		TraversingNodeHolder holder = new TraversingNodeHolder();
		holder.node = node;
		return matches(holder, criterion);
	}
	
	private boolean matches(TraversingNodeHolder nodeHolder, TraversingCriterion criterion) {
		if (criterion == null)
			return false;
		
		switch (criterion.criterionType()) {
		case JOKER:
			TraversingNode node = nodeHolder.node;
			if (node != null) {
				nodeHolder.node = node.prev;
				return true;
			}
			else
				return false;
		case CONJUNCTION:
			return matchesConjunction(nodeHolder, (ConjunctionCriterion)criterion);
		case DISJUNCTION:
			return matchesDisjunction(nodeHolder, (DisjunctionCriterion)criterion);
		case NEGATION:
			return !matches(nodeHolder, ((NegationCriterion)criterion).getCriterion());
		case PATTERN:
			return matchesPattern(nodeHolder, (PatternCriterion)criterion);
		case RECURSION:
			return matchesRecursion(nodeHolder, (RecursionCriterion)criterion);
		case ENTITY:
			return matchesEntity(nodeHolder, (EntityCriterion)criterion);
		case TYPE_CONDITION:
			return matchesTypeCondition(nodeHolder, (TypeConditionCriterion)criterion);
		case PROPERTY_TYPE:
			return matchesPropertyType(nodeHolder, (PropertyTypeCriterion)criterion);
		case VALUE_CONDITION:
			return matchesValueCondition(nodeHolder, (ValueConditionCriterion)criterion);
		case PROPERTY:
			return matchesProperty(nodeHolder, (PropertyCriterion)criterion);
		case LIST_ELEMENT:
			return matchesListElement(nodeHolder, (ListElementCriterion)criterion);
		case SET_ELEMENT:
			return matchesSetElement(nodeHolder, (SetElementCriterion)criterion);
		case MAP_ENTRY:
			return matchesMapEntry(nodeHolder, (MapEntryCriterion)criterion);
		case MAP_KEY:
			return matchesMapKey(nodeHolder, (MapKeyCriterion)criterion);
		case MAP_VALUE:
			return matchesMapValue(nodeHolder, (MapValueCriterion)criterion);
		case MAP:
			return matchesMap(nodeHolder, (MapCriterion)criterion);
		case ROOT:
			return matchesRoot(nodeHolder);
		default:
			break;
		
		}
		return false;
	}
	
	private boolean matchesConjunction(TraversingNodeHolder nodeHolder, ConjunctionCriterion conjunctionCriterion) {
		TraversingNode mark = nodeHolder.node;
		
		boolean first = true;
		for (TraversingCriterion operandCriterion: conjunctionCriterion.getCriteria()) {
			if (first) first = false;
			else nodeHolder.node = mark;
			
			if (!matches(nodeHolder, operandCriterion)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean matchesDisjunction(TraversingNodeHolder nodeHolder, DisjunctionCriterion disjunctionCriterion) {
		TraversingNode mark = nodeHolder.node;
		
		boolean first = true;
		for (TraversingCriterion operandCriterion: disjunctionCriterion.getCriteria()) {
			if (first) first = false;
			else nodeHolder.node = mark;

			if (matches(nodeHolder, operandCriterion))
				return true;
		}

		return false;
	}
	
	private boolean matchesPattern(TraversingNodeHolder nodeHolder, PatternCriterion patternCriterion) {
		ListIterator<TraversingCriterion> patternIterator = patternCriterion.getCriteria().listIterator(patternCriterion.getCriteria().size());
		while (patternIterator.hasPrevious()) {
			if (!matches(nodeHolder,patternIterator.previous())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean matchesRecursion(TraversingNodeHolder nodeHolder, RecursionCriterion recursionCriterion) {
		TraversingCriterion checkCriterion = recursionCriterion.getCriterion();
		int count = 0;

		Integer maxRecursion = recursionCriterion.getMaxRecursion();
		Integer minRecursion = recursionCriterion.getMinRecursion();

		while (matches(nodeHolder, checkCriterion)) {
			count++;
			if (maxRecursion != null && count > maxRecursion)
				return false;
		}

		return minRecursion == null || count >= minRecursion;
	}
	
	private boolean matchesEntity(TraversingNodeHolder nodeHolder, EntityCriterion c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.ENTITY)
				return false;			
			
			EntityTraversingNode entityTraversingNode = (EntityTraversingNode)node;
			
			String requiredTypeSignature = c2.getTypeSignature();

			if (requiredTypeSignature != null) {
				if (c2.getStrategy() == EntityTypeStrategy.assignable) {
					EntityType<?> requiredEntityType = typeReflection.getEntityType(requiredTypeSignature);
					EntityType<?> foundEntityType = entityTraversingNode.getType();
					
					
					return foundEntityType.isAssignableFrom(requiredEntityType);
					//return requiredEntityType.getSubTypes().contains(foundEntityType);
				}
				else {
					return entityTraversingNode.getType().getTypeSignature().equals(requiredTypeSignature);
				}
			}
			else
				return true;

		}
		else
			return false;
	}
	
	private boolean matchesTypeCondition(TraversingNodeHolder nodeHolder, TypeConditionCriterion typeConditionCriterion) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			GenericModelType type = node.getType();
			TypeCondition typeCondition = typeConditionCriterion.getTypeCondition();
			if (typeCondition == null)
				return true;
			
			return typeCondition.matches(type);
		}
		else
			return false;

	}
	
	private boolean matchesPropertyType(TraversingNodeHolder nodeHolder, PropertyTypeCriterion propertyTypeCriterion) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.PROPERTY)
				return false;
			
			GenericModelType type = node.getType();
			return matches(type, propertyTypeCriterion.getTypes());
		}
		else
			return false;
	}
	
	private boolean matchesValueCondition(TraversingNodeHolder nodeHolder, ValueConditionCriterion valueConditionCriterion) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			return matchesValue(node.getType(), valueConditionCriterion, node.getValue());
		}
		else
			return false;
	}
	
	private boolean matchesProperty(TraversingNodeHolder nodeHolder, PropertyCriterion c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.PROPERTY)
				return false;

			PropertyTraversingNode propertyTraversingNode = (PropertyTraversingNode)node;
			
			String propertyName1 = propertyTraversingNode.getProperty().getName();
			String typeSignature1 = propertyTraversingNode.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();
			String propertyName = c2.getPropertyName();
	
			return (typeSignature == null || typeSignature.equals(typeSignature1)) &&
					(propertyName == null || propertyName.equals(propertyName1));
		}
		else
			return false;
	}
	
	private boolean matchesListElement(TraversingNodeHolder nodeHolder, ListElementCriterion c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.LIST_ELEMENT)
				return false;
			
			String typeSignature1 = node.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(typeSignature1);
		}
		else
			return false;
	}
	
	private boolean matchesSetElement(TraversingNodeHolder nodeHolder, SetElementCriterion c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.SET_ELEMENT)
				return false;
			
			String typeSignature1 = node.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(typeSignature1);
		}
		else
			return false;
	}
	
	private boolean matchesMapValue(TraversingNodeHolder nodeHolder, MapValueCriterion  c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.MAP_VALUE)
				return false;
			
			String typeSignature1 = node.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(typeSignature1);
		}
		else
			return false;
	}
	
	private boolean matchesMapKey(TraversingNodeHolder nodeHolder, MapKeyCriterion  c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.MAP_KEY)
				return false;
			
			String typeSignature1 = node.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(typeSignature1);
		}
		else
			return false;
	}
	
	private boolean matchesMapEntry(TraversingNodeHolder nodeHolder, MapEntryCriterion c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.MAP_ENTRY)
				return false;
			
			String typeSignature1 = node.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(typeSignature1);
		}
		else
			return false;
	}
	
	private boolean matchesMap(TraversingNodeHolder nodeHolder, MapCriterion c2) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			if (node.getCriterionType() != CriterionType.MAP)
				return false;
			
			String typeSignature1 = node.getType().getTypeSignature();
			String typeSignature = c2.getTypeSignature();
			
			return (typeSignature == null || typeSignature.equals(typeSignature1));
		}
		else
			return false;
	}
	
	private boolean matchesRoot(TraversingNodeHolder nodeHolder) {
		TraversingNode node = nodeHolder.consum();
		if (node != null) {
			return node.getCriterionType() == CriterionType.ROOT;
		}
		else
			return false;
	}
	

	public boolean matches(GenericModelType type, Set<TypeMatch> typeMatches) {
		for (TypeMatch typeMatch: typeMatches) {
			if (matches(type, typeMatch))
				return true;
		}
		return false;
	}

	public boolean matches(GenericModelType type, TypeMatch typeMatch) {
		if (typeMatch instanceof AnyTypeMatch) {
			return true;
		}
		else if (typeMatch instanceof SimpleTypeMatch) {
			SimpleTypeMatch simpleTypeMatch = (SimpleTypeMatch)typeMatch;
			String typeName = simpleTypeMatch.getSimpleTypeName();
			return (type instanceof SimpleType && (typeName == null || (typeName.equals(type.getTypeName()))));
		}
		else if (typeMatch instanceof EnumTypeMatch) {
			return (type instanceof EnumType);
		}
		else if (typeMatch instanceof EntityTypeMatch) {
			if (type instanceof EntityType<?>) {
				EntityTypeMatch entityTypeMatch = (EntityTypeMatch)typeMatch;
				EntityType<?> entityType = (EntityType<?>)type;
				String typeSignature = entityTypeMatch.getTypeSignature();

				if (typeSignature != null) {
					EntityType<?> otherEntityType = typeReflection.getEntityType(typeSignature);

					if (entityTypeMatch.getWithSubClasses()) {
						while (true) {
							if (entityType == otherEntityType)
								return true;

							List<EntityType<?>> superTypes = entityType.getSuperTypes();

							if (superTypes == null || superTypes.size() == 0)
								break;
							else {
								entityType = superTypes.get(0);
							}
						}

						return false;
					}
					else {
						return otherEntityType == entityType;
					}
				}
				else return true;
			}
			else return false;
		}
		else if (typeMatch instanceof CollectionTypeMatch) {
			if (type instanceof CollectionType) {
				CollectionType collectionType = (CollectionType)type;
				CollectionTypeMatch collectionTypeMatch = (CollectionTypeMatch)typeMatch;
				String collectionTypeName = collectionTypeMatch.getCollectionTypeName();
				List<TypeMatch> parameterMatches = collectionTypeMatch.getParameterMatches();

				if (collectionTypeName != null && collectionTypeName.equals(collectionType.getTypeName()))
					return false;

				if (parameterMatches != null) {
					for (int i = 0; i < parameterMatches.size() && i < collectionType.getParameterization().length; i++) {
						TypeMatch parameterTypeMatch = parameterMatches.get(i);
						GenericModelType parameterType = collectionType.getParameterization()[i];

						if (!matches(parameterType, parameterTypeMatch))
							return false;
					}

					return true;
				}

				return true;
			}
			else return false;
		}
		else
			return false;
	}

	protected boolean matchesValue(GenericModelType type, ValueConditionCriterion criterion, Object stackValue) {
		TypeValuePair typeValuePair = new TypeValuePair();
		typeValuePair.type = type;
		typeValuePair.value = stackValue;
		
		typeValuePair = resolveValue(typeValuePair, criterion.getPropertyPath());
		
		type = typeValuePair.type;
		Object value = typeValuePair.value;
		
		boolean isSimpleType = type instanceof SimpleType;
		boolean isEntityType = type instanceof EntityType<?>; 
		
		if (isSimpleType || isEntityType) {
			Object o1 = value;
			Object o2 = criterion.getOperand();

			Integer res = null;
			
			if (o1 == o2) {
				res = 0;
			}
			else if (o1 == null) {
				res = -1;
			}
			else if (o2 == null) {
				res = 1;
			}
			else if (isSimpleType && o1.getClass() == o2.getClass()) {
				res = compare(o1, o2);
			}
			else {
				if (o1.equals(o2)) {
					res = 0;
				}
			}
				
			
			switch (criterion.getOperator()) {
			case equal:
				return res != null && res == 0; 
			case notEqual:
				return res == null || res != 0;
			case greater:
				return res != null && res > 0;
			case greaterOrEqual:
				return res != null && res >= 0;
			case less:
				return res != null && res < 0;
			case lessOrEqual:
				return res != null && res <= 0;
			case matches:
				return matches(o1, o2, false);
			case matchesIgnoreCase:
				return matches(o1, o2, true);
				
			default:
				return false;
			}
		}
		else
			return false;
	}

	private int compare(Object o1, Object o2) {
		return ((Comparable<Object>)o1).compareTo(o2);
	}
	
	private boolean matches(Object textObject, Object patternObject, boolean caseSensitive) {
		String pattern = resolveString(patternObject);

		if (pattern == null)
			return false;

		String text = resolveString(textObject);

		return (text != null) ? matches(text, pattern, caseSensitive) : false;
	}
	
	private String resolveString(Object o) {
		if (o instanceof String) {
			return (String) o;
		}

		if (o instanceof Enum) {
			return ((Enum<?>) o).name();
		}

		return null;
	}
	
	private boolean matches(String text, String pattern, boolean caseSensitive) {
		if (!caseSensitive) {
			text = text.toLowerCase();
			pattern = pattern.toLowerCase();
		}
		return text.matches(pattern);
	}
	
	
	protected static class TypeValuePair {
		public Object value;
		public GenericModelType type;
	}
	
	private TypeValuePair resolveValue(TypeValuePair base, String propertyPath) {
		if (propertyPath != null && propertyPath.length() > 0) {
			int index = propertyPath.indexOf('.');
			
			String remainingPropertyPath = null;
			String key = null;
			
			
			if (index != -1) {
				key = propertyPath.substring(0, index);
				remainingPropertyPath = propertyPath.substring(index + 1);
			}
			else 
				key = propertyPath;
			
			GenericModelType type = base.type;
			Object value = base.value;
			TypeValuePair nextBase = new TypeValuePair();
			
			switch (type.getTypeCode()) {
			case entityType:
				EntityType<?> entityType = (EntityType<?>)type;
				Property property = entityType.findProperty(key);
				
				if (property != null && value instanceof GenericEntity) {
					
					nextBase.value = property.get((GenericEntity)base.value);
					nextBase.type = property.getType();
				}
				break;
				
			case mapType:
				CollectionType mapType = (CollectionType)type;
				
				GenericModelType keyType = mapType.getParameterization()[0];
				GenericModelType valueType = mapType.getParameterization()[1];
				Object decodedKey = null;
				
				try {
					switch (keyType.getTypeCode()) {
					case enumType:
						decodedKey = ((EnumType)keyType).getInstance(key);
						break;
					case stringType:
						decodedKey = key;
						break;
					case integerType:
						decodedKey = Integer.parseInt(key);
						break;
					case longType:
						decodedKey = Long.parseLong(key);
						break;
					default:
						break;
					}
					
					if (decodedKey != null && value instanceof Map<?, ?>) {
						Map<?, ?> map = (Map<?, ?>)value;
						nextBase.value = map.get(decodedKey);
						nextBase.type = valueType;
					}
				} catch (Exception e) {
					// ignore as this is lenient
				}
				
				break;
				
			case listType:
				CollectionType listType = (CollectionType)type;
				
				try {
					if (value instanceof List<?>) {
						List<?> list = (List<?>)value;
						int listIndex = Integer.parseInt(key);
					
						if (listIndex < list.size()) {
							nextBase.value = list.get(listIndex);
							nextBase.type = listType.getCollectionElementType();
						}
					}
				} catch (NumberFormatException e) {
					// ignore as this is lenient
				}
				
				break;
				
			default:
				break;
			}
			
			if (remainingPropertyPath != null && nextBase.type != null && nextBase.value != null) {
				return resolveValue(nextBase, remainingPropertyPath);
			}
			else 
				return nextBase;
		}
		else
			return base;
	}
	
}
