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
package com.braintribe.model.generic.collection;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * TODO IMPLEMENT
 * 
 * This should be used later as a way to manipulation not-yet-loaded collections without the need to fully load them.
 * 
 * @author peter.gazdik
 */
public interface GmCollections {

	static <E> void add(Collection<? super E> collection, E element) {
		if (collection instanceof LinearCollectionBase)
			((LinearCollectionBase<? super E>) collection).add/* insert */(element);
		else
			collection.add(element);
	}

	static <E> void addAll(Collection<? super E> collection, Collection<? extends E> elements) {
		if (collection instanceof LinearCollectionBase)
			((LinearCollectionBase<? super E>) collection).addAll/* insertAll */(elements);
		else
			collection.addAll(elements);
	}
	
	static <E> void addAll(Collection<? super E> collection, Stream<? extends E> elements) {
		if (collection instanceof LinearCollectionBase)
			elements.forEach(collection::add);
			//((LinearCollectionBase<? super E>) collection).insertAll(elements);
		else
			elements.forEach(collection::add);
	}

}
