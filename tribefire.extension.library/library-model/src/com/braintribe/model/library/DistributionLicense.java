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
package com.braintribe.model.library;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

@SelectiveInformation("${name}")
public interface DistributionLicense extends StandardStringIdentifiable {

	final EntityType<DistributionLicense> T = EntityTypes.T(DistributionLicense.class);

	public final static String name = "name";
	public final static String url = "url";
	public final static String licenseFile = "licenseFile";
	public final static String licenseFilePdf = "licenseFilePdf";
	public final static String commercial = "commercial";
	public final static String internalDocumentationUrl = "internalDocumentationUrl";
	public final static String spdxLicenseId = "spdxLicenseId";
	public final static String spdxListedLicense = "spdxListedLicense";

	String getName();
	void setName(String name);

	String getUrl();
	void setUrl(String url);

	Resource getLicenseFile();
	void setLicenseFile(Resource licenseFile);

	Resource getLicenseFilePdf();
	void setLicenseFilePdf(Resource licenseFilePdf);

	boolean getCommercial();
	void setCommercial(boolean commercial);

	String getInternalDocumentationUrl();
	void setInternalDocumentationUrl(String internalDocumentationUrl);

	String getSpdxLicenseId();
	void setSpdxLicenseId(String spdxLicenseId);

	boolean getSpdxListedLicense();
	void setSpdxListedLicense(boolean spdxListedLicense);
}
