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
package com.braintribe.model.access.security.query;

import static com.braintribe.model.access.security.query.PasswordPropertyTools.isPasswordProperty;
import static com.braintribe.model.access.security.query.QueryOperandTools.resolveEntityProperty;

import com.braintribe.model.access.security.query.QueryOperandTools.EntityTypeProperty;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * Checks that we have no condition on a password property. If we do, a {@link IllegalArgumentException} is thrown.
 * 
 * @author peter.gazdik
 */
class PasswordConditionValidator {

	private final ModelMdResolver mdResolver;
	private final SourcesDescriptor querySources;

	public static void validate(Condition condition, SourcesDescriptor querySources, ModelMdResolver mdResolver) {
		new PasswordConditionValidator(mdResolver, querySources).validateNoConditionOnPasswordProperty(condition);
	}

	private PasswordConditionValidator(ModelMdResolver mdResolver, SourcesDescriptor querySources) {
		this.mdResolver = mdResolver;
		this.querySources = querySources;
	}

	private void validateNoConditionOnPasswordProperty(Condition condition) {
		switch (condition.conditionType()) {
			case conjunction:
			case disjunction:
				validateNoPasswordCondition((AbstractJunction) condition);
				return;
			case negation:
				validateNoPasswordCondition((Negation) condition);
				return;
			case valueComparison:
				validateNoPasswordCondition((ValueComparison) condition);
				return;
			case fulltextComparison:
				// We cannot do anything with fulltext comparisons...
			default:
				return;
		}
	}

	private void validateNoPasswordCondition(AbstractJunction condition) {
		for (Condition operand : condition.getOperands()) {
			validateNoConditionOnPasswordProperty(operand);
		}
	}

	private void validateNoPasswordCondition(Negation condition) {
		validateNoConditionOnPasswordProperty(condition.getOperand());
	}

	private void validateNoPasswordCondition(ValueComparison condition) {
		validateOperandIsNotPassword(condition.getLeftOperand());
		validateOperandIsNotPassword(condition.getRightOperand());
	}

	private void validateOperandIsNotPassword(Object operand) {
		if (!(operand instanceof PropertyOperand))
			return;

		PropertyOperand po = (PropertyOperand) operand;
		String propertyPath = po.getPropertyName();

		if (propertyPath == null)
			return;

		EntityTypeProperty etp = resolveEntityProperty(po, querySources);

		if (isPasswordProperty(etp, mdResolver))
			throw new IllegalArgumentException("Condition is not allowed on password property: " + etp);
	}

}
