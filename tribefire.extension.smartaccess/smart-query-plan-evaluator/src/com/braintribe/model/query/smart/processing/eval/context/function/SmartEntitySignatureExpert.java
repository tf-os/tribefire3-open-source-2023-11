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
package com.braintribe.model.query.smart.processing.eval.context.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.smartquery.eval.api.function.SignatureSelectionOperand;
import com.braintribe.model.processing.smartquery.eval.api.function.SmartQueryFunctionExpert;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.smart.processing.SmartQueryEvaluatorRuntimeException;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class SmartEntitySignatureExpert implements SmartQueryFunctionExpert<EntitySignature> {

	public static final SmartEntitySignatureExpert INSTANCE = new SmartEntitySignatureExpert();

	private SmartEntitySignatureExpert() {
	}

	@Override
	public Object evaluate(Tuple tuple, EntitySignature signatureFunction, Map<Object, Value> operandMappings,
			QueryEvaluationContext context) {

		Value operandValue = operandMappings.get(getSignatureOperand(signatureFunction));
		return context.resolveValue(tuple, operandValue);
	}

	@Override
	public Collection<SignatureSelectionOperand> listOperandsToSelect(EntitySignature signatureFunction) {
		return Arrays.asList(getSignatureOperand(signatureFunction));
	}

	private SignatureSelectionOperand getSignatureOperand(EntitySignature signatureFunction) {
		SignatureSelectionOperand result = SignatureSelectionOperand.T.createPlain();
		result.setSource(extractSource(signatureFunction));

		return result;
	}

	private Source extractSource(EntitySignature signatureFunction) {
		Object operand = signatureFunction.getOperand();

		if (operand instanceof Source) {
			return (Source) operand;
		}

		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;

			if (po.getPropertyName() != null) {
				throw new SmartQueryEvaluatorRuntimeException(
						"Cannot resolve type signature. Source operand expected, not property one. Property: " + po.getPropertyName());
			}

			return po.getSource();
		}

		throw new RuntimeException("Cannot resolve type signature. Source operand expected, but found: ");
	}
}
