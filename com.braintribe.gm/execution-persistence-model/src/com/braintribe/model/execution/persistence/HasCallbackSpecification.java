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
package com.braintribe.model.execution.persistence;

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Confidential;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface HasCallbackSpecification extends StandardStringIdentifiable {

	EntityType<HasCallbackSpecification> T = EntityTypes.T(HasCallbackSpecification.class);

	String callbackProcessorId = "callbackProcessorId";
	String callbackProcessorCustomData = "callbackProcessorCustomData";
	String callbackProcessorServiceDomain = "callbackProcessorServiceDomain";
	String callbackRestTargetServiceId = "callbackRestTargetServiceId";
	String callbackRestTargetUrl = "callbackRestTargetUrl";
	String callbackRestStatusTargetUrl = "callbackRestStatusTargetUrl";
	String callbackRestBasicAuthUser = "callbackRestBasicAuthUser";
	String callbackRestBasicAuthPassword = "callbackRestBasicAuthPassword";
	String callbackRestTargetDomain = "callbackRestTargetDomain";
	String callbackRestTargetCustomData = "callbackRestTargetCustomData";
	
	void setCallbackProcessorId(String callbackProcessorId);
	@Name("Callback Processor Id")
	@Description("The Id of the processor responsible for executing the callback.")
	String getCallbackProcessorId();

	void setCallbackProcessorCustomData(String callbackProcessorCustomData);
	@Name("Callback Custom Data")
	@Description("Data provided by the requestor to be included in the callback.")
	String getCallbackProcessorCustomData();

	void setCallbackProcessorServiceDomain(String callbackProcessorServiceDomain);
	@Name("Callback Service Domain")
	@Description("The service domain to be used for the callback.")
	String getCallbackProcessorServiceDomain();

	void setCallbackRestTargetUrl(String callbackRestTargetUrl);
	@Name("Callback REST URL")
	@Description("The URL to use for the callback POST request.")
	String getCallbackRestTargetUrl();

	void setCallbackRestStatusTargetUrl(String callbackRestStatusTargetUrl);
	@Name("Status Callback REST URL")
	@Description("The URL to use for the status update callback POST request.")
	String getCallbackRestStatusTargetUrl();

	void setCallbackRestBasicAuthUser(String callbackRestBasicAuthUser);
	@Name("Callback REST Username")
	@Description("If required, this contains the username used for Basic HTTP authentication in the REST callback.")
	String getCallbackRestBasicAuthUser();

	void setCallbackRestBasicAuthPassword(String callbackRestBasicAuthPassword);
	@Name("Callback REST Password")
	@Description("If required, this contains the password used for Basic HTTP authentication in the REST callback.")
	@Confidential
	String getCallbackRestBasicAuthPassword();

	void setCallbackRestTargetServiceId(String callbackRestTargetServiceId);
	@Name("Callback REST Target Service Id")
	@Description("The Id of the service processor responsible for handling the REST callback.")
	String getCallbackRestTargetServiceId();

	void setCallbackRestTargetDomain(String callbackRestTargetDomain);
	@Name("Callback REST Target Service Domain")
	@Description("The service domain to be used for the callback.")
	String getCallbackRestTargetDomain();

	void setCallbackRestTargetCustomData(String callbackRestTargetCustomData);
	@Name("Callback REST Custom Data")
	@Description("Custom data that should be included in the REST callback.")
	String getCallbackRestTargetCustomData();
}
