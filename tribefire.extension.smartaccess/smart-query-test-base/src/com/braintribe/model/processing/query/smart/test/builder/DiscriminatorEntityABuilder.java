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
import com.braintribe.model.processing.query.smart.test.model.accessA.discriminator.DiscriminatorEntityA;

/**
 * 
 */
public class DiscriminatorEntityABuilder extends AbstractBuilder<DiscriminatorEntityA, DiscriminatorEntityABuilder> {

	public static DiscriminatorEntityABuilder newInstance(SmartDataBuilder dataBuilder) {
		return new DiscriminatorEntityABuilder(dataBuilder.repoDriver());
	}

	public DiscriminatorEntityABuilder(RepositoryDriver repoDriver) {
		super(DiscriminatorEntityA.class, repoDriver);
	}

	public DiscriminatorEntityABuilder name(String value) {
		instance.setName(value);
		return this;
	}

	public DiscriminatorEntityABuilder type1Name(String value) {
		instance.setType1Name(value);
		return this;
	}

	public DiscriminatorEntityABuilder type2Name(String value) {
		instance.setType2Name(value);
		return this;
	}

	public DiscriminatorEntityABuilder discriminator(String value) {
		instance.setDiscriminator(value);
		return this;
	}

	public DiscriminatorEntityABuilder discriminator2(String value) {
		instance.setDiscriminator2(value);
		return this;
	}

}
