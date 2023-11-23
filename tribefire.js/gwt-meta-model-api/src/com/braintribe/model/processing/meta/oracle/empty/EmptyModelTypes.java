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

import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.processing.meta.oracle.ModelTypes;
import com.braintribe.model.processing.meta.oracle.TypeOracle;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyModelTypes implements ModelTypes {

	public static final EmptyModelTypes INSTANCE = new EmptyModelTypes();

	private EmptyModelTypes() {
	}

	@Override
	public ModelTypes filter(Predicate<? super GmCustomType> filter) {
		return this;
	}

	@Override
	public ModelTypes onlyDeclared() {
		return this;
	}

	@Override
	public ModelTypes onlyInherited() {
		return this;
	}

	@Override
	public ModelTypes onlyEnums() {
		return this;
	}

	@Override
	public ModelTypes onlyEntities() {
		return this;
	}

	@Override
	public <T extends GmCustomType> Stream<T> asGmTypes() {
		return Stream.empty();
	}

	@Override
	public <T extends CustomType> Stream<T> asTypes() {
		return Stream.empty();
	}

	@Override
	public <T extends TypeOracle> Stream<T> asTypeOracles() {
		return Stream.empty();
	}

}
