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
package com.braintribe.model.workbench;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

public interface WorkbenchConfiguration extends GenericEntity {

	EntityType<WorkbenchConfiguration> T = EntityTypes.T(WorkbenchConfiguration.class);

	Resource getStylesheet();
	void setStylesheet(Resource stylesheet);
	
	Resource getFavIcon();
	void setFavIcon(Resource favIcon);
	
	String getTitle();
	void setTitle(String title);
	
	Integer getAutoPagingSize();
	void setAutoPagingSize(Integer autoPagingSize);
	
	boolean getUseGlobalSearch();
	void setUseGlobalSearch(boolean useGlobalSearch);
	
	boolean getUseTopContextActionBar();
	void setUseTopContextActionBar(boolean useTopContextActionBar);
	
	boolean getUseTopGlobalActionBar();
	void setUseTopGlobalActionBar(boolean useTopGlobalActionBar);
	
	String getLocale();
	void setLocale(String locale);
	
	Integer getMaxNumberOfOpenedTabs();
	void setMaxNumberOfOpenedTabs(Integer maxNumberOfOpenedTabs);
	
	Integer getWorkbenchWidth();
	void setWorkbenchWidth(Integer workbenchWidth);
	
	boolean getDisplayShortName();
	void setDisplayShortName(boolean displayShortName);
}
