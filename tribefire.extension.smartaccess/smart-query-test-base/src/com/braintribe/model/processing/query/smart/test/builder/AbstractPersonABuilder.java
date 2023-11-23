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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessA.CarA;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompanyA;
import com.braintribe.model.processing.query.smart.test.model.accessA.Id2UniqueEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;

/**
 * 
 */
public class AbstractPersonABuilder<P extends PersonA, T extends AbstractPersonABuilder<P, T>> extends AbstractBuilder<P, T> {

	public AbstractPersonABuilder(Class<P> clazz, RepositoryDriver repoDriver) {
		super(clazz, repoDriver);
	}

	public T nameA(String value) {
		instance.setNameA(value);
		instance.setNickNameX(value);
		return self;
	}

	public T parentB(String value) {
		instance.setParentB(value);
		return self;
	}

	public T companyA(CompanyA value) {
		instance.setCompanyA(value);
		return self;
	}

	public T companyNameA(String value) {
		instance.setCompanyNameA(value);
		return self;
	}

	public T compositeId(Long value) {
		instance.setCompositeId(value);
		return self;
	}

	public T compositeName(String value) {
		instance.setCompositeName(value);
		return self;
	}

	public T compositeCompanyName(String value) {
		instance.setCompositeCompanyName(value);
		return self;
	}

	public T nickNamesA(String... values) {
		instance.setNickNamesListA(asList(values));
		instance.setNickNamesSetA(asSet(values));

		Map<Integer, String> map = newMap();
		int counter = 0;
		for (String s: values)
			map.put(2 * (++counter), s);

		instance.setNickNamesMapA(map);

		return self;
	}

	public T companies(CompanyA... values) {
		instance.setCompanyListA(asList(values));
		instance.setCompanySetA(asSet(values));

		return self;
	}

	public T addCompanyOwner(CompanyA key, PersonA value) {
		instance.getCompanyOwnerA().put(key, value);

		return self;
	}

	public T addCarAlias(CarA key, String value) {
		instance.getCarAliasA().put(key, value);

		return self;
	}

	public T companyNames(String... values) {
		instance.setCompanyNameListA(asList(values));
		instance.setCompanyNameSetA(asSet(values));

		return self;
	}

	public T addFriendCompanyName(String key, CompanyA value) {
		instance.getKeyFriendEmployerNameA().put(key, value.getNameA());

		return self;
	}

	public T id2UniqueEntityA(Id2UniqueEntityA value) {
		instance.setId2UniqueEntityA(value);

		return self;
	}

	public T id2UniqueEntityAs(Id2UniqueEntityA... values) {
		instance.setId2UniqueEntitySetA(asSet(values));

		return self;
	}
}
