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
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;

/**
 * 
 */
public class CompositeKpaEntityABuilder extends AbstractBuilder<CompositeKpaEntityA, CompositeKpaEntityABuilder> {

	public static CompositeKpaEntityABuilder newInstance(SmartDataBuilder dataBuilder) {
		return new CompositeKpaEntityABuilder(dataBuilder.repoDriver());
	}

	public CompositeKpaEntityABuilder(RepositoryDriver repoDriver) {
		super(CompositeKpaEntityA.class, repoDriver);
	}

	public CompositeKpaEntityABuilder personData(PersonA personA) {
		instance.setPersonId(personA.getCompositeId());
		instance.setPersonName(personA.getCompositeName());
		instance.setPersonCompanyName(personA.getCompositeCompanyName());

		return this;
	}

	public CompositeKpaEntityABuilder personId(Long value) {
		instance.setPersonId(value);
		return this;
	}

	public CompositeKpaEntityABuilder personName(String value) {
		instance.setPersonName(value);
		return this;
	}

	public CompositeKpaEntityABuilder personCompanyName(String value) {
		instance.setPersonCompanyName(value);
		return this;
	}

	public CompositeKpaEntityABuilder description(String value) {
		instance.setDescription(value);
		return this;
	}

}
