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
package tribefire.extension.spreadsheet.model.test;

import java.util.Date;

import com.braintribe.model.generic.annotation.meta.TimeZoneless;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Person extends TestRecord {
	EntityType<Person> T = EntityTypes.T(Person.class);

	String firstName = "firstName";
	String lastName = "lastName";
	String birthDate = "birthDate";
	String socialContractNumber = "socialContractNumber";
	String hobby = "hobby";
	String favouriteNumber = "favouriteNumber";
	String hash = "hash";

	String getFirstName();
	void setFirstName(String firstName);

	String getLastName();
	void setLastName(String lastName);

	@TimeZoneless
	Date getBirthDate();
	void setBirthDate(Date birthDate);

	String getSocialContractNumber();
	void setSocialContractNumber(String socialContractNumber);

	String getHobby();
	void setHobby(String hobby);

	int getFavouriteNumber();
	void setFavouriteNumber(int favouriteNumber);

	String getHash();
	void setHash(String hash);
}
