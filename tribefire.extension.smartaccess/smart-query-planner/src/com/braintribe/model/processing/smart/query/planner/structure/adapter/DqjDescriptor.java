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
package com.braintribe.model.processing.smart.query.planner.structure.adapter;

import java.util.List;

/**
 * 
 */
public interface DqjDescriptor {

	/**
	 * Returns list of joined properties for given DQJ. Joined entity means from smart entity point of view.
	 */
	List<String> getJoinedEntityDelegatePropertyNames();

	/**
	 * Returns owner property for given joined property. Relation owner means from smart entity point of view.
	 */
	String getRelationOwnerDelegatePropertyName(String joinedEntityDelegatePropertyName);

	ConversionWrapper getRelationOwnerPropertyConversion(String joinedEntityDelegatePropertyName);
	
	ConversionWrapper getJoinedEntityPropertyConversion(String joinedEntityDelegatePropertyName);
	
	boolean getForceExternalJoin();
	
}
