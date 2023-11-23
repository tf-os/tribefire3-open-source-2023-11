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
package model;

import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
public interface GenericEntity_aop extends GenericEntity {

	// @formatter:off
	@Override
	default <T> T getId() { return null; }
	@Override
	default void setId(Object id) {/*nothing*/}

	@Override
	default String getPartition() { return null; }
	@Override
	default void setPartition(String partition) {/*nothing*/}
	
	@Override
	default String getGlobalId() { return null; }
	@Override
	default void setGlobalId(String globalId) {/*nothing*/}
	// @formatter:on

}

