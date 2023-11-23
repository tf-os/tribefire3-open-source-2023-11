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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.api.ui.editors.StringEditor;
import com.braintribe.devrock.api.ui.viewers.artifacts.selector.editors.dependency.MatchingDependencySelector;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;


public class DependencySelector implements SelectionListener, ModifyListener, TerminalSelector {
	private StringEditor condensedEditor;

	private Label biasWarnLabel;
	private Label biasWarnTxt;
	
	private Image warnImage;

	private Font bigFont;
	private List<VersionedArtifactIdentification> initialIdentifications;

	private String selection;

	private Consumer<Boolean> validEntryConsumer;

	private MatchingDependencySelector selector;

	private Font initialFont;
	private Image addImage;

	private Button addToSelectedDependencyIdentifications;
	
	@Override
	public void setValidEntryConsumer(Consumer<Boolean> validEntryConsumer) {
		this.validEntryConsumer = validEntryConsumer;			
	}
	
	@Override
	public void setBigFont(Font bigFont) {
		this.bigFont = bigFont;
	}
	@Override
	public void setBaseFont(Font initialFont) {
		this.initialFont = initialFont;
	}
	
	@Override
	@Configurable
	public void setInitialIdentifications(List<VersionedArtifactIdentification> vais) {
		initialIdentifications = vais;		
	}
	
	public DependencySelector() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DependencySelector.class, "warning.png");
		warnImage = imageDescriptor.createImage();
		imageDescriptor = ImageDescriptor.createFromFile( CompoundTerminalSelector.class, "add.gif");
		addImage = imageDescriptor.createImage();
	}
	

	@Override
	public Maybe<List<CompiledTerminal>> getSelectedTerminals() {
		// matches
		List<CompiledDependencyIdentification> selectorsSelection = selector.getSelection();
		if (selectorsSelection == null) {
			selectorsSelection = new ArrayList<>();
		}		
		Set<String> strings = selectorsSelection.stream().map( cdi -> cdi.asString()).collect( Collectors.toCollection( LinkedHashSet::new));
		
		if (selection != null) {
			strings.add(selection);		
		}	
		List<CompiledTerminal> terminals = new ArrayList<>( strings.size());
		for (String str : strings) {
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(str);
			terminals.add(cdi);
		}		
		return Maybe.complete(terminals);
	}
	
	
	public Composite createControl( Composite parent, String tag) {
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        
        layout.verticalSpacing=2;                       
        
        Composite editorComposite = new Composite(composite, SWT.NONE);
        editorComposite.setLayout( layout);
        editorComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, false, 4, 2));
        
        Label introLabel = new Label( editorComposite, SWT.NONE);
		introLabel.setText( "Enter a qualified dependency");
		introLabel.setFont(bigFont);
		introLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		condensedEditor = new StringEditor();
		Composite condensedComposite = condensedEditor.createControl(editorComposite, "");
		condensedComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		addToSelectedDependencyIdentifications = new Button( editorComposite, SWT.NONE);
		addToSelectedDependencyIdentifications.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));		
		addToSelectedDependencyIdentifications.addSelectionListener( this);
		addToSelectedDependencyIdentifications.setEnabled(false);
		addToSelectedDependencyIdentifications.setImage(addImage);
		addToSelectedDependencyIdentifications.setToolTipText("adds the current dependency to the virtual artifact below");
						
		biasWarnLabel = new Label( editorComposite, SWT.NONE);
		biasWarnLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	
		biasWarnTxt = new Label( editorComposite, SWT.NONE);
		biasWarnTxt.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));		
				
		condensedEditor.addModifyListener( this);
		

		selector = new MatchingDependencySelector();
		selector.setInitialFont( initialFont);
		
		Composite selectedDependencyComposite = selector.createControl( composite, "CompiledTerminals for resolution");
		selectedDependencyComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));		 				
		
		// initialization 
		if (initialIdentifications != null) {
			if (initialIdentifications.size() > 1) {
				List<CompiledDependencyIdentification> cdis = new ArrayList<>( initialIdentifications.size());
				for (VersionedArtifactIdentification vai : initialIdentifications) {
					try {
						CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse( vai.asString());
						selector.addToSelection( cdi);
					}
					catch (Exception e) {				
					}
				}
				// 
				selector.addToSelection(cdis);
			}
			else if (initialIdentifications.size() == 1){
				condensedEditor.setSelection( initialIdentifications.get(0).asString());
			}
			if (validEntryConsumer != null) {
				validEntryConsumer.accept(true);
			}
			
		}
		
		return composite;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		selection = condensedEditor.getSelection();
		validateExpression( selection);	
	}
	
	private boolean validateExpression( String expression) {
		addToSelectedDependencyIdentifications.setEnabled( false);
		biasWarnTxt.setText("");
		biasWarnLabel.setImage( null);
		
		if (expression == null || expression.length() == 0) {
			if (validEntryConsumer != null) {
				validEntryConsumer.accept(false);
			}
			biasWarnTxt.setText("[" + selection + "] is not a valid dependency");
			biasWarnLabel.setImage(warnImage);
			return false;
		}
		try {
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(expression);
			if (cdi.getGroupId() != null && cdi.getArtifactId() != null && cdi.getVersion() != null) {
				if (validEntryConsumer != null) {
					validEntryConsumer.accept(true);
				}
				addToSelectedDependencyIdentifications.setEnabled(true);
				return true;
			}
		} catch (Exception e) {}
		
		if (validEntryConsumer != null) {
			validEntryConsumer.accept(false);
		}
		biasWarnTxt.setText("[" + selection + "] is not a valid dependency");
		biasWarnLabel.setImage(warnImage);
		return false;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == addToSelectedDependencyIdentifications) {
			String expression = condensedEditor.getSelection();
			if (validateExpression(expression)) {
				// no need to catch parse error as it has just been validated
				CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(expression);
				selector.addToSelection( cdi);		
			}
		}	
	}
	
	
	public void dispose() {
		warnImage.dispose();
		addImage.dispose();
		selector.dispose();
	}
	
	
}
