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
package com.braintribe.devrock.api.ui.editors.support;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;


/**
 * helper class to load either {@link IResource} (from a {@link IProject}) or a {@link File} into an associated editor in Eclipse. 
 * The choice what editor it is, is completely left to Eclipse, no choices here.
 * 
 * @author pit
 *
 */
public class EditorSupport {

	/**
	 * load a resource into the associated editor
	 * @param resource - the {@link IResource}
	 * @return - true if no errors occurred, false if anything went wrong 
	 */
	public static boolean load( IResource resource) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (resource instanceof IFile) {
			IFile iFile = (IFile) resource;
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(iFile.getName());
			try {
				page.openEditor(new FileEditorInput(iFile), desc.getId());
				return true;
			} catch (PartInitException e) {
		    	String msg = "cannot open resource [" + resource.getName() + "]";
		    	DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
		    	DevrockPlugin.instance().log(status);
			}
		}		
		return false;
	}
	
	/**
	 * load a file into the associated editor 
	 * @param file - the {@link File} 
	 * @return - true if no errors occurred, false if anything went wrong 
	 */
	public static boolean load( File file) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		IFileStore fileStore = EFS.getLocalFileSystem().getStore( file.toURI());		   		    
	    try {
	        IDE.openEditorOnFileStore( page, fileStore );
	        return true;
	    } catch ( PartInitException e ) {
	    	String msg = "cannot open file [" + file.getAbsolutePath() + "]";
	    	DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
	    	DevrockPlugin.instance().log(status);
	    	return false;
	    }
	}
}
