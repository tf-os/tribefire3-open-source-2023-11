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
package com.braintribe.model.generic.path.api;

import com.braintribe.model.generic.reflection.GenericModelType;

/**
 * 
 * 
 * @author dirk.scheffler
 * @author pit.steinlin
 * @author peter.gazdik
 */
public interface IModelPathElement {

	/** @return the {@link IModelPathElement} predecessor or null if there is none */
	IModelPathElement getPrevious();

	/**
	 * @return the actual node value which is an instance of any valid GM type which is also returned by
	 *         {@link #getType()}
	 */
	<T> T getValue();

	/** @return the {@link GenericModelType} of the value returned by {@link #getValue()} */
	<T extends GenericModelType> T getType();

	/**
	 * @return the {@link ModelPathElementType} which corresponds with the subclass of {@link IModelPathElement} and can
	 *         be used for switches which is faster than instanceof chains
	 */
	ModelPathElementType getElementType();

	/** @return the length of the path */
	int getDepth();

	default boolean isPropertyRelated() {
		return false;
	}

	default boolean isCollectionElementRelated() {
		return false;
	}

}
