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
package com.braintribe.model.processing.session.api.transaction;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.session.GmSession;

import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface Transaction extends TransactionFrame {

	TransactionFrame getCurrentTransactionFrame();

	Set<LocalEntityProperty> getManipulatedProperties();

	boolean hasManipulations();

	/**
	 * Returns <tt>true</tt> iff given entity was created within this transaction. Note that this returns true even if
	 * the entity was later {@link GmSession#deleteEntity deleted} or {@link #undo undone}, which is handled by
	 * {@link #willPersist(GenericEntity)}.
	 */
	boolean created(GenericEntity entity);

	/**
	 * Similar to {@link #created(GenericEntity)}, but this returns <tt>false</tt> for {@link GmSession#deleteEntity
	 * deleted} or {@link #undo undone} entities.
	 */
	boolean willPersist(GenericEntity entity);

}
