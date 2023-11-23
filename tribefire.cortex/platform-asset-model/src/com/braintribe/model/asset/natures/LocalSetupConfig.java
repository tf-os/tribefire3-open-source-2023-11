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
package com.braintribe.model.asset.natures;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface LocalSetupConfig extends GenericEntity {

	EntityType<LocalSetupConfig> T = EntityTypes.T(LocalSetupConfig.class);

	String installationPath = "installationPath";
	String htttpPort = "htttpPort";
	String httpsPort = "httpsPort";
	String tempDir = "tempDir";
	String checkWriteAccessForDirs = "checkWriteAccessForDirs";

	@Description("The directory in which the local setup is placed")
	String getInstallationPath();
	void setInstallationPath(String installationPath);

	@Description("The port where the server listens for HTTP requests")
	@Initializer("8080")
	Integer getHttpPort();
	void setHttpPort(Integer httpPort);

	@Description("The port where the server listens for secured HTTP requests")
	@Initializer("8443")
	Integer getHttpsPort();
	void setHttpsPort(Integer httpsPort);

	@Description("The directory for temporary files of the JVM (controlled by system property java.io.tmpdir)")
	String getTempDir();
	void setTempDir(String tempDir);

	@Description("A list of directory paths to be checked for write access. If a directory does not exist, it will be created.")
	List<String> getCheckWriteAccessForDirs();
	void setCheckWriteAccessForDirs(List<String> checkWriteAccessForDirs);

}
