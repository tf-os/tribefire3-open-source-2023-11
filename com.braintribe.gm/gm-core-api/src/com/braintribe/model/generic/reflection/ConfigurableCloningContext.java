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
package com.braintribe.model.generic.reflection;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.braintribe.common.lcd.function.TriFunction;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.session.GmSession;

/**
 * Alternative to {@link StandardCloningContext} which allows convenient way to build a {@link CloningContext} for the most common use-cases, without
 * the need to explicitly create a new class.
 * <p>
 * When it comes to behavior, blank instance of this class is fully compatible with {@link StandardCloningContext}. However, it offers ways to
 * override methods which are relevant for cloning by supplying just the functionality for that method.
 * <p>
 * There are two ways how to configure your instance. The simple way is to create a new instance and simply give the implementation for the desired
 * methods via setters. The names do not entirely match the cloning context methods, but the matches are intuitive enough. If in doubt, see the
 * setters' javadoc.
 * <p>
 * The other more interesting way for configuration is to use a fluent builder, available via {@link #build()} method. This not only allows you to
 * specify the values directly for the setters (except the builder methods start with the prefix "with", rather than "set", like
 * {@link CccBuilder#withClonedValuePostProcesor}), but also offers some convenient methods for some common use-case, which internally set the right
 * values to the appropriate setters. For now, those are the following methods:
 * <ul>
 * <li>{@link CccBuilder#skipIndentifying(boolean)}</li>
 * <li>{@link CccBuilder#skipGlobalId(boolean)}</li>
 * <li>{@link CccBuilder#supplyRawCloneWith(GmSession)}</li>
 * </ul>
 * 
 * @see ConfigurableCloningContext#build()
 * 
 * @author peter.gazdik
 */
public class ConfigurableCloningContext extends StandardTraversingContext implements CloningContext, PropertyTransferCompetence {

	protected TriFunction<Property, GenericEntity, AbsenceInformation, Boolean> canResolveAbsentPropertyTest = //
			(property, origin, absenceInformation) -> false;

	/**
	 * The result here is not taken directly, but when null is returned, it is an indicator that the result should be taken from the inherited
	 * {@link StandardTraversingContext#getAssociated(GenericEntity)}.
	 */
	protected Function<GenericEntity, GenericEntity> associatedResolver = origin -> null;

	protected Function<GenericEntity, GenericEntity> originPreProcessor = origin -> origin;

	protected BiFunction<EntityType<?>, GenericEntity, GenericEntity> entityFactory = (entityType, origin) -> entityType.createRaw();

	protected CanTransferPropertyTest canTransferPropertyTest = CanTransferPropertyTest.ALWAYS_TRUE;

	protected BiFunction<GenericModelType, Object, Object> clonedValuePostProcesor = (propertyType, clonedValue) -> clonedValue;

	protected TriFunction<GenericModelType, GenericEntity, Property, AbsenceInformation> absenceInfoFactory = //
			(type, origin, property) -> GMF.absenceInformation();

	protected StrategyOnCriterionMatch strategyOnCriterionMatch = StrategyOnCriterionMatch.partialize;

	protected PropertyTransferCompetence propertyTransferCompetence = //
			(actualType, entity, entityClone, property, postProcessedClonedPropertyValue) -> property.set(entityClone,
					postProcessedClonedPropertyValue);

	// ##########################################################
	// ## . . . . . . . . . Helper interface . . . . . . . . . ##
	// ##########################################################

	public static interface CanTransferPropertyTest {

		CanTransferPropertyTest ALWAYS_TRUE = (type, property, origin, clone, ai) -> true;
		CanTransferPropertyTest SKIP_IDENTIFYING = (type, property, origin, clone, ai) -> !property.isIdentifying();
		CanTransferPropertyTest SKIP_GLOBAL_ID = (type, property, origin, clone, ai) -> !property.isGlobalId();

		boolean test(EntityType<?> entityType, Property property, GenericEntity origin, GenericEntity clone, AbsenceInformation originAi);

