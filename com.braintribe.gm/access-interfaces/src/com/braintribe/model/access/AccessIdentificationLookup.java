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
package com.braintribe.model.access;

/**
 * access to the access identification : 
 * 
 * enables to identify an {@link IncrementalAccess} access and to retrieve the access again by its id  
 * 
 * @author pit
 * @author dirk
 *
 */
public interface AccessIdentificationLookup {

	/**
	 * @param access - the access we want to id of 
	 * @return - the id of the access 
	 */
	public String lookupAccessId( IncrementalAccess access);
	
	/**
	 * @param id - the id of the access 
	 * @return - returns the top level access of the access with this id
	 */
	public IncrementalAccess lookupAccess( String id);
}
