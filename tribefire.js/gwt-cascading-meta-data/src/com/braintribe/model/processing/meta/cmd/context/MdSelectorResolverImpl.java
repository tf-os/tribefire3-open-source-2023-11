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
package com.braintribe.model.processing.meta.cmd.context;

import static java.util.Collections.emptyMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.meta.selector.AccessTypeSelector;
import com.braintribe.model.meta.selector.AccessTypeSignatureSelector;
import com.braintribe.model.meta.selector.AclSelector;
import com.braintribe.model.meta.selector.BooleanPropertyDiscriminator;
import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.DatePropertyDiscriminator;
import com.braintribe.model.meta.selector.DeclaredPropertySelector;
import com.braintribe.model.meta.selector.DisjunctionSelector;
import com.braintribe.model.meta.selector.EntitySignatureRegexSelector;
import com.braintribe.model.meta.selector.EntityTypeSelector;
import com.braintribe.model.meta.selector.GmEntityTypeSelector;
import com.braintribe.model.meta.selector.IntegerPropertyDiscriminator;
import com.braintribe.model.meta.selector.LongDiscriminatorValue;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.meta.selector.NullPropertyDiscriminator;
import com.braintribe.model.meta.selector.PropertyNameSelector;
import com.braintribe.model.meta.selector.PropertyOfSelector;
import com.braintribe.model.meta.selector.PropertyRegexSelector;
import com.braintribe.model.meta.selector.PropertyTypeSelector;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.meta.selector.StringPropertyDiscriminator;
import com.braintribe.model.meta.selector.StringRegexPropertyDiscriminator;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.MdSelectorResolver;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessTypeSignatureSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.AclSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.BooleanPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.ConjunctionSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.DatePropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.DeclaredPropertySelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.DisjunctionSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.EntitySignatureRegexSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.EntityTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.GmEntityTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.IntegerPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.LongPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.NegationSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.NullPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyNameSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyOfSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyRegexSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyTypeSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.PropertyValueComparatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.RoleSelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.SimplePropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.StringPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.StringRegexPropertyDiscriminatorExpert;
import com.braintribe.model.processing.meta.cmd.context.experts.UseCaseSelectorExpert;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class MdSelectorResolverImpl implements MdSelectorResolver {

	private final MutableDenotationMap<MetaDataSelector, SelectorExpert<?>> selectorExperts;

	public MdSelectorResolverImpl() {
		this(emptyMap());
	}

	public MdSelectorResolverImpl(Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> customExperts) {
		this.selectorExperts = new PolymorphicDenotationMap<>();

		addBasicSelectorExperts();
		addCustomselectorExperts(customExperts);
		setSuppressInconsistencies(true);
	}

	private void addBasicSelectorExperts() {
		registerExpert(ConjunctionSelector.T, new ConjunctionSelectorExpert(this));
		registerExpert(DisjunctionSelector.T, new DisjunctionSelectorExpert(this));
		registerExpert(NegationSelector.T, new NegationSelectorExpert(this));

		registerExpert(BooleanPropertyDiscriminator.T, new BooleanPropertyDiscriminatorExpert());
		registerExpert(DatePropertyDiscriminator.T, new DatePropertyDiscriminatorExpert());
		registerExpert(IntegerPropertyDiscriminator.T, new IntegerPropertyDiscriminatorExpert());
		registerExpert(LongDiscriminatorValue.T, new LongPropertyDiscriminatorExpert());
		registerExpert(StringPropertyDiscriminator.T, new StringPropertyDiscriminatorExpert());
		registerExpert(StringRegexPropertyDiscriminator.T, new StringRegexPropertyDiscriminatorExpert());
		registerExpert(NullPropertyDiscriminator.T, new NullPropertyDiscriminatorExpert());

		registerExpert(PropertyValueComparator.T, new PropertyValueComparatorExpert());

		registerExpert(PropertyNameSelector.T, new PropertyNameSelectorExpert());
		registerExpert(PropertyRegexSelector.T, new PropertyRegexSelectorExpert());
		registerExpert(PropertyTypeSelector.T, new PropertyTypeSelectorExpert());
		registerExpert(PropertyOfSelector.T, new PropertyOfSelectorExpert());
		registerExpert(DeclaredPropertySelector.T, new DeclaredPropertySelectorExpert());

		registerExpert(GmEntityTypeSelector.T, new GmEntityTypeSelectorExpert());
		registerExpert(EntitySignatureRegexSelector.T, new EntitySignatureRegexSelectorExpert());
		registerExpert(EntityTypeSelector.T, new EntityTypeSelectorExpert());

		registerExpert(RoleSelector.T, new RoleSelectorExpert());
		registerExpert(AclSelector.T, new AclSelectorExpert());
		registerExpert(UseCaseSelector.T, new UseCaseSelectorExpert());
		registerExpert(AccessSelector.T, new AccessSelectorExpert());
		registerExpert(AccessTypeSelector.T, new AccessTypeSelectorExpert());
		registerExpert(AccessTypeSignatureSelector.T, new AccessTypeSignatureSelectorExpert());
	}

	private <S extends MetaDataSelector, E extends SelectorExpert<S>> void registerExpert(EntityType<S> selectorClass, E expert) {
		selectorExperts.put(selectorClass, expert);
	}

	private void addCustomselectorExperts(Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> experts) {
		if (experts != null)
			selectorExperts.putAll(experts);
	}

	public void setSuppressInconsistencies(boolean shouldSuppress) {
		selectorExperts.expertStream() //
				.filter(e -> e instanceof SimplePropertyDiscriminatorExpert) //
				.forEach(e -> ((SimplePropertyDiscriminatorExpert<?, ?>) e).setSuppressInconsistencies(shouldSuppress));
	}

	@Override
	public Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(MetaDataSelector mds) {
		if (mds == null)
			return Collections.emptySet();

		SelectorExpert<MetaDataSelector> selectorExpert = getSelectorExpert(mds);

		try {
			return selectorExpert.getRelevantAspects(mds);

		} catch (Exception e) {
			throw new CascadingMetaDataException("Problem while acquireing aspects for: " + mds, e);
		}
	}

	private <T extends MetaDataSelector> SelectorExpert<T> getSelectorExpert(MetaDataSelector mds) {
		EntityType<? extends MetaDataSelector> et = mds.entityType();

		SelectorExpert<T> selectorExpert = (SelectorExpert<T>) selectorExperts.find(et);
		if (selectorExpert == null)
			throw new CascadingMetaDataException("No selector expert found for: " + et.getTypeSignature());

		return selectorExpert;
	}

	@Override
	public SelectorExpert<?> findExpertFor(EntityType<? extends MetaDataSelector> et) {
		return selectorExperts.find(et);
	}
	/**
	 * @return true iff given {@link MetaDataSelector} is evaluated with true by it's corresponding {@link SelectorExpert}.
	 */
	@Override
	public boolean matches(MetaDataSelector mds, SelectorContext context) {
		SelectorExpert<MetaDataSelector> selectorExpert = getSelectorExpert(mds);

		try {
			return selectorExpert.matches(mds, context);

		} catch (Exception e) {
			throw new CascadingMetaDataException("Error while evaluating selector: " + mds.entityType().getTypeSignature(), e);
		}
	}

	/** @see LogicalSelectorExpert#maybeMatches(MetaDataSelector, SelectorContext) */
	public Boolean maybeMatches(MetaDataSelector mds, SelectorContext context) {
		SelectorExpert<MetaDataSelector> selectorExpert = getSelectorExpert(mds);

		try {
			if (selectorExpert instanceof LogicalSelectorExpert)
				return ((LogicalSelectorExpert<MetaDataSelector>) selectorExpert).maybeMatches(mds, context);

			if (shouldEvaluateSelector(mds, context))
				return selectorExpert.matches(mds, context);

			return null; // selector is ignored

		} catch (Exception e) {
			throw new CascadingMetaDataException("Error while evaluating selector: " + mds.entityType().getTypeSignature(), e);
		}
	}

	private boolean shouldEvaluateSelector(MetaDataSelector mds, SelectorContext context) {
		return ((ExtendedSelectorContext) context).notIgnoredSelectors().contains(mds.entityType());
	}

}
