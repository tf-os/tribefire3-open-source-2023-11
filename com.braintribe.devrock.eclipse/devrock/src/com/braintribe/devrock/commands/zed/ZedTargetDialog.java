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
package com.braintribe.devrock.commands.zed;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.selection.CustomRepositoryConfigurationSelector;
import com.braintribe.devrock.api.ui.viewers.artifacts.selector.ArtifactSelector;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.Canceled;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * allows to select a remote artifact for a zed analysis
 * @author pit
 *
 */
public class ZedTargetDialog extends DevrockDialog {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private Font bigFont;	
	private ArtifactSelector artifactSelector;
	private List<VersionedArtifactIdentification> initialIdentifications;
	
	private ClasspathResolutionScope selectedScope = ClasspathResolutionScope.compile;
	private CustomRepositoryConfigurationSelector customRepositoryConfigurationSelector;
	private StandaloneRepositoryConfigurationLoader loader = new StandaloneRepositoryConfigurationLoader();
	
	private String finallySelectedConfigurationFile;
	
	public ZedTargetDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SHELL_STYLE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {	
		initializeDialogUnits(parent);
		bigFont = FontHandler.buildBigFont(parent.getDisplay(), parent.getFont());
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);

		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);       
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
    
        // artifact selection
        artifactSelector = new ArtifactSelector();
        artifactSelector.setBaseFont( parent.getFont());
        artifactSelector.setBigFont(bigFont);
        artifactSelector.setInitialIdentifications(initialIdentifications);
        artifactSelector.setValidEntryConsumer( this::checkValid);
        artifactSelector.setConfigurationSupplier(this::getCustomRepositoryConfiguration);
        artifactSelector.setReturnTerminalAsDependency(true);
        
        Composite reasonViewerComposite = artifactSelector.createControl( composite, "artifact");
        reasonViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
                       
        // repository configuration 
        Composite repositorySelectionComposite = new Composite(composite, SWT.NONE);
        repositorySelectionComposite.setLayout( layout);       
        repositorySelectionComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
        
        // 
        customRepositoryConfigurationSelector = new CustomRepositoryConfigurationSelector();
        customRepositoryConfigurationSelector.setBigFont(bigFont);
        customRepositoryConfigurationSelector.setShell(getParentShell());
        Composite repoCfgSelectorComposite =  customRepositoryConfigurationSelector.createControl(repositorySelectionComposite, "custom repository configuration");
        repoCfgSelectorComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
                        				             	
        return composite;
	}
	
	
	@Override
	protected Point getDrInitialSize() {
		checkValid(false);
		return new Point( 800, 500);
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText("Select terminal for the analysis");		
	}
	@Override
	public boolean close() {
		// store values to be able to return them after close
		Boolean standardCfgSelected = customRepositoryConfigurationSelector.getCurrentlySelectedUsageOfStandadConfiguration();
		String selectedCustomCfg = customRepositoryConfigurationSelector.getCurrentlySelectedCustomConfiguration();
		
		if (standardCfgSelected == false && selectedCustomCfg != null && selectedCustomCfg.length() > 0) {
			finallySelectedConfigurationFile = selectedCustomCfg;
		}
		
		
		bigFont.dispose();
		customRepositoryConfigurationSelector.dispose();
		
		artifactSelector.dispose();
		
		/*
		 * if (standardCfgSelected != null) {
		 * DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.
		 * SLOT_AC_USE_STANDARD_CONFIGURATION, standardCfgSelected); }
		 * 
		 * if (selectedCustomCfg != null && selectedCustomCfg.length() > 0) {
		 * DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.
		 * SLOT_AC_CUSTOM_CONFIGURATION, selectedCustomCfg); }
		 */		
		return super.close();
	}
	
	/**
	 * @return - the selected {@link CompiledTerminal}
	 */
	public Maybe<List<CompiledTerminal>> getSelection() {
		return artifactSelector.getSelectedTerminals();
	}
	/**
	 * @return - the {@link ClasspathResolutionScope}
	 */
	public ClasspathResolutionScope getScopeSelection() {
		return selectedScope;
	}	
	
	public Maybe<RepositoryConfiguration> getCustomRepositoryConfiguration() {
		// after close, finallySelectedConfigurationFile is not null, hence it has precedence
		String selection = finallySelectedConfigurationFile != null ? finallySelectedConfigurationFile : customRepositoryConfigurationSelector.getCurrentlySelectedCustomConfiguration();
		if (selection == null || selection.length() == 0) {
			return Maybe.empty( Reasons.build(Canceled.T).toReason());			
		}
		else {
			File file = new File( selection);
			if (file.exists()) {
				
				loader.setVirtualEnvironment(DevrockPlugin.instance().virtualEnviroment());
				Maybe<RepositoryConfiguration> repositoryConfigurationMaybe = loader.loadRepositoryConfiguration( new File( selection));
				return repositoryConfigurationMaybe;
			}
			else {
				return Maybe.empty( Reasons.build(NotFound.T).text( "selected configuration file doesn't exist:" + file.getAbsolutePath()).toReason());
			}
		}
	}
	
	public void setInitialIdentifications(List<VersionedArtifactIdentification> vais) {
		this.initialIdentifications = vais;		
	}

	private void checkValid(Boolean valid) {
		Button ok = getButton(IDialogConstants.OK_ID);
		
		if (ok == null)
			return;
		
		if (valid) {
			ok.setEnabled(true);
		}
		else {
			ok.setEnabled(false);
		}
	}
}
