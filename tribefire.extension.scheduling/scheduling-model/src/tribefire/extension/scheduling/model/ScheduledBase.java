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

import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.scheduling.model.action.Action;
import tribefire.extension.scheduling.model.context.Context;

@SelectiveInformation("${scheduledDate}")
public interface ScheduledBase extends StandardStringIdentifiable {

	EntityType<ScheduledBase> T = EntityTypes.T(ScheduledBase.class);

	String scheduledDate = "scheduledDate";
	String action = "action";
	String context = "context";

	Date getScheduledDate();
	void setScheduledDate(Date scheduledDate);

	Action getAction();
	void setAction(Action action);

	Context getContext();
	void setContext(Context context);

	String getDescription();
	void setDescription(String description);
}
