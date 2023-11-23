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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.module.api.DenotationEnricher.EnrichFunction;
import tribefire.module.api.DenotationMorpher.MorphFunction;

/**
 * A registry for {@link DenotationTransformer}s.
 * 
 * @author peter.gazdik
 */
public interface DenotationTransformerRegistry {

	/**
	 * Registers a {@link DenotationMorpher} via {@link #registerMorpher(String, EntityType, EntityType, MorphFunction)}, but setting the name to
	 * "Standard_SourceType_To_TargetType" where "SourceType" and "TargetType" are {@link EntityType#getShortName() short names} of given source and
	 * target types.
	 */
	default <S extends GenericEntity, T extends GenericEntity> void registerStandardMorpher( //
			EntityType<S> sourceType, EntityType<T> targetType, MorphFunction<S, T> transformFunction) {

		String name = "Standard_" + sourceType.getShortName() + "_TO_" + targetType.getShortName();

		registerMorpher(name, sourceType, targetType, transformFunction);
	}

	/** Registers a {@link DenotationMorpher} created by {@link DenotationMorpher#create}. */
	default <S extends GenericEntity, T extends GenericEntity> void registerMorpher( //
			String name, EntityType<S> sourceType, EntityType<T> targetType, MorphFunction<S, T> transformFunction) {

		registerMorpher(DenotationMorpher.create(name, sourceType, targetType, transformFunction));
	}

	void registerMorpher(DenotationMorpher<?, ?> morpher);

	/** Registers a {@link DenotationEnricher} created by {@link DenotationEnricher#create}. */
	default <E extends GenericEntity> void registerEnricher(String name, EntityType<E> type, EnrichFunction<E> enrichFunction) {
		registerEnricher(DenotationEnricher.create(name, type, enrichFunction));
	}

	void registerEnricher(DenotationEnricher<?> enricher);
}
