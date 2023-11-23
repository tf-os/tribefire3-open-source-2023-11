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
package com.braintribe.model.platform.setup.api;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Restores a backup from an archived zip file. Restore will fail if installation folder is not empty unless
 * {@link #getForce()} is set to {@code true}. In that case, a backup folder of the installation folder is created and
 * restore overwrites its content.
 */
@Description("Restores a backup from an archived zip file which contains a tribefire installation.")
public interface RestoreBackup extends SetupRequest {
	EntityType<RestoreBackup> T = EntityTypes.T(RestoreBackup.class);

	@Override
	EvalContext<List<String>> eval(Evaluator<ServiceRequest> evaluator);

	@Description("The path of the backup file which contains a tribefire installation.")
	@Mandatory
	String getBackupArchive();
	void setBackupArchive(String backupArchive);

	@Description("The tribefire installation folder where the backup file will be extracted to.")
	@Mandatory
	String getInstallationFolder();
	void setInstallationFolder(String installationFolder);
	
	@Description("Whether or not to delete the installation folder if a previous tribefire installation is found.")
	boolean getForce();
	void setForce(boolean force);
}
