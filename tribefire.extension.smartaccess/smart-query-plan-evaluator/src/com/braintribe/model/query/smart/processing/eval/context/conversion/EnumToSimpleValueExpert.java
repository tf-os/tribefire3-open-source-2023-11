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
package com.braintribe.model.query.smart.processing.eval.context.conversion;

import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.model.accessdeployment.smart.meta.conversion.EnumToSimpleValue;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.processing.smartquery.eval.api.RuntimeSmartQueryEvaluationException;

/**
 * 
 */
public class EnumToSimpleValueExpert extends AbstractToSimpleTypeExpert<Enum<?>, EnumToSimpleValue, Object> {

	public static final EnumToSimpleValueExpert INSTANCE = new EnumToSimpleValueExpert();

	private EnumToSimpleValueExpert() {
	}

	@Override
	protected Enum<?> parse(Object value, EnumToSimpleValue conversion) {
		Map<GmEnumConstant, Object> valueMappings = conversion.getValueMappings();

		for (Entry<GmEnumConstant, Object> entry: valueMappings.entrySet()) {
			if (entry.getValue().equals(value)) {
				return parseEnum(entry.getKey());
			}
		}

		throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + value +
				"' to an enum. None of the configured GmEnumConstants matches this value. Mappings: " + conversion.getValueMappings());
	}

	private Enum<?> parseEnum(GmEnumConstant constant) {
		EnumType enumType = GMF.getTypeReflection().getType(constant.getDeclaringType().getTypeSignature());

		@SuppressWarnings("unchecked")
		Enum<?>[] enumConstants = ((Class<Enum<?>>) enumType.getJavaType()).getEnumConstants();

		for (Enum<?> e: enumConstants) {
			if (e.name().equals(constant.getName())) {
				return e;
			}
		}

		throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + constant +
				"' to an enum. No matching constant found for enum: " + enumType.getTypeSignature());
	}

	@Override
	protected Object toSimpleValue(Enum<?> value, EnumToSimpleValue conversion) {
		Map<GmEnumConstant, Object> valueMappings = conversion.getValueMappings();

		for (Entry<GmEnumConstant, Object> entry: valueMappings.entrySet()) {
			if (entry.getKey().getName().equals(value.name())) {
				return entry.getValue();
			}
		}

		throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + value +
				"' to a string. None of the configured GmEnumConstants matches this enum constant.");
	}

}
