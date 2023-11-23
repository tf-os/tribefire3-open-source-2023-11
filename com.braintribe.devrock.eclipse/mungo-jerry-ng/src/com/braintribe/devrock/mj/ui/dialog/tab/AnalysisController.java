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
package com.braintribe.devrock.mj.ui.dialog.tab;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.braintribe.build.gwt.GwtModule;
import com.braintribe.build.gwt.ModuleCheckProtocol;

public interface AnalysisController {
	
	Collection<ModuleCheckProtocol> getProtocols();
	void setProtocols(Collection<ModuleCheckProtocol> protocols);
	
	File getOutputFolder();
	void setOutputFolder( File folder);
	
	File getSourceFolder();
	void setSourceFolder( File folder);	
	
	IProject getProject();
	void setProject( IProject project);
	
	List<File> getClasspathAsFiles();
	void setClasspathAsFiles( List<File> files);
		
	List<GwtModule> getModules();
	void setModules( List<GwtModule> modules);
	
}
