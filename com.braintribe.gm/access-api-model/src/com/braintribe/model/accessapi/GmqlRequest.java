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
package com.braintribe.model.accessapi;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Executes the passed GMQL statement on the access identified by domainId.")
public interface GmqlRequest extends ServiceRequest, AuthorizedRequest {

	final EntityType<GmqlRequest> T = EntityTypes.T(GmqlRequest.class);

	@Mandatory
	@Description("The id of the access which should run the GMQL statement.")
	String getAccessId();
	void setAccessId(String accessId);

	@Mandatory
	@Description("The GMQL statement to run against the access.")
	String getStatement();

	void setStatement(String statement);

	@Deprecated
	@Description("The GmqlRequest was mistakenly deriving from DomainRequest which introduced this property. "
			+ "Now this is changed and the accessId property should be used instead. "
			+ "If no accessId is given we currently still support domainId as an alternative to ensure backward compatibility.")
	String getDomainId();
	@Deprecated
	void setDomainId(String domainId);

	@Override
	EvalContext<? extends QueryResult> eval(Evaluator<ServiceRequest> evaluator);

}
