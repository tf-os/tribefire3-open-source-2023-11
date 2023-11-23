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
package com.braintribe.utils.genericmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.common.lcd.transformer.Transformer;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.DelegatingEntityVisitor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.PrimitivesTools;
import com.braintribe.utils.ReflectionTools;
import com.braintribe.utils.lcd.Arguments;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.GraphTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * This class may contain Java-only (i.e. GWT incompatible) code. For further information please see
 * {@link com.braintribe.gwt.utils.genericmodel.GMCoreTools}.
 *
 * @author michael.lafite
 */
public class GMCoreTools extends com.braintribe.gwt.utils.genericmodel.GMCoreTools {

	/**
	 * Returns the type of the passed <code>property</code> as a Java class. In contrast to getting the type via
	 * {@link Property#getType()} and {@link GenericModelType#getJavaType()} this method distinguishes between primitive
	 * types and wrapper classes, i.e. it returns the type that was used in the entity definition and not always the
	 * wrapper class.
	 */
	public static Class<?> getPropertyType(final Property property) {
		final GenericModelType propertyType = property.getType();
		final String className = propertyType.getJavaType().getName();

		if (property.isNullable()) {
			try {
				/* PGA: Is there any reason why we are loading a class by a name, which we get from the "Class" object
				 * (see above -> getJavaType().getName()). The thing is, for entities which are entirely generated from
				 * meta-model, this only works because the classes in ITW are loaded using the system ClassLoader. This
				 * was not always so, and might also change in the future, so I would say it's much safer to return the
				 * java type we get from the property object. */
				return Class.forName(className);
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException("Couldn't find class for property type '" + className + "'!", e);
			}
		}

		/* The property type is a primitive type which means the class name is now set to the class name of the wrapper
		 * class and thus we have to get the primitive type. */
		final Class<?> wrapperClass = PrimitivesTools.getWrapperByClassName(className);
		if (wrapperClass == null) {
			throw new RuntimeException("Property is not nullable but the property type is not a wrapper of a primitive type! "
					+ CommonTools.getParametersString("property", property.getName(), "GM property type name", propertyType.getTypeName(),
							"Java property type name", propertyType.getJavaType()));
		}

		return PrimitivesTools.getPrimitive(wrapperClass);
	}

	/**
	 * Gets an enhanced entity set with the specified <code>elementType</code>.
	 */
	public static <T extends GenericEntity> Set<T> getEnhancedEntitySet(final GenericModelTypeReflection typeReflection, final Class<T> elementType) {
		@SuppressWarnings("unchecked")
		final Set<T> set = getEnhancedEntityCollection(typeReflection, Set.class, elementType);
		return set;
	}

	/**
	 * Gets an enhanced entity list with the specified <code>elementType</code>.
	 */
	public static <T extends GenericEntity> List<T> getEnhancedEntityList(final GenericModelTypeReflection typeReflection,
			final Class<T> elementType) {
		@SuppressWarnings("unchecked")
		final List<T> list = getEnhancedEntityCollection(typeReflection, List.class, elementType);
		return list;
	}

	/**
	 * Gets an enhanced entity collection (i.e. list or set) with the specified <code>elementType</code>.
	 */
	private static <C extends Collection<E>, E extends GenericEntity> C getEnhancedEntityCollection(final GenericModelTypeReflection typeReflection,
			final Class<C> collectionClass, final Class<E> elementClass) {
		final GenericModelType elementType = typeReflection.getType(elementClass.getName());
		final CollectionType collectionType = typeReflection.getCollectionType(collectionClass, new GenericModelType[] { elementType });
		@SuppressWarnings("unchecked")
		final C collection = (C) collectionType.create();
		return collection;
	}

	/**
	 * Returns the {@link EntityType} of the passed <code>entityClass</code>.
	 */
	public static <T extends GenericEntity> EntityType<T> getEntityType(final Class<T> entityClass) {
		final EntityType<T> entityType = GMF.getTypeReflection().getEntityType(entityClass);
		return entityType;
	}

