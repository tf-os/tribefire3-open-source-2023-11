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
package com.braintribe.devrock.artifactcontainer.ui.intelligence.manual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.bias.ArtifactBias;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.plugin.commons.preferences.StringEditor;
import com.braintribe.plugin.commons.preferences.listener.ModificationNotificationListener;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;

public class DirectEntryArtifactDialog extends Dialog implements ModificationNotificationListener, SelectionListener {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL | SWT.OK;	
	private Font bigFont;
	private Font boldItalicFont;
	
	private StringEditor condensedEditor;
	
	private Button normalizeRangeInArtifact;
	private Button searchArtifact;
	
	private List<Solution> selection;
	private List<Solution> matches = new ArrayList<>();
	private Dependency initial;
	
	private SolutionMatchContentProvider contentProvider;       	
	private CommonTableViewer commonTableViewer;
	private Table repositoryTable;
	
	private String walkScopeId = UUID.randomUUID().toString();
	
	private Executor executor = Executors.newCachedThreadPool();
	private LinkedBlockingQueue<Dependency> queue = new LinkedBlockingQueue<Dependency>();
	private Worker worker = new Worker();
	
	private ClasspathResolverContract contract = MalaclypseWirings.fullClasspathResolverContract().contract();
	private Image biasImage;
	private Image resetToStandardRangeImage;
	private Image searchImage;
	
	private Label biasWarnImage;
	private Label biasWarnTxt;
	
	private boolean singleEntryPointMode = false;
	private String title = "no name";
		
	public DirectEntryArtifactDialog(String title, Shell parentShell) {
		super(parentShell);
		setShellStyle( SHELL_STYLE);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DirectEntryArtifactDialog.class, "filter_ps.gif");
		biasImage = imageDescriptor.createImage();

		imageDescriptor = ImageDescriptor.createFromFile( DirectEntryArtifactDialog.class, "condensedName.gif");
		resetToStandardRangeImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( DirectEntryArtifactDialog.class, "update_index.gif");
		searchImage = imageDescriptor.createImage();
		
