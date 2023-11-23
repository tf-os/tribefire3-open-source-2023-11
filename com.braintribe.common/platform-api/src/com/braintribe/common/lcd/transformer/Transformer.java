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
package com.braintribe.common.lcd.transformer;

/**
 * <code>Transformer</code>s are used to {@link #transform(Object, Object) transform)} (or convert) values, often including type conversion.
 *
 * @author michael.lafite
 *
 * @param <I>
 *            the type of the input (to be transformed)
 * @param <O>
 *            the type of the output (i.e. the result returned by the transformer)
 * @param <C>
 *            the transformation context
 */

public interface Transformer<I, O, C> {

	/**
	 * Transforms the <code>input</code> and returns the transformed object as result.
	 */
	O transform(I input, C transformationContext) throws TransformerException;
}
