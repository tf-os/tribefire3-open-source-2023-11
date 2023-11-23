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
package com.braintribe.model.generic.base;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Base type for all GM values, except for simple ones, of course. The idea is to force/prefer (see below) instances of types which
 * implement this interface, because it makes some of the core algorithms faster.
 * 
 * <h4>Entities</h4>
 * 
 * All entity instances are guaranteed to implement this, as this is a super type of {@link GenericEntity}.
 * 
 * <h4>Enums</h4>
 *
 * For now, implementing this by GM enums is optional, but we want it to be mandatory in the future. It will also serve as a marker to
 * determine which enums are meant to be a part of the model (as there are enums in model artifacts which are not meant to be part of the
 * model).
 * 
 * <h4>Collections</h4>
 * 
 * Generally speaking, we support any collection implementation, but there might be some functionality that relies on collections
 * implementing this, as this influences the {@link CollectionType#isValueAssignable(Object)} implementation for collections (see the remark
 * there). Such components/algorithms should always have this property well documented. Side-note: the 'isInstance' method was added in
 * early March 2016, and by then there was no such usage yet.
 * 
 * <h5>Collection implementations</h5>
 * 
 * If you are working with entities bound to a PersistenceGmSession (one that has an access behind it), you do not have to worry as there is
 * a property interceptor that always ensures the collection you set is the right one (in that case an enhanced one, so that manipulation
 * tracking works). This would still be true for ManagedGmSession, though that is rare.
 * 
 * BTW: If you are using a NotifyingGmSession, you must yourself ensure that you have Collection Enhancing and Manipulation Tracking. (Which
 * however makes little sense... this should be automatic IMO).
 * 
 * There are two sets of implementations, similar like we have for entities - plain and enhanced. Their names reflect this directly, so they
 * are called like PlainList and EnhancedList.
 * 
 * Plain implementations can be found in GmCore artifact, enhanced are in BasicNotifyingGmSession (unfortunately, because they also provide
 * lazy-loading feature implemented using PersistenceGmSession).
 * 
 * @see EntityBase
 * @see EnumBase
 * @see CollectionBase
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public interface GenericBase {

	@JsMethod(name="Type")
	GenericModelType type();

	@JsMethod(name="IsEntity")
	default boolean isEntity() {
		return false;
	}

}
