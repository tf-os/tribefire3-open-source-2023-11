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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IStatus;
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
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.editors.StringEditor;
import com.braintribe.devrock.api.ui.viewers.artifacts.selector.editors.artifact.MatchingArtifactSelector;
import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonBuilder;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionInterval;
import com.braintribe.model.version.VersionRange;

/**
 * allows the selection a single (built, remote) artifact
 * @author pit
 *
 */
public class ArtifactSelector implements SelectionListener, ModifyListener, TerminalSelector, Consumer<Boolean> {
	private static Logger log = Logger.getLogger(ArtifactSelector.class);
	private static final String STATUS_PREFIX_TEXT = "Enter a qualified dependency - ";
	private static final String STATUS_INITIAL_TEXT = STATUS_PREFIX_TEXT + "add groupId and artifactId, separated by a ':'";
	private static final String STATUS_NO_HASH_TEXT = STATUS_PREFIX_TEXT + "add version part separated by a '#' or use the auto-ranger";
	private static final String STATUS_RESOLVE_TEXT = STATUS_PREFIX_TEXT + "resolve dependency to select a matching artifact";
	private static final String STATUS_ADD_VERSION_TEXT = STATUS_PREFIX_TEXT + "add version part or use the auto-ranger";
	private static final String STATUS_ADD_ARTIFACTID_TEXT = STATUS_PREFIX_TEXT + "add artifact id";
	
	private StringEditor condensedEditor;
	private Button normalizeRangeInArtifact;
	private Button searchArtifact;
	
	private Label biasWarnImage;
	private Label biasWarnTxt;
	
	private Image resetToStandardRangeImage;
	private Image searchImage;
	private Image biasImage;

	private Font bigFont;
	private MatchingArtifactSelector selector;
	private Font initialFont;
	private List<VersionedArtifactIdentification> initialIdentifications;
	private Consumer<Boolean> validEntryConsumer;
	private boolean terminalAsDependency = false;
	
	private boolean expectTwoSelections;
	
	private Supplier<Maybe<RepositoryConfiguration>> configurationSupplier;
	private Label statusLinelabel;
	private Composite matchingArtifactSelectorComposite;
	
	@Configurable
	public void setExpectTwoSelections(boolean produceTwoSelections) {
		this.expectTwoSelections = produceTwoSelections;
	}
	
	 @Configurable @Required
	public void setConfigurationSupplier(Supplier<Maybe<RepositoryConfiguration>> configurationSupplier) {
		this.configurationSupplier = configurationSupplier;
	}
	
	@Configurable
	public void setBigFont(Font bigFont) {
		this.bigFont = bigFont;
	}
	
	@Override
	public void setValidEntryConsumer(Consumer<Boolean> validEntryConsumer) {
		this.validEntryConsumer = validEntryConsumer;			
	}
	
	@Configurable
	public void setReturnTerminalAsDependency(boolean terminalAsDepedency) {
		this.terminalAsDependency = terminalAsDepedency;
	}
	
	@Override
	@Configurable
	public void setInitialIdentifications(List<VersionedArtifactIdentification> vais) {
		initialIdentifications = vais;		
	}
	@Override
	public Maybe<List<CompiledTerminal>> getSelectedTerminals() {
		List<CompiledArtifactIdentification> selection = selector.getSelection();
		if (selection == null) {
			return Maybe.empty( Reasons.build( NotFound.T).text("no selection in dialog").toReason());
		}
		
		List<CompiledTerminal> result = new ArrayList<>(selection.size());
		
		for (CompiledArtifactIdentification cai : selection) {
		
			// must resolve it to a CompiledArtifact firts		
			Maybe<CompiledArtifact> maybe = DevrockPlugin.mcBridge().resolve(cai);
			if (maybe.isSatisfied()) {
				CompiledArtifact artifact = maybe.get();
				CompiledTerminal ct;
				if (!terminalAsDependency) {
					ct = CompiledTerminal.from(artifact);
				}
				else {
					ct = CompiledTerminal.from( CompiledDependency.create(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "compile"));
				}
				result.add(ct);			
			}
			else {
				// 
				String msg = "terminal cannot be selected as it cannot be resolved with the current repository configuration:" + cai.asString();
				log.warn(msg);
				
				DevrockPluginStatus status = new DevrockPluginStatus(msg, IStatus.WARNING);
				DevrockPlugin.instance().log(status);
				return Maybe.empty( Reasons.build( NotFound.T).text( msg).toReason());
			}
		}		
		return Maybe.complete(result);
	}
	public void setBaseFont(Font initialFont) {
		this.initialFont = initialFont;				
	}
	
