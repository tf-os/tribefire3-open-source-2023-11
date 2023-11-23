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
package com.braintribe.model.generic.pr.criteria.matching;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.common.UserInfo;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.AclCriterion;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
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
import com.braintribe.model.generic.pr.criteria.PlaceholderCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyTypeCriterion;
import com.braintribe.model.generic.pr.criteria.RecursionCriterion;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.pr.criteria.TypedCriterion;
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
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.utils.collection.impl.AttributeContexts;

@SuppressWarnings("deprecation")
public class StandardMatcher implements Matcher {
	private TraversingCriterion criterion;
	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private boolean checkOnlyProperties = true;
	private Function<String, TraversingCriterion> placeholderLookup = s -> null;
	private Function<ValueDescriptor, Object> propertyValueComparisonResolver = null;

	public static StandardMatcher create(TraversingCriterion tc) {
		StandardMatcher result = new StandardMatcher();
		result.setCriterion(tc);

		return result;
	}

	public void setCriterion(TraversingCriterion criterion) {
		this.criterion = criterion;
	}

	public TraversingCriterion getCriterion() {
		return criterion;
	}
	
	public void setCheckOnlyProperties(boolean checkOnlyProperties) {
		this.checkOnlyProperties = checkOnlyProperties;
	}

	/** Sets a resolver for {@link PlaceholderCriterion}, which resolves the actual {@link TraversingCriterion} based on the placholder's name. */
	public void setPlaceholderLookup(Function<String, TraversingCriterion> placeholderLookup) {
		this.placeholderLookup = placeholderLookup;
	}
	
	public void setPropertyValueComparisonResolver(Function<ValueDescriptor, Object> propertyValueComparisonResolver) {
		this.propertyValueComparisonResolver = propertyValueComparisonResolver;
	}

	@Override
	public boolean matches(TraversingContext traversingContext) {
		if (checkOnlyProperties && traversingContext.getCurrentCriterionType() != CriterionType.PROPERTY) {
			return false;
		}
		
		return matches(new RewindIterator(traversingContext.getTraversingStack(), traversingContext.getObjectStack()), criterion);
	}
	
	public boolean matches(TcIterator iterator) {
		if(checkOnlyProperties && iterator.hasPrevious()){
			Object mark = iterator.mark();
			iterator.previous();
			BasicCriterion currentCriterion = iterator.getCriterion();
			iterator.reset(mark);
			if(currentCriterion.criterionType() != CriterionType.PROPERTY){
				return false;
			}
		}
		return matches(iterator, criterion);
	}
	
	public boolean matches(List<BasicCriterion> criterionStack) {
		return matches(new RewindIterator(criterionStack, Collections.nCopies(criterionStack.size(), null)), criterion);
	}
	
	private static class RewindIterator implements TcIterator {
		private final List<BasicCriterion> criteria;
		private final List<Object> values;
		private int index;
		
		public RewindIterator(List<BasicCriterion> criteria, List<Object> values) {
			this.criteria = criteria;
			this.values = values;
			reset(criteria.size());
		}
		
		/* (non-Javadoc)
		 * @see com.braintribe.model.generic.pr.criteria.matching.TcIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {
			return index > 0;
		}
		
		/* (non-Javadoc)
		 * @see com.braintribe.model.generic.pr.criteria.matching.TcIterator#previous()
		 */
		@Override
		public void previous() {
			index--;
		}
		
		/* (non-Javadoc)
		 * @see com.braintribe.model.generic.pr.criteria.matching.TcIterator#getCriterion()
		 */
		@Override
		public BasicCriterion getCriterion() {
			return criteria.get(index);
		}
		
		/* (non-Javadoc)
		 * @see com.braintribe.model.generic.pr.criteria.matching.TcIterator#getValue()
		 */
		@Override
		public Object getValue() {
			return values.get(index);
		}
		
		/* (non-Javadoc)
		 * @see com.braintribe.model.generic.pr.criteria.matching.TcIterator#mark()
		 */
		@Override
		public Object mark() {
			return index;
		}
		
		/* (non-Javadoc)
		 * @see com.braintribe.model.generic.pr.criteria.matching.TcIterator#reset(int)
		 */
		@Override
		public void reset(Object index) {
			this.index = (Integer)index;
		}
	}
	
