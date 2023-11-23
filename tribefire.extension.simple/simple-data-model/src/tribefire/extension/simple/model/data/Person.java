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
package tribefire.extension.simple.model.data;

import java.util.Date;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;

/**
 * A <code>Person</code> represents a natural person and holds information such as {@link #getFirstName() first} and {@link #getLastName() last} name,
 * {@link #getSsn() social security number}, {@link #getDateOfBirth() date of birth} and {@link #getGender() gender}. Furthermore a person has
 * relationships to other persons, i.e. {@link #getMother() mother}, {@link #getFather() father} and {@link #getChildren() children}. Similar to a
 * {@link Company#getAddress() company} a person also has an {@link #getAddress() address}.
 */
@ToStringInformation("${_type}(${id}, ${firstName}, ${lastName})")
@SelectiveInformation("${firstName} ${lastName}")
public interface Person extends StandardIdentifiable, HasAddress {

	// Constant to conveniently access the entity type.
	EntityType<Person> T = EntityTypes.T(Person.class);

	/* Constants which provide convenient access to all property names, which is e.g. useful for queries. */
	String ssn = "ssn";
	String firstName = "firstName";
	String lastName = "lastName";
	String dateOfBirth = "dateOfBirth";
	String gender = "gender";
	String picture = "picture";
	String mother = "mother";
	String father = "father";
	String children = "children";

	/**
	 * The unique social security number.
	 */
	String getSsn();
	void setSsn(String ssn);

	/**
	 * The first name.
	 */
	String getFirstName();
	void setFirstName(String firstName);

	/**
	 * The last name.
	 */
	String getLastName();
	void setLastName(String lastName);

	/**
	 * The date of birth.
	 */
	Date getDateOfBirth();
	void setDateOfBirth(Date dateOfBirth);

	/**
	 * The gender defined as {@link Gender} <code>enum</code>.
	 */
	Gender getGender();
	void setGender(Gender gender);

	/**
	 * A picture represented by an {@link Icon} entity which holds an image {@link Resource}.
	 */
	Icon getPicture();
	void setPicture(Icon picture);

	/**
	 * The mother of this person.
	 */
	Person getMother();
	void setMother(Person mother);

	/**
	 * The father of this person.
	 */
	Person getFather();
	void setFather(Person father);

	/**
	 * The children of this person.
	 */
	Set<Person> getChildren();
	void setChildren(Set<Person> children);

}
