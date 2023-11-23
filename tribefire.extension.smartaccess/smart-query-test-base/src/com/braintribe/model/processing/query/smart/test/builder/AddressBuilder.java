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
package com.braintribe.model.processing.query.smart.test.builder;

import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessA.Address;

/**
 * 
 */
public class AddressBuilder extends AbstractBuilder<Address, AddressBuilder> {

	public static AddressBuilder newInstance(SmartDataBuilder dataBuilder) {
		return new AddressBuilder(dataBuilder.repoDriver());
	}

	public AddressBuilder(RepositoryDriver repoDriver) {
		super(Address.class, repoDriver);
	}

	public AddressBuilder street(String value) {
		instance.setStreet(value);
		return this;
	}

	public AddressBuilder number(Integer value) {
		instance.setNumber(value);
		return this;
	}

	public AddressBuilder zipCode(String value) {
		instance.setZipCode(value);
		return this;
	}

	public AddressBuilder country(String value) {
		instance.setCountry(value);
		return this;
	}

}
