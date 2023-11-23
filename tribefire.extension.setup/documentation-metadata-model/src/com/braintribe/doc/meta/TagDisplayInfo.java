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
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author pit
 *
 */
public interface TagDisplayInfo extends GenericEntity{
	EntityType<TagDisplayInfo> T = EntityTypes.T(TagDisplayInfo.class);

	/**
	 * @return - the description of the tag (to be shown where?) 
	 */
	String getDescription();
	void setDescription(String description);
	
	/**
	 * @return - the title of the tag's display (to be shown where?)
	 */
	String getDisplayTitle();
	void setDisplayTitle(String displayTitle);
	
	/**
	 * @return - the ID of the tag (correlates with {@link FileDisplayInfo#getTags()}?)
	 */
	@Mandatory
	String getTagId();
	void setTagId(String tagId);
	
	/**
	 * @return - the path (URL) to an image (to be shown where?)
	 */
	String getImageUrl();
	void setImageUrl(String assetSchemedUrl);
}
