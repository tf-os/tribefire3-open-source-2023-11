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
package com.braintribe.ddra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.common.lcd.function.TriFunction;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.resource.Resource;

public class TypeTraversal<T> {
	private final Set<String> traversedTypeSignatures;
	private final Set<String> included = new HashSet<>();
	private final Set<String> excluded = new HashSet<>();
	private final ModelMdResolver mdResolver;
	private final T parent;
	private final TriFunction<T, Property, EntityType<?>, T> resultFactory;
	private final EntityType<?> entityType;

	public static List<TypeTraversalResult> traverseType(ModelMdResolver mdResolver, EntityType<?> entityType) {
		return traverseType(mdResolver, entityType, TypeTraversalResult::new);
	}
	
	public static <T extends TypeTraversalResult> List<T> traverseType(ModelMdResolver mdResolver, EntityType<?> entityType, TriFunction<T, Property, EntityType<?>, T> resultFactory) {
		return typeTraversal(mdResolver, resultFactory, entityType).traverseType();
	}
	
	private static <T> TypeTraversal<T> typeTraversal(ModelMdResolver mdResolver, TriFunction<T, Property, EntityType<?>, T> resultFactory, EntityType<?> entityType) {
		return new TypeTraversal<>(mdResolver, new HashSet<>(), null, resultFactory, entityType);
	}
	
	private TypeTraversal(ModelMdResolver mdResolver, Set<String> traversedTypeSignatures, T parent, TriFunction<T, Property, EntityType<?>, T> resultFactory, EntityType<?> entityType) {
		this.mdResolver = mdResolver;
		this.parent = parent;
		this.resultFactory = resultFactory;
		this.entityType = entityType;
		this.traversedTypeSignatures = traversedTypeSignatures;
	}
	
	private List<T> traverseType() {
		EntityMdResolver entityMdResolver = mdResolver.entityType(entityType);

		return entityType.getProperties() //
			.stream() //
			.filter(p -> propertyFilter(p, entityMdResolver))
			.sorted(MetadataUtils.propertyComparator(entityMdResolver)) //
			.flatMap(p -> handleProperty(entityMdResolver, p).stream())
			.collect(Collectors.toList());
	}
	
	private boolean propertyFilter(Property property, EntityMdResolver entityMdResolver) {
		// Embedded.include overrides Hidden
		if (!included.isEmpty())
			return included.contains(property.getName());
		
		return MetadataUtils.isVisible(entityMdResolver).atProperty(property) && !excluded.contains(property.getName());
	}

	private List<T> handleProperty(EntityMdResolver entityMdResolver, Property property) {
		List<T> encounteredProperties = new ArrayList<>();
		
		GenericModelType propertyType = property.getType();

		boolean embedProperty = false;
		if (propertyType.isBase() && included.contains(property.getName())) {
			embedProperty = true;
		}
		else if (propertyType.isScalar()) {
			embedProperty = true;
		} else if (propertyType.isCollection() && propertyType instanceof LinearCollectionType
				&& (((LinearCollectionType) propertyType).getCollectionElementType().isScalar() || ((LinearCollectionType) propertyType).getCollectionElementType() == Resource.T)) {
			embedProperty = true;
		} else {

			String propertyTypeSignature = propertyType.getTypeSignature();

			if (propertyTypeSignature.equals(Resource.T.getTypeSignature())) {
				embedProperty = true;
			}
			if (propertyType.isEntity() && traversedTypeSignatures.add(propertyTypeSignature)) {
				// traversedTypeSignatures are tracked to avoid potential cycles
				PropertyMdResolver propertyMdResolver = entityMdResolver.property(property);
				Embedded embedded = propertyMdResolver.meta(Embedded.T).exclusive();
				
				if (embedded != null) {
					TypeTraversal<T> child = child(property, embedded);
					encounteredProperties.addAll(child.traverseType());
					embedProperty = true;
				}
				traversedTypeSignatures.remove(propertyTypeSignature);
			}
		}

		if (embedProperty) {
			encounteredProperties.add(propertyTraversalResult(property));
		}
		
		return encounteredProperties;
	}

	private T propertyTraversalResult(Property property) {
		return resultFactory.apply(parent, property, entityType);
	}
	
	private TypeTraversal<T> child(Property property, Embedded embedded) {
		TypeTraversal<T> child = new TypeTraversal<>(mdResolver, traversedTypeSignatures, 
				propertyTraversalResult(property), resultFactory, 
				(EntityType<?>) property.getType());
		
		child.included.addAll(embedded.getIncludes());
		child.excluded.addAll(embedded.getExcludes());
		
		return child;
	}
}
