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
package jsinterop.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsFunction marks a functional interface as being the definition of a JavaScript function.
 *
 * <p>There are some limitations exists on JsFunction to make them practical and efficient:
 *
 * <ul>
 * <li>A JsFunction interface cannot extend any other interfaces.
 * <li>A class may not implement more than one JsFunction interface.
 * <li>A class that implements a JsFunction type cannot be a {@link JsType} (directly or
 *     indirectly).
 * <li>Fields and defender methods of the interfaces should be marked with {@link JsOverlay} and
 *     cannot be overridden by the implementations.
 * </ul>
 *
 * <p>As a best practice, we also recommend marking JsFunction interfaces with FunctionalInterface
 * to get improved checking in IDEs.
 *
 * <p><b>Instanceof and Castability:</b>
 *
 * <p>Instanceof and casting for JsFunction is effectively a JavaScript <tt>'typeof'</tt> check to
 * determine if the instance is a function.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface JsFunction {
	//
}