	/**
	 * Returns the (unenhanced) Java type of the passed <code>entity</code>.
	 */
	public static <T extends GenericEntity> Class<T> getJavaType(final T entity) {
		return (Class<T>) entity.entityType().getJavaType();
	}

	/**
	 * Returns the {@link PersistentEntityReference} for the specified entity. This can e.g. be used in queries.
	 */
	public static <T extends GenericEntity> PersistentEntityReference getEntityReference(final T entity) {
		final PersistentEntityReference entityReference = PersistentEntityReference.T.create();
		final EntityType<T> entityType = entity.entityType();
		entityReference.setTypeSignature(entityType.getTypeSignature());
		entityReference.setRefId(entity.getId());
		return entityReference;
	}

	/**
	 * Returns the {@link PersistentEntityReference}s for the passed <code>entities</code>. See
	 * {@link #getEntityReference(GenericEntity)}.
	 */
	public static <T extends GenericEntity> List<PersistentEntityReference> getEntityReferences(final Collection<T> entities) {
		final List<PersistentEntityReference> references = new ArrayList<>();
		for (final T entity : NullSafe.iterable(entities)) {
			final PersistentEntityReference reference = getEntityReference(entity);
			references.add(reference);
		}
		return references;
	}

	/**
	 * Visits all reachable entities (once), including the <code>root</code>. Only entities will be passed to the
	 * <code>visitor</code>. Please note that {@link AbsenceInformation} will be resolved, if necessary (while
	 * traversing the entity graph(s)). In case of traversing recursions (loops) it won't visit the entity again.
	 *
	 * @param root
	 *            the root object (usually an entity or a collection of entities).
	 * @param visitor
	 *            an optional visitor.
	 * @param entitiesWhereToStopFurtherTraversing
	 *            if set, entities reachable only from these entities will not be included in the result.
	 *
	 * @return the traversing context.
	 */
	public static StandardTraversingContext visitReachableEntities(final Object root, final TraversingVisitor visitor,
			final Set<? extends GenericEntity> entitiesWhereToStopFurtherTraversing) {
		return visitReachableEntities(root, visitor, entitiesWhereToStopFurtherTraversing, false);
	}

	/**
	 * Visits all reachable entities (once), including the <code>root</code>. Only entities will be passed to the
	 * <code>visitor</code>. Please note that {@link AbsenceInformation} will be resolved, if necessary (while
	 * traversing the entity graph(s)).
	 *
	 * @param root
	 *            the root object (usually an entity or a collection of entities).
	 * @param visitor
	 *            an optional visitor.
	 * @param entitiesWhereToStopFurtherTraversing
	 *            if set, entities reachable only from these entities will not be included in the result.
	 * @param allowRecursions
	 *            if set to true then recursions will be multiple traversed - this leads to duplicate entities
	 *
	 * @return the traversing context.
	 */
	public static StandardTraversingContext visitReachableEntities(final Object root, final TraversingVisitor visitor,
			final Set<? extends GenericEntity> entitiesWhereToStopFurtherTraversing, boolean allowRecursions) {
		Arguments.notNull(root);

		final StandardTraversingContext standardTraversingContext;
		if (allowRecursions) {
			standardTraversingContext = new StandardTraversingContext() {

				@Override
				public <T> T getAssociated(GenericEntity entity) {
					// leads that visiting recursions creates duplicate (visiting) entities
					return null;
				}
			};
		} else {
			standardTraversingContext = new StandardTraversingContext();
		}
		standardTraversingContext.setAbsenceResolvable(true);
		standardTraversingContext.setVisitMatchInclusive(true);
		standardTraversingContext.setMatcher(new EntityVisitingMatcher(entitiesWhereToStopFurtherTraversing));

		if (visitor != null) {
			final TraversingVisitor entityVisitor = new DelegatingEntityVisitor(visitor);
			standardTraversingContext.setTraversingVisitor(entityVisitor);
		}

		final BaseType baseType = GMF.getTypeReflection().getBaseType();
		baseType.traverse(standardTraversingContext, root);

		return standardTraversingContext;
	}

