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
package com.braintribe.devrock.api.ui.viewers.artifacts.selector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * selects a {@link CompiledTerminal}, either via artifact-selection or dependency-selection (aka virtual artifact)
 * @author pit
 *
 */
public class CompoundTerminalSelector implements TerminalSelector, SelectionListener {


	private Font bigFont;
	private List<VersionedArtifactIdentification> initialIdentifications;	
	private Font initialFont;

	private DependencySelector dependencySelector;
	private ArtifactSelector artifactSelector;
	
	private CTabFolder tabFolder;
	private Map<CTabItem, TerminalSelector> itemToSelectorMap = new HashMap<>();
	private Consumer<Boolean> validEntryConsumer;

	private Map<TerminalSelector, Boolean> selectorToValidEntriesMap = new HashMap<>();
	
	private Supplier<Maybe<RepositoryConfiguration>> configurationSupplier;
	
	 @Configurable @Required
	public void setConfigurationSupplier(Supplier<Maybe<RepositoryConfiguration>> configurationSupplier) {
		this.configurationSupplier = configurationSupplier;
	}
		
	@Override
	public void setValidEntryConsumer(Consumer<Boolean> validEntryConsumer) {
		this.validEntryConsumer = validEntryConsumer;			
	}
	@Override
	public void setBigFont(Font bigFont) {
		this.bigFont = bigFont;
	}
	@Override
	public void setInitialIdentifications(List<VersionedArtifactIdentification> vai) {
		initialIdentifications = vai;
		
	}
	@Override
	public Maybe<List<CompiledTerminal>> getSelectedTerminals() {
		CTabItem selection = tabFolder.getSelection();
		TerminalSelector ts = itemToSelectorMap.get(selection);
		if (ts != null)
			return ts.getSelectedTerminals();
		return Maybe.empty(Reasons.build(NotFound.T).text("no terminal selector").toReason());
	}
	@Override
	public void setBaseFont(Font initialFont) {
		this.initialFont = initialFont;				
	}
	
	public CompoundTerminalSelector() {	
	}
	
	public Composite createControl( Composite parent, String tag) {

		final Composite composite = new Composite(parent, SWT.NONE);
				
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        layout.verticalSpacing=2;                
        
        tabFolder = new CTabFolder(composite, SWT.NONE);
        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        tabFolder.addSelectionListener(this);
        tabFolder.setLayout( new FillLayout());
        
        // artifact
        CTabItem artifactSelectionItem = new CTabItem(tabFolder, SWT.NONE);
        ///failureItem.setImage( );
        artifactSelectionItem.setText("artifact");                      
        artifactSelectionItem.setToolTipText("select a single artifact as the terminal for the resolution");

        artifactSelector = new ArtifactSelector();
        artifactSelector.setBaseFont(initialFont);
        artifactSelector.setBigFont(bigFont);
        artifactSelector.setInitialIdentifications(initialIdentifications);
        artifactSelector.setValidEntryConsumer( this::acceptValidEntryFromArtifactSelector);
        artifactSelector.setConfigurationSupplier( configurationSupplier);        
        Composite reasonViewerComposite = artifactSelector.createControl( tabFolder, "artifact");	        
        artifactSelectionItem.setControl( reasonViewerComposite);
        itemToSelectorMap.put( artifactSelectionItem, artifactSelector);

        // dependency
        CTabItem dependencySelectionItem = new CTabItem(tabFolder, SWT.NONE);
        ///failureItem.setImage( );
        dependencySelectionItem.setText("dependency");
        dependencySelectionItem.setToolTipText("select one or more dependencies for a virtual artifact as a terminal for the resolution");
   
        dependencySelector = new DependencySelector();
        dependencySelector.setBaseFont(initialFont);
        dependencySelector.setBigFont(bigFont);
        dependencySelector.setInitialIdentifications(initialIdentifications);
        dependencySelector.setValidEntryConsumer( this::acceptValidEntryFromDependencySelector);
        Composite dependencyViewerComposite = dependencySelector.createControl( tabFolder, "dependency");	        
        dependencySelectionItem.setControl( dependencyViewerComposite);
        itemToSelectorMap.put(dependencySelectionItem, dependencySelector);
        
        tabFolder.setSelection( dependencySelectionItem);
          					
				
        return composite;
	}
	
	private void acceptValidEntryFromDependencySelector(Boolean valid) {			
		selectorToValidEntriesMap.put( dependencySelector, valid);		
		validEntryConsumer.accept(valid);
	}
	
	private void acceptValidEntryFromArtifactSelector(Boolean valid) {
		selectorToValidEntriesMap.put( artifactSelector, valid);		
		validEntryConsumer.accept(valid);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}
	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == tabFolder) {
			CTabItem tabItem = tabFolder.getSelection();
			TerminalSelector terminalSelector = itemToSelectorMap.get(tabItem);
			Boolean t = selectorToValidEntriesMap.get( terminalSelector);
			if (t == null) {
				t = false;
			}
			validEntryConsumer.accept( t);
		}
	}
	
	public void dispose() {
		dependencySelector.dispose();
		artifactSelector.dispose();
	}

}
