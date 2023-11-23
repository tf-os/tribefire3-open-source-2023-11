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

import java.util.Set;

import com.braintribe.model.command.Command;

import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 *  Command Interface for Run/Firing {@link GmModelPath} at TF Studio
 * 
 * 
 */

public interface GotoModelPath extends Command {

	final EntityType<GotoModelPath> T = EntityTypes.T(GotoModelPath.class);
	
	/**
	* @param modelPath
	* 
	* The {@link GmModelPath} which would be run/fire
	*/
	void setPath(GmModelPath modelPath);

	/**
	* @return the {@link GmModelPath}
	*/		
	GmModelPath getPath();
	
	/**
	* Collection of elements {@link GmModelPathElement} for which Action is triggered (if available)
	*/		
	void setOpenWithActionElements(Set<GmModelPathElement> openWithActionElements);
	/**
	* @return set of elements {@link GmModelPathElement}
	*/		
	Set<GmModelPathElement> getOpenWithActionElements();
	
	/**
	* Which element should be selected. If null, latest one is selected
	*/		
	void setSelectedElement(GmModelPathElement selectedElement);

	/**
	* @return selected element {@link GmModelPathElement}
	*/		
	GmModelPathElement getSelectedElement();
		
	/**
	* if True open at actual currentView as a new Tether elements, if False opened at new Tab
	*/		
	void  setAddToCurrentView(boolean add);
	/**
	* @return if add to current view
	*/		
	boolean getAddToCurrentView();
	
	/**
	* if True all elements are opened at Tether, if False just last one is opened
	*/			
	void  setShowFullModelPath(boolean show);	
	/**
	* @return if show full model path
	*/		
	boolean getShowFullModelPath();
}
