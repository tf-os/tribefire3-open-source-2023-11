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
package com.braintribe.model.access.crud.support.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.access.crud.api.query.ConditionAnalyser;
import com.braintribe.model.access.crud.api.query.ConditionAnalysis;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.conditions.Comparison;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * Traverses the passed {@link Condition} and separately collects {@link Comparison}'s referencing
 * id properties and non id properties. 
 * 
 * @author gunther.schenk
 */
public class BasicConditionAnalyser implements ConditionAnalyser {
	
	@SuppressWarnings("unused")
	private final static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	// ***************************************************************************************************
	// ConditionAnalyser
	// ***************************************************************************************************

	/**
	 * Provides a {@link ConditionAnalysis} with separated id and non id {@link Comparison}'s.
	 */
	@Override
	public ConditionAnalysis analyse(Condition condition, EntityType<?> requestedType) {
		ConditionVisitor conditionVisitor = buildConditionVisitor(requestedType);
		Condition.T.traverse(condition, null, conditionVisitor);
		
		return new ConditionAnalysis() {
			@Override
			public List<ValueComparison> getIdComparisons() {
				return conditionVisitor.getIdComparisons();
			}

			@Override
			public List<FulltextComparison> getFulltextComparisons() {
				return conditionVisitor.getFulltextComparisons();
			}
			
			@Override
			public Map<String, List<ValueComparison>> getPropertyComparisons() {
				return conditionVisitor.getPropertyComparisons();
			}
		};
	}
	
	// ***************************************************************************************************
	// Helper
	// ***************************************************************************************************

	protected ConditionVisitor buildConditionVisitor(EntityType<?> requestedType) {
		return new ConditionVisitor(requestedType);
	}
	
	protected class ConditionVisitor extends EntityVisitor {
		
		@SuppressWarnings("unused")
		private EntityType<?> requestedType;
		private List<FulltextComparison> fulltextComparisons = new ArrayList<>();
		private List<ValueComparison>  idComparisons = new ArrayList<>();
		private Map<String, List<ValueComparison>> propertyComparisons = new HashMap<>();
		
		public ConditionVisitor(EntityType<?> requestedType) {
			this.requestedType = requestedType;
		}
		
		@Override
		protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
			if (entity instanceof Comparison) {
				Comparison comparison = (Comparison) entity;
				analyseComparison(comparison);
			}
		}
		
		@SuppressWarnings("incomplete-switch")
		protected void analyseComparison(Comparison comparison) {
			switch (comparison.conditionType()) {
			case valueComparison:
				analyseValueComparison((ValueComparison) comparison);
				break;
			case fulltextComparison:
				analyseFulltextComparison((FulltextComparison) comparison);
				break;
			}
		}
		
		protected void analyseFulltextComparison(FulltextComparison comparison) {
			fulltextComparisons.add(comparison);
		}

		protected void analyseValueComparison(ValueComparison comparison) {
			analyseOperand(comparison.getLeftOperand(), comparison);
			analyseOperand(comparison.getRightOperand(), comparison);
		}

		protected void analyseOperand(Object operand, ValueComparison comparison) {
			/*if (operand instanceof EntityReference) {
				EntityReference reference = (EntityReference) operand;
				EntityType<?> referencedType = typeReflection.getEntityType(reference.getTypeSignature());
				if (requestedType == null || requestedType.isAssignableFrom(referencedType)) {
					idComparisons.add(comparison);
				} 
			}*/ 
			if (operand instanceof PropertyOperand) {
				PropertyOperand propertyOperand = (PropertyOperand) operand;
				String propertyName = propertyOperand.getPropertyName();
				if (GenericEntity.id.equals(propertyName) || propertyName == null) {
					idComparisons.add(comparison);
				} else {
					propertyComparisons
					.computeIfAbsent(propertyOperand.getPropertyName(),(k) -> new ArrayList<ValueComparison>())
					.add(comparison);
				}
			}
		}		
		
		public List<ValueComparison> getIdComparisons() {
			return idComparisons;
		}
		
		public List<FulltextComparison> getFulltextComparisons() {
			return fulltextComparisons;
		}
		
		public Map<String, List<ValueComparison>> getPropertyComparisons() {
			return propertyComparisons;
		}
		
	}
}
