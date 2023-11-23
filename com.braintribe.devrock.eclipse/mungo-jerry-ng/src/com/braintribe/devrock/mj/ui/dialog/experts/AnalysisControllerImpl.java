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
package com.braintribe.devrock.mj.ui.dialog.experts;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.braintribe.build.gwt.GwtModule;
import com.braintribe.build.gwt.ModuleCheckProtocol;
import com.braintribe.devrock.mj.ui.dialog.tab.AnalysisController;

public class AnalysisControllerImpl implements AnalysisController {
	
	private Collection<ModuleCheckProtocol> protocols;
	private File outputFolder;
	private File sourceFolder;
	private IProject project;
	private List<File> classpathAsFiles;
	private List<GwtModule> modules;

	@Override
	public Collection<ModuleCheckProtocol> getProtocols() {
		return protocols;
	}

	@Override
	public void setProtocols(Collection<ModuleCheckProtocol> protocols) {
		this.protocols = protocols;
	}

	@Override
	public File getOutputFolder() {
		return outputFolder;
	}

	@Override
	public void setOutputFolder(File folder) {
		this.outputFolder = folder;
	}

	@Override
	public File getSourceFolder() {
		return sourceFolder;
	}

	@Override
	public void setSourceFolder(File folder) {
		this.sourceFolder = folder;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	@Override
	public List<File> getClasspathAsFiles() {
		return classpathAsFiles;
	}

	@Override
	public void setClasspathAsFiles(List<File> files) {
		this.classpathAsFiles = files;
	}

	@Override
	public List<GwtModule> getModules() {
		return modules;
	}

	@Override
	public void setModules(List<GwtModule> modules) {
		this.modules = modules;

	}

}
