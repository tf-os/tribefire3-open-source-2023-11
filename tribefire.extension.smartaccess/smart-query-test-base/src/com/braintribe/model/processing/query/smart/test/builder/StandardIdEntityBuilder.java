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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;

/**
 * 
 */
public class StandardIdEntityBuilder extends AbstractBuilder<StandardIdEntity, StandardIdEntityBuilder> {

	public static StandardIdEntityBuilder newInstance(SmartDataBuilder dataBuilder) {
		return new StandardIdEntityBuilder(dataBuilder.repoDriver());
	}

	public StandardIdEntityBuilder(RepositoryDriver repoDriver) {
		super(StandardIdEntity.class, repoDriver);
	}

	public StandardIdEntityBuilder name(String value) {
		instance.setName(value);
		return this;
	}

	public StandardIdEntityBuilder id(long value) {
		instance.setId(value);
		return this;
	}

	public StandardIdEntityBuilder parent(StandardIdEntity value) {
		instance.setParent(value);
		return this;
	}

	public StandardIdEntityBuilder children(StandardIdEntity... values) {
		instance.setChildren(asSet(values));
		return this;
	}

}
