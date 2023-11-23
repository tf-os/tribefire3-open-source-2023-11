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

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.library.service.LibraryBaseResult;
import com.braintribe.model.resource.Resource;

public interface Vulnerabilities extends LibraryBaseResult {

	final EntityType<Vulnerabilities> T = EntityTypes.T(Vulnerabilities.class);

	public final static String cleanLibraries = "cleanLibraries";
	public final static String missingLibraries = "missingLibraries";
	public final static String librariesNotChecked = "librariesNotChecked";
	public final static String vulnerableLibraries = "vulnerableLibraries";
	public final static String report = "report";

	
	void setCleanLibraries(List<String> cleanLibraries);
	List<String> getCleanLibraries();

	void setMissingLibraries(List<String> missingLibraries);
	List<String> getMissingLibraries();

	void setLibrariesNotChecked(List<String> librariesNotChecked);
	List<String> getLibrariesNotChecked();

	void setVulnerableLibraries(List<VulnerableLibrary> vulnerableLibraries);
	List<VulnerableLibrary> getVulnerableLibraries();
	
	void setReport(Resource report);
	Resource getReport();


}
