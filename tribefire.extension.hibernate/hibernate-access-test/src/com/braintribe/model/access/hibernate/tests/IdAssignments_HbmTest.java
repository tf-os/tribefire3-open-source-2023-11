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
import com.braintribe.model.access.hibernate.base.model.simple.BasicScalarEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;

/**
 * Various test for setting the id property.
 * 
 * @see HibernateAccessRecyclingTestBase
 * 
 * @author peter.gazdik
 */
public class IdAssignments_HbmTest extends HibernateAccessRecyclingTestBase {

	@Override
	protected GmMetaModel model() {
		return HibernateAccessRecyclingTestBase.hibernateModels.basic_NoPartition();
	}

	/**
	 * If {@link Normalizer} isn't used in applyManipulation this causes an NPE when computing a hash-code of a reference created internally (because
	 * the code assumes such assignments are stripped by the normalizer).
	 */
	@Test
	public void settingIdToNull() throws Exception {
		BasicScalarEntity bse = session.create(BasicScalarEntity.T);
		bse.setId(null);
		bse.setName("BSE 1");

		session.commit();

		resetGmSession();

		bse = accessDriver.requireEntityByProperty(BasicScalarEntity.T, BasicScalarEntity.name, "BSE 1");
		assertThat(bse.<Object> getId()).isNotNull();
	}

}
