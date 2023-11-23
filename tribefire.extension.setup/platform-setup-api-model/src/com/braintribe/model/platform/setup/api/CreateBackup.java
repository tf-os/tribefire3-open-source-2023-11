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
 * Creates a backup of a tribefire installation, archived as a zip file.
 */
@Description("Creates a backup of tribefire installation. Backup is archived as a zip file.")
public interface CreateBackup extends SetupRequest {
	EntityType<CreateBackup> T = EntityTypes.T(CreateBackup.class);

	@Override
	EvalContext<List<String>> eval(Evaluator<ServiceRequest> evaluator);

	@Description("The tribefire installation folder to back up.")
	@Mandatory
	String getInstallationFolder();
	void setInstallationFolder(String installationFolder);

	@Description("Whether to include the hostname in the backup file name.")
	boolean getIncludeHostName();
	void setIncludeHostName(boolean includeHostName);

	@Description("The folder path where the backup file will be stored.")
	String getBackupFolder();
	void setBackupFolder(String backupFolder);

	@Description("The name of the backup file. Default file name is [projectName]-[version]-[hostName]-[YYYYMMDD-HHMMSS].zip or [projectName]-[version]-[timestamp] if no hostname is found.")
	String getBackupFilename();
	void setBackupFilename(String backupFilename);

}
