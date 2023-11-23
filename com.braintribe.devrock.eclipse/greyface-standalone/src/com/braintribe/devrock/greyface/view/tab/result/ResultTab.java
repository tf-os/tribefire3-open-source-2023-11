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
package com.braintribe.devrock.greyface.view.tab.result;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.ScanProcessNotificator;
import com.braintribe.devrock.greyface.process.notification.UploadProcessListener;
import com.braintribe.devrock.greyface.process.notification.UploadProcessNotificator;
import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class ResultTab extends GenericViewTab implements UploadProcessListener, UploadProcessNotificator, ScanProcessListener, ScanProcessNotificator, ViewExpansionCapable {
	
	private Set<UploadProcessListener> uploadListeners = new HashSet<UploadProcessListener>();
	private Set<ScanProcessListener> scanListeners = new HashSet<ScanProcessListener>();
	
	private Image relevancyImage;
	private Image activityImage;
	private Image exclamationImage;
	
	protected Map<GenericViewTab, CTabItem> tabToItemMap = new HashMap<GenericViewTab, CTabItem>();
	protected Map<CTabItem, GenericViewTab> itemToTabMap = new HashMap< CTabItem, GenericViewTab>();
	protected Map<Integer, GenericViewTab> indexToItemMap = new HashMap<Integer, GenericViewTab>();
	
	private boolean anyFailed = false;
	private boolean anyUploaded = false;
	
	CTabFolder tabFolder;
	
	private boolean condensed = true;
	
	public ResultTab(Display display) {
		super(display);		
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "accept.png");
		relevancyImage = imageDescriptor.createImage();
			
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "arrow_refresh_small.png");
		activityImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( GenericViewTab.class, "exclamation--frame.png");
		exclamationImage = imageDescriptor.createImage();		
				
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
		tabFolder.setLayout( new FillLayout());
		tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
		int index = 0;
				
		CTabItem lastFlatItem = new CTabItem( tabFolder, SWT.NONE);
		final FlatResultTab lastFlatTab = new FlatResultTab(display, true);
		Composite lastPageComposite = lastFlatTab.createControl(tabFolder);
		lastPageComposite.setBackground( mainComposite.getBackground());
		lastFlatItem.setControl( lastPageComposite);
		lastFlatItem.setText( "Current uploads");
		lastFlatItem.setToolTipText( "View of the last upload results");
		tabToItemMap.put( lastFlatTab, lastFlatItem);
		itemToTabMap.put( lastFlatItem, lastFlatTab);			
		indexToItemMap.put( index++, lastFlatTab);
		addUploadProcessListener( lastFlatTab);
		
		CTabItem flatItem = new CTabItem( tabFolder, SWT.NONE);
		final FlatResultTab flatTab = new FlatResultTab(display, false);
		Composite pageComposite = flatTab.createControl(tabFolder);
		pageComposite.setBackground( mainComposite.getBackground());
		flatItem.setControl( pageComposite);
		flatItem.setText( "Collected uploads");
		flatItem.setToolTipText( "View of the collected upload results");
		tabToItemMap.put( flatTab, flatItem);
		itemToTabMap.put( flatItem, flatTab);			
		indexToItemMap.put( index++, flatTab);
		addUploadProcessListener( flatTab);
		
		tabFolder.setSelection(0);
	
		
		return mainComposite;
	}
	

	@Override
	public void dispose() {
		relevancyImage.dispose();
		activityImage.dispose();
		exclamationImage.dispose();
	}

	@Override
	public void adjustSize() {
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			tab.adjustSize();
		}		
	}

	@Override
	public void acknowledgeUploadSolutionBegin(RepositorySetting setting, Solution solution) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadSolutionBegin(setting, solution);
		}				
	}
	
	@Override
	public void acknowledgeUploadSolutionEnd(RepositorySetting setting, Solution solution) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadSolutionEnd(setting, solution);
		}
		anyUploaded = true;
	}
	@Override
	public void acknowledgeUploadSolutionFail(RepositorySetting setting, Solution solution) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadSolutionFail(setting, solution);
		}	
		anyFailed = true;
	}
	@Override
	public void acknowledgeUploadedPart(RepositorySetting setting, Solution solution, Part part, long time, int worked) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadedPart(setting, solution, part, time, worked);
		}	
	}
	@Override
	public void acknowledgeFailedPart(RepositorySetting setting, Solution solution, Part part, String reason, int worked) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeFailedPart(setting, solution, part, reason, worked);
		}
		anyFailed = true;
	}
	
	@Override
	public void acknowledgeFailPartCRC(RepositorySetting setting, Solution solution, Part part, String reason, int index) {
		acknowledgeFailedPart(setting, solution, part, reason, index);		
	}

	@Override
	public void acknowledgeUploadBegin(RepositorySetting setting, int count) {
		imageListener.setItemImage(this, activityImage);
		
		anyFailed = false;
		anyUploaded = false;
		condensed = true;
		
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadBegin(setting, count);
		}
	}
	@Override
	public void acknowledgeUploadEnd(RepositorySetting setting) {	
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeUploadEnd(setting);
		}
		if (anyFailed) {
			imageListener.setItemImage(this, exclamationImage);
		} 
		else if (anyUploaded) {
			imageListener.setItemImage(this, relevancyImage);
		}
		else {
			imageListener.setItemImage(this, null);
		}
	}
	
	@Override
	public void acknowledgeRootSolutions(RepositorySetting setting,Set<Solution> solutions) {
		for (UploadProcessListener listener : uploadListeners) {
			listener.acknowledgeRootSolutions(setting, solutions);
		}
	}
	@Override
	public void addUploadProcessListener(UploadProcessListener listener) {
		uploadListeners.add( listener);
	}
	@Override
	public void removeUploadProcessListener(UploadProcessListener listener) {
		uploadListeners.remove( listener);
	}

	@Override
	public void addScanProcessListener(ScanProcessListener listener) {
		scanListeners.add( listener);
	}

	@Override
	public void removeScanProcessListener(ScanProcessListener listener) {
		scanListeners.remove(listener);
	}

	@Override
	public void acknowledgeStartScan() {
		for (ScanProcessListener listener : scanListeners) {
			listener.acknowledgeStartScan();
		}	
	}

	@Override
	public void acknowledgeStopScan() {
		for (ScanProcessListener listener : scanListeners) {
			listener.acknowledgeStopScan();
		}
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
	public void acknowledgeScanAbortedAsArtifactIsPresentInTarget( RepositorySetting target, Solution artifact, Set<Artifact> parents) {}

	@Override
	public void acknowledgeScannedArtifact(RepositorySetting setting, Solution artifact, Set<Artifact> parents, boolean presentInTarget) {}

	@Override
	public void acknowledgeScannedParentArtifact(RepositorySetting setting, Solution artifact, Artifact child, boolean presentInTarget) {}

	@Override
	public void acknowledgeScannedRootArtifact(RepositorySetting setting, Solution artifact, boolean presentInTarget) {}

	@Override
	public void acknowledgeUnresolvedArtifact(List<RepositorySetting> sources, Dependency dependency, Collection<Artifact> requestors) {}	
}
