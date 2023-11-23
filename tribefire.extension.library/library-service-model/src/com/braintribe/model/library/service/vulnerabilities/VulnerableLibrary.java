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
package com.braintribe.model.library.service.vulnerabilities;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface VulnerableLibrary extends StandardIdentifiable {

	final EntityType<VulnerableLibrary> T = EntityTypes.T(VulnerableLibrary.class);

	public final static String libraryId = "libraryId";
	public final static String name = "name";
	public final static String url = "url";
	public final static String severity = "severity";
	public final static String description = "description";
	public final static String cvssScore = "cvssScore";


	void setLibraryId(String libraryId);
	String getLibraryId();
	
	void setName(String name);
	String getName();

	void setUrl(String url);
	String getUrl();

	void setSeverity(Severity severity);
	Severity getSeverity();
	
	void setDescription(String description);
	String getDescription();
	
	void setCvssScore(float cvssScore);
	float getCvssScore();
	
	
}
