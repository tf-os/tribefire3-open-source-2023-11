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
package com.braintribe.model.processing.cmd.test.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Person extends GenericEntity {

	EntityType<Person> T = EntityTypes.T(Person.class);

	long getLongValue();
	void setLongValue(long longValue);

	String getName();
	void setName(String name);

	int getAge();
	void setAge(int age);

	Person getFriend();
	void setFriend(Person friend);

	List<Person> getFriends();
	void setFriends(List<Person> friends);

	Color getColor();
	void setColor(Color color);

	boolean getIsAlive();
	void setIsAlive(boolean b);

	Date getBirthDate();
	void setBirthDate(Date d);

	/* This property will be removed from properties of the sub-type (Teacher), and the MD still should be resolved */
	long getNotRepeatedProperty();
	void setNotRepeatedProperty(long b);
	
	
	Set<Person> getOtherFriends();
	void setOtherFriends(Set<Person> otherFriends);
	
	Map<String,String> getProperties();
	void setProperties(Map<String,String> properties);

}
