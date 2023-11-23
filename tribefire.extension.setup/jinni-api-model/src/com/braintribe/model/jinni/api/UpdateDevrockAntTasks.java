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
package com.braintribe.model.jinni.api;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.PlatformRequest;

@Description("Updates the bt jars that extend Ant. Kind of. "
		+ "Downloads the \"latest\" (see 'version') devrock-ant-tasks zip, moves the old bt jars to a sub-folder (for possible manual recovery), and extracts new jars in place.")
public interface UpdateDevrockAntTasks extends PlatformRequest {

	EntityType<UpdateDevrockAntTasks> T = EntityTypes.T(UpdateDevrockAntTasks.class);

	@Description("The desired target devrok-ant-task version. "
			+ "As of right now, only the major.minor part is considered, and the highest version for given major.minor will be used for update."
			+ "\n\n" + "If no value is given here, current Jinni version will be used instead. "
			+ "This means if there is a newer devrock-ant-tasks version with a higher major.minor, you have to specify those explicitly.")
	@Alias("v")
	String getVersion();
	void setVersion(String version);

	@Description("If true, the update is executed even if the latest found version is not newer than current version.")
	@Alias("f")
	boolean getForce();
	void setForce(boolean force);

}
