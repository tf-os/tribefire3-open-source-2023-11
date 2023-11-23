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
package com.braintribe.devrock.eclipse.model.scan;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * simple GE for the scan repository data 
 * 
 * @author pit
 *
 */
public interface SourceRepositoryEntry extends GenericEntity {
	
	EntityType<SourceRepositoryEntry> T = EntityTypes.T(SourceRepositoryEntry.class);
	
	String path = "path";
	String key = "key";
	String type = "type";
	String editable = "editable";
	String actualFile = "actualFile";
	String symbolLink = "symbolLink";

	
	/**
	 * @return - full or partial (dev-env) string representation of the directory
	 */
	String getPath();
	void setPath(String value);
	
	/**
	 * @return - the key assigned
	 */
	String getKey();
	void setKey( String key);
	
	/**
	 * @return - {@link SourceRepositoryType}, either git or svn
	 */
	SourceRepositoryType getType();
	void setType(SourceRepositoryType value);
	
	
	/**
	 * @return - true if it can be edited
	 */
	boolean getEditable();
	void setEditable(boolean value);

	/**
	 * @return - the actual directory 
	 */
	String getActualFile();
	void setActualFile(String value);

	/**
	 * @return - whether the path is a symbolic link or a real file
	 */
	boolean getSymbolLink();
	void setSymbolLink(boolean value);
	
	
}
