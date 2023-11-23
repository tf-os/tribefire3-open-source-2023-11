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
package com.braintribe.model.uicommand;

import com.braintribe.model.command.Command;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Command Interface for Run/Firing Web page defined as Url at TF Studio
 * 
 */

public interface GotoUrl extends Command {

	final EntityType<GotoUrl> T = EntityTypes.T(GotoUrl.class);

	/**
	* @param modelUrl
	* 
	* The Url defined by the String which would be run/fire
	*/
	public void setUrl(String modelUrl);
	
	/**
	* @return the Url as a String 
	*/		
	public String getUrl();
	
	/**
	* @param target
	* 
	* Define the target where to show the Url
	*/
	public void setTarget(String target);	
	
	/**
	* @return the Target as a String 
	*/		
	public String getTarget();
	
	/**
	* @param useImage
	* 
	* Define if use images at web page defined by Url
	*/
	public void setUseImage(Boolean useImage);
	
	/**
	* @return the Boolean if use images
	*/		
	public Boolean getUseImage();
}
