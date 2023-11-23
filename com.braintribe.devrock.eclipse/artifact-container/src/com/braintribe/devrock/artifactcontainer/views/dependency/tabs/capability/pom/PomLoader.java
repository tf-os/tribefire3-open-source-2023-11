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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom;

import java.io.File;
import java.lang.reflect.Array;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.workspace.WorkspaceProjectRegistry;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

/**
 * pom loading support for tabs 
 * 
 * @author pit
 *
 */
public class PomLoader implements HasPomLoadingTokens{
	
	/**
	 * loads all poms that are attached to the treeitems passed - see the {@link HasPomLoadingTokens} interface for the key values
	 * @param items - an {@link Array} of {@link TreeItem}
	 */
	public static void loadPom( TreeItem ... items) {
		if (items == null || items.length == 0)
			return;
		for (TreeItem item : items) {
			File pom = (File) item.getData( DATAKEY_POM);
			if (pom == null) {
				String msg = "no pom found for selected item [" + item.getText() + "]";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
				ArtifactContainerPlugin.getInstance().log(status);		
				return;
			}
			// find out whether the pom's from a local project 
			Artifact artifact = (Artifact) item.getData( DATAKEY_ARTIFACT);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			 
			if (artifact != null) {
				WorkspaceProjectRegistry registry = ArtifactContainerPlugin.getWorkspaceProjectRegistry();
				try {
					IProject project = registry.getProjectForArtifact(artifact);
					if (project != null) {
						IResource resource = project.findMember("pom.xml");
						if (resource instanceof IFile) {
							IFile iFile = (IFile) resource;
							IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(iFile.getName());
							page.openEditor(new FileEditorInput(iFile), desc.getId());
						}						
					}				
				} catch (PartInitException e) {
					String msg = "cannot open editor for pom [" + pom.getAbsolutePath() + "]";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
					ArtifactContainerPlugin.getInstance().log(status);			
				}
			}
			//  
			IFileStore fileStore = EFS.getLocalFileSystem().getStore( pom.toURI());		   		    
		    try {
		        IDE.openEditorOnFileStore( page, fileStore );
		    } catch ( PartInitException e ) {
		    	String msg = "cannot open pom [" + pom.getAbsolutePath() + "]";
		    	ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
		    	ArtifactContainerPlugin.getInstance().log(status);			
		    }
		}
	}
	
	/**
	 * attach a pom to a tree-item 
	 * @param item - the {@link TreeItem}
	 * @param solution - the {@link Solution} that contains the pom as a {@link Part}
	 */
	public static void attachPomToTreeItem( TreeItem item, Solution solution) {
		PartTuple pomTuple = PartTupleProcessor.createPomPartTuple();
		for (Part part : solution.getParts()) {
			if (PartTupleProcessor.equals(part.getType(), pomTuple)) {
				String location = part.getLocation();
				if (location != null) {
					File file = new File(location);
					if (file.exists()) {
						item.setData( DATAKEY_POM, file);
					}
				}
				return;
			}
		}
	}
}