	public ArtifactSelector() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( CompoundTerminalSelector.class, "filter_ps.gif");
		biasImage = imageDescriptor.createImage();

		imageDescriptor = ImageDescriptor.createFromFile( CompoundTerminalSelector.class, "condensedName.gif");
		resetToStandardRangeImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( CompoundTerminalSelector.class, "update_index.gif");
		searchImage = imageDescriptor.createImage();
	}
	
	public Composite createControl( Composite parent, String tag) {
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        
        layout.verticalSpacing=2;                
        //composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        
        Composite editorComposite = new Composite(composite, SWT.NONE);
        editorComposite.setLayout( layout);
        editorComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, false, 4, 2));
        
        /*
        Label introLabel = new Label( editorComposite, SWT.NONE);
		introLabel.setText( "Enter a qualified dependency");
		introLabel.setFont(bigFont);
		introLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		*/
				
		//Composite statusLineComposite = new Composite(editorComposite, SWT.NONE);
		//statusLineComposite.setLayout(layout);
		
        statusLinelabel = new Label( editorComposite, SWT.NONE);
        statusLinelabel.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));		
        statusLinelabel.setFont(bigFont);
        statusLinelabel.setText( STATUS_INITIAL_TEXT);
		
		condensedEditor = new StringEditor();
		Composite condensedComposite = condensedEditor.createControl(editorComposite, "");
		condensedComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1));
		condensedEditor.addModifyListener( this);
		
		normalizeRangeInArtifact = new Button( editorComposite, SWT.NONE);
		normalizeRangeInArtifact.setImage( resetToStandardRangeImage);
		normalizeRangeInArtifact.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		normalizeRangeInArtifact.addSelectionListener( this);
		normalizeRangeInArtifact.setToolTipText( "normalize version range of entered dependency");
		normalizeRangeInArtifact.setEnabled(false);
		
		searchArtifact = new Button( editorComposite, SWT.NONE);
		searchArtifact.setImage( searchImage);
		searchArtifact.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		searchArtifact.addSelectionListener( this);
		searchArtifact.setToolTipText( "resolve the dependency and show results");
		searchArtifact.setEnabled(false);
							
		
		biasWarnImage = new Label( editorComposite, SWT.NONE);
		biasWarnImage.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	
		biasWarnTxt = new Label( editorComposite, SWT.NONE);
		biasWarnTxt.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));		
		

		selector = new MatchingArtifactSelector();
		selector.setInitialFont( initialFont);
		selector.setValidSelectionListener(this);
		
		
		// matches
		
		matchingArtifactSelectorComposite = selector.createControl( composite, "Matching artifacts");
		matchingArtifactSelectorComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));		 
		
		if (initialIdentifications != null && initialIdentifications.size() > 0) {
			/*
			List<CompiledArtifactIdentification> cais = new ArrayList<>( initialIdentifications.size());
			for (VersionedArtifactIdentification vai : initialIdentifications) {
				try {
					CompiledArtifactIdentification cai = CompiledArtifactIdentification.from(vai);
					cais.add(cai);
				} catch (Exception e) {
				}
			}
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.from( cais.get(0));
			updateMatches( cdi, Collections.singletonList( cais.get(0)));
			if (validEntryConsumer != null) {
				validEntryConsumer.accept(true);
			}
			*/
			VersionedArtifactIdentification primed = initialIdentifications.get(0);
			String versionAsString = primed.getVersion();
			Version v = Version.parse(versionAsString);
			FuzzyVersion fv = FuzzyVersion.from(v);
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.create( primed.getGroupId(), primed.getArtifactId(), fv);
			condensedEditor.setSelection( cdi.asString());
			//searchAndUpdateMatches(cdi);	
			if (validEntryConsumer != null) {
				validEntryConsumer.accept(true);
			}
		}						
				
		return composite;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		String expression = condensedEditor.getSelection();
		validateExpression(expression);
		
	}
	
	private boolean validateExpression( String expression) {
		normalizeRangeInArtifact.setEnabled(false);
		searchArtifact.setEnabled(false);
		
		try {
			// minimal: groupId && artifactId 
			int iColon = expression.indexOf( ':');
			int iHash = expression.indexOf( '#');
			if (iColon == expression.length()-1) {
				statusLinelabel.setText( STATUS_ADD_ARTIFACTID_TEXT);
			}
			else if (iColon > 0) { 
				normalizeRangeInArtifact.setEnabled( true);
				if (iHash == expression.length()-1) {
					statusLinelabel.setText( STATUS_NO_HASH_TEXT);	
				}
				else if (iHash > 0) {					
					statusLinelabel.setText( STATUS_RESOLVE_TEXT);
				}
				else {
					statusLinelabel.setText( STATUS_ADD_VERSION_TEXT);
				}
			}						
			else {
				statusLinelabel.setText( STATUS_INITIAL_TEXT);
			}
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(expression);
			if (cdi.getGroupId() != null && cdi.getArtifactId() != null && cdi.getVersion() != null) {
				searchArtifact.setEnabled( true);
				//statusLinelabel.setText( STATUS_FINAL_TEXT);
				return true;
			}
		} catch (Exception e) {
			
		}
		
		if (validEntryConsumer != null) {
			validEntryConsumer.accept(false);
		}
		return false;
	}

	public void updateMatches(CompiledDependencyIdentification cdi, List<CompiledArtifactIdentification> cais) {		
		
		selector.setMatchingArtifactIdentifications( cdi, cais);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == searchArtifact) {
			// resolve
			String expression = condensedEditor.getSelection();
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse(expression);	
			searchAndUpdateMatches(cdi);			
		}
		
		
		if (event.widget == normalizeRangeInArtifact) {
			String expression = condensedEditor.getSelection();
			int posHash = expression.indexOf('#');
			String prefix;
			String version = null;
			if (posHash < 0) {
				prefix = expression;
			}
			else {
				prefix = expression.substring( 0, posHash);
				version = expression.substring(posHash+1);
			}
			if (version == null) {
				expression = prefix + "#[,]";
				condensedEditor.setSelection(expression);
			}
			else {
				try {
					VersionInterval versionInterval = VersionExpression.parseVersionInterval( version);
					if (versionInterval instanceof Version) {
						Version vL = (Version) versionInterval;
						if (vL.getRevision() != null) {
							vL.setRevision(0);
						}
						if (vL.getMinor() != null) {
							Version vH = Version.from(vL);
							vH.setMinor( vL.getMinor() + 1);
							expression = prefix + "#[" + vL.asString() + "," + vH.asString() + "]";
						}
						else {							
							expression = prefix + "#[" + vL.getMajor() + "," + (vL.getMajor()+1) + "]";
						}
					}
					else if (versionInterval instanceof VersionRange) {
						VersionRange vr = (VersionRange) versionInterval;
						expression = prefix + "#" + vr.asString();
					}
					else if (versionInterval instanceof FuzzyVersion) {
						FuzzyVersion fv = (FuzzyVersion) versionInterval;
						expression = prefix + "#" + fv.asString();
					}
					condensedEditor.setSelection(expression);
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	private void searchAndUpdateMatches(CompiledDependencyIdentification cdi) {
		// inject custom repository configuration
		McBridge mcBridge = DevrockPlugin.mcBridge();
		if (configurationSupplier != null) {
			Maybe<RepositoryConfiguration> cfg = configurationSupplier.get();
			if (cfg.isSatisfied()) {
				mcBridge = mcBridge.customBridge( cfg.get());
			}				
			else {
				System.out.println("can't get cfg as " + cfg.whyUnsatisfied().stringify());
			}
		}
		
		List<CompiledArtifactIdentification> matches = mcBridge.matchesFor( cdi);

		if (matches == null || matches.size() == 0) {				
			if (validEntryConsumer != null) {
				validEntryConsumer.accept(false);
			}			
		}			
		// show 			
		updateMatches( cdi, matches);
	}
	
	public List<CompiledArtifactIdentification> getSelection() {
		return selector.getSelection();
	}


	public void dispose() {
		biasImage.dispose();
		resetToStandardRangeImage.dispose();
		searchImage.dispose();
		selector.dispose();				
	}

	@Override
	public void accept(Boolean selection) {				
		validEntryConsumer.accept(selection);		
	}

	
	
}
