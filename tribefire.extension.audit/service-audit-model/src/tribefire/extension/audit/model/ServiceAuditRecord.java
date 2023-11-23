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
package tribefire.extension.audit.model;

import java.util.Date;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

@SelectiveInformation("${requestType} by ${user} at ${date}")
public interface ServiceAuditRecord extends GenericEntity {

	EntityType<ServiceAuditRecord> T = EntityTypes.T(ServiceAuditRecord.class);

	String requestType = "requestType";
	
	String user = "user";
	String userIpAddress = "userIpAddress";
	String date = "date";
	String domainId = "domainId";

	String request = "request";
	String requestMimeType = "requestMimeType";
	
	String result = "result";
	String resultMimeType = "resultMimeType";
	
	String satisfied = "satisfied";
	
	String executionTimeInMs = "executionTimeInMs";
	
	String callId = "callId";
	String parentCallId = "parentCallId";
	
	String getRequestType();
	void setRequestType(String requestType);
	
	String getCallId();
	void setCallId(String callId);
	
	String getParentCallId();
	void setParentCallId(String parentCallId);

	String getUser();
	void setUser(String user);

	Date getDate();
	void setDate(Date date);

	Resource getRequest();
	void setRequest(Resource request);
	
	Resource getResult();
	void setResult(Resource result);
	
	String getUserIpAddress();
	void setUserIpAddress(String userIpAddress);

	String getDomainId();
	void setDomainId(String domainId);
	
	long getExecutionTimeInMs();
	void setExecutionTimeInMs(long ms);
	
	boolean getSatisfied();
	void setSatisfied(boolean satisfied);
}