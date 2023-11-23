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
package com.braintribe.model.access.hibernate.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.access.hibernate.base.HibernateAccessRecyclingTestBase;
import com.braintribe.model.access.hibernate.base.wire.space.HibernateModelsSpace;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;

/**
 * Basic tests for entity that is fully woven, checking that Hibernate was given the right class-loader.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class Own_CL_HbmTest extends HibernateAccessRecyclingTestBase {

	private static final String NON_CP_INSTANCE_NAME = "NonCpEntity";

	@Override
	protected GmMetaModel model() {
		return HibernateAccessRecyclingTestBase.hibernateModels.nonClasspath();
	}

	@Test
	public void storesAndLoadsScalarEntity() throws Exception {
		prepareNonCpEntity();

		EntityType<?> et = GMF.getTypeReflection().getEntityType(HibernateModelsSpace.NON_CP_ENTITY_SIG);

		GenericEntity e = accessDriver.requireEntityByProperty(et, "name", NON_CP_INSTANCE_NAME);
		assertThat(e).isNotNull();
	}

	private void prepareNonCpEntity() {
		EntityType<?> et = GMF.getTypeReflection().getEntityType(HibernateModelsSpace.NON_CP_ENTITY_SIG);
		Property nameProperty = et.getProperty("name");

		GenericEntity nonCpEntity = session.create(et);
		nameProperty.set(nonCpEntity, NON_CP_INSTANCE_NAME);

		session.commit();

		resetGmSession();
	}
}