	public boolean matches(TcIterator it, TraversingCriterion criterion) {
		if (criterion == null)
			return false;
		
		switch (criterion.criterionType()) {
		case JOKER:
			if (it.hasPrevious()) {
				it.previous();
				return true;
			}
			else
				return false;
		case CONJUNCTION:
			return matchesConjunction(it, (ConjunctionCriterion)criterion);
		case DISJUNCTION:
			return matchesDisjunction(it, (DisjunctionCriterion)criterion);
		case NEGATION:
			return !matches(it, ((NegationCriterion)criterion).getCriterion());
		case PATTERN:
			return matchesPattern(it, (PatternCriterion)criterion);
		case PLACEHOLDER:
			return matchesPlaceholder(it, (PlaceholderCriterion)criterion);
		case RECURSION:
			return matchesRecursion(it, (RecursionCriterion)criterion);
		case ENTITY:
			return matchesEntity(it, (EntityCriterion)criterion);
		case ACL:
			return matchesAclEntity(it, (AclCriterion)criterion);
		case TYPE_CONDITION:
			return matchesTypeCondition(it, (TypeConditionCriterion)criterion);
		case PROPERTY_TYPE:
			return matchesPropertyType(it, (PropertyTypeCriterion)criterion);
		case VALUE_CONDITION:
			return matchesValueCondition(it, (ValueConditionCriterion)criterion);
		case PROPERTY:
			return matchesProperty(it, (PropertyCriterion)criterion);
		case LIST_ELEMENT:
			return matchesListElement(it, (ListElementCriterion)criterion);
		case SET_ELEMENT:
			return matchesSetElement(it, (SetElementCriterion)criterion);
		case MAP_ENTRY:
			return matchesMapEntry(it, (MapEntryCriterion)criterion);
		case MAP_KEY:
			return matchesMapKey(it, (MapKeyCriterion)criterion);
		case MAP_VALUE:
			return matchesMapValue(it, (MapValueCriterion)criterion);
		case MAP:
			return matchesMap(it, (MapCriterion)criterion);
		case ROOT:
			return matchesRoot(it);
		default:
			break;
		
		}
		return false;
	}
	
	
	private boolean matchesConjunction(TcIterator it, ConjunctionCriterion conjunctionCriterion) {
		Object mark = it.mark();
		boolean first = true;
		for (TraversingCriterion operandCriterion: conjunctionCriterion.getCriteria()) {
			if (first) first = false;
			else it.reset(mark);
			
			if (!matches(it, operandCriterion)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean matchesDisjunction(TcIterator it, DisjunctionCriterion disjunctionCriterion) {
		Object mark = it.mark();
		boolean first = true;
		for (TraversingCriterion operandCriterion: disjunctionCriterion.getCriteria()) {
			if (first) first = false;
			else it.reset(mark);

			if (matches(it, operandCriterion))
				return true;
		}

		return false;
	}
	
	private boolean matchesPattern(TcIterator it, PatternCriterion patternCriterion) {
		ListIterator<TraversingCriterion> patternIterator = patternCriterion.getCriteria().listIterator(patternCriterion.getCriteria().size());
		while (patternIterator.hasPrevious()) {
			if (!matches(it,patternIterator.previous())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean matchesPlaceholder(TcIterator it, PlaceholderCriterion placeholderCriterion) {
		TraversingCriterion tc = placeholderLookup.apply(placeholderCriterion.getName());
		if (tc == null)
			throw new NoSuchElementException("No TraversingCriterion was found for placeholder: " + placeholderCriterion.getName());
		
		return matches(it, tc);
	}

	private boolean matchesRecursion(TcIterator it, RecursionCriterion recursionCriterion) {
		TraversingCriterion checkCriterion = recursionCriterion.getCriterion();
		int count = 0;

		Integer maxRecursion = recursionCriterion.getMaxRecursion();
		Integer minRecursion = recursionCriterion.getMinRecursion();

		while (matches(it, checkCriterion)) {
			count++;
			if (maxRecursion != null && count > maxRecursion)
				return false;
		}

		return minRecursion == null || count >= minRecursion;
	}
	
	private boolean matchesEntity(TcIterator it, EntityCriterion c2) {
		if (it.hasPrevious()) {
			it.previous();
			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.ENTITY)
				return false;
			
			EntityCriterion c1 = (EntityCriterion)candidate;
			
			String foundTypeSignature = c1.getTypeSignature();
			String requiredTypeSignature = c2.getTypeSignature();

			if (requiredTypeSignature != null) {
				if (c2.getStrategy() == EntityTypeStrategy.assignable) {
					if (foundTypeSignature.equals(requiredTypeSignature))
						return true;

					EntityType<?> requiredEntityType = typeReflection.findType(requiredTypeSignature);
					EntityType<?> foundEntityType = typeReflection.findType(foundTypeSignature);

					if (requiredEntityType == null || foundEntityType == null)
						return false;

					return requiredEntityType.isAssignableFrom(foundEntityType);
				}
				else {
					return foundTypeSignature.equals(requiredTypeSignature);
				}
			}
			else
				return true;

		}
		else
			return false;
	}

	private boolean matchesAclEntity(TcIterator it, AclCriterion c2) {
		if (!it.hasPrevious())
			return false;

		it.previous();
		BasicCriterion candidate = it.getCriterion();
		if (candidate.criterionType() != CriterionType.ENTITY)
			return false;

		Object o = it.getValue();
		if (!(o instanceof HasAcl))
			return false;

		HasAcl aclEntity = (HasAcl) o;
		String op = c2.getOperation();

		UserInfo ui = resolveUserInfo();

		if (ui == null)
			return aclEntity.isOperationGranted(op, null, emptySet());
		else
			return aclEntity.isOperationGranted(op, ui.userName(), ui.roles());

	}

	private UserInfo resolveUserInfo() {
		AttributeContext ac = AttributeContexts.peek();
		return ac == null ? null : ac.findOrNull(UserInfoAttribute.class);
	}

	private boolean matchesTypeCondition(TcIterator it, TypeConditionCriterion criterion2) {
		if (it.hasPrevious()) {
			it.previous();
			BasicCriterion criterion1 = it.getCriterion();
			
			if (criterion1 != null) {
				TypedCriterion typedCriterion = criterion1;
				TypeConditionCriterion typeConditionCriterion = criterion2;
				String typeSignature = typedCriterion.getTypeSignature();
				TypeCondition typeCondition = typeConditionCriterion.getTypeCondition();
				if (typeCondition == null)
					return true;
				
				GenericModelType type = typeReflection.findType(typeSignature);
				return type != null && typeCondition.matches(type);
			}
			else
				return false;
		}
		else
			return false;

	}
	
	private boolean matchesPropertyType(TcIterator it, PropertyTypeCriterion propertyTypeCriterion) {
		if (it.hasPrevious()) {
			it.previous();
			BasicCriterion candidate = it.getCriterion();
			
			if (candidate.criterionType() != CriterionType.PROPERTY)
				return false;
			
			PropertyCriterion propertyCriterion = (PropertyCriterion)candidate;
	
			String typeSignature = propertyCriterion.getTypeSignature();
			GenericModelType type = typeReflection.findType(typeSignature);
	
			return type != null && matches(type, propertyTypeCriterion.getTypes());
		}
		else
			return false;
	}
	
	private boolean matchesValueCondition(TcIterator it, ValueConditionCriterion valueConditionCriterion) {
		if (it.hasPrevious()) {
			it.previous();
			BasicCriterion candidate = it.getCriterion();
			return matchesValue(candidate, valueConditionCriterion, it.getValue());
		}
		else
			return false;
	}
	
	private boolean matchesProperty(TcIterator it, PropertyCriterion c2) {
		if (it.hasPrevious()) {
			it.previous();
			BasicCriterion candidate = it.getCriterion();
			
			if (candidate.criterionType() != CriterionType.PROPERTY)
				return false;
			
			PropertyCriterion c1 = (PropertyCriterion)candidate;
			String typeSignature = c2.getTypeSignature();
			String propertyName = c2.getPropertyName();
	
			return (typeSignature == null || typeSignature.equals(c1.getTypeSignature())) &&
				(propertyName == null || propertyName.equals(c1.getPropertyName()));
		}
		else
			return false;
	}
	
	private boolean matchesListElement(TcIterator it, ListElementCriterion c2) {
		if (it.hasPrevious()) {
			it.previous();
			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.LIST_ELEMENT)
				return false;
			
			ListElementCriterion c1 = (ListElementCriterion)candidate;
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(c1.getTypeSignature());
		}
		else
			return false;
	}
	
	private boolean matchesSetElement(TcIterator it, SetElementCriterion c2) {
		if (it.hasPrevious()) {
			it.previous();
			
			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.SET_ELEMENT)
				return false;
			
			SetElementCriterion c1 = (SetElementCriterion)candidate;
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(c1.getTypeSignature());
		}
		else
			return false;
	}
	
	private boolean matchesMapValue(TcIterator it, MapValueCriterion  c2) {
		if (it.hasPrevious()) {
			it.previous();
			
			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.MAP_VALUE)
				return false;
			
			MapValueCriterion c1 = (MapValueCriterion)candidate;
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(c1.getTypeSignature());
		}
		else
			return false;
	}
	
	private boolean matchesMapKey(TcIterator it, MapKeyCriterion  c2) {
		if (it.hasPrevious()) {
			it.previous();

			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.MAP_KEY)
				return false;
			
			MapKeyCriterion c1 = (MapKeyCriterion)candidate;
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(c1.getTypeSignature());
		}
		else
			return false;
	}
	
	private boolean matchesMapEntry(TcIterator it, MapEntryCriterion c2) {
		if (it.hasPrevious()) {
			it.previous();
			
			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.MAP_ENTRY)
				return false;
			
			MapEntryCriterion c1 = (MapEntryCriterion)candidate;
			String typeSignature = c2.getTypeSignature();

			return typeSignature == null || typeSignature.equals(c1.getTypeSignature());
		}
		else
			return false;
	}
	
