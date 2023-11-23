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
package tribefire.extension.audit.model.test.data;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Person extends GenericEntity {
	EntityType<Person> T = EntityTypes.T(Person.class);
	
	String name = "name";
	String lastName = "lastName";
	String birthday = "birthday";
	String partner = "partner";
	String friends = "friends";
	
	String getName();
	void setName(String name);
	
	String getLastName();
	void setLastName(String lastName);
	
	Date getBirthday();
	void setBirthday(Date birthday);
	
	Person getPartner();
	void setPartner(Person partner);
	
	List<Person> getFriends();
	void setFriends(List<Person> friends);
}
