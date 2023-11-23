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
package com.braintribe.devrock.api.ui.viewers.yaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.utils.IOTools;

/**
 * simple viewer for YAML (or rather {@link AnalysisArtifactResolution}, {@link RepositoryConfiguration} and YAML directly
 * 
 * @author pit
 *
 */
public class YamlViewer {
	private YamlMarshaller marshaller;
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	private String payload = "<nothing/>";
		
	@Configurable
	public void setYaml( String yaml) {
		this.payload = yaml;		
	}	
	
	@Configurable
	public void setResolution( AnalysisArtifactResolution resolution) {
		
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			marshaller.marshall(out, resolution);
			payload = out.toString("UTF-8");
		} catch (Exception e) {
			payload = e.getMessage();			
		}
	}
	
	@Configurable
	public void setRepositoryConfiguration( RepositoryConfiguration configuration) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			marshaller.marshall(out, configuration);
			payload = out.toString("UTF-8");
		} catch (Exception e) {
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
	
        Button saveButton = new Button( treeLabelComposite, SWT.PUSH);
        saveButton.setText("save as");
        saveButton.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        saveButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog( parent.getShell(), SWT.SAVE);
				dialog.setFilterExtensions( Collections.singletonList("*.yaml").toArray(new String[0]));
				String selectedFileName = dialog.open();
				
				if (selectedFileName != null) { 			
					if (!selectedFileName.endsWith( ".yaml")) {
						selectedFileName += ".yaml";
					}
					File file = new File( selectedFileName);					
					file.getParentFile().mkdirs();
					try( OutputStream out = new FileOutputStream(file)) {
						IOTools.spit(file, payload, "UTF-8", false);
					}
					catch (Exception e1) {
						DevrockPluginStatus status = new DevrockPluginStatus("cannot dump yaml to [" + file.getAbsolutePath() + "]", e1);
						DevrockPlugin.instance().log(status);
					}
				}											
			}
        	
		});
        
		Browser reasonText = new Browser( composite, SWT.BORDER);		
		reasonText.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
		reasonText.setText("<pre>" +  payload + "</pre>");
		
		composite.pack();	
		return composite;
	}
	
	public void dispose() {		
	}
}
