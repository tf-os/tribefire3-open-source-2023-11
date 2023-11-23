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
package com.braintribe.model.generic.reflection;

import java.util.Collection;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.meta.Weavable;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface Model {

	ModelDeclaration getModelArtifactDeclaration();

	Collection<? extends Model> getDependencies();

	<M extends Weavable> M getMetaModel();

	Collection<Class<?>> getDeclaredJavaTypes();

	boolean isRootModel();

	default String name() {
		return getModelArtifactDeclaration().getName();
	}
	
	default String globalId() {
		return modelGlobalId(getModelArtifactDeclaration().getName());
	}
	
	static String modelGlobalId(String modelName) {
		return "model:" + modelName;
	}

}
