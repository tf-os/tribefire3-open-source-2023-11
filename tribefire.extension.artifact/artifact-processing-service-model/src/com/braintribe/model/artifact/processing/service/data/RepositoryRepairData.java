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
package com.braintribe.model.artifact.processing.service.data;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * data as sent be the local repository cleanup
 * @author pit
 *
 */
public interface RepositoryRepairData extends GenericEntity {
		
	EntityType<RepositoryRepairData> T = EntityTypes.T(RepositoryRepairData.class);
	
	String numberOfCleanedLockFiles = "numberOfCleanedLockFiles";
	String cleanedLocalFiles = "cleanedLocalFiles";

	/**
	 * @return - the number of cleaned lock files
	 */
	Integer getNumberOfCleanedLockFiles();
	void setNumberOfCleanedLockFiles(Integer value);
	
	/**
	 * @return - a {@link List} of the fully qualified names of the cleaned lock files 
	 */
	List<String> getCleanedLockFiles();
	void setCleanedLockFiles(List<String> value);

}
