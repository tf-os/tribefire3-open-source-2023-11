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
package com.braintribe.model.library.service.license;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.library.service.LibraryBaseResult;

public interface SpreadsheetImportReport extends LibraryBaseResult {

	final EntityType<SpreadsheetImportReport> T = EntityTypes.T(SpreadsheetImportReport.class);

	public final static String librariesImported = "librariesImported";
	public final static String librariesNotImported = "librariesNotImported";
	public final static String librariesAlreadyImported = "librariesAlreadyImported";
	public final static String licensesFound = "licensesFound";
	public final static String licensesMissing = "licensesMissing";

	void setLibrariesImported(List<String> librariesImported);
	List<String> getLibrariesImported();

	void setLibrariesNotImported(List<String> librariesNotImported);
	List<String> getLibrariesNotImported();

	void setLibrariesAlreadyImported(List<String> librariesAlreadyImported);
	List<String> getLibrariesAlreadyImported();

	void setLicensesFound(List<String> licensesFound);
	List<String> getLicensesFound();

	void setLicensesMissing(List<String> licensesMissing);
	List<String> getLicensesMissing();

}
