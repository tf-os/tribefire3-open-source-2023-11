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
package com.braintribe.model.deployment;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.data.ExplicitPredicate;
import com.braintribe.model.meta.data.ModelSkeletonCompatible;

/**
 * This {@link EntityTypeMetaData} serves as a marker for {@link Deployable} types.
 * 
 * <p>
 * When added to {@link com.braintribe.model.meta.GmEntityType}s representing a {@link Deployable} type, it marks the
 * related {@link Deployable} entity type as a component type, thus it can be used as a key for retrieving components
 * from a deployed unit.
 * 
 * <p>
 * This metadata is resolved in a non-exclusive way which results in multiple components per {@link Deployable}, leading
 * to the need of a bundling deployed unit.
 */
public interface DeployableComponent extends EntityTypeMetaData, ExplicitPredicate, ModelSkeletonCompatible {

	EntityType<DeployableComponent> T = EntityTypes.T(DeployableComponent.class);

}
