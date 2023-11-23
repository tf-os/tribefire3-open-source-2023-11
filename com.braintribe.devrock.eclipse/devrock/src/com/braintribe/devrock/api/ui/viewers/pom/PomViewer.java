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
package com.braintribe.devrock.api.ui.viewers.pom;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.utils.IOTools;

/**
 * simple viewer for XML (or rather a {@link DeclaredArtifact} or an pom XML directly 
 * 
 * @author pit
 *
 */
public class PomViewer {
	private String payload = "<nothing/>";
		
	@Configurable
	public void setPom( String pomAsString) {
		this.payload = pomAsString;		
	}	
	
	@Configurable
	public void setPom( File file) {
		try {
			payload = IOTools.slurp( file, "UTF-8");
		} catch (IOException e) {
			payload = e.getMessage();
		}		
	}
		
	public Composite createControl( Composite parent, String tag) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
	
		// label 
		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( tag);
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
			
        Text reasonText = new Text( composite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		reasonText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
		reasonText.setText( payload);
		
		composite.pack();	
		return composite;
	}
	
	public void dispose() {		
	}
}
