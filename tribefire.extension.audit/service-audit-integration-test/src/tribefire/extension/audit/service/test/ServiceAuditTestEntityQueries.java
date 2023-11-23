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
package tribefire.extension.audit.service.test;
import java.util.Date;

import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.conditions.Condition;

import tribefire.extension.audit.model.ServiceAuditRecord;

public class ServiceAuditTestEntityQueries extends EntityQueries {
	
	public static EntityQuery latestAuditRecord(Date after, Date before) {
		PropertyOperand DATE = property(ServiceAuditRecord.date);
		
		Condition condition = and( //
				gt(DATE, after), //
				lt(DATE, before)
				); 
		
		return from(ServiceAuditRecord.T).where(condition).orderBy(OrderingDirection.descending, DATE).limit(1);
	}
	
}

