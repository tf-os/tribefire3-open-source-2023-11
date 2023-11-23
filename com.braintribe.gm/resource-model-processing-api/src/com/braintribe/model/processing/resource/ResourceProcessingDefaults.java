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
package com.braintribe.model.processing.resource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.ResourceSpecification;

public interface ResourceProcessingDefaults {

	default void transferProperties(Resource source, Resource resource) {
		transferProperties(source, resource, EntityType::create);
	}
	default void transferProperties(Resource source, Resource resource, Function<EntityType<?>,GenericEntity> entityFactory) {

		if (source.getMd5() != null) {
			resource.setMd5(source.getMd5());
		}

		if (source.getName() != null) {
			resource.setName(source.getName());
		}

		if (source.getFileSize() != null) {
			resource.setFileSize(source.getFileSize());
		}

		if (source.getMimeType() != null) {
			resource.setMimeType(source.getMimeType());
		}

		if (source.getCreated() != null) {
			resource.setCreated(source.getCreated());
		}

		if (source.getCreator() != null) {
			resource.setCreator(source.getCreator());
		}

		ResourceSpecification sourceSpec = source.getSpecification();
		if (sourceSpec != null) {
			
			ResourceSpecification clonedSpec = sourceSpec.entityType().clone(new StandardCloningContext() {
				@Override
				public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
					return entityFactory.apply(entityType);
				}
				@Override
				public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
						GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
					if (property.isIdentifier() || property.isPartition() || property.isGlobalId()) {
						return false;
					}
					return super.canTransferPropertyValue(entityType, property, instanceToBeCloned, clonedInstance, sourceAbsenceInformation);
				}
			}, sourceSpec, StrategyOnCriterionMatch.skip);
			resource.setSpecification(clonedSpec);
		}

		if (source.getTags() != null && !source.getTags().isEmpty()) {
			if (resource.getTags() == null) {
				Set<String> tags = new HashSet<>();
				tags.addAll(source.getTags());
				resource.setTags(tags);
			} else {
				resource.getTags().addAll(source.getTags());
			}
		}

	}

}
