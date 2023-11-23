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

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A department belongs to a {@link #getCompany() company}, has a certain {@link #getNumberOfEmployees() number of employees}, a {@link #manager} and
 * is hopefully {@link #getProfitable() profitable}.
 */
@SelectiveInformation("${name}")
public interface Department extends StandardIdentifiable {

	// Constant to conveniently access the entity type.
	EntityType<Department> T = EntityTypes.T(Department.class);

	/* Constants which provide convenient access to all property names, which is e.g. useful for queries. */
	String name = "name";
	String manager = "manager";
	String numberOfEmployees = "numberOfEmployees";
	String profitable = "profitable";
	String company = "company";

	/**
	 * The department name.
	 */
	String getName();
	void setName(String name);

	/**
	 * The manager of this department.
	 */
	Person getManager();
	void setManager(Person manager);

	/**
	 * The number of employees working in this department.
	 */
	int getNumberOfEmployees();
	void setNumberOfEmployees(int numberOfEmployees);

	/**
	 * A flag indicating whether this department is profitable.<br>
	 * The default value is <code>true</code>.
	 */
	@Initializer("true")
	boolean getProfitable();
	void setProfitable(boolean profitable);

	/**
	 * The back link to the {@link Company} this department belongs to.
	 */
	Company getCompany();
	void setCompany(Company company);
}
