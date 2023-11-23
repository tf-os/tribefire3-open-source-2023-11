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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EclipseWorkspaceOrgJdtCorePrefs extends EclipseWorkspaceHelper {

	public EclipseWorkspaceOrgJdtCorePrefs(File devEnv) {

		super(devEnv, //
				".metadata/.plugins/org.eclipse.core.runtime/.settings", // folder name in eclipse-workspace
				"org.eclipse.jdt.core.prefs", // file name
				"""
				eclipse.preferences.version=1
				org.eclipse.jdt.core.classpathVariable.TOMCAT_HOME=@TOMCAT_HOME@
				org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled
				org.eclipse.jdt.core.compiler.codegen.targetPlatform=17
				org.eclipse.jdt.core.compiler.compliance=17
				org.eclipse.jdt.core.compiler.problem.assertIdentifier=error
				org.eclipse.jdt.core.compiler.problem.enumIdentifier=error
				org.eclipse.jdt.core.compiler.release=enabled
				org.eclipse.jdt.core.compiler.source=17
				"""); // fill below!

	}

}
