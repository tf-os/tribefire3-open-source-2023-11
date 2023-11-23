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

import java.util.stream.Stream;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;

import jsinterop.annotations.JsType;

/**
 * 
 */
@JsType(namespace = GmCoreApiInteropNamespaces.model)
@SuppressWarnings("unusable-by-js")
public interface ModelDependencies {

	ModelDependencies transitive();

	ModelDependencies includeSelf();

	/**
	 * TODO this might be dangerous, the model equivalent exists only sometimes, and I think only in JVM?
	 */
	Stream<Model> asModels();

	Stream<GmMetaModel> asGmMetaModels();

	Stream<ModelOracle> asModelOracles();

}
