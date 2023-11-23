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
package com.braintribe.model.processing.deployment.processor.bidi.data;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Person extends GenericEntity {

	EntityType<Person> T = EntityTypes.T(Person.class);

	/** 1:1 - {@link Company#getOwner()} */
	@Bidirectional(type = Company.class, property = "owner")
	Company getOwnCompany();
	void setOwnCompany(Company ownCompany);

	/** 1:n - {@link Company#getEmployeeSet()} (Set) */
	@Bidirectional(type = Company.class, property = "employeeSet")
	Company getEmployerCompany();
	void setEmployerCompany(Company employerCompany);

	/** 1:n - {@link Company#getEmployeeList()} (List) */
	@Bidirectional(type = Company.class, property = "employeeList")
	Company getEmployerCompanyList();
	void setEmployerCompanyList(Company employerCompanyList);

	/** n:n - {@link Company#getOwnersFriends()} */
	@Bidirectional(type = Company.class, property = "ownersFriends")
	Set<Company> getFriendsCompanies();
	void setFriendsCompanies(Set<Company> friendsCompanies);

	/** just identifier */
	String getName();
	void setName(String name);

}
