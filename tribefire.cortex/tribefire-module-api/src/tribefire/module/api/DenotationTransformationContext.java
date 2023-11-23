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
package tribefire.module.api;

import static java.util.Objects.requireNonNull;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;

/**
 * Context given to {@link DenotationMorpher} and {@link DenotationEnricher} when doing the transformation.
 * 
 * @author peter.gazdik
 */
public interface DenotationTransformationContext {

	/**
	 * Non-null identifier of the denotation instance.
	 * <p>
	 * For entities from {@link EnvironmentDenotations} this is the bindId.
	 */
	String denotationId();

	<T extends GenericEntity> T create(EntityType<T> entityType);

	/** Creates a raw instance of given type. Raw means the {@link Property#getInitializer() initial values} of properties are not set. */
	<T extends GenericEntity> T createRaw(EntityType<T> entityType);

	/** Returns entity with given {@code globalId}, or {@code null} if no such entity is found. */
	<T extends GenericEntity> T findEntityByGlobalId(String globalId);

	/** Returns entity with given {@code globalId}, or throws an exception if no such entity is found. */
	default <T extends GenericEntity> T getEntityByGlobalId(String globalId) {
		return requireNonNull(findEntityByGlobalId(globalId), () -> "No entity found with globalId: " + globalId);
	}

}
