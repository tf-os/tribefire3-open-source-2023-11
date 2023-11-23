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
package com.braintribe.model.manipulation.parser.impl.manipulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.manipulation.parser.impl.model.Joat;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlManipulatorParserConfiguration;
import com.braintribe.model.processing.session.api.managed.EntityManager;

/**
 * Similar to {@link LenientManipulatorTest}, but for errors that happen within the underlying {@link EntityManager}.
 * 
 * @see AbstractModifiedGmmlManipulatorTest
 * 
 * @author peter.gazdik
 */
public class LenientEntityManagerErrorTest extends AbstractModifiedGmmlManipulatorTest {

	private static final String stageName = "StageX";

	@Test
	public void duplicateGlobalId() throws Exception {
		gmmlModifier = s -> s.replaceAll("joat2", "joat1");

		recordStringifyAndApply(session -> {
			session.create(Joat.T, "joat1");
			session.create(Joat.T, "joat2");
		});

		Set<GenericEntity> entities = smood.getEntitiesPerType(GenericEntity.T);
		assertThat(entities).hasSize(2);

		assertThat(findEntity("joat1")).isNotNull();
		assertThat(findEntity("gmml://" + stageName + ".copy#1.joat1")).isNotNull();
	}

	private GenericEntity findEntity(String globalId) {
		return smood.findEntityByGlobalId(globalId);
	}

	@Override
	protected MutableGmmlManipulatorParserConfiguration parserConfig() {
		MutableGmmlManipulatorParserConfiguration result = super.parserConfig();
		result.setStageName(stageName);
		return result;
	}

}
