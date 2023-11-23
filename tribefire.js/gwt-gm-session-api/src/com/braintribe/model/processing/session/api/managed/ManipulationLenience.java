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

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.value.EntityReference;

import jsinterop.annotations.JsType;

/**
 * One of parameters of {@link ManagedGmSession#manipulate()}, which describes how manipulations related to unknown
 * entities (i.e. such that given {@link EntityReference} cannot be resolved) should be handled.
 */
@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public enum ManipulationLenience {

	/**
	 * Standard mode which means that manipulations related to unknown entities are not expected, in case they occur,
	 * the behavior is undefined.
	 */
	none,

	/**
	 * All manipulations related to unknown entities will be ignored.
	 */
	ignoreOnUnknownEntity,

	/**
	 * If an unknown entity is encountered, a new instance is created and manifested. In such case, the manifested
	 * entities are accessible via the {@link ManipulationReport#getLenientManifestations()}.
	 * 
	 * NOTE that a created entity has all it's properties marked as absent, except for the id property of course.
	 * 
	 * The standard use-case where this is used is the processing of induced manipulations by the session. In such case,
	 * all unknown entities are manifested and then after being applied, all the manifested entities are being
	 * "refreshed" by executing an entity query for these entities and then merging the result to the session.
	 */
	manifestOnUnknownEntity,
}
