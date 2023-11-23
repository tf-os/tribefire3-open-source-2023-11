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
package com.braintribe.model.processing.query.smart.test.builder.constant;

import com.braintribe.model.processing.query.smart.test.builder.AbstractBuilder;
import com.braintribe.model.processing.query.smart.test.builder.SmartDataBuilder;
import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessA.constant.ConstantPropEntityA2;

/**
 * 
 */
public class ConstantPropEntityA2Builder extends AbstractBuilder<ConstantPropEntityA2, ConstantPropEntityA2Builder> {

	public static ConstantPropEntityA2Builder newInstance(SmartDataBuilder dataBuilder) {
		return new ConstantPropEntityA2Builder(dataBuilder.repoDriver());
	}

	public ConstantPropEntityA2Builder(RepositoryDriver repoDriver) {
		super(ConstantPropEntityA2.class, repoDriver);
	}

	public ConstantPropEntityA2Builder name(String value) {
		instance.setName(value);
		return self;
	}

}
