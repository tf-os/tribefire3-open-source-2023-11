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
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeIkpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;

/**
 * 
 */
public class CompositeIkpaEntityABuilder extends AbstractBuilder<CompositeIkpaEntityA, CompositeIkpaEntityABuilder> {

	public static CompositeIkpaEntityABuilder newInstance(SmartDataBuilder dataBuilder) {
		return new CompositeIkpaEntityABuilder(dataBuilder.repoDriver());
	}

	public CompositeIkpaEntityABuilder(RepositoryDriver repoDriver) {
		super(CompositeIkpaEntityA.class, repoDriver);
	}

	public CompositeIkpaEntityABuilder personData(PersonA personA) {
		instance.setPersonId(personA.getId());
		instance.setPersonName(personA.getNameA());

		return this;
	}

	public CompositeIkpaEntityABuilder personData_Set(PersonA personA) {
		instance.setPersonId_Set(personA.getId());
		instance.setPersonName_Set(personA.getNameA());

		return this;
	}

	public CompositeIkpaEntityABuilder personId(Long value) {
		instance.setPersonId(value);
		return this;
	}

	public CompositeIkpaEntityABuilder personName(String value) {
		instance.setPersonName(value);
		return this;
	}

	public CompositeIkpaEntityABuilder description(String value) {
		instance.setDescription(value);
		return this;
	}

}
