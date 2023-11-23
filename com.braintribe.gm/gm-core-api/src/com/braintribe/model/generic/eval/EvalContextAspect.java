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

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

/**
 * @param <T>
 *            specifies the type of the aspect, e.g. String
 */
@JsType(namespace = GmCoreApiInteropNamespaces.eval)
public interface EvalContextAspect<T> extends TypeSafeAttribute<T> {
	// nothing to add
}
