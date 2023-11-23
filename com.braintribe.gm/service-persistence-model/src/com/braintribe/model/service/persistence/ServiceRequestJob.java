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
package com.braintribe.model.service.persistence;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.execution.persistence.HasTimeToLive;
import com.braintribe.model.execution.persistence.HasUserDetails;
import com.braintribe.model.execution.persistence.Job;
import com.braintribe.model.execution.persistence.Retriable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

@SelectiveInformation("${requestSelectiveInformation}")
public interface ServiceRequestJob extends Job, Retriable, HasUserDetails, HasTimeToLive {

	EntityType<ServiceRequestJob> T = EntityTypes.T(ServiceRequestJob.class);

	String reasonCode = "reasonCode";
	String serializedRequest = "serializedRequest";
	String serializedResult = "serializedResult";
	String requestResources = "requestResources";
	String resultResources = "resultResources";
	String queueId = "queueId";
	String requestTypeSignature = "requestTypeSignature";
	String requestTypeShortName = "requestTypeShortName";
	String requestSelectiveInformation = "requestSelectiveInformation";

	void setReasonCode(ReasonCode reasonCode);
	@Priority(90d)
	@Name("Reason Code")
	@Description("The reason code if the job is in an error state.")
	ReasonCode getReasonCode();

	void setSerializedRequest(String serializedRequest);
	@Name("Request")
	@Description("The serialized service request.")
	String getSerializedRequest();

	void setSerializedResult(String serializedResult);
	@Name("Result")
	@Description("The serialized result of the service request.")
	String getSerializedResult();
	
	void setRequestResources(Map<String,Resource> requestResources);
	@Name("Request Resources")
	@Description("Contains the Resources referenced by the request.")
	Map<String,Resource> getRequestResources();

	void setResultResources(Set<Resource> resultResources);
	@Name("Result Resources")
	@Description("Contains the Resources referenced by the result.")
	Set<Resource> getResultResources();

	void setQueueId(String queueId);
	@Priority(-100d)
	@Name("Queue Id")
	@Description("The Id of the queue where the job is enqueued.")
	String getQueueId();

	void setRequestTypeSignature(String requestTypeSignature);
	@Priority(77d)
	@Name("Request Type Signature")
	@Description("The full type of the serialized request.")
	String getRequestTypeSignature();

	void setRequestTypeShortName(String requestTypeShortName);
	@Priority(80d)
	@Name("Request Type")
	@Description("The type of the serialized request.")
	String getRequestTypeShortName();

	void setRequestSelectiveInformation(String requestSelectiveInformation);
	@Priority(85d)
	@Name("Request Description")
	@Description("The description of the serialized request.")
	String getRequestSelectiveInformation();

}
