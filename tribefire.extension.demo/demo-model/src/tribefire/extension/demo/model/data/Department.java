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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This type holds informations about a particular department of a
 * {@link Company}
 */

@SelectiveInformation("${name}")
public interface Department extends GenericEntity, HasComments {

	
	final EntityType<Department> T = EntityTypes.T(Department.class);
	
	/*
	 * Constants for each property name.
	 */
	public static final String name = "name";
	public static final String manager = "manager";
	public static final String numberOfEmployees = "numberOfEmployees";
	public static final String profitable = "profitable";
	public static final String company = "company";

	/**
	 * The name of this department as string.
	 */
	@Mandatory
	String getName();
	void setName(String name);

	/**
	 * A reference to the manager (natural {@link Person}).
	 */
	Person getManager();
	void setManager(Person manager);

	/**
	 * The number of employees working in this department as integer.
	 */
	@Initializer("0")
	int getNumberOfEmployees();
	void setNumberOfEmployees(int numberOfEmployees);

	/**
	 * A boolean flag indicating whether this department is profitable.<br />
	 * The field is initialized with true when an instance is created.
	 */
	@Initializer("true")
	boolean getProfitable();
	void setProfitable(boolean profitable);

	/**
	 * The back link to the company this department belongs to.
	 */
	@Bidirectional(type=Company.class, property=Company.departments)
	Company getCompany();
	void setCompany(Company company);

}
