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
package com.braintribe.model.processing.smart.query.planner.base;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlanPrinter;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.ValueComparison;

/**
 * The access is only used for one thing in the planner - to query for id property in case smart id is not mapped to
 * delegate id. The query then looks as follows: <code>select id from DelegateEntity where uniqueProperty = ?</code>. So
 * we take the right operand of the condition and use the configured list(
 * {@link #idMapping}) to find the right value. (The list is expected to have this
 * structure: (unique1, id1, unique2, id2, ...)
 */
public class TestAccess extends AbstractAccess {

	public static List<?> idMapping;

	public TestAccess(String... partitions) {
		this.partitions = asSet(partitions);
	}

	@Override
	public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
		Restriction r = query.getRestriction();
		if (r == null) {
			throwUnexpectedQuery(query);
		}

		Condition c = r.getCondition();
		if (!(c instanceof ValueComparison)) {
			throwUnexpectedQuery(query);
		}

		ValueComparison vc = (ValueComparison) c;
		if (vc.getOperator() != Operator.equal) {
			throwUnexpectedQuery(query);
		}

		Map<Object, Object> uniqueToId = asMap(idMapping.toArray());

		Object uniqueValue = vc.getRightOperand();
		Object idValue = uniqueToId.get(uniqueValue);

		SelectQueryResult result = SelectQueryResult.T.createPlain();
		result.setHasMore(false);
		result.setResults(Arrays.asList(idValue));

		return result;
	}

	private void throwUnexpectedQuery(SelectQuery query) {
		throw new RuntimeException("Query not expected: " + SmartQueryPlanPrinter.print(query));
	}

	@Override
	public EntityQueryResult queryEntities(EntityQuery request) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'EmptyAccess.queryEntities' is not supported");
	}

	@Override
	public PropertyQueryResult queryProperty(PropertyQuery request) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'EmptyAccess.queryProperty' is not supported");
	}

	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest) throws ModelAccessException {
		throw new UnsupportedOperationException("Method 'EmptyAccess.applyManipulation' is not supported");
	}

	@Override
	public GmMetaModel getMetaModel() {
		throw new UnsupportedOperationException("Method 'EmptyAccess.getMetaModel' is not supported");
	}
}