		default CanTransferPropertyTest and(CanTransferPropertyTest other) {
			if (this == ALWAYS_TRUE)
				return other;
			else
				return (type, property, origin, clone, ai) -> //
				/*    */ this.test(type, property, origin, clone, ai) && //
						other.test(type, property, origin, clone, ai);
		}

	}

	// ##########################################################
	// ## . . . . . . . . . . . Setters . . . . . . . . . . . .##
	// ##########################################################

	/** Configures override of {@link TraversingContext#isAbsenceResolvable} */
	public void setCanResolveAbsentPropertyTest(TriFunction<Property, GenericEntity, AbsenceInformation, Boolean> canResolveAbsentPropertyTest) {
		this.canResolveAbsentPropertyTest = canResolveAbsentPropertyTest;
	}

	/** Configures override of {@link TraversingContext#getAssociated} */
	public void setAssociatedResolver(Function<GenericEntity, GenericEntity> associatedResolver) {
		this.associatedResolver = associatedResolver;
	}

	/** Configures override of {@link CloningContext#preProcessInstanceToBeCloned} */
	public void setOriginPreProcessor(Function<GenericEntity, GenericEntity> originPreProcessor) {
		this.originPreProcessor = originPreProcessor;
	}

	/** Configures override of {@link CloningContext#supplyRawClone} */
	public void setEntityFactory(BiFunction<EntityType<?>, GenericEntity, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
	}

	/** Configures override of {@link CloningContext#canTransferPropertyValue} */
	public void setCanTransferPropertyTest(CanTransferPropertyTest canTransferPropertyTest) {
		this.canTransferPropertyTest = canTransferPropertyTest;
	}

	/** Configures override of {@link CloningContext#postProcessCloneValue} */
	public void setClonedValuePostProcesor(BiFunction<GenericModelType, Object, Object> clonedValuePostProcesor) {
		this.clonedValuePostProcesor = clonedValuePostProcesor;
	}

	/** Configures override of {@link CloningContext#createAbsenceInformation} */
	public void setAbsenceInfoFactory(TriFunction<GenericModelType, GenericEntity, Property, AbsenceInformation> absenceInfoFactory) {
		this.absenceInfoFactory = absenceInfoFactory;
	}

	/** Configures override of {@link CloningContext#getStrategyOnCriterionMatch} */
	public void setStrategyOnCriterionMatch(StrategyOnCriterionMatch strategyOnCriterionMatch) {
		this.strategyOnCriterionMatch = strategyOnCriterionMatch;
	}

	/** Configures override of {@link PropertyTransferCompetence#transferProperty(EntityType, GenericEntity, GenericEntity, Property, Object)} */
	public void setPropertyTransferCompetence(PropertyTransferCompetence propertyTransferCompetence) {
		this.propertyTransferCompetence = propertyTransferCompetence;
	}

	// ##########################################################
	// ## . . . . . . . TraversingContext methods . . . . . . .##
	// ##########################################################

	@Override
	public boolean isAbsenceResolvable(Property property, GenericEntity entity, AbsenceInformation absenceInformation) {
		return canResolveAbsentPropertyTest.apply(property, entity, absenceInformation);
	}

	@Override
	public <T> T getAssociated(GenericEntity origin) {
		T result = (T) associatedResolver.apply(origin);
		return result != null ? result : super.getAssociated(origin);
	}

	// ##########################################################
	// ## . . . . . . . . CloningContext methods . . . . . . . ##
	// ##########################################################

	@Override
	public GenericEntity preProcessInstanceToBeCloned(GenericEntity origin) {
		return originPreProcessor.apply(origin);
	}

	@Override
	public GenericEntity supplyRawClone(EntityType<?> entityType, GenericEntity origin) {
		return entityFactory.apply(entityType, origin);
	}

	@Override
	public boolean canTransferPropertyValue(EntityType<?> entityType, Property property, GenericEntity origin, GenericEntity clone,
			AbsenceInformation originAi) {

		return canTransferPropertyTest.test(entityType, property, origin, clone, originAi);
	}

