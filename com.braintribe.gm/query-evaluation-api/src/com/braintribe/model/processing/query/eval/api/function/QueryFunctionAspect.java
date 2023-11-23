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
package com.braintribe.model.processing.query.eval.api.function;

/**
 * An interface for describing various aspects (pieces of information) for evaluating query functions. This uses the
 * pattern to emulate extensible enumerations, where our constants are the class literals of sub-types of this
 * interface. For example, if our function needs a date format (of type String), we could use a class like:
 * 
 * {@code  public interface DateFormatAspect extends QueryFunctionAspect<String> ... }
 * 
 * @param <T>
 *            Type of a given aspect.
 */
public interface QueryFunctionAspect<T> {
	// empty
}
