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
package com.braintribe.model.processing.smartquery.eval.api.function;

import java.util.Collection;

import com.braintribe.model.processing.query.eval.api.function.QueryFunctionExpert;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.QueryFunction;

/**
 * Customization of {@link QueryFunctionExpert} for SmartAccess. SmartAccess can also work with standard
 * {@link QueryFunctionExpert}s, but in some cases, it is not enough. The reason is, that the
 * {@link #listOperandsToSelect(QueryFunction)} function is used for two different things - when normalizing the query
 * (there it needs to provide all operands it contains, so that they themselves can be normalized), and when determining
 * which values need to be retrieved from the delegate (in the planner).
 * 
 * In this second case, we might want to work with different operands. For example, the {@link EntitySignature} function
 * can have an operand of type {@link Source}, but we do not need to retrieve the entire entity from the delegate, just
 * it's signature. In such case, if a {@link SmartQueryFunctionExpert} is configured for given function, it's
 * {@link #listOperandsToSelect(QueryFunction)} is used for this purpose.
 */
public interface SmartQueryFunctionExpert<F extends QueryFunction> extends QueryFunctionExpert<F> {

	Collection<? extends Operand> listOperandsToSelect(F queryFunction);

}
