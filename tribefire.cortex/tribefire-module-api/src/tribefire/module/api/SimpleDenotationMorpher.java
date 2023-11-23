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

import static com.braintribe.utils.lcd.NullSafe.nonNull;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * @author peter.gazdik
 */
public abstract class SimpleDenotationMorpher<S extends GenericEntity, T extends GenericEntity> implements DenotationMorpher<S, T> {

	private final EntityType<S> sourceType;
	private final EntityType<T> targetType;

	protected SimpleDenotationMorpher(EntityType<S> sourceType, EntityType<T> targetType) {
		this.sourceType = nonNull(sourceType, "sourceType");
		this.targetType = nonNull(targetType, "targetType");
	}

	// @formatter:off
	@Override public String name() { return getClass().getSimpleName(); }
	@Override public EntityType<S> sourceType() { return sourceType; }
	@Override public EntityType<T> targetType() { return targetType; }
	// @formatter:on

}
