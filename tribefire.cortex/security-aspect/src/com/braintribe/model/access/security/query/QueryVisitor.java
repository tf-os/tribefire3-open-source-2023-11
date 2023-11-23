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
package com.braintribe.model.access.security.query;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.Source;

/**
 * Visitor used for detecting sources of given {@link Query}.
 */
class QueryVisitor extends EntityVisitor {

	private final Set<Source> sources = newSet();
	private final Set<PropertyOperand> props = newSet();

	private final Set<Source> simpleCollectionJoins = newSet();

	private final SourcesDescriptor result = new SourcesDescriptor();

	@Override
	protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
		if (entity instanceof Source) {
			sources.add((Source) entity);

		} else if (entity instanceof PropertyOperand) {
			props.add((PropertyOperand) entity);

		} else if (entity instanceof Query) {
			/* These two cases are grouped for performance reasons. Since with each traversing we visit a query only
			 * once, these cases almost never happen. So if we have to make one extra check every time entity is say
			 * EntityQuery, but spare one check every time entity is none of the types we look for (we do not check both
			 * EntityQuery and Property Query, but just Query), this should save us some time during every single
			 * traversing. */

			if (entity instanceof EntityQuery) {
				EntityQuery entityQuery = (EntityQuery) entity;
				result.defaultSource = newSource(entityQuery.getEntityTypeSignature());
				sources.add(result.defaultSource);

			} else if (entity instanceof PropertyQuery) {
				PropertyQuery propertyQuery = (PropertyQuery) entity;
				EntityType<?> entityType = propertyQuery.getEntityReference().valueType();

				result.setPropertyOwnerType(entityType);

				EntityType<?> propertyType = resolvePropertyType(entityType, propertyQuery.getPropertyName());

				if (propertyType != null) {
					result.defaultSource = newSource(propertyType.getTypeSignature());
					// this could happen if we query a property which is not entity (if that is possible)
					sources.add(result.defaultSource);
				}
			}
		}
	}

	private From newSource(String entityTypeSignature) {
		From from = From.T.create();
		from.setEntityTypeSignature(entityTypeSignature);
		return from;
	}

	/** creates new query source description for visited sources */
	protected final SourcesDescriptor resolveSourceTypes() {
		for (Source source : sources)
			resolveSourceType(source);

		for (PropertyOperand propertyOperand : props) {
			Source chainSource = propertyOperand.getSource();
			resolveEntireChain(chainSource, propertyOperand.getPropertyName());
		}

		return result;
	}

	/* Keeps track of paths we have already examined for given source. Note that it's important to track this for
	 * sources and not just source-types, since we want to add an ETILS condition for each eligible source+path. So even
	 * if two sources are in fact the same entity type, we want to add conditions for both of them. */
	private final Map<Source, Map<String, EntityType<?>>> sourcePaths = newMap();

	private EntityType<?> resolveEntireChain(Source chainSource, String propertyPath) {
		Map<String, EntityType<?>> pathsForSource = acquirePathFor(sourceOrDefaultSource(chainSource));

		EntityType<?> result = pathsForSource.get(propertyPath);
		if (result != null)
			return result;

		result = resolveForGivenPath(chainSource, propertyPath);
		if (result == null)
			// this can happen on the top level, if the entire path leads to a property that is not an entity
			return null;

		pathsForSource.put(propertyPath, result);
		notifyResolvedPath(chainSource, propertyPath, result);

		return result;
	}

	private Map<String, EntityType<?>> acquirePathFor(Source chainSource) {
		Map<String, EntityType<?>> map = sourcePaths.get(chainSource);

		if (map == null) {
			map = newMap();
			sourcePaths.put(chainSource, map);
		}

		return map;
	}

	private EntityType<?> resolveForGivenPath(Source chainSource, String propertyPath) {
		if (propertyPath == null)
			return resolveSourceType(chainSource);

		EntityType<?> ownerType;
		String propertyName;

		int pos = propertyPath.lastIndexOf(".");
		if (pos > 0) {
			String pathToOwner = propertyPath.substring(0, pos);
			ownerType = resolveEntireChain(chainSource, pathToOwner);
			propertyName = propertyPath.substring(pos + 1);

		} else {
			ownerType = resolveSourceType(chainSource);
			propertyName = propertyPath;
		}

		return resolvePropertyType(ownerType, propertyName);
	}

	private EntityType<?> resolveSourceType(Source source) {
		source = sourceOrDefaultSource(source);

		EntityType<?> sourceType = result.getSourceType(source);

		if (sourceType == null) {
			if (simpleCollectionJoins.contains(source))
				return null;

			if (source instanceof From) {
				String typeSignature = ((From) source).getEntityTypeSignature();
				sourceType = GMF.getTypeReflection().getEntityType(typeSignature);

			} else if (source instanceof Join) {
				Join join = (Join) source;
				sourceType = resolveTypeForProperty(join.getSource(), join.getProperty());

			} else {
				throw new RuntimeException("Unsupported Source type: " + source.getClass().getName());
			}

			if (sourceType != null)
				result.addExplicitSource(source, sourceType);
			else
				simpleCollectionJoins.add(source);
		}

		return sourceType;
	}

	private void notifyResolvedPath(Source chainSource, String propertyPath, EntityType<?> propertyType) {
		PropertyOperand propertyOperand = QueryTools.newPropertyOperand(chainSource, propertyPath);

		result.addImplicitSource(propertyOperand, propertyType);
	}

	private EntityType<?> resolveTypeForProperty(Source source, String propName) {
		EntityType<?> sourceType = resolveSourceType(source);
		return resolvePropertyType(sourceType, propName);
	}

	private EntityType<?> resolvePropertyType(EntityType<?> sourceType, String propName) {
		Property property = sourceType.getProperty(propName);
		GenericModelType propertyType = property.getType();

		if (propertyType instanceof CollectionType) {
			propertyType = ((CollectionType) propertyType).getCollectionElementType();
		}

		return propertyType instanceof EntityType<?> ? (EntityType<?>) propertyType : null;
	}

	private Source sourceOrDefaultSource(Source source) {
		return source != null ? source : result.defaultSource;
	}

}