	@Override
	public Object postProcessCloneValue(GenericModelType propertyOrElementType, Object clonedValue) {
		return clonedValuePostProcesor.apply(propertyOrElementType, clonedValue);
	}

	@Override
	public AbsenceInformation createAbsenceInformation(GenericModelType type, GenericEntity origin, Property property) {
		return absenceInfoFactory.apply(type, origin, property);
	}

	@Override
	public StrategyOnCriterionMatch getStrategyOnCriterionMatch() {
		return strategyOnCriterionMatch;
	}

	// ##########################################################
	// ## . . . . . PropertyTransferCompetence methods . . . . ##
	// ##########################################################

	@Override
	public void transferProperty( //
			EntityType<?> sourceEntityType, GenericEntity sourceEntity, GenericEntity targetEntity, Property property, Object propertyValue) {

		propertyTransferCompetence.transferProperty(sourceEntityType, sourceEntity, targetEntity, property, propertyValue);
	}

	// ##########################################################
	// ## . . . . . . . . . . . Builder . . . . . . . . . . . .##
	// ##########################################################

	/** Returns a fluent-API builder for a new {@link ConfigurableCloningContext} instance. */
	public static CccBuilder build() {
		return new CccBuilder();
	}

	public static class CccBuilder {

		private boolean skipIndentifying;
		private boolean skipGlobalId;

		private TriFunction<Property, GenericEntity, AbsenceInformation, Boolean> canResolveAbsentPropertyTest;
		private Function<GenericEntity, GenericEntity> associatedResolver;
		private Matcher matcher;

		private Function<GenericEntity, GenericEntity> originPreProcessor;
		private BiFunction<EntityType<?>, GenericEntity, GenericEntity> entityFactory;
		private CanTransferPropertyTest canTransferPropertyTest;
		private BiFunction<GenericModelType, Object, Object> clonedValuePostProcesor;
		private TriFunction<GenericModelType, GenericEntity, Property, AbsenceInformation> absenceInfoFactory;
		private StrategyOnCriterionMatch strategyOnCriterionMatch;
		private PropertyTransferCompetence propertyTransferCompetence;

		/** All new instances will be created on this session. */
		public CccBuilder supplyRawCloneWith(GmSession session) {
			return withEntityFactory((entityType, origin) -> session.create(entityType));
		}

		/** Id and partition won't be cloned. See also {@link #withCanTransferPropertyTest}. */
		public CccBuilder skipIndentifying(boolean skipIndentifying) {
			this.skipIndentifying = skipIndentifying;
			return this;
		}

		/** globalId won't be cloned. See also {@link #withCanTransferPropertyTest}. */
		public CccBuilder skipGlobalId(boolean skipGlobalId) {
			this.skipGlobalId = skipGlobalId;
			return this;
		}

		/** globalId won't be cloned. See also {@link #withCanTransferPropertyTest}. */
		public CccBuilder keepAbsenceInformation() {
			return withCanResolveAbsentPropertyTest((p, e, ai) -> false);
		}

		public CccBuilder withMatcher(Matcher matcher) {
			this.matcher = matcher;
			return this;
		}

		/** Configures override of {@link TraversingContext#isAbsenceResolvable} */
		public CccBuilder withCanResolveAbsentPropertyTest(
				TriFunction<Property, GenericEntity, AbsenceInformation, Boolean> canResolveAbsentPropertyTest) {
			this.canResolveAbsentPropertyTest = canResolveAbsentPropertyTest;
			return this;
		}

		/** Configures override of {@link TraversingContext#getAssociated} */
		public CccBuilder withAssociatedResolver(Function<GenericEntity, GenericEntity> associatedResolver) {
			this.associatedResolver = associatedResolver;
			return this;
		}

		/** Configures override of {@link CloningContext#preProcessInstanceToBeCloned} */
		public CccBuilder withOriginPreProcessor(Function<GenericEntity, GenericEntity> originPreProcessor) {
			this.originPreProcessor = originPreProcessor;
			return this;
		}

