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

import java.util.List;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.panther.SourceArtifact;

@SelectiveInformation("${artifactId}#${version} (${groupId})")
public interface Library extends SourceArtifact {

	final EntityType<Library> T = EntityTypes.T(Library.class);

	public final static String name = "name";
	public final static String organization = "organization";
	public final static String organizationUrl = "organizationUrl";
	public final static String libraryUrl = "libraryUrl";
	public final static String licenses = "licenses";
	public final static String copyright = "copyright";
	public final static String distributionType = "distributionType";
	public final static String spdxLicenseId = "spdxLicenseId";
	public final static String spdxLicenseExpression = "spdxLicenseExpression";
	public final static String sha1 = "sha1";
	public final static String sha256 = "sha256";
	public final static String filename = "filename";

	String getName();
	void setName(String name);

	String getOrganization();
	void setOrganization(String organization);

	String getOrganizationUrl();
	void setOrganizationUrl(String organizationUrl);

	String getLibraryUrl();
	void setLibraryUrl(String libraryUrl);

	List<DistributionLicense> getLicenses();
	void setLicenses(List<DistributionLicense> licenses);

	String getCopyright();
	void setCopyright(String copyright);

	DistributionType getDistributionType();
	void setDistributionType(DistributionType distributionType);

	String getSpdxLicenseId();
	void setSpdxLicenseId(String spdxLicenseId);

	String getSpdxLicenseExpression();
	void setSpdxLicenseExpression(String spdxLicenseExpression);

	String getSha1();
	void setSha1(String sha1);

	String getSha256();
	void setSha256(String sha256);

	String getFilename();
	void setFilename(String filename);
}
