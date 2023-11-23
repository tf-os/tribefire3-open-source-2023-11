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
package com.braintribe.web.servlet.home.model;

import java.util.List;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface LinkGroup extends StandardIdentifiable {

	final EntityType<LinkGroup> T = EntityTypes.T(LinkGroup.class);

	void setName(String name);
	String getName();

	void setIconRef(String iconRef);
	String getIconRef();

	void setOpenLink(Link openLink);
	Link getOpenLink();
	
	void setLinks(List<GroupElement> links);
	List<GroupElement> getLinks();
	
	boolean getSkip();
	void setSkip(boolean skip);

}
