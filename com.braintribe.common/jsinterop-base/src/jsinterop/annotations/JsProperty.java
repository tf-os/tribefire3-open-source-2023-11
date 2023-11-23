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
 * JsProperty marks a field or method that is translated directly into a JavaScript property
 * preserving its name.
 *
 * <p>If it is applied to a method, it will be treated as a property accessor. As a result, instead
 * of translating method calls to JsProperty methods as method calls in JS, they will be translated
 * as property lookups. When a JsProperty method implemented by a Java class, such methods will be
 * generated as property accessor in JavaScript, hence the property access will trigger the
 * execution of the matching getter or setter methods.
 *
 * <p>JsProperty follows JavaBean style naming convention to extract the default property name. If
 * the JavaBean convention is not followed, the name should be set explicitly. For example:
 *
 * <ul>
 * <li> {@code @JsProperty getX()} or {@code @JsProperty isX()} translates as <tt>this.x</tt>
 * <li> {@code @JsProperty setX(int y)} translates as <tt>this.x=y</tt>
 * </ul>
 *
 * <p>Note: In JavaScript, instance members are defined on the prototype and class members are
 * defined on the constructor function of the type which mimics ES6 class style.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface JsProperty {

  /**
   * Customizes the name of the member in generated JavaScript. If none is provided;
   *
   * <ul>
   * <li>if it is field, the simple Java name will be used.
   * <li>if it is a method, the name will be generated based on JavaBean conventions.
   * </ul>
   */
  String name() default "<auto>";

  /**
   * Customizes the namespace of the static member in generated JavaScript. If none is provided,
   * namespace is the enclosing class' fully qualified JavaScript name.
   */
  String namespace() default "<auto>";
}
