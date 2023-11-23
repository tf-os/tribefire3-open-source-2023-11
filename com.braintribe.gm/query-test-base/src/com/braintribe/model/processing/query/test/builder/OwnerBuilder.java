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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.smood.Smood;

/**
 * 
 */
public class OwnerBuilder extends AbstractPersonBuilder<Owner, OwnerBuilder> {

	public static OwnerBuilder newOwner() {
		return new OwnerBuilder(null);
	}

	public static OwnerBuilder newOwner(DataBuilder dataBuilder) {
		return new OwnerBuilder(dataBuilder.smood);
	}

	public OwnerBuilder(Smood smood) {
		super(Owner.class, smood);
	}

	public OwnerBuilder addToCompanySet(Company... company) {
		Set<Company> cs = instance.getCompanySet();
		cs.addAll(Arrays.asList(company));

		return this;
	}

	public OwnerBuilder addToCompanyList(Company... company) {
		List<Company> cs = instance.getCompanyList();
		cs.addAll(Arrays.asList(company));

		return this;
	}

	public OwnerBuilder addToCompanyMap(String s, Company company) {
		Map<String, Company> map = instance.getCompanyMap();
		map.put(s, company);

		return this;
	}

	public OwnerBuilder addToCompanyTypeMap(String s, String type) {
		Map<String, String> map = instance.getCompanyTypeMap();
		map.put(s, type);

		return this;
	}

	public OwnerBuilder addToCompanyValueMap(Company company, Integer value) {
		Map<Company, Integer> map = instance.getCompanyValueMap();
		map.put(company, value);

		return this;
	}

}
