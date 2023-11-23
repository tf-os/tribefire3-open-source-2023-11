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
package tribefire.extension.setup.dev_env_generator.processing.eclipse;

import java.io.File;

public class EclipseWorkspaceTomcatPrefs extends EclipseWorkspaceHelper {

	public EclipseWorkspaceTomcatPrefs(File devEnv) {

		super(devEnv, //
				".metadata/.plugins/org.eclipse.core.runtime/.settings", // folder name in eclipse-workspace
				"net.sf.eclipse.tomcat.prefs", // file name
				/// content, IF created new from scratch:
				"""
				computeSourcePath=true
				eclipse.preferences.version=1
				jvmParameters=-Djava.util.logging.config.file%3Dconf%2Flogging.properties;-Djava.util.logging.manager%3Dcom.braintribe.logging.juli.BtClassLoaderLogManager;
				managerUrl=http\\://localhost\\:8080/manager
				contextsDir=@DEVENV@/tf-setups/main/runtime/host/conf/Catalina/localhost
				tomcatConfigFile=@DEVENV@/tf-setups/main/runtime/host/conf/server.xml
				tomcatDir=@DEVENV@/tf-setups/main/runtime/host
				tomcatVersion=tomcatV9
				""");
	}

}
