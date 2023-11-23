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
package com.braintribe.common.lcd.function;

import java.util.function.Consumer;

/**
 * Represents a functions which accepts one input and returns a result of the same type. The result would typically be a modified copy of the input
 * value.
 * <p>
 * This can be used when some inner parts of an object have to be completed / resolved / decrypted just-in-time.
 *
 * @author peter.gazdik
 */
public interface GenericTransformer {

	// @formatter:off
	GenericTransformer identityTransformer = new GenericTransformer() {@Override public <T> T transform(T t) { return t; } }; 
	// @formatter:on

	<T> T transform(T t);

	default <T> void transform(T t, Consumer<? super T> consumer) {
		consumer.accept(transform(t));
	}

}