	/**
	 * Returns the result of {@link #findReachableEntities(Object, Set)} with no entities where to stop.
	 */
	public static Set<GenericEntity> findReachableEntities(final Object root) {
		return findReachableEntities(root, null);
	}

	/**
	 * Finds all reachable entities starting from the <code>root</code> object.
	 *
	 * @param root
	 *            the root object (usually an entity or a collection of entities).
	 * @param entitiesWhereToStopFurtherTraversing
	 *            see {@link #visitReachableEntities(Object, TraversingVisitor, Set)}
	 * @return all reachable entities (including <code>root</code>)
	 *
	 * @see #visitReachableEntities(Object, TraversingVisitor, Set)
	 */
	public static Set<GenericEntity> findReachableEntities(final Object root,
			final Set<? extends GenericEntity> entitiesWhereToStopFurtherTraversing) {
		final Set<GenericEntity> reachableEntities = new HashSet<>();

		final TraversingContext traversingContext = visitReachableEntities(root, null, entitiesWhereToStopFurtherTraversing);

		reachableEntities.addAll(traversingContext.getVisitedObjects());
		return reachableEntities;
	}

	/**
	 * Finds all the <i>independent</i> subsets of entities in the passed <code>entitySet</code> and returns them as a
	 * set of sets of entities. Entities <i>depend</i> on each other, if there is a direct or indirect relation (or
	 * <i>path</i>) between them (regardless of the direction). If the passed <code>entitySet</code> is
	 * <code>null</code> or empty, an empty set will be returned.
	 */
	public static <T extends GenericEntity> Set<Set<T>> findIndependentSubsets(final Set<T> entitySet) {
		final Set<Set<T>> subsets = new HashSet<>();
		if (!CommonTools.isEmpty(entitySet)) {
			@SuppressWarnings("unchecked")
			final Set<GenericEntity> entitySetUsedForCasting = (Set<GenericEntity>) entitySet;
			@SuppressWarnings("unchecked")
			final Set<Set<T>> independentSubsetsUsedForCasting = (Set<Set<T>>) (Object) GraphTools.findIndependentSubsets(entitySetUsedForCasting,
					new ReachableEntitiesFinder());
			subsets.addAll(independentSubsetsUsedForCasting);
		}
		return subsets;
	}

	/**
	 * Removes all entity references (including self-references), i.e. sets all entity type properties to
	 * <code>null</code> and removes all elements from collections/maps that hold entities.
	 */
	public static <T extends GenericEntity> void removeEntityReferences(final T entity) {
		Arguments.notNull(entity);
		final EntityType<T> entityType = entity.entityType();
		for (final Property property : entityType.getProperties()) {
			final Object propertyValue = property.get(entity);
			if (propertyValue != null) {
				if (propertyValue instanceof GenericEntity) {
					property.set(entity, null);
				} else if (property.getType().isCollection()) {
					final CollectionType collectionType = (CollectionType) property.getType();
					final GenericModelType[] parameterization = collectionType.getParameterization();
					switch (collectionType.getCollectionKind()) {
						case list:
							// fall through
						case set:
							final Collection<?> collection = (Collection<?>) propertyValue;
							if (!collection.isEmpty()) {

								final GenericModelType collectionElementType = parameterization[0];

								// make sure there is no unexpected type
								assertSupportedCollectionElementType(collectionElementType);

								if (collectionElementType.isEntity()) {
									collection.clear();
								} else if (collectionElementType.isBase()) {
									Set<GenericEntity> elementsToRemove = new HashSet<>();
									for (Object element : collection) {
										if (element instanceof GenericEntity) {
											elementsToRemove.add((GenericEntity) element);
										}
									}
									collection.removeAll(elementsToRemove);
								}
							}

							break;
						case map:
							final Map<?, ?> map = (Map<?, ?>) propertyValue;
							if (!map.isEmpty()) {

								final GenericModelType keyElementType = parameterization[0];
								final GenericModelType valueElementType = parameterization[1];

								// make sure there is no unexpected type
								assertSupportedCollectionElementType(keyElementType);
								assertSupportedCollectionElementType(valueElementType);

								if (keyElementType.isEntity() || valueElementType.isEntity()) {
									map.clear();
								} else if (keyElementType.isBase() || valueElementType.isBase()) {
									Set<Object> keysToRemove = new HashSet<>();
									for (Map.Entry<?, ?> mapEntry : map.entrySet()) {
										Object key = mapEntry.getKey();
										Object value = mapEntry.getValue();
										if (key instanceof GenericEntity || value instanceof GenericEntity) {
											keysToRemove.add(key);
										}
									}
									for (Object keyToRemove : keysToRemove) {
										map.remove(keyToRemove);
									}
								}
							}

							break;
						default:
							throw new UnsupportedEnumException(collectionType.getCollectionKind());
					}
				}
			}
		}
	}

