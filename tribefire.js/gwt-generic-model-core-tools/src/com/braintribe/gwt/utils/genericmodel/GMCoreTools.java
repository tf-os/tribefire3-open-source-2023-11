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
package com.braintribe.gwt.utils.genericmodel;

import static com.braintribe.model.generic.reflection.GmReflectionTools.getAbsenceInformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.braintribe.common.lcd.AssertionException;
import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.utils.genericmodel.entity.lcd.GmEntityTools;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.NullSafe;

/**
 * This class provides Generic Model related utility methods. It can be used to quickly and safely add new GM related
 * methods without having to modify the core artifacts (though for some methods it may be better to directly add them to
 * the appropriate artifact).
 *
 * @author michael.lafite
 */
public class GMCoreTools extends GmEntityTools {

	/**
	 * Returns the first property with the specified <code>propertyName</code> from the passed <code>properties</code>
	 * or <code>null</code> if no matching property is found.
	 */
	public static Property getProperty(final String propertyName, final Property... properties) {
		if (!CommonTools.isEmpty(properties)) {
			for (final Property property : properties) {
				if (property.getName().equals(propertyName)) {
					return property;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the descriptions of the two objects are equal.
	 *
	 * @see #checkDescription(Object, Object)
	 */
	public static boolean descriptionEquals(final Object object1, final Object object2) {
		try {
			checkDescription(object1, object2);
			return true;
		} catch (final AssertionException e) {
			return false;
		}
	}

	/**
	 * Makes sure that the {@link #getDescriptionForObject(Object) description} of the passed <code>object</code>
	 * matches the one of <code>objectToCompareTo</code>.
	 *
	 * @throws AssertionException
	 *             if the descriptions don't match.
	 */
	public static void checkDescription(final Object object, final Object objectToCompareTo) throws AssertionException {
		final String description = getDescriptionForObject(object);
		final String descriptionToCompareTo = getDescriptionForObject(objectToCompareTo);
		if (!description.equals(descriptionToCompareTo)) {
			throw new AssertionException("The descriptions don't match!", description, descriptionToCompareTo);
		}
	}

	public static String stringify(GenericEntity ge) {
		return ge == null ? "null" : ge.stringify();
	}

	/**
	 * Returns a full decription of the passed <code>entity</code> containing all sub entities.
	 */
	public static String getDescription(final GenericEntity entity) {
		return getDescriptionForObject(entity);
	}

	/**
	 * Returns the {@link #getDescription(GenericEntity) description} of the passed <code>entity</code> and adds the
	 * <code>message</code> (+ new line) as prefix.
	 */
	public static String getDescription(final String message, final GenericEntity entity) {
		return message + Constants.LINE_SEPARATOR + getDescriptionForObject(entity);
	}

	/**
	 * Returns a full decription of the passed <code>entities</code> containing all sub entities.
	 */
	public static String getDescription(final Collection<? extends GenericEntity> entities) {
		return getDescriptionForObject(entities);
	}

	/**
	 * Returns a full description of the passed Map of <code>entities</code> containing all sub entities.
	 */
	public static String getDescription(final Map<?, ?> entities) {
		return getDescriptionForObject(entities);
	}

	/**
	 * Returns the {@link #getDescription(Collection) description} of the passed <code>entities</code> and adds the
	 * <code>message</code> (+ new line) as prefix.
	 */
	public static String getDescription(final String message, final Collection<? extends GenericEntity> entities) {
		return message + Constants.LINE_SEPARATOR + getDescriptionForObject(entities);
	}

	/**
	 * Gets a description of the passed <code>object</code> which must be either a {@link GenericEntity} or a
	 * {@link Collection}/{@link Map} of <code>GenericEntity</code>s.
	 *
	 * @see #getDescription(GenericEntity)
	 * @see #getDescription(Collection)
	 */
	public static String getDescriptionForObject(final Object object) {
		if (object == null) {
			return "" + object;
		}

		final StringBuilder stringBuilder = new StringBuilder();
		final Set<GenericEntity> traversedEntities = new HashSet<>();
		final String indentation = "";
		if (object instanceof GenericEntity) {
			getDescription((GenericEntity) object, stringBuilder, traversedEntities, indentation);
		} else if (object instanceof Collection) {
			getDescription((Collection<?>) object, stringBuilder, traversedEntities, indentation);
		} else if (object instanceof Map) {
			getDescription((Map<?, ?>) object, stringBuilder, traversedEntities, indentation);
		} else {
			throw new IllegalArgumentException(
					"Unsupported object type! " + CommonTools.getParametersString("object type", object.getClass(), "object", object));
		}

		return stringBuilder.toString();
	}

	/**
	 * Returns a full decription of the passed <code>entity</code> containing all sub entities and absence information.
	 *
	 * @param indentation
	 *            the indentation used for new lines (not for the current line)
	 *
	 */
	private static void getDescription(final GenericEntity entity, final StringBuilder stringBuilder, final Set<GenericEntity> traversedEntities,
			final String indentation) {

		if (traversedEntities.contains(entity)) {
			stringBuilder.append(getSimpleDescription(entity) + " (already traversed)");
			return;
		}

		traversedEntities.add(entity);

		final String propertyIndentation = indentation + "  ";
		final String collectionElementIndentation = propertyIndentation + "  ";

		final EntityType<GenericEntity> entityType = entity.entityType();
		stringBuilder.append(entityType.getShortName() + "[" + CommonTools.LINE_SEPARATOR);

		final List<Property> properties = CollectionTools.copy(entityType.getProperties());
		Collections.sort(properties, new PropertyComparator());

		for (final Property property : properties) {
			final String propertyName = property.getName();
			stringBuilder.append(propertyIndentation + propertyName + " = ");

			GenericModelType propertyType = property.getType();
			final AbsenceInformation absenceInformation = getAbsenceInformationForProperty(entity, propertyName);

			// TODO: get property value WITHOUT loading absent information
			Object propertyValue = null;
			if (absenceInformation != null) {
				stringBuilder.append("?");
			} else {
				/* TODO: this gets the property value, but also loads absent data --> remove as soon as we can access
				 * the property value without loading that absent data (see above) */
				propertyValue = property.get(entity);
				if (propertyValue == null) {
					stringBuilder.append("null");
				} else {

					if (propertyType instanceof BaseType) {
						// the propertyType is just BaseType, we have to get the actual type
						propertyType = BaseType.INSTANCE.getActualType(propertyValue);
					}

					if (propertyType instanceof SimpleType || propertyType instanceof EnumType) {
						stringBuilder.append(CommonTools.getStringRepresentation(propertyValue));
					} else if (propertyType instanceof EntityType) {
						getDescription((GenericEntity) propertyValue, stringBuilder, traversedEntities, propertyIndentation);
					} else if (propertyType instanceof CollectionType) {
						final CollectionType collectionPropertyType = (CollectionType) propertyType;

						switch (collectionPropertyType.getCollectionKind()) {
							case set:
							case list: {
								getDescription((Collection<?>) propertyValue, stringBuilder, traversedEntities, collectionElementIndentation);
								break;
							}
							case map: {
								getDescription((Map<?, ?>) propertyValue, stringBuilder, traversedEntities, collectionElementIndentation);
								break;
							}
							default:
								throw new UnknownEnumException(collectionPropertyType.getCollectionKind());
						}
					} else {
						stringBuilder.append("(unsupported property type: " + propertyType + "; property value: " + propertyValue);
					}

					// PGA: commenting out as it is dead code:
					// if (absenceInformation != null) {
					// stringBuilder.append(CommonTools.LINE_SEPARATOR);
					// getDescription(absenceInformation, stringBuilder, traversedEntities, propertyIndentation);
					// }
				}
			}

			stringBuilder.append(CommonTools.LINE_SEPARATOR);

		} // end of properties loop
		stringBuilder.append(indentation + "]");
	}

	/**
	 * Returns a description of the passed <code>collection</code>. If it contains entities,
	 * {@link GMCoreTools#getDescription(GenericEntity, StringBuilder, Set, String)} will be used to create description
	 * text.
	 */
	private static void getDescription(Collection<?> collection, final StringBuilder stringBuilder, final Set<GenericEntity> traversedEntities,
			final String indentation) {
		String collectionType = null;
		boolean elementIndexShown = false;
		if (collection instanceof List) {
			collectionType = "list";
			elementIndexShown = true;
		} else if (collection instanceof Set) {
			collectionType = "set";
		} else {
			/* this shouldn't happen since only list or set are expected, but since it's no problem, we don't throw an
			 * exception. */
			collectionType = "collection";
		}

		if (CommonTools.isEmpty(collection)) {
			stringBuilder.append("[empty " + collectionType + "]");
		} else {
			stringBuilder.append(collection.size() + " element" + CommonTools.getPluralS(collection.size()) + ":" + CommonTools.LINE_SEPARATOR);
			int elementIndex = 0;

			if (collection instanceof Set) {
				// replace set with sorted list (to be able to compare descriptions)
				final Set<Object> entitySet = (Set<Object>) collection;
				final List<Object> list = new ArrayList<>();
				list.addAll(entitySet);
				Collections.sort(list, new SimpleDescriptionBasedComparator());
				collection = list;
			}

			for (final Object element : collection) {
				stringBuilder.append(indentation + "element" + (elementIndexShown ? " " + elementIndex++ : "") + ": ");

				if (element != null && element instanceof GenericEntity) {
					getDescription((GenericEntity) element, stringBuilder, traversedEntities, indentation);
				} else {
					stringBuilder.append(CommonTools.getStringRepresentation(element));
				}
				stringBuilder.append(CommonTools.LINE_SEPARATOR);
			}
			// remove last line separator
			stringBuilder.delete(stringBuilder.length() - CommonTools.LINE_SEPARATOR.length(), stringBuilder.length());
		}
	}

	/**
	 * Returns a description of the passed <code>map</code>. If it contains entities,
	 * {@link GMCoreTools#getDescription(GenericEntity, StringBuilder, Set, String)} will be used to create description
	 * text.
	 * <p/>
	 * TODO: not tested yet!
	 */
	private static void getDescription(final Map<?, ?> map, final StringBuilder stringBuilder, final Set<GenericEntity> traversedEntities,
			final String indentation) {

		if (CommonTools.isEmpty(map)) {
			stringBuilder.append("[empty map]");
		} else {
			stringBuilder
					.append(map.size() + " " + CommonTools.getSingularOrPlural("entry", "entries", map.size()) + ":" + CommonTools.LINE_SEPARATOR);

			final Map<Object, Object> treeMap = new TreeMap<>(new SimpleDescriptionBasedComparator());
			treeMap.putAll(map);

			for (final Entry<?, ?> entry : treeMap.entrySet()) {
				stringBuilder.append(indentation + "entry key: ");
				final Object key = entry.getKey();
				if (key != null && key instanceof GenericEntity) {
					getDescription((GenericEntity) key, stringBuilder, traversedEntities, indentation);
				} else {
					stringBuilder.append(CommonTools.getStringRepresentation(key));
				}
				stringBuilder.append(CommonTools.LINE_SEPARATOR);
				stringBuilder.append(indentation + "entry value: ");
				final Object value = entry.getValue();
				if (value != null && value instanceof GenericEntity) {
					getDescription((GenericEntity) value, stringBuilder, traversedEntities, indentation);
				} else {
					stringBuilder.append(CommonTools.getStringRepresentation(value));
				}
				stringBuilder.append(CommonTools.LINE_SEPARATOR);
			}
		}
	}

	/**
	 * @see #getSimpleDescription(GenericEntity, EntityType)
	 */
	public static String getSimpleDescription(final GenericEntity entity) {
		return getSimpleDescription(entity, entity.entityType());
	}

	/**
	 * Returns a simple description of the passed <code>entity</code>. It contains the short entity type name and the id
	 * property.
	 */
	public static <T extends GenericEntity> String getSimpleDescription(final T entity, final EntityType<T> entityType) {
		String idString = "ID=" + CommonTools.getStringRepresentation(entity.getId());
		return entityType.getShortName() + "[" + idString + "]";
	}

	/**
	 * Checks if the entity referenced by <code>entityReference</code> is contained in the passed
	 * <code>collection</code>.
	 */
	public static boolean contains(final Collection<?> collection, final EntityReference entityReference) {
		if (!CommonTools.isEmpty(collection)) {
			for (final Object element : collection) {
				if (element != null && element instanceof GenericEntity) {
					final GenericEntity entityElement = (GenericEntity) element;
					final EntityType<GenericEntity> entityType = entityElement.entityType();
					final String typeSignature = entityType.getTypeSignature();
					final Object id = entityElement.getId();
					if (entityReference.getRefId().equals(id) && entityReference.getTypeSignature().equals(typeSignature)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns the {@link AbsenceInformation} for the specified property or <code>null</code> if there is none.
	 */
	public static AbsenceInformation getAbsenceInformationForProperty(final GenericEntity entity, final String propertyName) {
		if (entity != null) {
			if (entity instanceof EnhancedEntity) {
				return getAbsenceInformation(entity, propertyName);
			}
		}
		return null;
	}

	/**
	 * Checks whether the specified <code>property</code> is absent or not.
	 *
	 * @see #getAbsenceInformationForProperty(GenericEntity, String)
	 */
	public static boolean isAbsent(final GenericEntity entity, final String propertyName) {
		return getAbsenceInformationForProperty(entity, propertyName) != null;
	}

	/**
	 * Returns the Java package name for the passed <code>typeSignature</code>, i.e. the sub string before the last '.'.
	 */
	public static String getJavaPackageNameFromEntityTypeSignature(final String typeSignature) {
		return CommonTools.getPackageNameFromFullyQualifiedClassName(typeSignature);
	}

	/**
	 * Returns the short entity type name for the passed <code>typeSignature</code>, i.e. the sub string after the last
	 * '.'.
	 */
	public static String getSimpleEntityTypeNameFromTypeSignature(final String typeSignature) {
		return CommonTools.getSimpleNameFromFullyQualifiedClassName(typeSignature);
	}

	/**
	 * Returns the short enum type name for the passed <code>typeSignature</code>, i.e. the sub string after the last
	 * '.'.
	 */
	public static String getSimpleEnumTypeNameFromTypeSignature(final String typeSignature) {
		return CommonTools.getSimpleNameFromFullyQualifiedClassName(typeSignature);
	}

	/**
	 * Returns the fully qualified name of the Java class representing the simple type specified by the passed
	 * <code>simpleTypeName</code>.
	 */
	public static String getJavaTypeNameForSimpleType(final String simpleTypeName) {
		final SimpleType simpleType = GMF.getTypeReflection().getType(simpleTypeName);
		if (simpleType == null) {
			throw new IllegalArgumentException(
					"Cannot process unknown simple type name " + CommonTools.getStringRepresentation(simpleTypeName) + "!");
		}
		return simpleType.getJavaType().getName();
	}

	/**
	 * Checks if the passed <code>collection</code> contains ONLY {@link GenericEntity}s (and <code>null</code>)s.
	 */
	public static boolean isEntityCollection(final Collection<?> collection) {
		for (final Object object : NullSafe.iterable(collection)) {
			if (object != null && !(object instanceof GenericEntity)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the {@link PersistentEntityReference} for the specified entity. This can e.g. be used in queries.
	 */
	public static PersistentEntityReference getEntityReference(final Class<? extends GenericEntity> entityClass, final Object id) {
		final PersistentEntityReference entityReference = PersistentEntityReference.T.create();
		entityReference.setTypeSignature(entityClass.getName());
		entityReference.setRefId(id);
		return entityReference;
	}

	/**
	 * Returns the {@link PersistentEntityReference}s for the specified <code>entities</code>. See
	 * {@link #getEntityReference(Class, Object)}.
	 */
	public static List<PersistentEntityReference> getEntityReferences(final Class<? extends GenericEntity> entityClass, final Collection<?> ids) {
		final List<PersistentEntityReference> references = new ArrayList<>();
		for (final Object id : NullSafe.iterable(ids)) {
			final PersistentEntityReference reference = getEntityReference(entityClass, id);
			references.add(reference);
		}
		return references;
	}

	/**
	 * Returns "[entityCount] + " " + entity/entities" (depending on the <code>entityCount</code>). Examples: "1 entity"
	 * , "2 entities".
	 */
	public static String getEntityCountString(final int entityCount) {
		return CommonTools.getCountAndSingularOrPlural(entityCount, "entity", "entities");
	}

	/**
	 * Works like {@link #getEntityCountString(int)}.
	 */
	public static String getEntityCountString(final Collection<?> entities) {
		return getEntityCountString(NullSafe.size(entities));
	}

	/**
	 * Returns "[entityCount] + " " + [entityDescriptionPrefix] + " " + "entity/entities" (depending on the
	 * <code>entityCount</code>). Examples: "1 reachable entity", "2 root entities".
	 */
	public static String getEntityCountString(final int entityCount, final String entityDescriptionPrefix) {
		return CommonTools.getCountAndSingularOrPlural(entityCount, entityDescriptionPrefix + " entity", entityDescriptionPrefix + " entities");
	}

	/**
	 * Works like {@link #getEntityCountString(int, String)}.
	 */
	public static String getEntityCountString(final Collection<?> entities, final String entityDescriptionPrefix) {
		return getEntityCountString(NullSafe.size(entities), entityDescriptionPrefix);
	}

	/**
	 * Creates a new {@link SelectQueryResult} and sets the passed <code>results</code>. It is assumed that the result
	 * is complete (see {@link SelectQueryResult#getHasMore()}).
	 */
	public static SelectQueryResult createSelectQueryResult(List<GenericEntity> results) {
		SelectQueryResult result = SelectQueryResult.T.create();
		result.setHasMore(false);
		List<Object> resultsAsObjectList = (List<Object>) (Object) results;
		result.setResults(resultsAsObjectList);
		return result;
	}
}
