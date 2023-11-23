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
package tribefire.extension.tracing.model.service.status.local;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Span extends GenericEntity {

	EntityType<Span> T = EntityTypes.T(Span.class);

	String duration = "duration";
	String logs = "logs";
	String operationName = "operationName";
	String serviceName = "serviceName";
	String start = "start";
	String attributes = "attributes";

	long getDuration();
	void setDuration(long duration);

	List<LogData> getLogs();
	void setLogs(List<LogData> logs);

	String getOperationName();
	void setOperationName(String operationName);

	String getServiceName();
	void setServiceName(String serviceName);

	long getStart();
	void setStart(long start);

	Map<String, Object> getAttributes();
	void setAttributes(Map<String, Object> attributes);

}