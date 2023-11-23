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
 * JsOptional marks a parameter in a method as optional indicating that the argument can be omitted
 * from the function call when called from JavaScript side.
 *
 * <p>Note that JsOptional can only be used in a JsConstructor, a JsMethod or a JsFunction method.
 * An optional argument cannot precede a non-optional argument (a vararg is considered an
 * optional argument).
 *
 * <p>This annotation is informational in the GWT compiler but other compilers or tools might use it
 * for example to annotate types in the output JavaScript program.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface JsOptional {
	//
}
