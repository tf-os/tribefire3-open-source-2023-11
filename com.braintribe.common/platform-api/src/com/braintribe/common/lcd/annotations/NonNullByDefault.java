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
package com.braintribe.common.lcd.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that {@link NonNull} is the default null annotation, which means that all parameters/return values/variables must not be
 * <code>null</code> unless {@link Nullable} is set. Note that this annotation can be {@link #value() disabled}. This is especially useful when
 * overriding methods that are not null-annotated.<br/>
 * This annotation is compatible with the annotation-based null analysis of Eclipse.
 *
 * @author michael.lafite
 */
@Retention(RetentionPolicy.CLASS)
@Target({ METHOD, CONSTRUCTOR, TYPE, PACKAGE })
@Documented
public @interface NonNullByDefault {
	/**
	 * Whether the annotation is enabled or not.
	 */
	boolean value() default true;
}
