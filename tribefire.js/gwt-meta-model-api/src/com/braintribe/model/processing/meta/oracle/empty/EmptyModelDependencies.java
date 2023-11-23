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

import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.ModelDependencies;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyModelDependencies implements ModelDependencies {

	public static final EmptyModelDependencies INSTANCE = new EmptyModelDependencies();

	private EmptyModelDependencies() {
	}

	@Override
	public ModelDependencies transitive() {
		return this;
	}

	@Override
	public ModelDependencies includeSelf() {
		return this;
	}

	@Override
	public Stream<Model> asModels() {
		return Stream.empty();
	}

	@Override
	public Stream<GmMetaModel> asGmMetaModels() {
		return Stream.empty();
	}

	@Override
	public Stream<ModelOracle> asModelOracles() {
		return Stream.empty();
	}

}
