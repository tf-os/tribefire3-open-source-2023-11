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
import java.util.Map;

import com.braintribe.model.generic.annotation.meta.TypeSpecification;

/**
 * Specifies a hard restriction on a type of given property and is primarily used when we want the allow two or more
 * different types for a property. In that case we use a common super-type as the type of the property alongside with
 * this annotation.
 * 
 * In case of a collection we only specify the types of elements or key/value for maps. It is therefore not possible to
 * restrict a type of an Object property to a collection with this annotation.
 * 
 * For example meta-data of a GmProperty can be either UniversalMetaData or PropertyMetaData.
 * 
 * @see TypeSpecification
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface TypeRestriction {

	Class<?>[] value() default {};

	/** This can only be non-empty if the annotated property is of type {@link Map} */
	Class<?>[] key() default {};

	boolean allowVd() default false;

	boolean allowKeyVd() default false;

	String globalId() default "";
}
