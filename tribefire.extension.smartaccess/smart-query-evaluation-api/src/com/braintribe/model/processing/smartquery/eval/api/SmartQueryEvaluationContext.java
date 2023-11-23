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
package com.braintribe.model.processing.smartquery.eval.api;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.smartqueryplan.functions.AssembleEntity;

/**
 * 
 */
public interface SmartQueryEvaluationContext extends QueryEvaluationContext {

	SelectQueryResult runQuery(IncrementalAccess access, SelectQuery query);

	AssembleEntityContext acquireAssembleEntityContext(AssembleEntity assembleEntityFunction);

	GenericEntity findEntity(String typeSignature, Object id, String partition);

	GenericEntity instantiate(String typeSignature);

	PersistenceGmSession getSession();

}
