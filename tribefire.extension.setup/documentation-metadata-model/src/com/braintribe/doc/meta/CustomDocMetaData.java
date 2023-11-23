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

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * {@link CustomDocMetaData} is used in the configuration artifact of a documentation-asset chain.
 * It declares its content, i.e. specifies what is to come to the landing-page
 * 
 * 
 * @author pit - javadoc only
 *
 */
public interface CustomDocMetaData extends CustomJavaDocMetaData {
	EntityType<CustomDocMetaData> T = EntityTypes.T(CustomDocMetaData.class);

	/**
	 * @return - a string to act as the title of the landing-page
	 */
	String getTitle();
	void setTitle(String title);
	
	/**
	 * @return - true if entry points should be generated automatically (why? what's the alternative? the entry points below?)
	 */
	boolean getAutoGenerateEntryPoints();
	void setAutoGenerateEntryPoints(boolean autoGenerateEntryPoints);
	
	/**
	 * @return - a {@link List} of {@link Entrypoint} making up the contents of the documentation, each entry-point
	 * stands for a contained documentation asset 
	 */
	List<Entrypoint> getEntrypoints();
	void setEntrypoints(List<Entrypoint> entrypoints);

	/**
	 * @return - a {@link List} of {@link TagDisplayInfo} (to be displayed where?)
	 */
	List<TagDisplayInfo> getTags();
	void setTags(List<TagDisplayInfo> tags);
	
	/**
	 * @return - a {@link Map} of String {@code (<groupId>:<artifactId>)} to {@link CustomAssetMetaData}. Per cfg, the metadata is null
	 */
	Map<String, CustomAssetMetaData> getAssets();
	void setAssets(Map<String, CustomAssetMetaData> assets);
}
