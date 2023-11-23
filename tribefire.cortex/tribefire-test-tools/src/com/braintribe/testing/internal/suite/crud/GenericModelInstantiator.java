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
package com.braintribe.testing.internal.suite.crud;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.base.GenericBase;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.lcd.CommonTools;

/**
 * * instantiates transitively all entities of the model of given Session = metamodel of its access -> entities of
 * dependencies will be instantiated as well. <br>
 * * Alternatively you can select a subset of sub-models (dependencies of the session access model) - then only entities
 * directly declared in these models will be instantiated <br>
 * * If no filter is set, all properties of all entities will be set as well with instantiated entities or some hard
 * coded values (not null) <br>
 * - If you want to filter some properties so that they won't be set you can use
 * {@link #setPropertyFilter(PropertyFilterPredicate)}<br>
 * - There is a default filter if you set none <br>
 * * Use {@link #start()}
 *
 * @author Neidhart
 *
 */
public class GenericModelInstantiator {
	private static Logger logger = Logger.getLogger(GenericModelInstantiator.class);

	private final Map<GenericModelType, GenericEntity> alreadyCreatedInstancesMap;

	private final PersistenceGmSession session;
	private final Set<CustomType> modelEntities;
	private PropertyFilterPredicate propertyFilterPredicate;

	// @formatter:off
	static final Map<GenericModelType, Object> defaultValues = asMap(
		SimpleType.TYPE_STRING, "Hallo",
		SimpleType.TYPE_BOOLEAN, true,
		SimpleType.TYPE_DATE, new Date(),
		SimpleType.TYPE_DECIMAL, new BigDecimal(2.4),
		SimpleType.TYPE_DOUBLE, 2.4d,
		SimpleType.TYPE_FLOAT, 2.4f,
		SimpleType.TYPE_INTEGER, 2,
		SimpleType.TYPE_LONG, 2l
	);
	// @formatter:on

	public GenericModelInstantiator(PersistenceGmSession session) {
		this.session = session;
		modelEntities = new HashSet<>();
		alreadyCreatedInstancesMap = new HashMap<>();
		propertyFilterPredicate = this::defaultSkipPropertyPredicate;
	}

	public GenericModelInstantiator(PersistenceGmSession session, Collection<EntityType<?>> types) {
		this(session);

		if (types == null || types.size() == 0) {
			throw new IllegalArgumentException("GenericModelInstantiator does not allow empty or null parameters in constructor");
		} else {
			modelEntities.addAll(new ArrayList<>(types));
		}
	}

	public GenericModelInstantiator(PersistenceGmSession session, EntityType<?>... types) {
		this(session);

		if (types.length == 0) {
			throw new IllegalArgumentException("GenericModelInstantiator does not allow empty or null parameters in constructor");
		} else {
			modelEntities.addAll(CommonTools.toList(types));
		}
	}

	public GenericModelInstantiator(PersistenceGmSession session, GmMetaModel... models) {
		this(session);

		if (models.length == 0) {
			throw new IllegalArgumentException("GenericModelInstantiator does not allow empty or null parameters in constructor");
		} else {
			for (GmMetaModel model : models) {
				modelEntities.addAll(
						model.entityTypes().map(x -> GMF.getTypeReflection().getEntityType(x.getTypeSignature())).collect(Collectors.toSet()));
			}
		}
	}

	public void setPropertyFilter(PropertyFilterPredicate propertyFilterPredicate) {
		this.propertyFilterPredicate = propertyFilterPredicate;
	}

	/**
	 * prevents the reuse of entities that existed before clearing the cache <br>
	 * see {@link #getValueOfType(GenericModelType) }
	 */
	public void clearCache() {
		alreadyCreatedInstancesMap.clear();
	}

	public Collection<GenericEntity> getCachedEntities() {
		return alreadyCreatedInstancesMap.values();
	}
	
	public Set<EntityType<?>> getModelEntities(){
		if (modelEntities.isEmpty()) {
			ModelOracle mo = session.getModelAccessory().getOracle();
			modelEntities.addAll(mo.getTypes().onlyEntities().asTypes().collect(Collectors.toSet()));
		}
		
		return modelEntities.stream().map(e -> (EntityType<?>) e).collect(Collectors.toSet());
	}

