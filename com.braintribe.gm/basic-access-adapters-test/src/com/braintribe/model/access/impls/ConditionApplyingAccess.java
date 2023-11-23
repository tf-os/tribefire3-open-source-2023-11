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
package com.braintribe.model.access.impls;

import java.util.List;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.Condition;

/**
 * 
 */
public class ConditionApplyingAccess extends AbstractTestAccess {

	private final Smood dataSource;

	public ConditionApplyingAccess(Smood dataSource, boolean idIsIndexed) {
		this.dataSource = dataSource;
		this.repository.setAssumeIdIsIndexed(idIsIndexed);
	}

	@Override
	protected List<GenericEntity> queryPopulation(String typeSignature, Condition condition) throws ModelAccessException {
		EntityQuery query = EntityQueryBuilder.from(typeSignature).done();
		query.setRestriction(restrictionFor(condition));

		return dataSource.queryEntities(query).getEntities();
	}

	private Restriction restrictionFor(Condition condition) {
		if (condition == null) {
			return null;
		}

		Restriction restriction = Restriction.T.create();
		restriction.setCondition(condition);

		return restriction;
	}

	@Override
	protected GenericEntity getEntity(EntityReference entityReference) throws ModelAccessException {
		return dataSource.getEntity(entityReference);
	}

}
