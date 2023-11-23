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
package com.braintribe.model.generic.manipulation;

/**
 * Describes the type of {@link Manipulation}. Each such manipulation (non-abstract) must return a correct value by implementing the
 * {@link Manipulation#manipulationType()} method. This value is used to speed up the branching in case the different manipulations should
 * be handled in different ways (switch statement is much faster than a long chain of {@code instanceof} operators).
 */
public enum ManipulationType {

	DELETE,
	INSTANTIATION,
	ACQUIRE,
	MANIFESTATION,

	// PROPERTY
	ABSENTING,
	CHANGE_VALUE,

	// COLLECTION
	ADD,
	REMOVE,
	CLEAR_COLLECTION,

	// meta
	VOID,
	COMPOUND;
}
