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
 * JsOverlay is used to enhance Java API of the native JsTypes and JsFunctions so richer and more
 * Java friendly abstractions could be provided.
 *
 * <pre>
 * {@literal @}JsType(isNative=true)
 * class Person {
 *   {@literal @}JsOverlay
 *   private static final Person NO_BODY = new Person();
 *
 *   private String name;
 *   private String lastName;
 *
 *   {@literal @}JsOverlay
 *   public String getFullName() {
 *     return (name + " " + lastName).trim();
 *   }
 * }</pre>
 *
 * <p>Note that:
 *
 * <ul>
 * <li> JsOverlay methods cannot override any existing methods.
 * <li> JsOverlay methods should be effectively final.
 * <li> JsOverlay methods cannot be called from JavaScript
 * </ul>
 *
 * These restrictions are in place to avoid polymorphism because underneath the original type is not
 * modified and the overlay fields/methods are simply turned into static dispatches.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface JsOverlay {
	//
}
