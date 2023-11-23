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

import com.braintribe.model.generic.base.EntityBase;

/**
 * Template for the implementation of the {@link Object#toString() toString} method of an entity type.
 * 
 * This template can also use placeholders for properties and a few special variables:
 * <ul>
 * <li>#type: type name (signature)</li>
 * <li>_type: type name(same as #type)</li>
 * <li>#type_short: simple type name</li>
 * <li>#runtimeId: {@link EntityBase#runtimeId() runtimeId}</li>
 * <li>#id: id if not null, otherwise {@link EntityBase#runtimeId() runtimeId} prefixed with '~'</li>
 * </ul>
 * 
 * A property/variable can be specified like this "${#type_short}" and escaped with doubling the "$" sign, like this "$${#type_short}".
 * <p>
 *
 * Example:<br>
 * Template: "${#type_short} - ${name}" could result in values like "Person - John" or "Item - Hammer"
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface ToStringInformation {
	public String value();
}
