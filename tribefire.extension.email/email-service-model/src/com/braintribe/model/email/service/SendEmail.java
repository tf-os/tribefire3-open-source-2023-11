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
package com.braintribe.model.email.service;

import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.model.email.data.Email;
import com.braintribe.model.email.service.reason.MailServerError;
import com.braintribe.model.email.service.reason.PrepareOutgoingMailError;
import com.braintribe.model.email.service.reason.SendConnectorMissing;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.UnsatisfiedBy;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@UnsatisfiedBy(SendConnectorMissing.class)
@UnsatisfiedBy(PrepareOutgoingMailError.class)
@UnsatisfiedBy(MailServerError.class)
@UnsatisfiedBy(UnsupportedOperation.class)
public interface SendEmail extends EmailServiceRequest {

	EntityType<SendEmail> T = EntityTypes.T(SendEmail.class);

	@Name("Connector ID")
	@Description("The external ID of the connection that should be used. When this is not set, the first matching connector will be used.")
	String getConnectorId();
	void setConnectorId(String connectorId);

	@Name("Email")
	@Description("The email that should be sent.")
	Email getEmail();
	void setEmail(Email email);

	@Override
	EvalContext<? extends SentEmail> eval(Evaluator<ServiceRequest> evaluator);

}
