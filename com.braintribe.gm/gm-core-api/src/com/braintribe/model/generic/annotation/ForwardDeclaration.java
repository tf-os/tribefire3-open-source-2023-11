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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.ValueDescriptor;

/**
 * This annotation says that given entity does not belong to the model corresponding to the artifact it is declared in, but to another one
 * specified by the {@link #value()}.
 * 
 * The use-case that lead to the introduction was that {@link GenericEntity} has a dependency on e.g. {@link ValueDescriptor}, so they have
 * to be in the same artifact, but we still want it to be part of the ValueDescriptorModel. So the entity is "physically" present in the
 * RootModel, but logically part of the ValueDescriptorModel.
 * 
 * Name is taken from <a href="https://en.wikipedia.org/wiki/Forward_declaration">this</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ForwardDeclaration {
	/**
	 * Name of the model (groupId:artifactId) to which this type should belongs.
	 */
	String value();
}
