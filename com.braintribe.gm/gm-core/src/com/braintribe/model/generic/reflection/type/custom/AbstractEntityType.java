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
package com.braintribe.model.generic.reflection.type.custom;

import static com.braintribe.utils.lcd.CollectionTools2.mapBy;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.enhance.SessionUnboundPropertyAccessInterceptor;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.reflection.AbstractGenericModelType;
import com.braintribe.model.generic.reflection.AbstractProperty;
import com.braintribe.model.generic.reflection.Attribute;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityInitializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.PropertyTransferCompetence;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TransientProperty;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.GlobalEntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * Subclasses for instantiable entities must override the methods: {@link #createRaw()} and {@link #createPlainRaw()}.
 */
public abstract class AbstractEntityType<T extends GenericEntity> extends AbstractCustomType implements EntityType<T> {
	private boolean isAbstract;
	private List<Property> properties;
	private List<TransientProperty> transientProperties;
	private List<Property> declaredProperties;
	private List<Property> customTypeProperties;
	private List<AbstractEntityType<?>> superTypes = newList();
	private Set<EntityType<?>> subTypes = newSet();
	// TODO this should not be here after BTT-XY implemented is - in GWT we want the fast JsStringMap for this
	private Map<String, Property> propertiesByName = Collections.EMPTY_MAP;
	private Map<String, TransientProperty> transientPropertiesByName = Collections.EMPTY_MAP;
	private Property idProperty;
	private boolean hasExplicitSelectiveInformation;

	private GenericModelType evaluatesTo;
	private boolean isEffectiveEvaluatesToResolved;
	private GenericModelType effectiveEvaluatesTo;

	private Class<? extends T> plainClass;
	private Class<? extends T> enhancedClass;
	private EntityCriterion criterion;
	private Boolean vd;

	public AbstractEntityType() {
		super(null);
	}

	public void setIsAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	@Override
	public TypeCode getTypeCode() {
		return TypeCode.entityType;
	}

	@Override
	public Class<T> getJavaType() {
		return (Class<T>) javaType;
	}

	@Override
	public final boolean isEntity() {
		return true;
	}

	@Override
	public boolean isVd() {
		if (vd == null)
			vd = ValueDescriptor.T.isAssignableFrom(this);

		return vd;
	}

	public void setJavaType(Class<?> javaType) {
		this.javaType = javaType;
	}

	protected abstract EntityInitializer[] getInitializers();

	@Override
	public Class<? extends T> plainClass() {
		return plainClass;
	}

	public void setPlainClass(Class<? extends T> plainClass) {
		this.plainClass = plainClass;
	}

	@Override
	public Class<? extends T> enhancedClass() {
		return enhancedClass;
	}

	public void setEnhancedClass(Class<? extends T> enhancedClass) {
		this.enhancedClass = enhancedClass;
	}

	public void setProperties(Property[] properties) {
		propertiesByName = newMap(properties.length);

		for (Property property : properties) {
			propertiesByName.put(property.getName(), property);
			if (property.isIdentifier())
				idProperty = property;
		}

		Arrays.sort(properties, Comparator.comparing(Property::getName));
		this.properties = Collections.unmodifiableList(Arrays.asList(properties));
	}

	public void setTransientProperties(List<TransientProperty> properties) {
		transientPropertiesByName = mapBy(properties, TransientProperty::getName);

		properties.sort(Comparator.comparing(TransientProperty::getName));
		this.transientProperties = Collections.unmodifiableList(properties);
	}

	@Override
	public Property getIdProperty() {
		return idProperty;
	}

	@Override
	public GenericModelType getEvaluatesTo() {
		return evaluatesTo;
	}

	@Override
	public GenericModelType getEffectiveEvaluatesTo() {
		if (!isEffectiveEvaluatesToResolved) {
			effectiveEvaluatesTo = resolveEffectiveEvaluatesTo();
			isEffectiveEvaluatesToResolved = true;
		}

		return effectiveEvaluatesTo;
	}
	
	private GenericModelType resolveEffectiveEvaluatesTo() {
		if (evaluatesTo != null)
			return evaluatesTo;

		GenericModelType result = null;
		for (EntityType<?> superType : getSuperTypes())
			result = pickMoreSpecificType(result, superType.getEffectiveEvaluatesTo());

		return result;
	}

	/** We assume one of the types is more specific than the other. */
	private GenericModelType pickMoreSpecificType(GenericModelType t1, GenericModelType t2) {
		if (t1 == null)
			return t2;
		if (t2 == null)
			return t1;

		if (t1.isAssignableFrom(t2))
			return t2;
		else
			return t1;
	}

	
	public void setEvaluatesTo(GenericModelType evaluatesTo) {
		this.evaluatesTo = evaluatesTo;
	}

	@Override
	@Deprecated
	public Set<EntityType<?>> getSubTypes() {
		return subTypes;
	}

	/**
	 * Returns an unmodifiable list of the entity's properties.
	 */
	@Override
	public List<Property> getProperties() {
		return this.properties;
	}

	@Override
	public Property getProperty(String name) throws GenericModelException {
		Property property = propertiesByName.get(name);
		if (property == null)
			throw new GenericModelException("Property [" + name + "] not found for: " + getTypeName());
		return property;
	}

	@Override
	public Property findProperty(String name) {
		return propertiesByName.get(name);
	}

	@Override
	public List<TransientProperty> getTransientProperties() {
		return transientProperties;
	}

	@Override
	public TransientProperty getTransientProperty(String name) throws GenericModelException {
		TransientProperty property = transientPropertiesByName.get(name);
		if (property == null)
			throw new GenericModelException("Transient property \"" + name + "\" not found for: " + getTypeName());
		return property;
	}

	@Override
	public TransientProperty findTransientProperty(String name) {
		return transientPropertiesByName.get(name);
	}

	@Override
	public Stream<Attribute> getAttributes() {
		return null;
	}

	@Override
	public boolean isAssignableFrom(GenericModelType type) {
		if (type.getTypeCode() == TypeCode.entityType)
			return isAssignableFrom((EntityType<?>) type);
		else
			return false;
	}

	@Override
	public abstract boolean isAssignableFrom(EntityType<?> entityType);

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public EntityReference createReference(T entity, Object idValue) {
		EntityReference ref;

		if (idValue != null) {
			ref = PersistentEntityReference.T.createPlain();
			ref.setRefId(idValue);

		} else {
			ref = PreliminaryEntityReference.T.createPlain();
			ref.setRefId(entity.runtimeId());
		}

		ref.setTypeSignature(getTypeName());
		ref.setRefPartition(entity.getPartition());

		return ref;
	}

	@Override
	public EntityReference createGlobalReference(T entity, String globalId) {
		EntityReference ref;

		if (globalId != null) {
			ref = GlobalEntityReference.T.createPlain();
			ref.setRefId(globalId);

		} else {
			ref = PreliminaryEntityReference.T.createPlain();
			ref.setRefId(entity.runtimeId());
		}

		ref.setTypeSignature(getTypeName());
		ref.setRefPartition(entity.getPartition());

		return ref;
	}

	// ############################################################
	// ## . . . . . . Instantiation + Initialization . . . . . . ##
	// ############################################################

	@Override
	public final T createPlain() {
		T result = createPlainRaw();
		return initialize(result);
	}

	@Override
	public T createPlainRaw() {
		throw new UnsupportedOperationException("Cannot instantiate abstract entity type: " + getTypeName());
	}

	@Override
	public final T create() {
		return create(SessionUnboundPropertyAccessInterceptor.INSTANCE);
	}

	@Override
	public final T create(String globalId) {
		T result = create();
		result.setGlobalId(globalId);
		return result;
	}

	@Override
	public final T create(PropertyAccessInterceptor pai) {
		T result = createRaw(pai);
		return initialize(result);
	}

	@Override
	public T createRaw() {
		throw new UnsupportedOperationException("Cannot instantiate abstract entity type: " + getTypeName());
	}

	// This should not be considered as override, as the interface method is deprecated (but this stays)
	@Override
	public <E extends T> E initialize(E entity) {
		EntityInitializer[] initializers = getInitializers();
		if (initializers != null) {
			for (EntityInitializer ei : initializers) {
				ei.initialize(entity);
			}
		}

		return entity;
	}

	public static enum PropertiesOutline {
		entityProperties,
		allProperties
	}

	@Override
	@Deprecated
	public Set<EntityType<?>> getInstantiableSubTypes() {
		Set<EntityType<?>> instantiableSubTypes = newSet();

		getInstantiableSubTypes(instantiableSubTypes);

		return instantiableSubTypes;
	}

	public void getInstantiableSubTypes(Set<EntityType<?>> instantiableSubTypes) {
		if (!isAbstract())
			instantiableSubTypes.add(this);

		for (EntityType<?> subType : subTypes)
			((AbstractEntityType<?>) subType).getInstantiableSubTypes(instantiableSubTypes);
	}

	@Override
	public Object cloneImpl(CloningContext cloningContext, Object object, StrategyOnCriterionMatch strategy) throws GenericModelException {
		if (object == null)
			return null;

		GenericEntity entity = (GenericEntity) object;
		entity = cloningContext.preProcessInstanceToBeCloned(entity);

		GenericEntity entityClone = cloningContext.getAssociated(entity);

		// was it already cloned so we can return a reference here
		if (entityClone != null)
			return entityClone;

		// 'this' might be a super-type of the actual type
		AbstractEntityType<?> actualType = (AbstractEntityType<?>) entity.entityType();

		// create the raw clone and register it as visited to avoid double cloning
		entityClone = cloningContext.supplyRawClone(actualType, entity);
		cloningContext.registerAsVisited(entity, entityClone);

		List<AbstractProperty> actualProperties = (List<AbstractProperty>) (List<?>) actualType.getProperties();

		PropertyTransferCompetence propertyTransferCompetence = cloningContext instanceof PropertyTransferCompetence
				? (PropertyTransferCompetence) cloningContext : null;

		try {

			cloningContext.pushTraversingCriterion(actualType.acquireCriterion(), entity);

			int size = actualProperties.size();
			for (int i = 0; i < size; ++i) {
				AbstractProperty property = actualProperties.get(i);

				AbstractGenericModelType propertyType = (AbstractGenericModelType) property.getType();

				AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

				if (!cloningContext.canTransferPropertyValue(actualType, property, entity, entityClone, absenceInformation))
					continue;

				Object propertyValue = null;
				boolean isAbsenceResolvable = cloningContext.isAbsenceResolvable(property, entity, absenceInformation);
				boolean isPropertyAccessible = (absenceInformation == null || isAbsenceResolvable);
				boolean isPropertyValueUsedForMatch = cloningContext.isPropertyValueUsedForMatching(actualType, entity, property);
				boolean isPropertyResolved = false;
				if (isPropertyAccessible && isPropertyValueUsedForMatch) {
					propertyValue = property.get(entity);
					isPropertyResolved = true;
					absenceInformation = null;
				}

				Object clonedPropertyValue = null;
				boolean handleProperty = true;

				try {
					cloningContext.pushTraversingCriterion(getPropertyCriterion(entity, property), propertyValue);
					if (cloningContext.isTraversionContextMatching()) {
						switch (strategy) {
							case partialize: {
								// here the cloning is stopped and an absence information is placed instead
								if (absenceInformation == null)
									absenceInformation = cloningContext.createAbsenceInformation(property.getType(), entity, property);
								break;
							}
							case reference: {
								// here the cloning is stopped and original reference is transferred
								if (!isPropertyResolved && isPropertyAccessible) {
									propertyValue = property.get(entity);
									isPropertyResolved = true;
									absenceInformation = null;
								}
								clonedPropertyValue = propertyValue;
								break;
							}
							case skip:
								// just do nothing for this property -> clone's property stays untouched
								handleProperty = false;
								break;
						}
					} else {

						if (!isPropertyResolved && isPropertyAccessible) {
							propertyValue = property.get(entity);
							isPropertyResolved = true;
							absenceInformation = null;
						}

						// transfer the cloned value or just null if original value was null
						clonedPropertyValue = propertyValue != null ? propertyType.cloneImpl(cloningContext, propertyValue, strategy) : null;
					}

					// here we actually transfer the clone property value and or existing or generated absence
					// information
					if (handleProperty) {
						if (absenceInformation != null) {
							property.setAbsenceInformation(entityClone, absenceInformation);
							continue;
						}

						// standard post process clonedValue
						Object postProcessedClonedPropertyValue = cloningContext.postProcessCloneValue(propertyType, clonedPropertyValue);
						if (propertyTransferCompetence != null) {
							propertyTransferCompetence.transferProperty(actualType, entity, entityClone, property, postProcessedClonedPropertyValue);
						} else {
							property.set(entityClone, postProcessedClonedPropertyValue);
						}
					}

				} finally {
					cloningContext.popTraversingCriterion();
				}
			}
		} finally {
			cloningContext.popTraversingCriterion();
		}

		return entityClone;
	}

	private BasicCriterion getPropertyCriterion(GenericEntity entity, AbstractProperty property) {
		if (!property.isIdentifier())
			return property.acquireCriterion();

		Object id = entity.getId();
		if (id == null)
			return property.acquireCriterion();

		GenericModelType actualIdType = GMF.getTypeReflection().getType(id);

		PropertyCriterion result = PropertyCriterion.T.createPlainRaw();
		result.setPropertyName(GenericEntity.id);
		result.setTypeSignature(actualIdType.getTypeSignature());

		return result;
	}

	private EntityCriterion acquireCriterion() {
		if (criterion == null) {
			EntityCriterion ec = EntityCriterion.T.createPlainRaw();
			ec.setTypeSignature(getTypeSignature());

			criterion = ec;
		}

		return criterion;
	}

	@Override
	public GenericModelType getActualType(Object value) {
		return value == null ? this : ((GenericEntity) value).type();
	}

	@Override
	public void traverseImpl(TraversingContext traversingContext, Object object) throws GenericModelException {
		if (object == null)
			return;

		GenericEntity entity = (GenericEntity) object;

		// break traversing on recursion
		if (traversingContext.getAssociated(entity) != null)
			return;

		traversingContext.registerAsVisited(entity, entity);

		AbstractEntityType<?> actualType = (AbstractEntityType<?>) entity.entityType();

		try {
			traversingContext.pushTraversingCriterion(actualType.acquireCriterion(), entity);

			/* Some Matchers may operate only on ENTITY level (and e.g. not PROPERTY or ROOT level). To handle that
			 * case, we have to check again here whether the Matcher matches. (see BTT-4113) */
			if (traversingContext.isTraversionContextMatching())
				return;

			List<AbstractProperty> actualProperties = (List<AbstractProperty>) (List<?>) actualType.getProperties();
			int size = actualProperties.size();
			for (int i = 0; i < size; i++) {
				AbstractProperty property = actualProperties.get(i);

				boolean doTraverse = true;

				AbsenceInformation ai = property.getAbsenceInformation(entity);

				if (ai != null)
					doTraverse = traversingContext.isAbsenceResolvable(property, entity, ai);

				if (doTraverse) {
					Object propertyValue = property.get(entity);

					try {
						traversingContext.pushTraversingCriterion(getPropertyCriterion(entity, property), propertyValue);
						if (!traversingContext.isTraversionContextMatching()) {
							AbstractGenericModelType propertyType = (AbstractGenericModelType) property.getType();
							propertyType.traverseImpl(traversingContext, propertyValue);
						}

					} finally {
						traversingContext.popTraversingCriterion();
					}
				}
			}
		} finally {
			traversingContext.popTraversingCriterion();
		}
	}

	public void addSubType(EntityType<?> subType) {
		Set<EntityType<?>> newSubTypes = GMF.platform().isSingleThreaded() ? subTypes : newSet(subTypes);
		newSubTypes.add(subType);
		subTypes = newSubTypes;
	}

	// TODO this might only be used in GWT, maybe move the method to GwtEntityType
	public void setSuperTypes(List<AbstractEntityType<?>> superTypes) {
		this.superTypes = superTypes;

		for (AbstractEntityType<?> superType : superTypes) {
			superType.addSubType(this);
		}
	}

	/** returns the direct supertypes of this {@link EntityType} */
	@Override
	public List<EntityType<?>> getSuperTypes() {
		return (List<EntityType<?>>) (Object) superTypes;
	}

	@Override
	public Iterable<EntityType<?>> getTransitiveSuperTypes(boolean includeSelf, boolean distinct) {
		Collection<EntityType<?>> result = getTransitiveSuperTypes(distinct);
		if (includeSelf)
			result.add(this);

		return result;
	}

	private Collection<EntityType<?>> getTransitiveSuperTypes(boolean distinct) {
		if (distinct) {
			return getSuperTypesRecursive();
		} else {
			List<EntityType<?>> superTypes = newList();
			getSuperTypesRecursive(superTypes);
			return superTypes;
		}
	}

	private Set<EntityType<?>> getSuperTypesRecursive() {
		Set<EntityType<?>> superTypes = newSet();
		getSuperTypesRecursive(superTypes);
		return superTypes;
	}

	private void getSuperTypesRecursive(Collection<EntityType<?>> superTypes) {
		for (AbstractEntityType<?> superType : this.superTypes) {
			superTypes.add(superType);
			superType.getSuperTypesRecursive(superTypes);
		}
	}

	/** This is overridden in sub-types when {@link ToStringInformation} annotation was present. */
	// This should not be considered as override, as the interface method is deprecated (but this stays)
	@Override
	@SuppressWarnings("deprecation") // this will stay, just the iface method will be dropped
	public String toString(T instance) {
		return instance.asString();
	}

	// This should not be considered as override, as the interface method is deprecated (but this stays)
	@Override
	public String getSelectiveInformation(Object instance) {
		return instance != null ? getSelectiveInformationFor((T) instance) : "";
	}

	/**
	 * This should be overridden by sub-class if needed
	 */
	protected String getSelectiveInformationFor(GenericEntity instance) {
		StringBuilder sb = new StringBuilder();

		sb.append(getShortName());
		sb.append('(');
		Object id = instance.getId();

		if (id != null) {
			sb.append(id);
		} else {
			sb.append('~');
			sb.append(instance.runtimeId());
		}

		String partition = instance.getPartition();
		if (partition != null) {
			sb.append(',');
			sb.append(' ');
			sb.append(partition);
		}

		sb.append(')');

		return sb.toString();
	}

	public void setHasExplicitSelectiveInformation(boolean hasExplicitSelectiveInformation) {
		this.hasExplicitSelectiveInformation = hasExplicitSelectiveInformation;
	}

	@Override
	public boolean hasExplicitSelectiveInformation() {
		return hasExplicitSelectiveInformation;
	}

	@Override
	public boolean areEntitiesReachable() {
		return true;
	}

	@Override
	public boolean areCustomInstancesReachable() {
		return true;
	}

	/**
	 * Returns list with the portion of the properties that have types which allow to reach enums or entities
	 */
	@Override
	public List<Property> getCustomTypeProperties() {
		if (customTypeProperties == null)
			customTypeProperties = computeCustomProperties();

		return customTypeProperties;
	}

	private List<Property> computeCustomProperties() {
		List<Property> result = newList(properties.size());

		for (Property property : properties)
			if (property.getType().areCustomInstancesReachable())
				result.add(property);

		return unmodifiableList(result);
	}

	@Override
	public List<Property> getDeclaredProperties() {
		if (declaredProperties == null)
			declaredProperties = computeDeclaredProperties();

		return declaredProperties;
	}

	private List<Property> computeDeclaredProperties() {
		List<Property> list = newList(properties.size());

		for (Property property : properties)
			if (property.getDeclaringType() == this)
				list.add(property);

		return Collections.unmodifiableList(list);
	}

	@Override
	public List<TransientProperty> getDeclaredTransientProperties() {
		return transientProperties.stream() //
				.filter(tp -> tp.getDeclaringType() == this) //
				.collect(Collectors.toList());
	}

	@Override
	public boolean isEmpty(Object value) {
		return value == null;
	}

}
