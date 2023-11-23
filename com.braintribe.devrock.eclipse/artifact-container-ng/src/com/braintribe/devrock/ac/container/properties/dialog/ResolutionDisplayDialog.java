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
package com.braintribe.devrock.ac.container.properties.dialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class ResolutionDisplayDialog extends Dialog {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private Font bigFont;
	private Shell parentShell;
	private Image successImage;
	private Image warningImage;
	
	private YamlMarshaller marshaller = new YamlMarshaller();
	
	private AnalysisArtifactResolution resolution;
	
	public ResolutionDisplayDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		
		setShellStyle(SHELL_STYLE);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( ResolutionDisplayDialog.class, "success.gif");
		successImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ResolutionDisplayDialog.class, "warning.png");
		warningImage = imageDescriptor.createImage();
	}
	
	@Configurable @Required
	public void setResolution(AnalysisArtifactResolution resolution) {
		this.resolution = resolution;
	}

	@Override
	protected Control createDialogArea(Composite parent) {	
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);

		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        Label label = new Label( composite, SWT.NONE);
        label.setText("Backing resolution");
        label.setFont(bigFont);
        label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4,1));
        
        if (resolution.hasFailed()) {
        	
        }
    
        
		Composite browserComposite = new Composite(composite, SWT.NONE);
		browserComposite.setLayout( new FillLayout());
		browserComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1,4));

		// add data here as text entry
		Browser reasonText = new Browser( browserComposite, SWT.NONE);
		//Text reasonText = new Text( browserComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		
		String yamlDump;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			marshaller.marshall(out, resolution);
			yamlDump = out.toString("UTF-8");
		} catch (Exception e) {
			yamlDump = e.getMessage();			
		}
						
		reasonText.setText("<pre>" +  yamlDump + "</pre>");
		
		
	
		
		parentShell.layout(true);
		return composite;
	}
	

	
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData(  new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		GridLayout layout= new GridLayout();
	    layout.numColumns = 2;
		buttonComposite.setLayout( layout);
		
		
		// save button 
		Button saveButton = new Button( buttonComposite, SWT.NONE);
		saveButton.setText("Save");
		saveButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, true, false,1,1));
		saveButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterExtensions( Collections.singletonList("*.yaml").toArray(new String[0]));
				String selectedFileName = dialog.open();
				
				if (selectedFileName != null) { 				
					File file = new File( selectedFileName);
					file.getParentFile().mkdirs();
					try( OutputStream out = new FileOutputStream(file)) {
						marshaller.marshall(out, resolution);
					}
					catch (Exception e1) {
						ArtifactContainerStatus status = new ArtifactContainerStatus("cannot dump resolution to [" + file.getAbsolutePath() + "]", e1);
						ArtifactContainerPlugin.instance().log(status);
					}
				}												
				//super.widgetSelected(e);
			}				
		});

		// close button
		Button closeButton = new Button( buttonComposite, SWT.NONE);
		closeButton.setText("Close");
		closeButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.widget == closeButton) 
					close();				
			}
			
		});
		buttonComposite.setLayoutData(  new GridData( SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		return closeButton;
	}
	@Override
	protected Point getInitialSize() {		
		return new Point( 500, 600);
	}


	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText("Detailed view of the classpath resolution");
	}

	@Override
	public boolean close() {
		bigFont.dispose();		
		warningImage.dispose();
		successImage.dispose();
	
		
		return super.close();
	}

}
