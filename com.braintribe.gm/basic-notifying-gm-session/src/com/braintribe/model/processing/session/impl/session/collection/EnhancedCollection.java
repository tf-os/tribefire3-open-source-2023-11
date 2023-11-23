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
package com.braintribe.model.processing.session.impl.session.collection;

import com.braintribe.model.generic.base.CollectionBase;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.tracking.ManipulationListener;

public interface EnhancedCollection extends CollectionBase {

	void setCollectionOwner(LocalEntityProperty owner);

	LocalEntityProperty getCollectionOwner();

	/** The return type is the same as the collection this is invoked on i.e. List, Set or Map. */
	Object getDelegate();

	/** @see #isIncomplete() */
	void setIncomplete(boolean incomplete);

	/** Indicates whether the collection was loaded entirely, or just partially (first few elements) - used in GWT */
	boolean isIncomplete();

	/** Indicates whether the content of the collection was already retrieved, or we just created one (shallow) without  */
	boolean isLoaded();

	void addManipulationListener(ManipulationListener listener);

	void removeManipulationListener(ManipulationListener listener);

}
