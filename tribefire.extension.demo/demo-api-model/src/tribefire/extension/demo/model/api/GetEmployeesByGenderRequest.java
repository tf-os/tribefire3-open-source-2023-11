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
package tribefire.extension.demo.model.api;

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;

import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Gender;

@Abstract
public interface GetEmployeesByGenderRequest extends AccessDataRequest, AuthorizedRequest {

	
	final EntityType<GetEmployeesByGenderRequest> T = EntityTypes.T(GetEmployeesByGenderRequest.class);
	
	/**
	 * The company from which the Persons should be taken 
	 */
	void setCompany(Company company);
	Company getCompany();

	@Initializer("enum(tribefire.extension.demo.model.data.Gender,female)")
	Gender getGender();
	void setGender(Gender gender);
	
}
