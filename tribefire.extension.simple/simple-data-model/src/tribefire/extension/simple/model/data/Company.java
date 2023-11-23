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

import java.math.BigDecimal;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A <code>Company</code> has a {@link #getName() name} and holds references to its {@link #getCeo() CEO}, {@link #getEmployees() employees} and
 * {@link #getDepartments() departments}. It also has a (main) {@link #getAddress() address}, inherited from a {@link HasAddress super type}.
 */
@SelectiveInformation("${name}")
public interface Company extends StandardIdentifiable, HasAddress {

	// Constant to conveniently access the entity type.
	EntityType<Company> T = EntityTypes.T(Company.class);

	/* Constants which provide convenient access to all property names, which is e.g. useful for queries. */
	String name = "name";
	String ceo = "ceo";
	String departments = "departments";
	String employees = "employees";
	String averageRevenue = "averageRevenue";

	/**
	 * The company name.
	 */
	String getName();
	void setName(String name);

	/**
	 * The CEO of this company.
	 */
	Person getCeo();
	void setCeo(Person ceo);

	/**
	 * The average revenue of the company.
	 */
	BigDecimal getAverageRevenue();
	void setAverageRevenue(BigDecimal averageRevenue);

	/**
	 * The set of {@link Department}s of this company.
	 */
	Set<Department> getDepartments();
	void setDepartments(Set<Department> departments);

	/**
	 * The set of {@link Person}s employed at this company.
	 */
	Set<Person> getEmployees();
	void setEmployees(Set<Person> employees);

}
