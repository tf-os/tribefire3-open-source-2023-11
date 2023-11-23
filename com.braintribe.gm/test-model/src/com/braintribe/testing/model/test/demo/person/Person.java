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
package com.braintribe.testing.model.test.demo.person;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>Person</code> represents a real (human) person and therefore has a {@link #getLastName() name}, hopefully
 * some {@link #getFriends() friends}, etc.
 *
 * @author michael.lafite
 */

public interface Person extends GenericEntity {

	EntityType<Person> T = EntityTypes.T(Person.class);

	String getFirstName();
	void setFirstName(String value);

	String getLastName();
	void setLastName(String value);

	Integer getAge();
	void setAge(Integer age);

	Address getAddress();
	void setAddress(Address address);

	Set<Person> getFriends();
	void setFriends(Set<Person> friends);

	Person getMother();
	void setMother(Person person);

	Person getFather();
	void setFather(Person father);

	Set<Person> getChildren();
	void setChildren(Set<Person> children);
}
