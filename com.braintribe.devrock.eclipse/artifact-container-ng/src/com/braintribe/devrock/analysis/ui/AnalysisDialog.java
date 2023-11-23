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
package com.braintribe.devrock.analysis.ui;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.selection.CustomRepositoryConfigurationSelector;
import com.braintribe.devrock.api.ui.viewers.artifacts.selector.CompoundTerminalSelector;
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

public class AnalysisDialog extends Dialog {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private Font bigFont;	
	private CompoundTerminalSelector terminalSelector;
	private List<VersionedArtifactIdentification> initialIdentifications;
	
	private Button runCompileClasspath;
	private Button runRuntimeClasspath;
	private Button runTestClasspath;
	private ClasspathResolutionScope selectedScope = ClasspathResolutionScope.compile;
	private CustomRepositoryConfigurationSelector customRepositoryConfigurationSelector;
	private StandaloneRepositoryConfigurationLoader loader = new StandaloneRepositoryConfigurationLoader();
	
	private String finallySelectedConfigurationFile;
	
	public AnalysisDialog(Shell parentShell) {
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
        
        terminalSelector = new CompoundTerminalSelector();
        terminalSelector.setBaseFont( parent.getFont());
        terminalSelector.setBigFont(bigFont);
        terminalSelector.setInitialIdentifications(initialIdentifications);
        terminalSelector.setValidEntryConsumer( this::checkValid);
        terminalSelector.setConfigurationSupplier(this::getCustomRepositoryConfiguration);
        
        @SuppressWarnings("unused")
		Composite terminalSelectorComposite = terminalSelector.createControl(composite, "dependency");
        
        // scope selection
        Composite resolutionTypeComposite = new Composite(composite, SWT.BORDER);
        resolutionTypeComposite.setLayout( layout);       
        resolutionTypeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
        
        Label resolutionTypeLabel = new Label( resolutionTypeComposite, SWT.NONE);
        resolutionTypeLabel.setText("resolution scope");
        resolutionTypeLabel.setFont(bigFont);
        resolutionTypeLabel.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        
        runCompileClasspath = new Button( resolutionTypeComposite, SWT.RADIO);
        runCompileClasspath.setText("compile");
        runCompileClasspath.setToolTipText("runs a classpath resolution as if the artifact needed to be compiled");
        runCompileClasspath.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        runCompileClasspath.setSelection(true);
        runCompileClasspath.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			
				updateSelectedScope();
			}        	
		});
        
        runRuntimeClasspath = new Button( resolutionTypeComposite, SWT.RADIO);
        runRuntimeClasspath.setText("runtime");
        runRuntimeClasspath.setToolTipText("runs a classpath resolution as if the artifact needed to be run");
        runRuntimeClasspath.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));        
        runRuntimeClasspath.setSelection(false);
        runRuntimeClasspath.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			
				updateSelectedScope();
			}        	
		});
        
        
        runTestClasspath = new Button( resolutionTypeComposite, SWT.RADIO);
        runTestClasspath.setText("test");
        runTestClasspath.setToolTipText("runs a classpath resolution as if the artifact needed to be tested");
        runTestClasspath.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        runTestClasspath.setSelection(false);
        runTestClasspath.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			
				updateSelectedScope();
			}        	
		});
        
        // repository configuration 
        Composite repositorySelectionComposite = new Composite(composite, SWT.BORDER);
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
	protected Point getInitialSize() {
		checkValid(false);
		return new Point( 500, 600);
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
		
		terminalSelector.dispose();
		
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
		return terminalSelector.getSelectedTerminals();
	}
	/**
	 * @return - the {@link ClasspathResolutionScope}
	 */
	public ClasspathResolutionScope getScopeSelection() {
		return selectedScope;
	}	
	/**
	 * @return - the {@link File} continaing the custom configuration (if any)
	 */
	public File getCustomRepositoryConfigurationX() {
		String selection = customRepositoryConfigurationSelector.getCurrentlySelectedCustomConfiguration();
		if (selection != null && selection.length() > 0) {
			return new File( selection);
		}
		else {
			return null;
		}
	}
	
	public Maybe<RepositoryConfiguration> getCustomRepositoryConfiguration() {
		// after close, finallySelectedConfigurationFile is not null, hence it has precedence
		String selection = finallySelectedConfigurationFile != null ? finallySelectedConfigurationFile : customRepositoryConfigurationSelector.getCurrentlySelectedCustomConfiguration();
		if (selection == null || selection.length() > 0) {
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
	
	private void updateSelectedScope() {
		if (runTestClasspath.getSelection()) {
			selectedScope = ClasspathResolutionScope.test;
		}
		else if (runRuntimeClasspath.getSelection()) {
			selectedScope = ClasspathResolutionScope.runtime;
		}
		else {
			selectedScope = ClasspathResolutionScope.compile;
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
