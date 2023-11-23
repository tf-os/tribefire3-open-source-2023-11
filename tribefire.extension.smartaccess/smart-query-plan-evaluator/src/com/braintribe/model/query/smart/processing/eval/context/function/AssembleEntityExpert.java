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

import java.util.Map;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.smartquery.eval.api.AssembleEntityContext;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.query.smart.processing.SmartQueryEvaluatorRuntimeException;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;
import com.braintribe.model.smartqueryplan.functions.PropertyMappingNode;

/**
 * 
 */
public class AssembleEntityExpert implements QueryFunctionExpert<AssembleEntity> {

	public static final AssembleEntityExpert INSTANCE = new AssembleEntityExpert();

	private AssembleEntityExpert() {
	}

	@Override
	public Object evaluate(Tuple tuple, AssembleEntity aeFunction, Map<Object, Value> operandMappings, QueryEvaluationContext _context) {
		SmartQueryEvaluationContext context = (SmartQueryEvaluationContext) _context;

		AssembleEntityContext aeContext = context.acquireAssembleEntityContext(aeFunction);

		PropertyMappingNode mappingNode = aeContext.getPropertyMappingNode(aeFunction.getEntitySignature());
		Value idValue = resolveValue(GenericEntity.id, mappingNode);
		Object id = context.resolveValue(tuple, idValue);

		if (id == null)
			// This can happen if we were doing a left-join.
			return null;

		String entitySignature = _context.resolveValue(tuple, aeFunction.getSmartEntitySignature());
		if (entitySignature == null)
			// This can happen if we were doing a left-join.
			return null;

		Value partitionValue = resolveValue(GenericEntity.partition, mappingNode);
		String partition = context.resolveValue(tuple, partitionValue);

		GenericEntity result = context.findEntity(entitySignature, id, partition);

		if (result == null)
			return createInstance(entitySignature, tuple, context, aeContext);
		else
			return result;
	}

	private GenericEntity createInstance(String signature, Tuple tuple, SmartQueryEvaluationContext context, AssembleEntityContext aeContext) {
		GenericEntity result = context.instantiate(signature);

		PropertyMappingNode mappingNode = aeContext.getPropertyMappingNode(signature);

		for (Property property : result.entityType().getProperties()) {
			// TODO check/explain if it makes sense to set an AI here. What happens to unmapped properties then? 
			Value value = resolveValueIfPossible(property.getName(), mappingNode);

			if (value == null) {
				property.setAbsenceInformation(result, GMF.absenceInformation());

			} else {
				Object resolvedVal = context.resolveValue(tuple, value);
				if (resolvedVal != null || property.isNullable())
					property.set(result, resolvedVal);
			}
		}

		return result;
	}

	private Value resolveValue(String propertyName, PropertyMappingNode mappingNode) {
		Value result = resolveValueIfPossible(propertyName, mappingNode);
		if (result == null)
			throw new SmartQueryEvaluatorRuntimeException(
					"Cannot resolve value for property '" + propertyName + "'. This usually happens when the property is not mapped.");

		return result;
	}

	private Value resolveValueIfPossible(String propertyName, PropertyMappingNode mappingNode) {
		while (mappingNode != null) {
			Value value = mappingNode.getPropertyMappings().get(propertyName);

			if (value != null)
				return value;

			mappingNode = mappingNode.getSuperTypeNode();
		}

		return null;
	}

}
