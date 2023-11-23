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
package com.braintribe.model.service.api.callback;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Holds all the information necessary to invoke a Callback Request at the specified processor.")
public interface AsynchronousRequestProcessorCallback extends AsynchronousRequestCallback {

	final EntityType<AsynchronousRequestProcessorCallback> T = EntityTypes.T(AsynchronousRequestProcessorCallback.class);
	
	String callbackProcessorId = "callbackProcessorId";
	String callbackProcessorCustomData = "callbackProcessorCustomData";
	String callbackProcessorServiceDomain = "callbackProcessorServiceDomain";

	//External ID of a service processor that should handle the callback request
	//This processor must be able to process <? super ConversionCallbackRequest> 
	void setCallbackProcessorId(String callbackProcessorId);
	@Name("Callback Processor Id")
	@Description("The Id of the service processor that should handle the request. When this is not set, the Callback Service Domain has to be set instead.")
	String getCallbackProcessorId();

	//Might be any kind of data, e.g., a URL
	void setCallbackProcessorCustomData(String callbackProcessorCustomData);
	@Name("Callback Custom Data")
	@Description("Any kind of data that will be forwarded to the receiving Callback Processor. This should help the Callback Processor to identify the item of work that has been finished.")
	String getCallbackProcessorCustomData();

	void setCallbackProcessorServiceDomain(String callbackProcessorServiceDomain);
	@Name("Callback Processor Service Domain")
	@Description("The Service Domain (e.g., an Access) where the Callback Processor is registered. This information is necessary when the Callback Processor Id is not available / set.")
	String getCallbackProcessorServiceDomain();

}
