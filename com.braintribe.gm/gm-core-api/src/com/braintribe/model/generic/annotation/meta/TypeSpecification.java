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
package com.braintribe.model.generic.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.braintribe.model.generic.annotation.TypeRestriction;

/**
 * Specifies what the type of given property should be, but this limitation is not part of the woven type - it is only
 * present as meta-data and it's up to an implementer of given use-case how he uses this.
 * 
 * Typical example is the specification for the id property, where this information is usually respected unless the
 * underlying persistence layer has a good reason not to. For example our CollaborativeSmoodAccess uses only Strings as
 * ids.
 * 
 * @see TypeRestriction
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface TypeSpecification {

	Class<?> value();

	String globalId() default "";
}
