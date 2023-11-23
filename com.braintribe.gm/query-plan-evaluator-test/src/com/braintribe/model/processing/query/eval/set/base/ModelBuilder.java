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
package com.braintribe.model.processing.query.eval.set.base;

import java.util.Date;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;

/**
 * 
 */
public class ModelBuilder {

	private static final long MILLISECOND = 1;
	private static final long SECOND = 1000 * MILLISECOND;
	private static final long HOUR = 60 * 60 * SECOND;
	private static final long DAY = 24 * HOUR;
	private static final long YEAR = 365 * DAY;

	public static Owner owner(String name) {
		Owner result = instantiate(Owner.class);
		result.setName(name);

		return result;
	}

	public static Person person(String name) {
		return person(name, null, 0);
	}

	public static Person person(String name, String company, int age) {
		return fillPerson(instantiate(Person.class), name, company, age, new Date(System.currentTimeMillis() - age * YEAR));
	}

	public static Person person(String name, String company, int age, Date birthDate) {
		return fillPerson(instantiate(Person.class), name, company, age, birthDate);
	}

	public static Person fillPerson(Person result, String name) {
		return fillPerson(result, name, null, 0, null);
	}

	public static Person fillPerson(Person result, String name, String company, int age, Date birthDate) {
		result.setName(name);
		result.setIndexedName(name);
		result.setCompanyName(company);
		result.setAge(age);
		result.setBirthDate(birthDate);

		return result;
	}

	public static Company company(String name) {
		Company result = instantiate(Company.class);
		result.setName(name);
		result.setIndexedName(name);

		return result;
	}

	public static Address address(String street) {
		Address result = instantiate(Address.class);
		result.setStreet(street);

		return result;
	}

	protected static <T extends GenericEntity> T instantiate(Class<T> beanClass) {
		return typeReflection().<T> getEntityType(beanClass).create();
	}

	private static GenericModelTypeReflection typeReflection() {
		return GMF.getTypeReflection();
	}
}
