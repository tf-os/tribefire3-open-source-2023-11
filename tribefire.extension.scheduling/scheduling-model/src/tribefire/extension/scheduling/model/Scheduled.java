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
package tribefire.extension.scheduling.model;

import java.util.Date;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${scheduledDate}")
public interface Scheduled extends ScheduledBase {

	EntityType<Scheduled> T = EntityTypes.T(Scheduled.class);

	String executionDate = "executionDate";
	String executionSuccess = "executionSuccess";
	String errorMessage = "errorMessage";

	Date getExecutionDate();
	void setExecutionDate(Date executionDate);

	Boolean getExecutionSuccess();
	void setExecutionSuccess(Boolean executionSuccess);

	String getErrorMessage();
	void setErrorMessage(String errorMessage);
}
