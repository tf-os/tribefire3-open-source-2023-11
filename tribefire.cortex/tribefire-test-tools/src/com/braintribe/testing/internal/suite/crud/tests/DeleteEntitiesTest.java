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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.product.rat.imp.impl.utils.QueryHelper;

/**
 * See {@link #run(PersistenceGmSession)}
 * @author Neidhart
 *
 */
public class DeleteEntitiesTest extends AbstractAccessCRUDTest {
	private Collection<GenericEntity> entitiesToDelete;

	public DeleteEntitiesTest(String accessId, PersistenceGmSessionFactory factory) {
		super(accessId, factory);
	}

	public Collection<GenericEntity> getEntitiesToDelete() {
		return entitiesToDelete;
	}



	public void setEntitiesToDelete(Collection<GenericEntity> entitiesToDelete) {
		this.entitiesToDelete = entitiesToDelete;
	}


	/**
	 * {@link #setEntitiesToDelete(Collection) must be called first}<br>
	 * deletes every of these entities one by one<br>
	 * Verification step: Checks if the entities really can't be found now any more by a query
	 * @return deleted entities
	 */
	@Override
	protected List<GenericEntity> run(PersistenceGmSession session) {
		if (entitiesToDelete == null)
			throw new IllegalStateException("setEntitiesToDelete() must be called first");
		
		QueryHelper queryHelper = new QueryHelper(session);
		
		for (GenericEntity entity : entitiesToDelete) {
			GenericEntity updatedEntity = session.query().entity(entity).refresh();
			session.deleteEntity(updatedEntity);
			session.commit();

//			assertThat(queryHelper.findById(GenericEntity.T, updatedEntity.getId())).as("Entity was not deleted properly").isNull();
		}
		
		return new ArrayList<>(entitiesToDelete);
	}


	@Override
	protected void verifyResult(Verificator verificator, List<GenericEntity> testResult) {
		verificator.assertEntitiesAreNotPresent(testResult);
	}
}
