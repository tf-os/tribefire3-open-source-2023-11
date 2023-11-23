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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.removeLast;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMultiMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMultiMap;

import tribefire.module.api.DenotationEnricher;
import tribefire.module.api.DenotationMorpher;
import tribefire.module.api.DenotationTransformer;
import tribefire.module.api.DenotationTransformerRegistry;

/**
 * @author peter.gazdik
 */
public class DenotationTransformerRegistryImpl implements DenotationTransformerRegistry {

	private static final Logger log = Logger.getLogger(DenotationTransformerRegistryImpl.class);

	private final MutableDenotationMultiMap<GenericEntity, DenotationMorpher<?, ?>> morpherMap = new PolymorphicDenotationMultiMap<>();
	private final MutableDenotationMultiMap<GenericEntity, DenotationEnricher<?>> enricherMap = new PolymorphicDenotationMultiMap<>();

	@Override
	public void registerMorpher(DenotationMorpher<?, ?> morpher) {
		registerIfNameUnique("Morpher", morpher, morpherMap);
	}

	@Override
	public void registerEnricher(DenotationEnricher<?> enricher) {
		registerIfNameUnique("Enricher", enricher, enricherMap);
	}

	private <T extends DenotationTransformer<?, ?>> void registerIfNameUnique(//
			String morpherOrEnricher, T transformer, MutableDenotationMultiMap<GenericEntity, T> map) {

		String name = transformer.name();
		if (name == null)
			throw new IllegalArgumentException(morpherOrEnricher + " has no name: " + transformer.describeYourself());

		Optional<T> tWithSameName = map.expertStream() //
				.filter(registeredTransformer -> name.equals(registeredTransformer.name())) //
				.findAny();

		if (tWithSameName.isPresent())
			throw new IllegalArgumentException("Attempting to register two " + morpherOrEnricher + "s with name [" + name + "].\n    First: "
					+ tWithSameName.get().describeYourself() + "\n    Second: " + transformer.describeYourself());

		log.info("Registering " + morpherOrEnricher + ": " + transformer.name());
		map.put(transformer.sourceType(), transformer);
	}

	public Maybe<List<DtStep>> resolveTransformationPipeline(EntityType<?> sourceType, EntityType<?> targetType) {
		Maybe<List<DenotationMorpher<?, ?>>> morphers = resolveMorpherSequence(sourceType, targetType);
		if (morphers.isEmpty())
			return morphers.cast();

		List<DtStep> result = newList();
		addEnrichmentStepIfPossible(result, sourceType);

		for (DenotationMorpher<?, ?> morhper : morphers.get()) {
			addMorpherStep(result, morhper);
			addEnrichmentStepIfPossible(result, morhper.targetType());
		}

		return Maybe.complete(result);
	}

	private Maybe<List<DenotationMorpher<?, ?>>> resolveMorpherSequence(EntityType<?> sourceType, EntityType<?> targetType) {
		return new MorpherSequenceResolver(sourceType, targetType).run();
	}

	private void addEnrichmentStepIfPossible(List<DtStep> result, EntityType<?> type) {
		List<DenotationEnricher<?>> es = enricherMap.findAll(type);
		if (!es.isEmpty())
			result.add(new DtEnrichmentStep(es));
	}

	private void addMorpherStep(List<DtStep> result, DenotationMorpher<?, ?> morhper) {
		result.add(new DtMetamorphosisStep(morhper));
	}

	private class MorpherSequenceResolver {

		private final EntityType<?> sourceType;
		private final EntityType<?> targetType;

		private Maybe<List<DenotationMorpher<?, ?>>> result;
		private final List<DenotationMorpher<?, ?>> morphers = newList();

		private final Set<EntityType<?>> visitedTypes = newSet();

		public MorpherSequenceResolver(EntityType<?> sourceType, EntityType<?> targetType) {
			this.sourceType = sourceType;
			this.targetType = targetType;
		}

		public Maybe<List<DenotationMorpher<?, ?>>> run() {
			go(sourceType);

			return result == null ? noPathFound() : result;
		}

		private Maybe<List<DenotationMorpher<?, ?>>> noPathFound() {
			NotFound result = NotFound.T.create();
			result.setText(
					"No morpher sequence found to transform [" + sourceType.getTypeSignature() + "] to [" + targetType.getTypeSignature() + "]");
			return result.asMaybe();
		}

		private void go(EntityType<?> currentType) {
			if (targetType.isAssignableFrom(currentType)) {
				if (result == null)
					result = Maybe.complete(newList(morphers));
				else if (result.isSatisfied())
					result = tryDisambiguate(result.get());

				if (result.isUnsatisfied()) {
					InvalidArgument error = result.whyUnsatisfied();
					error.setText(error.getText() + nextAmbiguousPath(morphers));
				}

				return;
			}

			if (!visitedTypes.add(currentType))
				return;

			for (DenotationMorpher<?, ?> morpher : morpherMap.findAll(currentType)) {
				morphers.add(morpher);
				go(morpher.targetType());
				removeLast(morphers);
			}

			visitedTypes.remove(currentType);

		}

		private Maybe<List<DenotationMorpher<?, ?>>> tryDisambiguate(List<DenotationMorpher<?, ?>> list) {
			List<DenotationMorpher<?, ?>> moreSpecificList = tryDisambiguate(list, morphers);
			if (moreSpecificList != null)
				return Maybe.complete(newList(moreSpecificList));

			InvalidArgument result = InvalidArgument.T.create();
			result.setText("Ambiguous morpher sequence from [" + sourceType.getShortName() + "] to [" + targetType.getShortName() + "]"
					+ nextAmbiguousPath(list));

			return result.asMaybe();
		}

		/* We can disambiguate two lists iff the first index from the start where they differ, one morpher's source is a sub-type of the other. In
		 * this case, the more specific Morpher (and thus his list) wins. No other situation can be disambiguated automatically. */
		private List<DenotationMorpher<?, ?>> tryDisambiguate(List<DenotationMorpher<?, ?>> l1, List<DenotationMorpher<?, ?>> l2) {
			Iterator<DenotationMorpher<?, ?>> it1 = l1.iterator();
			Iterator<DenotationMorpher<?, ?>> it2 = l2.iterator();

			while (it1.hasNext() && it2.hasNext()) {
				DenotationMorpher<?, ?> m1 = it1.next();
				DenotationMorpher<?, ?> m2 = it2.next();

				// as long as the morhpers are the same, we continue
				if (m1 == m2)
					continue;

				// Different Morphers - we either pick the one with more specific source or say it's impossible to disambiguate
				EntityType<?> s1 = m1.sourceType();
				EntityType<?> s2 = m2.sourceType();

				if (s1 == s2)
					// same source, different Morphers, no way to disambiguate
					return null;

				if (s2.isAssignableFrom(s1))
					// s1 is sub-type of s2
					return l1;

				if (s1.isAssignableFrom(s2))
					// s2 is sub-type of s1
					return l2;

				return null;
			}

			return null;
		}

		private int ambiguousPaths;

		private String nextAmbiguousPath(List<DenotationMorpher<?, ?>> sequence) {
			return "\nPath: " + (++ambiguousPaths) + " " + describeMorhperSequence(sequence);
		}

		private String describeMorhperSequence(List<DenotationMorpher<?, ?>> sequence) {
			return sequence.stream() //
					.map(DenotationTransformer::describeYourself) //
					.collect(Collectors.joining(", "));
		}

	}

}
