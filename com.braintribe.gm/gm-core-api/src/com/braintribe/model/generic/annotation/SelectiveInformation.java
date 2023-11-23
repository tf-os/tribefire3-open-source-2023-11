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
package com.braintribe.model.generic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies how a given entity should be displayed as text (in the client). It is similar to
 * {@link ToStringInformation}, but that one is not meant to be displayed for the user, but for example as an entity
 * print in the logs.
 * 
 * This annotation is not perfectly compatible with the SelectiveInformation meta data, because the latter has a
 * property of type LocalizedString. So the value of this annotation is stored as the default value of that
 * LocalizedString, and the same applies in the opposite direction, when we create source code or bytecode based on that
 * meta data.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface SelectiveInformation {
	String value();

	String globalId() default "";
}
