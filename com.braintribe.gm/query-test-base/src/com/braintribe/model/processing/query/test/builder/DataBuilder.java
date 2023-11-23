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

import com.braintribe.model.processing.smood.Smood;

/**
 * 
 */
public class DataBuilder {

	protected final Smood smood;

	public DataBuilder(Smood smood) {
		this.smood = smood;
	}

	public AddressBuilder address(String name) {
		return AddressBuilder.newAddress(this).name(name);
	}

	public PersonBuilder person(String name) {
		return PersonBuilder.newPerson(this).name(name);
	}

	public OwnerBuilder owner(String name) {
		return OwnerBuilder.newOwner(this).name(name);
	}

	public CompanyBuilder company(String name) {
		return CompanyBuilder.newCompany(this).name(name);
	}

}
