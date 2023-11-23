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
package com.braintribe.testing.internal.suite.crud.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;

public class StandardTraversingCriteriaTest extends AbstractAccessCRUDTest {
	private static Logger logger = Logger.getLogger(StandardTraversingCriteriaTest.class);

	public StandardTraversingCriteriaTest(String accessId, PersistenceGmSessionFactory factory) {
		super(accessId, factory);
	}

	@Override
	protected List<GenericEntity> run(PersistenceGmSession session) {
		QueryHelper queryHelper = new QueryHelper(session);
		return queryHelper.allPersistedEntities();
	}

	@Override
	protected void verifyResult(Verificator verificator, List<GenericEntity> testResult) {
		if (testResult.isEmpty())
			logger.warn("Could not find any entities - this makes the result of this test meaningless");
		
		testResult.forEach(entity -> assertAbsenceInformationSetOnAllComplexProperties(entity, verificator));
	}
	
	private void assertAbsenceInformationSetOnAllComplexProperties(GenericEntity entity, Verificator verificator) {
		for (Property property : verificator.nonFilteredPropertiesOf(entity)) {
			if (property.getType().isScalar() || property.isIdentifying())
				assertThat(property.isAbsent(entity))
					.as("Expected property to be not absent but it was: " + entity.type().getTypeName() + "." + property.getName() + ": " + property)
					.isFalse();
			else
				assertThat(property.isAbsent(entity))
					.as("Expected property to be absent but it wasn't: " + entity.type().getTypeName() + "." + property.getName() + ": " + property)
					.isTrue();
		}
	}
}
