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
package tribefire.extension.demo.model.data;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;

/**
 * This type holds informations about a natural person that is identified by its social security number along with it's
 * first name, last name and birthday. <br />
 * Furthermore it holds information about the gender and the relationship to other persons (mother, father, children)
 * and the location ({@link Address}) of this person.
 */
@ToStringInformation("${_type}(${id}, ${firstName}, ${lastName})")
@SelectiveInformation("${firstName} ${lastName}")
public interface Person extends GenericEntity, HasAddress, HasComments {

	EntityType<Person> T = EntityTypes.T(Person.class);

	/* Constants for each property name. */
	String ssn = "ssn";
	String firstName = "firstName";
	String lastName = "lastName";
	String birthday = "birthday";
	String gender = "gender";
	String picture = "picture";
	String mother = "mother";
	String father = "father";
	String children = "children";
	String anything = "anything";

	/** The unique social security number of this person as string. */
	@Unique
	String getSsn();
	void setSsn(String ssn);

	/**
	 * The first name of this person as string. <br>
	 * Description and name metadata are added to this property via {@link Description} and {@link Name} annotations.
	 */
	@Mandatory
	@Description("This is the first name of the person")
	@Description(value = "Dies ist der Vorname der Person", locale = "de")
	@Name("First Name")
	@Name(value = "Vorname", locale = "de")
	String getFirstName();
	void setFirstName(String firstName);

	/**
	 * The last name of this person as string. <br>
	 * Description and name metadata are added to this property via {@link Description} and {@link Name} annotations.
	 */
	@Mandatory
	@Description("This is the last name of the person")
	@Description(value = "Dies ist der Nachname der Person", locale = "de")
	@Name("Last Name")
	@Name(value = "Nachname", locale = "de")
	String getLastName();
	void setLastName(String lastName);

	/** The birthday of this person as a {@link Date} */
	Date getBirthday();
	void setBirthday(Date birthday);

	/** The gender of this person defined by {@link Gender} enum. */
	// @Initializer("enum(tribefire.extension.demo.model.data.Gender,female)")
	Gender getGender();
	void setGender(Gender gender);

	/** An external picture (or pictures) represented by an {@link Icon} entity which holds {@link Resource}(s). */
	Icon getPicture();
	void setPicture(Icon picture);

	/** The mother of this person. */
	Person getMother();
	void setMother(Person mother);

	/** The father of this person. */
	Person getFather();
	void setFather(Person father);

	/** The children of this person organized in a natural ordered collection (List). */
	List<Person> getChildren();
	void setChildren(List<Person> children);

	/** A property that can store any information. This is just used to demonstrate the usage of an Object property. */
	Object getAnything();
	void setAnything(Object anything);

}