	public void start() {
		for (EntityType<?> entityType : getModelEntities()) {
			logger.debug("found type " + entityType.getTypeSignature());

			reuseEntityOrCreateNewIncludingProperties(entityType);

			session.commit();

		}
	}

	/**
	 * will reuse entities that were created during the lifetime of this class instance
	 */
	public Object getValueOfType(GenericModelType propertyType) {
		if (propertyType.isEnum()) {
			EnumType enumType = ((EnumType) propertyType);
			Enum<?> enumConstantJava = enumType.getEnumValues()[0];

			return enumConstantJava;
		} else if (propertyType.isEntity()) {
			GenericBase instance = reuseEntityOrCreateNewIncludingProperties((EntityType<?>) propertyType);
			return instance;
		} else if (propertyType.isCollection()) {
			CollectionType collectionType = propertyType.cast();
			Object collection = collectionType.createPlain();
			GenericModelType collectionElementType = collectionType.getCollectionElementType();
			Object element = getValueOfType(collectionElementType);

			switch (collectionType.getCollectionKind()) {
				case list:
				case set:
					((Collection<Object>) collection).add(element);
					return collection;
				case map:
					MapType mapType = (MapType) collectionType;
					Object key = getValueOfType(mapType.getKeyType());
					((Map<Object, Object>) collection).put(key, element);
					return collection;
				default:
					throw new IllegalStateException("Don't know how to handle Collection of type " + collectionType.getTypeSignature());
			}
		} else {
			logger.debug("Set to " + defaultValues.get(propertyType));

			return defaultValues.get(propertyType);
		}

	}

	/**
	 * skips inherited properties except when they are mandatory skips unique types as well as they are not handled
	 * correctly at the moment
	 *
	 */
	private boolean defaultSkipPropertyPredicate(Property property, GenericEntity entity, PersistenceGmSession session) {
		PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);
		boolean relevantType = property.getDeclaringType().equals(entity.entityType()) || propertyMeta.is(Mandatory.T);

		return relevantType;

	}

	public boolean isProblematicProperty(Property property, GenericEntity entity, PersistenceGmSession session) {
		PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);

		return propertyMeta.is(Unique.T);
	}
	
	public int numMandatoryProperties(GenericEntity entity){
		int mandatoryPropertyCounter = 0;
		
		for (Property property: entity.entityType().getProperties()){
			PropertyMdResolver propertyMeta = session.getModelAccessory().getMetaData().entity(entity).property(property);
			
			if (propertyMeta.is(Mandatory.T))
				mandatoryPropertyCounter ++;
		}
		
		return mandatoryPropertyCounter;
	}

	private GenericEntity reuseEntityOrCreateNewIncludingProperties(EntityType<?> entityType) {
		GenericEntity currentEntity = alreadyCreatedInstancesMap.get(entityType);

		if (entityType.isAbstract()) {
			ModelOracle mo = session.getModelAccessory().getOracle();
			Set<EntityType<?>> instantiables = mo.findEntityTypeOracle(entityType).getSubTypes().transitive().onlyInstantiable().asTypes();

			if (instantiables.isEmpty()) {
				logger.warn("Could not find instantiable subtype for " + entityType.getTypeSignature());
				return null;
			} else {
				EntityType<?> instantiableType = instantiables.iterator().next();
				logger.debug("Abstract type: Instantiating instead " + instantiableType.getTypeSignature());

				return reuseEntityOrCreateNewIncludingProperties(instantiableType);
			}
		}

		else if (currentEntity == null) {
			logger.debug("creating new " + entityType.getTypeSignature());

			currentEntity = session.create(entityType);
			alreadyCreatedInstancesMap.put(entityType, currentEntity);

			for (Property property : entityType.getProperties()) {

				if (propertyFilterPredicate.test(property, currentEntity, session) && !isProblematicProperty(property, currentEntity, session)) {
					logger.debug("Found property " + property.getName() + " / " + property.getType().getTypeSignature());

					GenericModelType propertyType = GMF.getTypeReflection().getType(property.getType().getTypeSignature());

					Object value = getValueOfType(propertyType);
					property.set(currentEntity, value);
				}
			}

		}

		return currentEntity;
	}

	public boolean propertyIsNotFiltered(Property property, GenericEntity actualValue) {
		return propertyFilterPredicate.test(property, actualValue, session);
	}
}
