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
package tribefire.platform.impl.configuration.denotrans;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.junit.Before;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

import tribefire.module.api.DenotationEnricher;
import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationMorpher;
import tribefire.module.api.DenotationTransformationContext;

/**
 * @author peter.gazdik
 */
public class AbstractDenotransTest {

	protected DenotationTransformerRegistryImpl transformerRegistry;

	@Before
	public void setupDenoTrans() {
		transformerRegistry = new DenotationTransformerRegistryImpl();
	}

	protected void registerDummy(EntityType<?> source, EntityType<?> target) {
		registerMorpher(dummyMorpher(source, target));
	}

	protected void registerMorpher(DenotationMorpher<?, ?> morpher) {
		transformerRegistry.registerMorpher(morpher);
	}

	protected void registerEnricher(DenotationEnricher<?> enricher) {
		transformerRegistry.registerEnricher(enricher);
	}

	protected static <S extends GenericEntity> DenotationEnricher<S> directEnricher( //
			String name, EntityType<S> type, BiConsumer<DenotationTransformationContext, ? super S> c) {

		return DenotationEnricher.create(name, type, (ctx, e) -> {
			c.accept(ctx, e);

			return DenotationEnrichmentResult.allDone(e, null);
		});
	}

	protected static <S extends GenericEntity, T extends GenericEntity> DenotationMorpher<S, T> directMorpher( //
			EntityType<S> sourceType, EntityType<T> targetType, BiFunction<DenotationTransformationContext, S, T> f) {

		return DenotationMorpher.create(sourceType.getShortName() + "_TO_" + targetType.getShortName(), sourceType, targetType,
				(ctx, e) -> Maybe.complete(f.apply(ctx, e)));
	}
	protected static <S extends GenericEntity, T extends GenericEntity> DenotationMorpher<S, T> dummyMorpher( //
			EntityType<S> sourceType, EntityType<T> targetType) {

		return DenotationMorpher.create(sourceType.getShortName() + "_TO_" + targetType.getShortName(), sourceType, targetType,
				(ctx, e) -> Maybe.complete(targetType.create()));
	}

}
