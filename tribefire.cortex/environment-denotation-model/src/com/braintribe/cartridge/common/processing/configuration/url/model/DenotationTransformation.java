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
package com.braintribe.cartridge.common.processing.configuration.url.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.module.api.DenotationTransformer;

/**
 * Describes a transformation of an environment denotation instance as a series of transformer steps, where each step is given by the name of the
 * corresponding transformer name.
 * <p>
 * See {@link DenotationTransformer transformer} for more details about Denotation Transformation process.
 * <p>
 * In most cases the transformation would not have to be described by the user (i.e. the corresponding {@link RegistryEntry#getTransformation()} would
 * be <tt>null</tt>). Why?
 * <p>
 * In the example from {@link DenotationTransformer}, where a DB connection info is provided for an IncrementalAccess, as long as there is only one
 * way how to turn the initial connection data into an access, no configuration is needed. However, if this is not the case, e.g. if there were two
 * different transformers that turn connection data into different connection pools, then the user has to specify the transformers explicitly, using
 * the {@link #getTransformers() transformers} property.
 * 
 * @see DenotationTransformer
 */
public interface DenotationTransformation extends GenericEntity {

	EntityType<DenotationTransformation> T = EntityTypes.T(DenotationTransformation.class);

	/** List of {@link DenotationTransformer} names. */
	List<String> getTransformers();
	void setTransformers(List<String> transformers);

}
