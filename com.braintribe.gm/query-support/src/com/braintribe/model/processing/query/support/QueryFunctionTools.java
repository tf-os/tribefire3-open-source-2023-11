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
package com.braintribe.model.processing.query.support;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.eval.context.function.AsStringExpert;
import com.braintribe.model.processing.query.eval.context.function.ConcatenationExpert;
import com.braintribe.model.processing.query.eval.context.function.EntitySignatureExpert;
import com.braintribe.model.processing.query.eval.context.function.LocalizeExpert;
import com.braintribe.model.processing.query.eval.context.function.LowerExpert;
import com.braintribe.model.processing.query.eval.context.function.UpperExpert;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.query.functions.value.Lower;
import com.braintribe.model.query.functions.value.Upper;

/**
 * 
 */
public class QueryFunctionTools {

	private static final Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> DEFAULT_FUNCTION_EXPERTS = newMap();

	static {
		DEFAULT_FUNCTION_EXPERTS.put(Localize.T, LocalizeExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(EntitySignature.T, EntitySignatureExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(Concatenation.T, ConcatenationExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(Lower.T, LowerExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(Upper.T, UpperExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(AsString.T, AsStringExpert.INSTANCE);
	}

	public static Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> functionExperts(
			Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> configuredExperts) {

		Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> result = newMap();
		result.putAll(DEFAULT_FUNCTION_EXPERTS);
		if (configuredExperts != null)
			result.putAll(configuredExperts);

		return result;
	}

}
