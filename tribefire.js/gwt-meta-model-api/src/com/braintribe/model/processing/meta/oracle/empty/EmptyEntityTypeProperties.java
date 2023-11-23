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
package com.braintribe.model.processing.meta.oracle.empty;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.meta.oracle.EntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyEntityTypeProperties implements EntityTypeProperties {

	public static final EmptyEntityTypeProperties INSTANCE = new EmptyEntityTypeProperties();

	private EmptyEntityTypeProperties() {
	}

	@Override
	public EntityTypeProperties filter(Predicate<? super GmProperty> filter) {
		return this;
	}

	@Override
	public EntityTypeProperties onlyDeclared() {
		return this;
	}

	@Override
	public EntityTypeProperties onlyInherited() {
		return this;
	}

	@Override
	public Stream<GmProperty> asGmProperties() {
		return Stream.empty();
	}

	@Override
	public Stream<Property> asProperties() {
		return Stream.empty();
	}

	@Override
	public Stream<PropertyOracle> asPropertyOracles() {
		return Stream.empty();
	}

}