	private static void assertSupportedCollectionElementType(final GenericModelType collectionElementType) {
		if (!isSupportedCollectionElementType(collectionElementType)) {
			throw new GenericRuntimeException("Unsupported collection element type '" + collectionElementType.getTypeSignature() + "'!");
		}
	}

	private static boolean isSupportedCollectionElementType(final GenericModelType type) {
		// PGA TODO - isn't it easier to check that type is not a collection?
		// return !type.isCollection();
		return ReflectionTools.isInstanceOfAny(type, SimpleType.class, EnumType.class, EntityType.class, BaseType.class);
	}

	/**
	 * Checks if the two sets have the same size and only contain {@link #referenceEquals(GenericEntity, GenericEntity)
	 * reference-equal} entities, i.e. for each entity in <code>entitySet1</code> there must be a reference-equal entity
	 * in </code><code>entitySet2</code> and vice-versa.
	 */
	public static boolean referenceEquals(final Set<GenericEntity> entitySet1, final Set<GenericEntity> entitySet2) {
		Arguments.notNullWithNames("entitySet1", entitySet1, "entitySet2", entitySet2);

		if (entitySet1.size() != entitySet2.size()) {
			return false;
		}

		for (final GenericEntity entity : entitySet1) {
			if (!referenceContains(entitySet2, entity)) {
				return false;
			}
		}

		for (final GenericEntity entity : entitySet2) {
			if (!referenceContains(entitySet1, entity)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Compares the two lists and checks if each pair of entities is
	 * {@link #referenceEquals(GenericEntity, GenericEntity) reference-equal}.
	 */
	public static boolean referenceEquals(final List<GenericEntity> entityList1, final List<GenericEntity> entityList2) {
		Arguments.notNullWithNames("entityList1", entityList1, "entityList2", entityList2);

		if (entityList1.size() != entityList2.size()) {
			return false;
		}

		final Iterator<GenericEntity> iterator1 = entityList1.iterator();
		final Iterator<GenericEntity> iterator2 = entityList2.iterator();

		while (iterator1.hasNext()) {
			final GenericEntity entity1 = iterator1.next();
			final GenericEntity entity2 = iterator2.next();
			if (!referenceEquals(entity1, entity2)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks the <code>entityCollection</code> contains an entity that
	 * {@link #referenceEquals(GenericEntity, GenericEntity) reference-equals} the <code>searchedEntity</code>.
	 */
	public static boolean referenceContains(final Collection<GenericEntity> entityCollection, final GenericEntity searchedEntity) {
		Arguments.notNull(searchedEntity);
		for (final GenericEntity entity : NullSafe.iterable(entityCollection)) {
			if (referenceEquals(entity, searchedEntity)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the passed entity instances reference the same GM entity (which is the case if type signature and id
	 * are equal). This method can e.g. be used to compare entity instances from different sessions.
	 *
	 * @throws IllegalArgumentException
	 *             if one of the entities or their ids are <code>null</code>.
	 */
	public static boolean referenceEquals(final GenericEntity entity1, final GenericEntity entity2) throws IllegalArgumentException {
		Arguments.notNullWithNames("entity1", entity1, "entity2", entity2);

		final EntityType<?> entityType1 = entity1.entityType();
		final EntityType<?> entityType2 = entity2.entityType();

		if (!entityType1.getTypeSignature().equals(entityType2.getTypeSignature())) {
			return false;
		}

		final Object id1 = entity1.getId();
		final Object id2 = entity2.getId();

		Arguments.notNullWithNames("entity1 id", id1, "entity2 id", id2);

		return id1.equals(id2);
	}

	/**
	 * Not fully implemented yet! Do not use!
	 * <p/>
	 *
	 * Traverses all entities starting from the passed <code>root</code> and transforms entities using the specified
	 * <code>transformer</code>.
	 *
	 * @param root
	 *            any Generic Model object, e.g. a {@link GenericEntity} or List/Set/Map.
	 *
	 * @param transformer
	 *            the transformer that performs the actual transformation. All entities will be passed to this
	 *            transformer. The transformer may return any object of any type (as long as it's compatible, e.g. with
	 *            the property type) or it can also just return the passed entity (i.e. no transformation).
	 *
	 * @return either the <code>root</code> instance (where entities in the entity tree may have been transformed) or
	 *         the single transformation result (if <code>root</code> itself was transformed).
	 *
	 */
	public static Object traverseAndTransformEntities(Object root,
			final Transformer<GenericEntity, ? extends Object, TraversingContext> transformer) {

		StandardTraversingContext tc = new StandardTraversingContext();
		tc.setTraversingVisitor(new TraversingVisitor() {

			@Override
			public void visitTraversing(TraversingContext traversingContext) {
				Stack<Object> objectStack = traversingContext.getObjectStack();
				Object currentObject = objectStack.peek();

				if (traversingContext.getCurrentCriterionType().equals(CriterionType.PROPERTY)) {
					PropertyCriterion propertyCriterion = (PropertyCriterion) traversingContext.getTraversingStack().peek();
					String propertyName = propertyCriterion.getPropertyName();

					// current object, i.e. last element in stack, is the property value
					Object propertyValue = currentObject;
					// one element before the last element is the entity
					GenericEntity entity = (GenericEntity) objectStack.get(objectStack.size() - 2);

					if (propertyValue instanceof GenericEntity) {
						Object transformedPropertyValue = transformer.transform((GenericEntity) propertyValue, traversingContext);
						if (transformedPropertyValue != propertyValue) {
							// value has been transformed --> set the new property value
							// TODO: check if the transformed value is compatible with the property type
							final EntityType<GenericEntity> entityType = entity.entityType();
							entityType.getProperty(propertyName).set(entity, transformedPropertyValue);
						}
					}
				}
				// TODO: also transform entities in list/set/map
			}
		});

		Object result;
		if (root instanceof GenericEntity) {
			// since the root value itself might be transformed, we have to put the entity into a set
			Set<GenericEntity> helper = new HashSet<>();
			helper.add((GenericEntity) root);
			GMF.getTypeReflection().getBaseType().traverse(tc, helper);
			result = CollectionTools.getSingleElement(helper);
		} else {
			GMF.getTypeReflection().getBaseType().traverse(tc, root);
			result = root;
		}

		return result;
	}

	public static Object getPropertyValue(GenericEntity entity, String propertyName) {
		return entity.entityType().getProperty(propertyName).get(entity);
	}

}
