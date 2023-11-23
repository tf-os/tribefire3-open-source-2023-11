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
package com.braintribe.model.processing.query.smart.test.builder.special;

import com.braintribe.model.processing.query.smart.test.builder.AbstractBuilder;
import com.braintribe.model.processing.query.smart.test.builder.SmartDataBuilder;
import com.braintribe.model.processing.query.smart.test.builder.repo.RepositoryDriver;
import com.braintribe.model.processing.query.smart.test.model.accessA.special.ManualA;

/**
 * 
 */
public class ManualABuilder extends AbstractBuilder<ManualA, ManualABuilder> {

	public static ManualABuilder newInstance(SmartDataBuilder dataBuilder) {
		return new ManualABuilder(dataBuilder.repoDriver());
	}

	public ManualABuilder(RepositoryDriver repoDriver) {
		super(ManualA.class, repoDriver);
	}

	public ManualABuilder title(String value) {
		instance.setTitle(value);
		return this;
	}

	public ManualABuilder author(String value) {
		instance.setAuthor(value);
		return this;
	}

	public ManualABuilder isbn(String value) {
		instance.setIsbn(value);
		return this;
	}

	public ManualABuilder manualString(String value) {
		instance.setManualString(value);
		return this;
	}

}
