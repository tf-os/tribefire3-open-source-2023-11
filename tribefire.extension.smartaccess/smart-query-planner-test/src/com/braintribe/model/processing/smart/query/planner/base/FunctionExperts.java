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
package com.braintribe.model.processing.smart.query.planner.base;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.processing.query.eval.context.function.AsStringExpert;
import com.braintribe.model.processing.query.eval.context.function.LocalizeExpert;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.smart.processing.eval.context.function.AssembleEntityExpert;
import com.braintribe.model.query.smart.processing.eval.context.function.ResolveDelegatePropertyExpert;
import com.braintribe.model.query.smart.processing.eval.context.function.ResolveIdExpert;
import com.braintribe.model.query.smart.processing.eval.context.function.SmartEntitySignatureExpert;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveDelegateProperty;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveId;

/**
 * 
 */
public class FunctionExperts {

	public static final Map<EntityType<? extends QueryFunction>, QueryFunctionExpert<?>> DEFAULT_FUNCTION_EXPERTS = newMap();

	static {
		DEFAULT_FUNCTION_EXPERTS.put(Localize.T, LocalizeExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(AssembleEntity.T, AssembleEntityExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(AsString.T, AsStringExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(ResolveId.T, ResolveIdExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(ResolveDelegateProperty.T, ResolveDelegatePropertyExpert.INSTANCE);
		DEFAULT_FUNCTION_EXPERTS.put(EntitySignature.T, SmartEntitySignatureExpert.INSTANCE);
	}

}
