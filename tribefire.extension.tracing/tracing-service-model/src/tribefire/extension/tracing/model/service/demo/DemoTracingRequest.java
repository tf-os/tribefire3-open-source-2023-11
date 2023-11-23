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
package tribefire.extension.tracing.model.service.demo;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

@Abstract
public interface DemoTracingRequest extends AuthorizedRequest {

	EntityType<DemoTracingRequest> T = EntityTypes.T(DemoTracingRequest.class);

	@Override
	EvalContext<? extends DemoTracingResponse> eval(Evaluator<ServiceRequest> evaluator);

	String sendNotifications = "sendNotifications";
	String message = "message";
	String logLevel = "logLevel";
	String throwException = "throwException";

	@Name("Send Notifications")
	@Description("If enabled the response returns notifications for the caller.")
	boolean getSendNotifications();
	void setSendNotifications(boolean sendNotifications);

	@Initializer("'Demo Message'")
	String getMessage();
	void setMessage(String message);

	@Mandatory
	@Initializer("enum(com.braintribe.model.logging.LogLevel,INFO)")
	LogLevel getLogLevel();
	void setLogLevel(LogLevel logLevel);

	@Mandatory
	@Initializer("false")
	boolean getThrowException();
	void setThrowException(boolean throwException);

}
