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
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;

/**
 * 
 */
public class CompanyABuilder extends AbstractBuilder<CompanyA, CompanyABuilder> {

	public static CompanyABuilder newInstance(SmartDataBuilder dataBuilder) {
		return new CompanyABuilder(dataBuilder.repoDriver());
	}

	public CompanyABuilder(RepositoryDriver repoDriver) {
		super(CompanyA.class, repoDriver);
	}

	public CompanyABuilder nameA(String value) {
		instance.setNameA(value);
		return this;
	}

	public CompanyABuilder owner(PersonA value) {
		instance.setOwnerA(value);
		return this;
	}

	public CompanyABuilder ownerIdA(Long id) {
		instance.setOwnerIdA(id);
		return this;
	}

}