		this.title = title;
	}
	
	public void setSingleEntryPointMode(boolean singleEntryPointMode) {
		this.singleEntryPointMode = singleEntryPointMode;
	}
		
		
	@Override
	protected Control createDialogArea(Composite parent) {		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout( new FillLayout());		
								           				
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=5;
        
        Composite editorComposite = new Composite(composite, SWT.NONE);
        editorComposite.setLayout( layout);
        
	
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		FontData [] fontDataItalicBold = initialFont.getFontData();
		for (FontData data : fontDataItalicBold) {
			data.setStyle( data.getStyle() | SWT.BOLD | SWT.ITALIC);		
		}
		boldItalicFont = new Font( getShell().getDisplay(), fontDataItalicBold);
		
		
		Label introLabel = new Label( editorComposite, SWT.NONE);
		introLabel.setText( "Enter a qualified dependency");
		introLabel.setFont(bigFont);
		introLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		condensedEditor = new StringEditor();
		Composite condensedComposite = condensedEditor.createControl(editorComposite, "");
		condensedComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if (initial != null) {
			condensedEditor.setSelection( NameParser.buildName( initial));
		}
		condensedEditor.addListener( this);
		
		
		normalizeRangeInArtifact = new Button( editorComposite, SWT.NONE);
		normalizeRangeInArtifact.setImage( resetToStandardRangeImage);
		normalizeRangeInArtifact.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		normalizeRangeInArtifact.addSelectionListener( this);
		normalizeRangeInArtifact.setToolTipText( "normalize version range");
		
		normalizeRangeInArtifact.setEnabled( initial == null ? false : true);
		
		searchArtifact = new Button( editorComposite, SWT.NONE);
		searchArtifact.setImage( searchImage);
		searchArtifact.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		searchArtifact.addSelectionListener( this);
		searchArtifact.setToolTipText( "resolve the dependency");
		searchArtifact.setEnabled( initial == null ? false : true);					
		
		biasWarnImage = new Label( editorComposite, SWT.NONE);
		biasWarnImage.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	
		biasWarnTxt = new Label( editorComposite, SWT.NONE);
		biasWarnTxt.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));		
		
		Composite selectionComposite = new Composite( editorComposite, SWT.NONE);
		selectionComposite.setLayout( layout);
		selectionComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		Label matchLabel = new Label( selectionComposite, SWT.NONE);
		matchLabel.setText( "Matches");
		matchLabel.setFont(bigFont);
		matchLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
				
		Composite tableComposite = new Composite( selectionComposite, SWT.NONE);
		tableComposite.setLayout( layout);
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));

		
		
		int style = !singleEntryPointMode ? SWT.V_SCROLL | SWT.MULTI : SWT.V_SCROLL | SWT.SINGLE;
		commonTableViewer = new CommonTableViewer(tableComposite, style);
		CommonTableColumnData [] columnData = new CommonTableColumnData[1];
		
		SolutionNameColumnLabelProvider solutionNameColumnLabelProvider = new SolutionNameColumnLabelProvider();
		solutionNameColumnLabelProvider.setReflection( contract.repositoryReflectionSupport());
		
		columnData[0] = new CommonTableColumnData("solution", 100, 100, "found solution for entered dependency", solutionNameColumnLabelProvider);
		SolutionBiasColumnLabelProvider solutionBiasColumnLabelProvider = new SolutionBiasColumnLabelProvider();
		solutionBiasColumnLabelProvider.setFont(boldItalicFont);
		solutionBiasColumnLabelProvider.setReflection( contract.repositoryReflectionSupport());
		solutionBiasColumnLabelProvider.setImage(biasImage);
		
		commonTableViewer.setup(columnData);
		
		repositoryTable = commonTableViewer.getTable();
		repositoryTable.setHeaderVisible(false);
		repositoryTable.setLinesVisible(true);
		repositoryTable.addSelectionListener( this);
		
	
		contentProvider = new SolutionMatchContentProvider();    
		contentProvider.setResults( matches);
		
		commonTableViewer.setContentProvider(contentProvider);
		commonTableViewer.setInput( matches);
	
		Label hintLabel = new Label( selectionComposite, SWT.NONE);
		if (singleEntryPointMode) {
			hintLabel.setText( "Select one of the listed solutions and press OK");
		}
		else {
			hintLabel.setText( "Select one or more of the listed solutions and press OK");
		}
		hintLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));

		parent.layout();
		parent.setFocus();
		
						
		return composite;
	}
		

	@Override
	protected Point getInitialSize() {		
		PixelConverter pc = new PixelConverter(getParentShell());
		return new Point( pc.convertWidthInCharsToPixels(60), pc.convertHeightInCharsToPixels(25));		
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);	
		newShell.setText( title);
	}
	

	
	@Override
	public int open() {
		worker.start();
		if (initial != null) {
			queue.offer(initial);								
		}
				
		int value = super.open();		
		return value;		
	}


	@Override
	public boolean close() {		
		// dispose stuff 			
		biasImage.dispose();
		bigFont.dispose();
		boldItalicFont.dispose();
		
		resetToStandardRangeImage.dispose();
		searchImage.dispose();
		
		try {
			worker.interrupt();			
			worker.join();
		} catch (InterruptedException e) {
			String msg = "Exception on worker join";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		
		return super.close();
	}
	
	public List<Solution> getSelection() {
		return selection;
	}
	
	public void setInitial( Dependency dependency) {
		this.initial = dependency;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar( parent);		
		getButton( IDialogConstants.OK_ID).setEnabled( false);				
		return control;
	}

	@Override
	protected void okPressed() {	
		int [] indices = repositoryTable.getSelectionIndices();
		selection = new ArrayList<>();
		for (int index : indices) {		
			selection.add(matches.get(index));		
			super.okPressed();		
		}
	}

	@Override
	public void acknowledgeChange(Object sender, String value) {
		isValidDependency( value);
	}
	
	private Dependency isValidDependency( String ... expressions) {
		normalizeRangeInArtifact.setEnabled( false);
		searchArtifact.setEnabled(false);
		if (expressions == null)
			return null;
		
		String expression;
		if (expressions.length == 1) {
			expression = expressions[0];
		}
		else {
			StringBuffer buffer = new StringBuffer();
			for (String exp : expressions) { 
				if (exp == null) {
					return null;
				}
				buffer.append( exp);
			}
			expression = buffer.toString();
		}
		
		
		try {
			Dependency dependency = NameParser.parseCondensedDependencyNameAndAutoRangify( expression);
			normalizeRangeInArtifact.setEnabled(true);
			searchArtifact.setEnabled( true);
			return dependency;
		} catch (Exception e) {
			;
		}
		return null;		
	}
	
	
	
	private void updateMatches() {
		// see whether the condensed field has a candidate
		String condensed = condensedEditor.getSelection();
		Dependency dependency = null;
		if (condensed != null) {
			dependency = isValidDependency( condensed);						
		}
		if (dependency == null) 
			return;
	
		queue.offer(dependency);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		Button button = getButton(IDialogConstants.OK_ID);
		if (event.widget == repositoryTable) {
			int count = repositoryTable.getSelectionCount();
			if (count == 0) {
				if (button != null) {
					button.setEnabled(false);
				}
			}
			else {
				if (button != null) {
					button.setEnabled(true);
				}				
			}
		}
		
		if (event.widget == normalizeRangeInArtifact) {
			try {
				Dependency dependency = NameParser.parseCondensedDependencyName( condensedEditor.getSelection());
				VersionRange range = dependency.getVersionRange();
				VersionRange correctedRange = correctRange(range);
				dependency.setVersionRange(correctedRange);
				
				condensedEditor.setSelection( NameParser.buildName( dependency));
			} catch (NameParserException e) {
				String msg="cannot normalize version range from [" + condensedEditor.getSelection() +"]";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				ArtifactContainerPlugin.getInstance().log(status);
			} 
		}
		
		if (event.widget == searchArtifact) {
			updateMatches();
		}
	}


	private VersionRange correctRange( VersionRange range) {
		Version version;
		if (range.getInterval()) {
			version = range.getMinimum();
		}
		else {
			version = range.getDirectMatch();
		}
		VersionMetricTuple metric = VersionProcessor.getVersionMetric(version);
		metric.revision = null;
		Version correctedVersion = VersionProcessor.createFromMetrics(metric);
		VersionRange correctedRange = VersionRangeProcessor.createfromVersion(correctedVersion);
		VersionRangeProcessor.autoRangify(correctedRange);
		
		return correctedRange;
	}
	// 
	// thread that processes the input
	//
	private class Caller implements Runnable {
		private Dependency dependency; 
		
		public Caller( Dependency dependency){
			this.dependency = dependency;
		}		
		@Override
		public void run() {
			DependencyResolver resolver = contract.dependencyResolver();
			Set<Solution> resolve = resolver.resolveMatchingDependency(walkScopeId, dependency);
			
			synchronized (matches) {
						
				matches.clear();
				if (resolve != null && resolve.size() > 0) {
					matches.addAll(resolve);	
					matches.sort( new Comparator<Solution>() {
	
						@Override
						public int compare(Solution o1, Solution o2) {
							// top first sorting
							return VersionProcessor.compare( o1.getVersion(), o2.getVersion()) * -1;
						}
						
					});
				}
			} 
			
			getShell().getDisplay().asyncExec( new Runnable() {
				
				@Override
				public void run() {
					if (matches.size() > 0) {
				
						ArtifactBias artifactBias = contract.repositoryReflectionSupport().getArtifactBias( matches.get(0));
						if (artifactBias != null) {
							biasWarnImage.setImage(biasImage);
							biasWarnTxt.setText( "Bias detected! The result below is filtered");
							biasWarnImage.setToolTipText( createTooltipFromBias( artifactBias));
							biasWarnTxt.setToolTipText( createTooltipFromBias( artifactBias));
						}
						else {
							biasWarnImage.setImage( null);
							biasWarnImage.setToolTipText("no bias detected");
							biasWarnTxt.setText( "");
							biasWarnTxt.setToolTipText("no bias detected");
						}
						biasWarnImage.getParent().layout();
					}
					contentProvider.setResults(matches);
					commonTableViewer.setInput(matches);
					commonTableViewer.refresh();			
				}
			});
						
		}	
	}
		
	private String createTooltipFromBias( ArtifactBias bias) {
		return bias.toString();
	}
 	//
	// thread that handles queue.. 
	//
	private class Worker extends Thread {		
		@Override
		public void run() {		
			for (;;) {
				try {
					// grab invocation from queue
					// test of more than one entry is in the queue 
					int len = queue.size(); 
					//System.out.println("Queue length: " + len);
					if ( len > 1) {
						// if so, drain the queue and leave only one in the queue. 
						List<Dependency> expressions = new ArrayList<Dependency>();
						queue.drainTo( expressions, len - 1);						
					}
					Dependency dependency = queue.take();					
					// process  					
					Caller caller = new Caller( dependency);
					executor.execute(caller);					
					//handleArtifactSelection(expression);
				} catch (InterruptedException e) {
					// shutdown requested, expected situation
					return;
				}
			}
		}		
	}

	

}
