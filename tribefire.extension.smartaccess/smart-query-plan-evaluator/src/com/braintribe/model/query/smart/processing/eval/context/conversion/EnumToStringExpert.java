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

import com.braintribe.model.accessdeployment.smart.meta.conversion.EnumToString;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.processing.smartquery.eval.api.RuntimeSmartQueryEvaluationException;

/**
 * 
 */
public class EnumToStringExpert extends AbstractToStringExpert<Enum<?>, EnumToString> {

	public static final EnumToStringExpert INSTANCE = new EnumToStringExpert();

	private EnumToStringExpert() {
	}

	@Override
	protected Enum<?> parse(String value, EnumToString conversion) {
		Map<GmEnumConstant, String> valueMappings = conversion.getValueMappings();

		if (valueMappings == null || valueMappings.isEmpty()) {
			return parseEnum(value, conversion);
		}

		for (Entry<GmEnumConstant, String> entry: valueMappings.entrySet()) {
			if (entry.getValue().equals(value)) {
				return parseEnum(entry.getKey().getName(), conversion);
			}
		}

		throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + value +
				"' to an enum. None of the configured GmEnumConstants matches this value. Enum: " +
				conversion.getEnumType().getTypeSignature());
	}

	private Enum<?> parseEnum(String value, EnumToString conversion) {
		EnumType enumType = GMF.getTypeReflection().getType(conversion.getEnumType().getTypeSignature());

		@SuppressWarnings("unchecked")
		Enum<?>[] enumConstants = ((Class<Enum<?>>) enumType.getJavaType()).getEnumConstants();

		for (Enum<?> e: enumConstants) {
			if (e.name().equals(value)) {
				return e;
			}
		}

		throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + value + "' to an enum. No matching constant found for enum: " +
				enumType.getTypeSignature());
	}

	@Override
	protected String toString(Enum<?> value, EnumToString conversion) {
		Map<GmEnumConstant, String> valueMappings = conversion.getValueMappings();

		if (valueMappings == null || valueMappings.isEmpty()) {
			return value.name();
		}

		for (Entry<GmEnumConstant, String> entry: valueMappings.entrySet()) {
			if (entry.getKey().getName().equals(value.name())) {
				return entry.getValue();
			}
		}

		throw new RuntimeSmartQueryEvaluationException("Cannot convert '" + value +
				"' to a string. None of the configured GmEnumConstants matches this enum constant.");
	}

}
