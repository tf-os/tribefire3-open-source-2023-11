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
package com.braintribe.devrock.zed.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.braintribe.devrock.api.project.JavaProjectDataExtracter;
import com.braintribe.devrock.api.ui.editors.support.EditorSupport;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;

/**
 * some commons for the different viewers 
 * 
 * @author pit
 *
 */
public class ZedViewerCommons {
	/**
	 * open a source file corresponding to the type's data 
	 * @param packageName - the package name of the type 
	 * @param typeName - the name of the type 
	 */
	public static void openFile(ZedViewingContext context, String packageName, String typeName) {
		IProject project = context.getProject();
		// extract the source directories 
		Maybe<List<File>> maybeSourceDirectories = JavaProjectDataExtracter.getSourceDirectories(project);
		List<File> sourceDirectories = new ArrayList<>();
		if (maybeSourceDirectories.isUnsatisfied()) {
			DevrockPluginStatus status = new DevrockPluginStatus("cannot extract location of the source directories of:" + project.getName(), IStatus.ERROR);
			DevrockPlugin.instance().log(status);
			return;
		}
		else if (maybeSourceDirectories.isIncomplete()) {
			DevrockPluginStatus status = new DevrockPluginStatus("cannot extract location of ALL source directories of:" + project.getName(), IStatus.WARNING);
			DevrockPlugin.instance().log(status);
			sourceDirectories = maybeSourceDirectories.value();
		}
		else {
			sourceDirectories = maybeSourceDirectories.get();
		}
		// iterate over it 
		for (File file : sourceDirectories) {
			IPath location = project.getLocation();
			IPath filePath = new Path( file.getAbsolutePath());
			IPath relativePath = filePath.makeRelativeTo(location);
			String fileName = relativePath.toOSString() + "/" + packageName.replace('.', '/') + "/" + typeName + ".java";
		
			/*
			File file2 = new File( fileName);
			if (file2.exists()) {
				EditorSupport.load(file2);
			}
			*/
			
			IResource resource = project.findMember(fileName);
			// if a resource's found, we show it and return..
			if (resource != null) {
				EditorSupport.load( resource);
				break;
			}
			
		}		
	}
}
