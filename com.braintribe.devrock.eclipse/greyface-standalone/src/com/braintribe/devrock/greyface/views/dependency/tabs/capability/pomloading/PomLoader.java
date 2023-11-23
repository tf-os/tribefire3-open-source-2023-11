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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.GreyfaceStatus;
import com.braintribe.devrock.greyface.view.tab.HasTreeTokens;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;

public class PomLoader implements HasTreeTokens{

	public static void loadPoms( TreeItem [] items) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		PartTuple pomTuple = PartTupleProcessor.createPomPartTuple();
		for (TreeItem item : items) {
			Part part = (Part) item.getData( KEY_PART);
			if (part == null) {
				continue;
			}
			if (PartTupleProcessor.equals(pomTuple, part.getType())) {
				File file = new File( part.getLocation());
				if (file.exists()) {
					IFileStore fileStore = EFS.getLocalFileSystem().getStore( file.toURI());		   		    
				    try {
				        IDE.openEditorOnFileStore( page, fileStore );
				    } catch ( PartInitException e ) {
				    	String msg = "cannot open pom [" + file.getAbsolutePath() + "]";
				    	GreyfaceStatus status = new GreyfaceStatus( msg, e);
				    	GreyfacePlugin.getInstance().getLog().log(status);			
				    }
				}
			}
		}

	}
}
