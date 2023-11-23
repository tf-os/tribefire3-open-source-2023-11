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
package com.braintribe.model.processing.itw.synthesis.gm.experts;

/**
 * This interface is used to directly access the properties of given entity using property indices. There are two cases
 * where this is utilized.
 * <ul>
 * <li><b>Enhanced entity</b> in case where we want to access the property directly (the field, or the getter/setter
 * method from the supertype), i.e. in case we want to bypass the property access interceptors (or when the last such
 * interceptor in the chain is invoked)</li>
 * <li><b>Weak properties</b> the classes for weak properties do not even have getters/setters, so they use this as a
 * getter/setter interface</li>
 * </ul>
 */
public interface IndexedPropertyEntity {

	void putIndexedProperty(int index, Object value);
	Object retrieveIndexedProperty(int index);

}
