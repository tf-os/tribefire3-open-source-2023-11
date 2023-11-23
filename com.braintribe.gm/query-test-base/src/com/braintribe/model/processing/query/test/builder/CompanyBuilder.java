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
package com.braintribe.model.processing.query.test.builder;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Date;

import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Color;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.Smood;

/**
 * 
 */
public class CompanyBuilder extends AbstractBuilder<Company, CompanyBuilder> {

	public static CompanyBuilder newCompany(DataBuilder dataBuilder) {
		return new CompanyBuilder(dataBuilder.smood);
	}

	public CompanyBuilder(Smood smood) {
		super(Company.class, smood);
	}

	public CompanyBuilder name(String value) {
		instance.setName(value);
		return this;
	}

	public CompanyBuilder description(String value) {
		instance.setDescription(value);
		return this;
	}
	
	public CompanyBuilder indexedName(String value) {
		instance.setIndexedName(value);
		return this;
	}

	public CompanyBuilder date(Date value) {
		instance.setIndexedDate(value);
		return this;
	}

	public CompanyBuilder address(Address value) {
		instance.setAddress(value);
		return this;
	}

	public CompanyBuilder owner(Person person) {
		instance.setOwner(person);
		return this;
	}

	public CompanyBuilder persons(Person... persons) {
		instance.setPersons(asList(persons));
		instance.setPersonSet(asSet(persons));
		return this;
	}

	public CompanyBuilder personNames(String... names) {
		instance.setPersonNameList(asList(names));
		instance.setPersonNameSet(asSet(names));
		return this;
	}

	public CompanyBuilder colors(Color... colors) {
		instance.setColors(asSet(colors));
		return this;
	}

}
