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
package com.braintribe.model.generic.eval;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.processing.async.api.JsPromise;

import jsinterop.annotations.JsType;

/**
 * This is only used in the GWT codebase. It is defined here, because the TypeScript declarations refer to this type (the eval method returns
 * JsEvalContext). So it needs to be somewhere visible from each model, but cannot be even deeper (say jsinterop-base), because this extends
 * EvalContext which is defined here.
 * 
 * @author peter.gazdik
 */
@JsType(namespace = GmCoreApiInteropNamespaces.eval)
public interface JsEvalContext<R> extends EvalContext<R> {

	JsPromise<R> andGet();

	JsPromise<Maybe<R>> andGetReasoned();

}