	private boolean matchesMap(TcIterator it, MapCriterion c2) {
		if (it.hasPrevious()) {
			it.previous();
			
			BasicCriterion candidate = it.getCriterion();
			if (candidate.criterionType() != CriterionType.MAP)
				return false;
			
			MapCriterion c1 = (MapCriterion)candidate;
			String typeSignature = c2.getTypeSignature();

			return (typeSignature == null || typeSignature.equals(c1.getTypeSignature()));
		}
		else
			return false;
	}
	
	private boolean matchesRoot(TcIterator it) {
		if (it.hasPrevious()) {
			it.previous();
			return it.getCriterion().criterionType() == CriterionType.ROOT;
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
					EntityType<?> otherEntityType = typeReflection.findEntityType(typeSignature);

					if (otherEntityType == null)
						return false;

					if (entityTypeMatch.getWithSubClasses()) {
						while (true) {
							if (entityType == otherEntityType)
								return true;

							List<EntityType<?>> superTypes = entityType.getSuperTypes();

							if (superTypes == null || superTypes.isEmpty())
								return false;

							entityType = superTypes.get(0);
						}
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

	protected boolean matchesValue(BasicCriterion stackCriterion, ValueConditionCriterion criterion, Object stackValue) {
		String typeSignature = stackCriterion.getTypeSignature();
		GenericModelType type = typeReflection.findType(typeSignature);
		if (type == null)
			return false;
		
		TypeValuePair typeValuePair = new TypeValuePair();
		typeValuePair.type = type;
		typeValuePair.value = stackValue;
		
		typeValuePair = resolveValue(typeValuePair, criterion.getPropertyPath());
		
		type = typeValuePair.type;
		Object value = typeValuePair.value;
		
		
		GenericModelType valueType = typeReflection.getType(value);
		
		boolean isNull = (value == null);
		boolean isSimpleType = valueType.isSimple();
		boolean isEntityType = valueType.isEntity();
		boolean isEnumType = valueType.isEnum();

		if (isNull || isSimpleType || isEnumType || isEntityType) {
			Object o1 = value;
			Object o2 = resolvePropertyValueComparison(criterion.getOperand());

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

	private Object resolvePropertyValueComparison(Object operand) {
		if (operand == null) {
			return null;
		}
		if (this.propertyValueComparisonResolver != null && operand instanceof ValueDescriptor) {
			return this.propertyValueComparisonResolver.apply(((ValueDescriptor) operand));
		}
		return operand; 
	}

	private int compare(Object o1, Object o2) {
		return ((Comparable<Object>) o1).compareTo(o2);
	}
	
	private boolean matches(Object textObject, Object patternObject, boolean caseSensitive) {
		String pattern = resolveString(patternObject);

		if (pattern == null) {
			return false;
		}

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
