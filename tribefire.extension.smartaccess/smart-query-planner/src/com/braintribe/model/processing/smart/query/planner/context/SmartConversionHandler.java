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
package com.braintribe.model.processing.smart.query.planner.context;

import java.util.Map;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SimpleValueNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smartquery.eval.api.ConversionDirection;
import com.braintribe.model.processing.smartquery.eval.api.SmartConversionExpert;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;

/**
 * 
 */
public class SmartConversionHandler {

	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;

	private final Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts;

	public SmartConversionHandler(SmartQueryPlannerContext smartQueryPlannerContext,
			Map<EntityType<? extends SmartConversion>, SmartConversionExpert<?>> conversionExperts) {

		this.context = smartQueryPlannerContext;
		this.planStructure = context.planStructure();

		this.conversionExperts = conversionExperts;
	}

	public SmartConversion findConversion(Object operand) {
		if (!(operand instanceof Operand) || context.isEvaluationExclude(operand))
			return null;

		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;
			String propertyName = po.getPropertyName();

			if (propertyName == null) {
				SourceNode sourceNode = planStructure.getSourceNode(po.getSource());

				// This means our PropertyOpearand represents a joined simple-value collection
				if (sourceNode instanceof SimpleValueNode)
					return ((SimpleValueNode) sourceNode).findSmartConversion();
				else
					return null;
			}

			EntitySourceNode sourceNode = planStructure.getSourceNode(po.getSource());

			return sourceNode.findSmartPropertyConversion(propertyName);
		}

		return null;
	}

	public Object convertToDelegateValue(Object smartValue, SmartConversion conversion) {
		return getExpertFor(conversion).convertValue(conversion, smartValue, ConversionDirection.smart2Delegate);
	}

	private SmartConversionExpert<SmartConversion> getExpertFor(SmartConversion conversion) {
		EntityType<?> conversionType = conversion.entityType();
		SmartConversionExpert<SmartConversion> result = (SmartConversionExpert<SmartConversion>) conversionExperts.get(conversionType);

		if (result == null)
			throw new RuntimeQueryEvaluationException("No expert found for conversion:" + conversionType.getTypeSignature());

		return result;
	}

}
