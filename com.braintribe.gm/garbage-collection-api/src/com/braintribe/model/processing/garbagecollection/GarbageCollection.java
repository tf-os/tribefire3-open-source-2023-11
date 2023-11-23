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
package com.braintribe.model.processing.garbagecollection;

import java.util.List;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * The <code>GarbageCollection</code> deletes entities that are no longer needed. Usually that's the case, if they are
 * not reachable from one of "root" entities (i.e. entities that must not be deleted by GC).
 *
 * @author michael.lafite
 */
public interface GarbageCollection {

	/**
	 * Performs the garbage collection on the specified <code>subsets</code> using the passed <code>session</code> and
	 * returns a report.
	 *
	 * @param session
	 *            the session through which the data will be accessed.
	 * @param subsets
	 *            the list of subsets to perform the GC on.
	 * @param testModeEnabled
	 *            If enabled, the actual deletion of entities will be skipped.
	 *
	 * @throws GarbageCollectionException
	 *             if any error occurs.
	 */
	GarbageCollectionReport performGarbageCollection(PersistenceGmSession session, List<SubsetConfiguration> subsets,
			boolean testModeEnabled) throws GarbageCollectionException;

}
