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
package com.braintribe.model.processing.meta.oracle;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.model)
@SuppressWarnings("unusable-by-js")
public interface EntityTypeProperties {

	EntityTypeProperties filter(Predicate<? super GmProperty> filter);

	EntityTypeProperties onlyDeclared();

	EntityTypeProperties onlyInherited();

	Stream<GmProperty> asGmProperties();

	Stream<Property> asProperties();

	Stream<PropertyOracle> asPropertyOracles();

}
