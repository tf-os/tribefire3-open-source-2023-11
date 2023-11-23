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
package com.braintribe.devrock.ac.container.resolution.yaml;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.devrock.api.ui.viewers.yaml.YamlViewer;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class YamlResolutionViewer extends Dialog {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;	

	private YamlViewer yamlViewer;
	private AnalysisArtifactResolution resolution;
	
	public void setResolution(AnalysisArtifactResolution resolution) {
		this.resolution = resolution;
	}
	
	public YamlResolutionViewer(Shell parentShell) {
		super(parentShell);	
		setShellStyle(SHELL_STYLE);
	}
	
	@Override
	protected Point getInitialSize() {		
		return new Point( 800, 600);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {	
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
		yamlViewer = new YamlViewer();
        yamlViewer.setResolution(resolution);
        
        Composite yamlViewerComposite = yamlViewer.createControl(composite, "yaml");
        yamlViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
       
		return composite;
	}
	
	
}
