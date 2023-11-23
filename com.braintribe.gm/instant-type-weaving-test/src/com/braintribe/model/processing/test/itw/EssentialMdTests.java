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
package com.braintribe.model.processing.test.itw;

import static com.braintribe.model.processing.test.itw.entity.EssentialMdEntity.confidential;
import static com.braintribe.model.processing.test.itw.entity.EssentialMdEntity.regularProperty;
import static com.braintribe.model.processing.test.itw.entity.EssentialMdSiblingEntity.siblingConfidential;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.entity.EssentialMdEntity;
import com.braintribe.model.processing.test.itw.entity.EssentialMdSiblingEntity;
import com.braintribe.model.processing.test.itw.entity.EssentialMdSubEntity;

/**
 * Tests for essential {@link MetaData} which are also reflected by GM reflection. Currently only {@link Confidential} is supported.
 */
public class EssentialMdTests extends ImportantItwTestSuperType {

	@Test
	public void propertiesWithEssentialMDs() {
		MetaData.T.isAbstract();

		assertThat(EssentialMdEntity.T.getProperty(regularProperty).isConfidential()).isFalse();

		assertThat(EssentialMdEntity.T.getProperty(confidential).isConfidential()).isTrue();
		assertThat(EssentialMdSubEntity.T.getProperty(confidential).isConfidential()).isTrue();

		assertThat(EssentialMdSiblingEntity.T.getProperty(siblingConfidential).isConfidential()).isTrue();
		assertThat(EssentialMdSubEntity.T.getProperty(siblingConfidential).isConfidential()).isTrue();
	}

	@Test
	public void confidentialNotVisibleInToString() {
		EssentialMdEntity entity = EssentialMdEntity.T.create();
		entity.setConfidential("abcXYZ");

		assertThat(entity.toString()).doesNotContain(entity.getConfidential());

		// sub-type with initializer

		EssentialMdSubEntity subEntity = EssentialMdSubEntity.T.create();

		assertThat(subEntity.getConfidential()).isNotEmpty();
		assertThat(subEntity.toString()).doesNotContain(subEntity.getConfidential());
	}

}
