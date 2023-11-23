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
import java.util.Set;

import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@ToStringInformation("Person[${name}]")
public interface Person extends NamedEntity {

	EntityType<Person> T = EntityTypes.T(Person.class);

	String getIndexedName();
	void setIndexedName(String indexedName);

	String getIndexedUniqueName();
	void setIndexedUniqueName(String indexedUniqueName);

	int getIndexedInteger();
	void setIndexedInteger(int indexedInteger);

	Company getCompany();
	void setCompany(Company company);

	Company getIndexedCompany();
	void setIndexedCompany(Company indexedCompany);

	String getCompanyName();
	void setCompanyName(String companyName);

	String getPhoneNumber();
	void setPhoneNumber(String phoneNumber);

	LocalizedString getLocalizedString();
	void setLocalizedString(LocalizedString localizedString);

	int getAge();
	void setAge(int age);

	Date getBirthDate();
	void setBirthDate(Date birthDate);

	Set<String> getNicknames();
	void setNicknames(Set<String> nicknames);

	Person getIndexedFriend();
	void setIndexedFriend(Person indexedFriend);

	Color getEyeColor();
	void setEyeColor(Color eyeColor);

	Set<Address> getAddressSet();
	void setAddressSet(Set<Address> addressSet);
	
}
