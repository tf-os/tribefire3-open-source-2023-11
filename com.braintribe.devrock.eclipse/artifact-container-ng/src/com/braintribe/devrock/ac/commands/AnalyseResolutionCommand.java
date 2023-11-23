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
package com.braintribe.devrock.ac.commands;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.resolution.viewer.ContainerResolutionViewer;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * allows to load a stored {@link AnalysisArtifactResolution} in YAML format and to view it 
 * with the analyzer view
 * 
 * @author pit
 *
 */
public class AnalyseResolutionCommand extends AbstractHandler {
	private YamlMarshaller yamlMarshaller = new YamlMarshaller();	
	private StaxMarshaller staxMarshaller = StaxMarshaller.defaultInstance;

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {	

		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

		String preselected = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_LAST_FILE,null);
		
		FileDialog fd = new FileDialog(shell);
		if (preselected != null) {
			File lastFile = new File( preselected);								
			fd.setFileName(lastFile.getName());
			fd.setFilterPath( lastFile.getParent());
		}
		fd.setFilterExtensions( new String[] {"*.yaml", "*.xml"});
		
		String selectedFile = fd.open();
		
		if (selectedFile == null) {
			return null;
		}
		
		File file = new File( selectedFile);
		if (!file.exists()) {
			ArtifactContainerStatus status = new ArtifactContainerStatus("File [" + file.getAbsolutePath() + "] doesn't exist", IStatus.ERROR);
			ArtifactContainerPlugin.instance().log(status);
			return null;
		}
		
		AnalysisArtifactResolution resolution = null;

		// set busy cursor
		Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
		shell.setCursor( busy);
		 
		 try {		  
			try ( InputStream in = new FileInputStream( file)) {
				if (selectedFile.endsWith(".yaml")) {
					Object obj = yamlMarshaller.unmarshall( in);
					resolution = (AnalysisArtifactResolution) obj;
				}
				else if (selectedFile.endsWith( ".xml")) {
					resolution = (AnalysisArtifactResolution) staxMarshaller.unmarshall( in);
				}
				else {
					ArtifactContainerStatus status = new ArtifactContainerStatus("File [" + file.getAbsolutePath() + "] is not of a supported format (yaml/xml)", IStatus.ERROR);
					ArtifactContainerPlugin.instance().log(status);
				}
			} catch (Exception e) {		
				ArtifactContainerStatus status = new ArtifactContainerStatus("File [" + file.getAbsolutePath() + "] cannot be read", e);
				ArtifactContainerPlugin.instance().log(status);
				return null;
			}		
		 }
		 // always restore cursor
		 finally {
			 shell.setCursor(null);
		 }
		
		DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_LAST_FILE, selectedFile);
		// Dialog to select analysis target
		// show resolution
		ContainerResolutionViewer resolutionViewer = new ContainerResolutionViewer( shell);
		resolutionViewer.setResolution( resolution);
		resolutionViewer.preemptiveDataRetrieval();
		resolutionViewer.open();
		
		return null;
	}

	
}
