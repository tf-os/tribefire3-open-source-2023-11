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
package com.braintribe.model.processing.query.test.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ToStringInformation("Company[${name}]")
public interface Company extends NamedEntity {

	EntityType<Company> T = EntityTypes.T(Company.class);

	String getIndexedName();
	void setIndexedName(String indexedName);

	String getDescription();
	void setDescription(String description);

	Date getIndexedDate();
	void setIndexedDate(Date indexedDate);

	Person getOwner();
	void setOwner(Person owner);

	List<Person> getPersons();
	void setPersons(List<Person> persons);

	Set<Person> getPersonSet();
	void setPersonSet(Set<Person> personSet);

	List<String> getPersonNameList();
	void setPersonNameList(List<String> personNameList);

	Set<String> getPersonNameSet();
	void setPersonNameSet(Set<String> personNameSet);

	Address getAddress();
	void setAddress(Address address);

	Set<Address> getAddressSet();
	void setAddressSet(Set<Address> addressSet);

	List<Address> getAddressList();
	void setAddressList(List<Address> addressList);

	Map<String, Address> getAddressMap();
	void setAddressMap(Map<String, Address> addressMap);

	Set<Color> getColors();
	void setColors(Set<Color> colors);

}
