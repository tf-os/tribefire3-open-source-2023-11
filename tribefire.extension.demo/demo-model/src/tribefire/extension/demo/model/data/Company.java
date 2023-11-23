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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Bidirectional;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * Along with other attributes a Company is basically described by it's name and
 * holds references to it's employees (instances of {@link Person}), departments
 * ({@link Department}). <br />
 * It also derives a list of Comments and a reference to {@link Address} from
 * it's super types.
 */

@SelectiveInformation("${name}")
public interface Company extends GenericEntity, HasAddress, HasComments {

	
	final EntityType<Company> T = EntityTypes.T(Company.class);
	
	/*
	 * Constants for each property name.
	 */
	public static final String name = "name";
	public static final String ceo = "ceo";
	public static final String departments = "departments";
	public static final String employees = "employees";
	public static final String averageRevenue = "averageRevenue";
	public static final String paperworkByCategory = "paperworkByCategory";

	/**
	 * The name of the company as a simple String.
	 */
	@Mandatory
	String getName();
	void setName(String name);

	/**
	 * The CEO of the company referenced by an instance of {@link Person}
	 */
	@Mandatory
	Person getCeo();
	void setCeo(Person ceo);

	/**
	 * The average revenue of the company hold by a Decimal property.
	 */
	@Initializer("1000.00b")
	BigDecimal getAverageRevenue();
	void setAverageRevenue(BigDecimal averageRevenue);

	/**
	 * The {@link Department}s of the company organized in a collection with no
	 * explicit order (Set).
	 */
	@Bidirectional(type=Department.class, property=Department.company)
	Set<Department> getDepartments();
	void setDepartments(Set<Department> departments);

	/**
	 * The Employees of the company (instances of type {@link Person}) organized
	 * in a collection with no explicit order (Set).
	 */
	Set<Person> getEmployees();
	void setEmployees(Set<Person> employees);

	/**
	 * Relevant paperwork of this company organized in a Map that categories the
	 * writings with a string key. <br />
	 * Each of the writings (e.g., the mission statement) is stored externally
	 * and represented by (as well as accessible via) a {@link Resource}.
	 */
	Map<String, Resource> getPaperworkByCategory();
	void setPaperworkByCategory(Map<String, Resource> paperworkByCategory);

}
