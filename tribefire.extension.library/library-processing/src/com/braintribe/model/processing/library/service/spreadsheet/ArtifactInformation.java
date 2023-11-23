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
package com.braintribe.model.processing.library.service.spreadsheet;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.library.DistributionLicense;
import com.braintribe.utils.StringTools;

public class ArtifactInformation {
	private String artifact;
	private String groupId;
	private String artifactId;
	private String version;
	private List<String> licenseNames = new ArrayList<>();
	private List<DistributionLicense> licenses = new ArrayList<>();
	private String libraryName;
	private String libraryUrl;
	private String copyright;
	private String organization;
	private String spdxLicenseId;
	private String spdxLicenseExpression;
	private String sha1;
	private String sha256;
	private String filename;

	public boolean isValid() {
		if (isValid(artifact) && isValid(copyright) && isValid(organization) && isValid(libraryName) && isValid(libraryUrl) && isValid(groupId)
				&& isValid(artifactId) && isValid(version) && !licenseNames.isEmpty() && isValid(spdxLicenseId) && isValid(spdxLicenseExpression)
				&& isValid(sha1) && isValid(sha256) && isValid(filename)) {
			return true;
		}
		return false;
	}

	private boolean isValid(String value) {
		if (StringTools.isEmpty(value)) {
			return false;
		}
		value = value.trim();
		if (value.equals("") || value.equalsIgnoreCase("x")) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(artifact + "/" + licenseNames + "/" + organization);
		return sb.toString();
	}

	public String getArtifact() {
		return artifact;
	}
	public void setArtifact(String artifact) {
		this.artifact = artifact;
		this.groupId = StringTools.getSubstringBefore(artifact, ":");
		this.artifactId = StringTools.getSubstringBetween(artifact, ":", "#");
		this.version = StringTools.getSubstringAfter(artifact, "#");
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public List<String> getLicenseNames() {
		return licenseNames;
	}
	public void setLicenseName(String licenseName) {
		if (!StringTools.isEmpty(licenseName)) {
			String[] splitCharSeparatedString = StringTools.splitCharSeparatedString(licenseName, ';', '\"', true);
			if (splitCharSeparatedString != null) {
				for (String s : splitCharSeparatedString) {
					if (!StringTools.isEmpty(s)) {
						licenseNames.add(s);
					}
				}
			}
		}
	}
	public List<DistributionLicense> getLicenses() {
		return licenses;
	}
	public void addLicense(DistributionLicense license) {
		this.licenses.add(license);
	}
	public String getLibraryName() {
		return libraryName;
	}
	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}
	public String getLibraryUrl() {
		return libraryUrl;
	}
	public void setLibraryUrl(String libraryUrl) {
		this.libraryUrl = libraryUrl;
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getSpdxLicenseId() {
		return spdxLicenseId;
	}
	public void setSpdxLicenseId(String spdxLicenseId) {
		this.spdxLicenseId = spdxLicenseId;
	}
	public String getSpdxLicenseExpression() {
		return spdxLicenseExpression;
	}
	public void setSpdxLicenseExpression(String spdxLicenseExpression) {
		this.spdxLicenseExpression = spdxLicenseExpression;
	}
	public String getSha1() {
		return sha1;
	}
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	public String getSha256() {
		return sha256;
	}
	public void setSha256(String sha256) {
		this.sha256 = sha256;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
}
