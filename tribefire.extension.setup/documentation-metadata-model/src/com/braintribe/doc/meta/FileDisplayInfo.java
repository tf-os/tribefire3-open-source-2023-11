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

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author pit - javadoc only
 *
 */
public interface FileDisplayInfo extends GenericEntity{
	EntityType<FileDisplayInfo> T = EntityTypes.T(FileDisplayInfo.class);

	/**
	 * @return - {@link String} to display as title (where is it shown?)
	 */
	String getDisplayTitle();
	void setDisplayTitle(String displayTitle);
	
	/**
	 * @return - {@link String} to act as a short description (where is it shown?)
	 */
	String getShortDescription();
	void setShortDescription(String shortDescription);
	
	/**
	 * @return - an int representing the 'priority' (what is the metric here? And what is the priority used for?)
	 */
	int getPriority();
	void setPriority(int priority);
	
	/**
	 * @return - true if it's to be hidden (why? from where?)
	 */
	boolean getHidden();
	void setHidden(boolean hidden);
	
	/**
	 * @return - a {@link Set} of {@link String} to act as 'tags' (for what? and where?)
	 */
	Set<String> getTags();
	void setTags(Set<String> tags);
	
	/**
	 * @return - a {@link Set} of {@link String} that represents 'boosted search terms' (what? why? where?)
	 */
	Set<String> getBoostedSearchTerms();
	void setBoostedSearchTerms(Set<String> boosted);
}
