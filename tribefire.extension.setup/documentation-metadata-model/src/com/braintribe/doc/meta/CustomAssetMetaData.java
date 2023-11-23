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
package com.braintribe.doc.meta;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * generic entity to represent a documentation asset 
 *  
 * @author pit - javadoc only
 *
 */
public interface CustomAssetMetaData extends GenericEntity {
	EntityType<CustomAssetMetaData> T = EntityTypes.T(CustomAssetMetaData.class);

	/**
	 * @return - true if it's supposed to be 'hidden' (from where?)
	 */
	boolean getHidden();
	void setHidden(boolean hidden);
	
	/**
	 * @return - a string to act as a title (shown in the landing page) 
	 */
	String getDisplayTitle();
	void setDisplayTitle(String displayTitle);
	
	/**
	 * @return - a string that represents the file name of the starting point (used in the left side menu) (html? i.e. after production)
	 */
	String getStartingPoint();
	void setStartingPoint(String startingPoint);
	
	/**
	 * @return - a string with a short description (shown in the landing page)
	 */
	String getShortDescription();
	void setShortDescription(String shortDescription);
	
	/**
	 * @return - a relative URL to the image to be used (where? what imagetypes?)  
	 */
	String getImageUrl();
	void setImageUrl(String assetRelativeUrl);
}
