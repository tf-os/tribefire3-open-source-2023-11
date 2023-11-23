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
package tribefire.extension.cache.model.service.demo;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

public interface CacheDemo extends AuthorizedRequest {

	EntityType<CacheDemo> T = EntityTypes.T(CacheDemo.class);

	@Override
	EvalContext<? extends CacheDemoResult> eval(Evaluator<ServiceRequest> evaluator);

	String sendNotifications = "sendNotifications";
	String durationInMs = "durationInMs";
	String throwException = "throwException";
	String resultValue = "resultValue";

	@Name("Send Notifications")
	@Description("If enabled the response returns notifications for the caller.")
	@Mandatory
	boolean getSendNotifications();
	void setSendNotifications(boolean sendNotifications);

	@Initializer("0l")
	@Mandatory
	long getDurationInMs();
	void setDurationInMs(long durationInMs);

	@Initializer("false")
	@Mandatory
	boolean getThrowException();
	void setThrowException(boolean throwException);

	String getResultValue();
	void setResultValue(String resultValue);

}
