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

public class EclipseWorkspaceUiCorePrefs extends EclipseWorkspaceHelper {

	public EclipseWorkspaceUiCorePrefs(File devEnv) {

		super(devEnv, //
				".metadata/.plugins/org.eclipse.core.runtime/.settings", // folder name in eclipse-workspace
				"org.eclipse.ui.ide.prefs", // file name
				/// content IF CREATED FROM SCRATCH:
				"""
				WORKSPACE_NAME=@THENAME@
				eclipse.preferences.version=1
				quickStart=false
				tipsAndTricks=true
				""");

	}

}
