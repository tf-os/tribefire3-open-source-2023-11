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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * {@link DenotationTransformer} which creates a new instance of type {@code T} from an instance of type {@code S}.
 * 
 * @see DenotationTransformer
 * @see DenotationEnricher
 * 
 * @author peter.gazdik
 */
public interface DenotationMorpher<S extends GenericEntity, T extends GenericEntity> extends DenotationTransformer<S, T> {

	/** Tries to turn given instance of type {@link #sourceType()} into an instance of type {@link #targetType()}. */
	Maybe<T> morph(DenotationTransformationContext context, S denotation);

	@Override
	default String describeYourself() {
		return "[" + name() + "]: " + sourceType().getTypeSignature() + " ==> " + targetType().getTypeSignature() + "";
	}

	@FunctionalInterface
	public interface MorphFunction<S extends GenericEntity, T extends GenericEntity> {
		Maybe<T> morph(DenotationTransformationContext context, S denotation);
	}

	public static <S extends GenericEntity, T extends GenericEntity> DenotationMorpher<S, T> create(String name, EntityType<S> sourceType,
			EntityType<T> targetType, MorphFunction<S, T> morphFunction) {

		nonNull(name, "name");
		nonNull(sourceType, "sourceType");
		nonNull(targetType, "targetType");
		nonNull(morphFunction, "morphFunction");

		return new DenotationMorpher<S, T>() {

			// @formatter:off
			@Override public String name() { return name; }
			@Override public EntityType<S> sourceType() { return sourceType; }
			@Override public EntityType<T> targetType() { return targetType; }
			// @formatter:on

			@Override
			public Maybe<T> morph(DenotationTransformationContext context, S denotation) {
				return morphFunction.morph(context, denotation);
			}

		};
	}

}
