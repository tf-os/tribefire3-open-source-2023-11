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
package com.braintribe.model.license;

import java.util.Date;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

  
public interface License extends StandardStringIdentifiable {

	final EntityType<License> T = EntityTypes.T(License.class);

	public static final String systemLicenseId = "systemLicenseId";
	public static final String uploadDate = "uploadDate";
	public static final String uploader = "uploader";
	public static final String expiryDate = "expiryDate";
	public static final String licensee = "licensee";
	public static final String licensor = "licensor";
	public static final String licenseResource = "licenseResource";
	public static final String active = "active";
	public static final String issueDate = "issueDate";
	public static final String licenseeAccount = "licenseeAccount";
	
	void setSystemLicenseId(String systemLicenseId);
	String getSystemLicenseId();
	
	void setUploadDate(Date uploadDate);
	Date getUploadDate();
	
	void setUploader(String uploader);
	String getUploader();
	
	void setExpiryDate(Date expiryDate);
	Date getExpiryDate();
	
	void setLicensee(String licensee);
	String getLicensee();
	
	void setLicensor(String licensor);
	String getLicensor();
	
	void setLicenseResource(Resource licenseResource);
	Resource getLicenseResource();
	
	void setActive(boolean active);
	boolean getActive();
	
	void setIssueDate(Date issueDate);
	Date getIssueDate();

	void setLicenseeAccount(String licenseeAccount);
	String getLicenseeAccount();

}
