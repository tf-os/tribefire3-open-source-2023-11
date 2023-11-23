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
package tribefire.extension.messaging.model.service.consume;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.messaging.model.Envelop;

/**
 * To be evaluated when consuming a message
 */
public interface ProcessConsumedMessage extends AuthorizedRequest, DispatchableRequest {

	EntityType<ProcessConsumedMessage> T = EntityTypes.T(ProcessConsumedMessage.class);

	@Override
	EvalContext<? extends ProcessConsumedMessageResult> eval(Evaluator<ServiceRequest> evaluator);

	String envelop = "envelop";

	@Mandatory
	@Name("Envelop")
	@Description("The envelop containing messages")
	Envelop getEnvelop();
	void setEnvelop(Envelop envelop);
}