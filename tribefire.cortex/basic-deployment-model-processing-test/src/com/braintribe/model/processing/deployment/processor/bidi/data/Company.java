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

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Company extends GenericEntity {

	EntityType<Company> T = EntityTypes.T(Company.class);

	/** 1:1 - {@link Person#getOwnCompany()} */
	@Bidirectional(type = Person.class, property = "ownCompany")
	Person getOwner();
	void setOwner(Person owner);

	/** n:1 - {@link Person#getEmployerCompany()} */
	@Bidirectional(type = Person.class, property = "employerCompany")
	Set<Person> getEmployeeSet();
	void setEmployeeSet(Set<Person> employeeSet);

	/** n:1 - {@link Person#getEmployerCompanyList()} */
	@Bidirectional(type = Person.class, property = "employerCompanyList")
	List<Person> getEmployeeList();
	void setEmployeeList(List<Person> employeeList);

	/** n:n - {@link Person#getFriendsCompanies()} */
	@Bidirectional(type = Person.class, property = "friendsCompanies")
	Set<Person> getOwnersFriends();
	void setOwnersFriends(Set<Person> ownersFriends);

	String getName();
	void setName(String name);

	Integer getAge();
	void setAge(Integer age);

	int getAge2();
	void setAge2(int age2);

}
