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
package com.braintribe.model.processing.session.api.managed;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

import jsinterop.annotations.JsType;

/**
 * A fluent builder for setting various parameters for the manipulation application process. This was created because we
 * needed to parameterize the {@link ManagedGmSession#manipulate()} method in a flexible way, that will not force us to
 * change in interface that much in the future.
 */
@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface ManipulationApplicationContextBuilder {

	/**
	 * A method that triggers the actual application of the manipulation, similar to
	 * {@link ManagedGmSession#manipulate()}.
	 */
	ManipulationReport apply(Manipulation manipulation) throws GmSessionException;

	ManipulationApplicationContext context();
	
	/**
	 * Determines what type of manipulations will be provided to the {@link #apply(Manipulation)} method.
	 */
	ManipulationApplicationContextBuilder mode(ManipulationMode mode);
	
	/**
	 * Tells whether manipulations should be treated lenient (silently ignored instead of generating exception)
	 */
	ManipulationApplicationContextBuilder lenience(ManipulationLenience lenience);
	
	/**
	 * Configures the instantiations which were performed already elsewhere.
	 */
	ManipulationApplicationContextBuilder instantiations(Map<PreliminaryEntityReference, GenericEntity> instantiations);

}
