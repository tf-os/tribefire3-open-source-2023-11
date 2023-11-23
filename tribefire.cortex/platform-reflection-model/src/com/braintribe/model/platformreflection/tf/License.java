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
package com.braintribe.model.platformreflection.tf;

import java.util.Date;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformreflection.request.PlatformReflectionResponse;

public interface License extends PlatformReflectionResponse {

	EntityType<License> T = EntityTypes.T(License.class);

	void setLicensee(String licensee);
	String getLicensee();

	void setLicenseeAccount(String licenseeAccount);
	String getLicenseeAccount();

	void setExpiryDate(Date expiryDate);
	Date getExpiryDate();

	void setLicensor(String licensor);
	String getLicensor();

	void setActive(boolean active);
	boolean getActive();

	void setIssueDate(Date issueDate);
	Date getIssueDate();

	void setLicenseResourceId(String licenseResourceId);
	String getLicenseResourceId();

	void setUploadDate(Date uploadDate);
	Date getUploadDate();

	void setUploader(String uploader);
	String getUploader();

}
