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
package com.braintribe.devrock.greyface.view.tab.selection;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.process.ProcessControl;
import com.braintribe.devrock.greyface.process.ProcessId;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.ScanProcessNotificator;
import com.braintribe.devrock.greyface.process.notification.SelectionContext;
import com.braintribe.devrock.greyface.process.notification.SelectionContextListener;
import com.braintribe.devrock.greyface.process.notification.SelectionContextNotificator;
import com.braintribe.devrock.greyface.process.notification.SelectionProcessListener;
import com.braintribe.devrock.greyface.process.notification.SelectionProcessNotificator;
import com.braintribe.devrock.greyface.process.notification.UploadContext;
import com.braintribe.devrock.greyface.process.notification.UploadProcessListener;
import com.braintribe.devrock.greyface.process.notification.UploadProcessNotificator;
import com.braintribe.devrock.greyface.process.upload.Uploader;
import com.braintribe.devrock.greyface.settings.preferences.GreyfacePreferenceConstants;
import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard.ClipboardCopyCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading.PomLoadingCapable;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.selection.GlobalSelectionCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class SelectionTab extends GenericViewTab implements ScanProcessListener, ScanProcessNotificator, 
															SelectionProcessNotificator, SelectionProcessListener, SelectionListener, 
															UploadProcessListener, UploadProcessNotificator, 
															SelectionContextListener, SelectionContextNotificator, 
															ViewExpansionCapable, GlobalSelectionCapable, PomLoadingCapable, ClipboardCopyCapable {
	private static final String TAB_ID = "Selection";
	private Image relevancyImage;
	
	private Image notRelevantAsPresentImage;
	private Image notRelevantAsUnresolvedImage;	
	private Image activityImage;
	
	CTabFolder tabFolder;
	Button upload;
	
	private Uploader uploader;
	private Set<Solution> rootSolutions;
	private Set<Solution> solutionsToUpload;
	private Set<Part> partsToUpload;
	private SelectionContext selectionContext;
	
	private List<ScanProcessListener> scanlisteners = new ArrayList<ScanProcessListener>();
	private List<SelectionProcessListener> selectionlisteners = new ArrayList<SelectionProcessListener>();
	private List<UploadProcessListener> uploadListeners = new ArrayList<UploadProcessListener>();
	private List<SelectionContextListener> selectionContextListeners = new ArrayList<SelectionContextListener>();
	
	protected Map<GenericViewTab, CTabItem> tabToItemMap = new HashMap<GenericViewTab, CTabItem>();
	protected Map<CTabItem, GenericViewTab> itemToTabMap = new HashMap< CTabItem, GenericViewTab>();
	protected Map<Integer, GenericViewTab> indexToItemMap = new HashMap<Integer, GenericViewTab>();
		
	private enum ReceptionState { validResult, present, unresolved, none }
	private ReceptionState finalScanResultState = ReceptionState.none;
	

	private FlatSelectionTab flatTab;
	private StructureSelectionTab structureTab;
	private LicenseSelectionTab licenseTab;
	private Button abortUpload;
	private Button purgePoms;
	private ProcessControl processControl;
	private boolean condensed = true;
	
	@Configurable
	public void setProcessControl(ProcessControl processControl) {
		this.processControl = processControl;
	}

	public SelectionTab(Display display, Uploader uploader) {
		super(display);
		this.uploader = uploader;
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "accept.png");
		relevancyImage = imageDescriptor.createImage();
			
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "arrow_refresh_small.png");
		activityImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "suspend_co.png");
		notRelevantAsPresentImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "error.gif");
		notRelevantAsUnresolvedImage = imageDescriptor.createImage();
		
		id = TAB_ID;
	}
	
	
	@Override
	public Composite createControl(Composite parent) {
		
		Composite mainComposite = super.createControl(parent);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		mainComposite.setLayout( layout);
		mainComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));	
		
		tabFolder = new CTabFolder( mainComposite, SWT.NONE);
		tabFolder.setBackground( parent.getBackground());
		tabFolder.setSimple( false);		
		tabFolder.setLayout( layout);
		tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4,4));
		int index = 0;
				
		// flat tab
		CTabItem flatItem = new CTabItem( tabFolder, SWT.NONE);
		flatTab = new FlatSelectionTab(display);
		Composite pageComposite = flatTab.createControl(tabFolder);
		pageComposite.setBackground( mainComposite.getBackground());
		flatItem.setControl( pageComposite);
		flatItem.setText( "Listing");
		flatItem.setToolTipText( "Flat view of scan results");
		tabToItemMap.put( flatTab, flatItem);
		itemToTabMap.put( flatItem, flatTab);			
		indexToItemMap.put( index++, flatTab);
		addScanProcessListener(flatTab);
		flatTab.setSelectionListener( this);
		addSelectionProcessListener(flatTab);
		addUploadProcessListener(flatTab);
		addSelectionContextListener(flatTab);
		
		// structured tab 
		CTabItem structureItem = new CTabItem( tabFolder, SWT.NONE);
		structureTab = new StructureSelectionTab(display);
		pageComposite = structureTab.createControl(tabFolder);
		pageComposite.setBackground( mainComposite.getBackground());
		structureItem.setControl( pageComposite);
		structureItem.setText( "Structure");
		structureItem.setToolTipText( "Structural view of scan results");
		tabToItemMap.put( structureTab, structureItem);
		itemToTabMap.put( structureItem, structureTab);			
		indexToItemMap.put( index++, structureTab);
		addScanProcessListener(structureTab);
		structureTab.setSelectionListener( this);
		addSelectionProcessListener(structureTab);
		addUploadProcessListener(structureTab);
		addSelectionContextListener(structureTab);
		
		// license tab
		
		CTabItem licenseItem = new CTabItem( tabFolder, SWT.NONE);
		licenseTab = new LicenseSelectionTab(display);
		pageComposite = licenseTab.createControl(tabFolder);
		pageComposite.setBackground( mainComposite.getBackground());
		licenseItem.setControl( pageComposite);
		licenseItem.setText( "Licenses");
		licenseItem.setToolTipText( "View of scan results grouped by license");
		tabToItemMap.put( licenseTab, licenseItem);
		itemToTabMap.put( licenseItem, licenseTab);			
		indexToItemMap.put( index++, licenseTab);
		addScanProcessListener(licenseTab);
		licenseTab.setSelectionListener( this);
		addSelectionProcessListener(licenseTab);
		addUploadProcessListener(licenseTab);
		addSelectionContextListener(licenseTab);
		
				
		
		// purge
		Label purgeLabel = new Label( mainComposite, SWT.NONE);
		purgeLabel.setText( "manipulations");
		purgeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		purgeLabel.setFont( bigFont);
		
		purgePoms = new Button( mainComposite, SWT.CHECK);
		purgePoms.setText( GreyfacePreferenceConstants.PURGE_POMS_OF_REPOSITORIES);
		purgePoms.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		purgePoms.setSelection(GreyfacePlugin.getInstance().getGreyfacePreferences( false).getPurgePoms());		
	
		upload = new Button( mainComposite, SWT.PUSH);
		upload.setEnabled(false);
		upload.setText( "Upload selected artifacts");
		upload.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3,1));
		upload.addSelectionListener( this);
		
		
		abortUpload = new Button( mainComposite, SWT.PUSH);
		abortUpload.setEnabled(false);
		abortUpload.setText( "cancel");
		abortUpload.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1,1));
		abortUpload.addSelectionListener( this);
		
		upload.setEnabled(false);
		abortUpload.setEnabled(false);
		
		tabFolder.setSelection(0);
		return mainComposite;
	}

	@Override
	public void dispose() {				
		activityImage.dispose();
		relevancyImage.dispose();
		notRelevantAsPresentImage.dispose();
		notRelevantAsUnresolvedImage.dispose();
		super.dispose();
	}

	@Override
	public void adjustSize() {		
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			tab.adjustSize();
		}
		
	}
	
	
	private void switchUploadButtons() {
		boolean anySolutions = false;
		boolean anyParts = false;
		if (selectionContext.getRepairExistingInTarget()) {
			anyParts = flatTab.getSelectedParts().size() > 0;
		}
		else {
			anySolutions = structureTab.getSelectedSolutions().size() > 0;
		}
		if (anySolutions || anyParts) {
			upload.setEnabled( true);
		}
		else {
			upload.setEnabled(false);
		}
			
		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == upload) {
			UploadContext context = UploadContext.T.create(); 			
			
			context.setPrunePom( purgePoms.getSelection());
			// add selections..
			if (selectionContext.getRepairExistingInTarget()) {
				context.getParts().addAll(flatTab.getSelectedParts());
			}
			else {
				// must filter them here.. 
				Set<Solution> filteredSolutions = CodingSet.createHashSetBased( new SelectionCodingCodec());
				filteredSolutions.addAll(structureTab.getSelectedSolutions());
				context.getSolutions().addAll(filteredSolutions);
			}
			
			if (
					context.getSolutions().size() == 0 &&
					context.getParts().size() == 0
				) {
				upload.setEnabled(false);		
				return;
			}
			
			if (rootSolutions != null && rootSolutions.size() > 0) {
				context.getRootSolutions().addAll(rootSolutions);
			}			
			abortUpload.setEnabled(true);
			uploader.upload(null, context);
		}
		if (e.widget == abortUpload) {
			if (processControl != null) {
				processControl.cancelCurrentProcess(ProcessId.upload);
			}
		}
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	@Override
	public void acknowledgeStartScan() {
		solutionsToUpload = CodingSet.createHashSetBased( new SelectionCodingCodec());
		partsToUpload = new HashSet<Part>();
		rootSolutions = CodingSet.createHashSetBased( new SelectionCodingCodec());
		condensed = true;
		finalScanResultState = ReceptionState.none;
		
		final GenericViewTab tab = this;
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {	
				
				for (ScanProcessListener listener : scanlisteners) {
					listener.acknowledgeStartScan();
				}
				imageListener.setItemImage( tab, activityImage);
				upload.setEnabled(false);
			}
		});		
	}
	
	@Override
	public void acknowledgeStopScan() {		
		for (ScanProcessListener listener : scanlisteners) {
			listener.acknowledgeStopScan();
		}
		// process 
		// terminal not empty -> anything to process 
		final GenericViewTab tab = this;						
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {			
				switch ( finalScanResultState) {
					case present:
						upload.setEnabled(false);
						imageListener.setItemImage( tab, notRelevantAsPresentImage);
						break;
					case unresolved:
						upload.setEnabled(false);
						imageListener.setItemImage( tab, notRelevantAsUnresolvedImage);
						break;
					case validResult:
						imageListener.setItemImage( tab, relevancyImage);										
						break;
					case none:
					default:
						imageListener.setItemImage( tab, null);
						upload.setEnabled(false);
						break;					
				}
			}
		});				
	}


	
	/*
	 * notification hub support for the sub tabs 	 
	 */
	@Override
	public void acknowledgeScannedArtifact( final RepositorySetting setting, final Solution artifact, final Set<Artifact> parents, boolean presentInTarget) {		
		for (ScanProcessListener listener : scanlisteners) {
			listener.acknowledgeScannedArtifact(setting, artifact, parents, presentInTarget);
		}	
	}	
	@Override
	public void acknowledgeScannedParentArtifact(final RepositorySetting setting, final Solution artifact, final Artifact child, boolean presentInTarget) {				
		for (ScanProcessListener listener : scanlisteners) {
			listener.acknowledgeScannedParentArtifact(setting, artifact, child, presentInTarget);
		}				
	}
	@Override
	public void acknowledgeScannedRootArtifact(final RepositorySetting setting, final Solution artifact, boolean presentInTarget) {
		finalScanResultState = ReceptionState.validResult;
		rootSolutions.add( artifact);
		// 
		for (ScanProcessListener listener : scanlisteners) {
			listener.acknowledgeScannedRootArtifact(setting, artifact, presentInTarget);
		}	
	}
	@Override
	public void acknowledgeScanAbortedAsArtifactIsPresentInTarget( RepositorySetting target, Solution artifact, Set<Artifact> parents) {
		if (finalScanResultState == ReceptionState.none) {
			finalScanResultState = ReceptionState.present;
		}
		for (ScanProcessListener listener : scanlisteners) {
			listener.acknowledgeScanAbortedAsArtifactIsPresentInTarget(target, artifact, parents);
		}			
	}
	@Override
	public void acknowledgeUnresolvedArtifact(List<RepositorySetting> sources, Dependency dependency, Collection<Artifact> requestors) {
		if (finalScanResultState == ReceptionState.none) {
			finalScanResultState = ReceptionState.unresolved;
		}
		for (ScanProcessListener listener : scanlisteners) {
			listener.acknowledgeUnresolvedArtifact(sources, dependency, requestors);
		}			
	}
	@Override
	public void addScanProcessListener(ScanProcessListener listener) {
		scanlisteners.add( listener);
	}
	@Override
	public void removeScanProcessListener(ScanProcessListener listener) {
		scanlisteners.remove(listener);
	}
	@Override
	public void addSelectionProcessListener( SelectionProcessListener listener) {
		selectionlisteners.add( listener);
	}
	@Override
	public void removeSelectionProcessListener(SelectionProcessListener listener) {
		selectionlisteners.remove( listener);	
	}
	@Override
	public void acknowledgeArtifactSelected(Solution solution) {
		solutionsToUpload.add( solution);
		upload.setEnabled(true);		
		for (SelectionProcessListener listener : selectionlisteners) {
			listener.acknowledgeArtifactSelected( solution);
		}
	}
	@Override
	public void acknowledgeArtifactDeSelected(Solution solution) {
		solutionsToUpload.remove(solution);
		if (solutionsToUpload.size() > 0)
			upload.setEnabled(false);
		for (SelectionProcessListener listener : selectionlisteners) {
			listener.acknowledgeArtifactDeSelected( solution);
		}		
	}


	@Override
	public void acknowledgeSelectionContext(String id, SelectionContext context) {
		// context change : 	
		selectionContext = context;
		for (SelectionContextListener listener : selectionContextListeners) {
			listener.acknowledgeSelectionContext(id, context);
		}
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				switchUploadButtons();
			}
		});
		
	}

	@Override
	public void acknowledgeUploadBegin(RepositorySetting setting, int count) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				upload.setEnabled(false);
				abortUpload.setEnabled(true);
			}
		});
	}
	@Override
	public void acknowledgeUploadEnd(RepositorySetting setting) {
		display.asyncExec( new Runnable() { 
			@Override
			public void run() {
				upload.setEnabled(true);
				abortUpload.setEnabled(false);
			}
		});
		
	}
	@Override
	public void acknowledgeUploadSolutionBegin(RepositorySetting setting, Solution solution) {}
	@Override
	public void acknowledgeUploadSolutionFail(RepositorySetting setting, Solution solution) {}
	@Override
	public void acknowledgeRootSolutions(RepositorySetting setting, Set<Solution> solution) {}
	@Override
	public void acknowledgePartSelected(Part part) {
		partsToUpload.add( part);
		upload.setEnabled(true);	
	}
	@Override
	public void acknowledgePartDeSelected(Part part) {
		partsToUpload.remove( part);
		if (partsToUpload.size() == 0)
			upload.setEnabled(false);
	}

	@Override
	public void acknowledgeFailedPart(RepositorySetting setting, Solution solution, Part part, String reason, int worked) {
		//
		if (selectionContext.getOverwriteExistingInTarget() || !selectionContext.getRepairExistingInTarget()) {
			selectionContext.setOverwriteExistingInTarget(false);
			selectionContext.setRepairExistingInTarget(true);
			acknowledgeSelectionContext( getId(), selectionContext);		
		}
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeFailedPart(setting, solution, part, reason, worked);
		}
	}	
	@Override
	public void acknowledgeFailPartCRC(RepositorySetting setting, Solution solution, Part part, String reason, int index) {
		acknowledgeFailedPart(setting, solution, part, reason, index);		
	}
			
	@Override
	public Set<Solution> getSelectedSolutions() {
		return null;
	}
	@Override
	public Set<Part> getSelectedParts() {
		return null;
	}


	@Override
	public void acknowledgeUploadedPart(RepositorySetting setting,Solution solution, Part part, long time, int worked) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadedPart(setting, solution, part, time, worked);
		}
	}

	@Override	
	public void acknowledgeUploadSolutionEnd(RepositorySetting setting, Solution solution) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadSolutionEnd(setting, solution);
		}
	
	}

	@Override
	public void addUploadProcessListener(UploadProcessListener listener) {
		uploadListeners.add( listener);		
	}

	@Override
	public void removeUploadProcessListener(UploadProcessListener listener) {
		uploadListeners.remove(listener);		
	}

	@Override
	public void addSelectionContextListener(SelectionContextListener listener) {
		selectionContextListeners.add(listener);
		
	}

	@Override
	public void removeSelectionContextListener(SelectionContextListener listener) {
		selectionContextListeners.remove( listener);
		
	}
	@Override
	public void expand() {
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			if (tab instanceof ViewExpansionCapable) {
				((ViewExpansionCapable)tab).expand();
			}
		}
		condensed = false;
	}

	@Override
	public void condense() {
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			if (tab instanceof ViewExpansionCapable) {
				((ViewExpansionCapable)tab).condense();
			}
		}
		condensed = true;
	}

	@Override
	public boolean isCondensed() {
		return condensed;
	}

	@Override
	public void selectAll() {
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			if (tab instanceof GlobalSelectionCapable)  {
				((GlobalSelectionCapable) tab).selectAll();
			}		
		}
	}

	@Override
	public void deselectAll() {
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			if (tab instanceof GlobalSelectionCapable)  {
				((GlobalSelectionCapable) tab).deselectAll();
			}		
		}
		
	}

	@Override
	public String copyContents() {
		GenericViewTab tab = itemToTabMap.get(tabFolder.getSelection());				
		if (tab instanceof ClipboardCopyCapable)  {
			return ((ClipboardCopyCapable) tab).copyContents();
		}				
		return null; 
	}


	@Override
	public void loadPom() {
		GenericViewTab tab = itemToTabMap.get(tabFolder.getSelection());
		if (tab instanceof PomLoadingCapable)  {
			((PomLoadingCapable) tab).loadPom();
		}					
	}
	
	
}
