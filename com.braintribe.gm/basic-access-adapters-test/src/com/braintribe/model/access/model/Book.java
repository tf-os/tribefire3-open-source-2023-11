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
package com.braintribe.model.access.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * 
 */
@ToStringInformation("${title}")
public interface Book extends StandardIdentifiable {

	EntityType<Book> T = EntityTypes.T(Book.class);

	String getIsin();
	void setIsin(String isin);

	String getTitle();
	void setTitle(String title);

	int getPages();
	void setPages(int pages);

	Library getLibrary();
	void setLibrary(Library library);

	Set<String> getCharsUsed();
	void setCharsUsed(Set<String> charsUsed);

	List<String> getWriter();
	void setWriter(List<String> writer);

	Map<String, String> getProperties();
	void setProperties(Map<String, String> properties);

}
