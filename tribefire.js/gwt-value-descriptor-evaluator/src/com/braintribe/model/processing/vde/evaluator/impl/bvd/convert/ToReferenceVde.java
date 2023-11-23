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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.convert;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.braintribe.model.bvd.convert.ToReference;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

public class ToReferenceVde implements ValueDescriptorEvaluator<ToReference> {

	@Override
	public VdeResult evaluate(VdeContext context, ToReference valueDescriptor) throws VdeRuntimeException {
		Object operand = context.evaluate(valueDescriptor.getOperand());
		Object result = convert(operand);
		return new VdeResultImpl(result, false);
	}

	private Object convert(Object operand) {
		if (operand == null) {
			return null;
		}
		GenericModelType operandType = BaseType.INSTANCE.getActualType(operand);
		TypeCode typeCode = operandType.getTypeCode();
		switch (typeCode) {
			case entityType:
				return ((GenericEntity) operand).reference();
			case enumType:
				return EnumReference.of((Enum<?>)operand);
			case listType:
			case setType:
				Collection<?> collection = (Collection<?>) operand;
				//@formatter:off
				return collection
					.stream()
					.map(this::convert)
					.collect(getCollector(typeCode));
				//@formatter:on
			case mapType:
				Map<?,?> map = (Map<?,?>) operand;
				//@formatter:off
				return map.entrySet()
					.stream()
					.collect(Collectors.toMap(this::convert, this::convert));
				//@formatter:on
			default:
				return operand;
		}
	}

	private Collector<Object, ?, ? extends Collection<Object>> getCollector(TypeCode typeCode) {
		return (typeCode == TypeCode.setType) ? Collectors.toSet() : Collectors.toList();
	}

}
