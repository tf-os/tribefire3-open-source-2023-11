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
package com.braintribe.devrock.zarathud.model.context;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * handling the maven/ant/tb integration 
 * @author pit
 *
 */
@Abstract
public interface ResolutionAspect extends GenericEntity {
	
	EntityType<ResolutionAspect> T = EntityTypes.T(ResolutionAspect.class);
	String settingsFilename = "settingsFilename";
	String repositoryLocation = "repositoryLocation";
	String environmentOverrides = "environmentOverrides";

	/**
	 * deprecated : standard setup uses a 'environment sensitive configuration' wire module 
	 * @return - the settings.xml file to use 
	 */
	@Deprecated
	String getSettingsFilename();
	@Deprecated
	void setSettingsFilename(String value);
	
	/**
	 * deprecated : standard setup uses a 'environment sensitive configuration' wire module
	 * @return - the name of the local repository to use 
	 */
	@Deprecated	
	String getRepositoryLocation();
	@Deprecated
	void setRepositoryLocation(String value);
	
	/**
	 * allows tweaking of the interval virtual enviroment : environment variables
	 * @return - a {@link Map} of variable-name to variable-value
	 */
	Map<String,String> getEnvironmentOverrides();
	void setEnvironmentOverrides(Map<String,String> value);
	
	



}
