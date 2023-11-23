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
package com.braintribe.model.openapi.v3_0;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.openapi.v3_0.meta.OpenapiContact;
import com.braintribe.model.openapi.v3_0.meta.OpenapiLicense;

/**
 * General metadata for the whole OpenAPI document
 *
 * See https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#infoObject
 */
public interface OpenapiInfo extends GenericEntity {

	EntityType<OpenapiInfo> T = EntityTypes.T(OpenapiInfo.class);

	@Mandatory
	String getTitle();
	void setTitle(String title);

	@Mandatory
	String getVersion();
	void setVersion(String version);

	String getDescription();
	void setDescription(String description);

	String getTermsOfService();
	void setTermsOfService(String termsOfService);

	OpenapiContact getContact();
	void setContact(OpenapiContact contact);

	OpenapiLicense getLicense();
	void setLicense(OpenapiLicense license);

}