		/** Configures override of {@link CloningContext#supplyRawClone} */
		public CccBuilder withEntityFactory(BiFunction<EntityType<?>, GenericEntity, GenericEntity> entityFactory) {
			if (this.entityFactory != null)
				throw new IllegalStateException("EntityFactory was already configured.");

			this.entityFactory = entityFactory;
			return this;
		}

		/**
		 * Configures override for {@link CloningContext#canTransferPropertyValue}.
		 * <p>
		 * NOTE that if more than one of this, {@link #skipIndentifying(boolean)} and {@link #skipGlobalId(boolean)} is configured, their effect is
		 * added, i.e. property is being skipped if at least one of the configured filters implies it should be skipped.
		 */
		public CccBuilder withCanTransferPropertyTest(CanTransferPropertyTest canTransferPropertyTest) {
			this.canTransferPropertyTest = canTransferPropertyTest;
			return this;
		}

		/** Configures override of {@link CloningContext#postProcessCloneValue} */
		public CccBuilder withClonedValuePostProcesor(BiFunction<GenericModelType, Object, Object> clonedValuePostProcesor) {
			this.clonedValuePostProcesor = clonedValuePostProcesor;
			return this;
		}

		/** Configures override of {@link CloningContext#createAbsenceInformation} */
		public CccBuilder withAbsenceInfoFactory(TriFunction<GenericModelType, GenericEntity, Property, AbsenceInformation> absenceInfoFactory) {
			this.absenceInfoFactory = absenceInfoFactory;
			return this;
		}

		/** Configures override of {@link CloningContext#getStrategyOnCriterionMatch} */
		public CccBuilder withStrategyOnCriterionMatch(StrategyOnCriterionMatch strategyOnCriterionMatch) {
			this.strategyOnCriterionMatch = strategyOnCriterionMatch;
			return this;
		}

		public CccBuilder withPropertyTransferCompetence(PropertyTransferCompetence propertyTransferCompetence) {
			this.propertyTransferCompetence = propertyTransferCompetence;
			return this;
		}

		// ######################################################
		// ## . . . . . . . Builder finalization . . . . . . . ##
		// ######################################################

		public ConfigurableCloningContext done() {
			ConfigurableCloningContext result = new ConfigurableCloningContext();

			result.setMatcher(matcher);

			if (canResolveAbsentPropertyTest != null)
				result.setCanResolveAbsentPropertyTest(canResolveAbsentPropertyTest);

			if (associatedResolver != null)
				result.setAssociatedResolver(associatedResolver);

			if (originPreProcessor != null)
				result.setOriginPreProcessor(originPreProcessor);

			if (entityFactory != null)
				result.setEntityFactory(entityFactory);

			if (clonedValuePostProcesor != null)
				result.setClonedValuePostProcesor(clonedValuePostProcesor);

			if (absenceInfoFactory != null)
				result.setAbsenceInfoFactory(absenceInfoFactory);

			if (strategyOnCriterionMatch != null)
				result.setStrategyOnCriterionMatch(strategyOnCriterionMatch);

			if (propertyTransferCompetence != null)
				result.setPropertyTransferCompetence(propertyTransferCompetence);

			CanTransferPropertyTest _canTransferPropertyTest = buildCanTransferPropertyTest();
			if (_canTransferPropertyTest != null)
				result.setCanTransferPropertyTest(_canTransferPropertyTest);

			return result;
		}

		private CanTransferPropertyTest buildCanTransferPropertyTest() {
			CanTransferPropertyTest result = CanTransferPropertyTest.ALWAYS_TRUE;

			if (canTransferPropertyTest != null)
				result = result.and(canTransferPropertyTest);

			if (skipIndentifying)
				result = result.and(CanTransferPropertyTest.SKIP_IDENTIFYING);

			if (skipGlobalId)
				result = result.and(CanTransferPropertyTest.SKIP_GLOBAL_ID);

			return result == CanTransferPropertyTest.ALWAYS_TRUE ? null : result;
		}

	}
}
