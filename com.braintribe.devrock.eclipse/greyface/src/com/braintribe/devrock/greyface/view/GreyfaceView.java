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
package com.braintribe.devrock.greyface.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.braintribe.devrock.greyface.GreyfacePlugin;
import com.braintribe.devrock.greyface.process.ProcessControl;
import com.braintribe.devrock.greyface.process.ProcessId;
import com.braintribe.devrock.greyface.process.notification.ScanContext;
import com.braintribe.devrock.greyface.process.notification.ScanProcessListener;
import com.braintribe.devrock.greyface.process.notification.ScanProcessNotificator;
import com.braintribe.devrock.greyface.process.notification.UploadContext;
import com.braintribe.devrock.greyface.process.notification.UploadProcessListener;
import com.braintribe.devrock.greyface.process.notification.UploadProcessNotificator;
import com.braintribe.devrock.greyface.process.scan.AbstractScannerImpl;
import com.braintribe.devrock.greyface.process.scan.AsynchronousScannerImpl;
import com.braintribe.devrock.greyface.process.scan.Scanner;
import com.braintribe.devrock.greyface.process.scan.SynchronousScannerImpl;
import com.braintribe.devrock.greyface.process.upload.Uploader;
import com.braintribe.devrock.greyface.process.upload.UploaderImpl;
import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.devrock.greyface.view.tab.parameter.ParameterTab;
import com.braintribe.devrock.greyface.view.tab.result.ResultTab;
import com.braintribe.devrock.greyface.view.tab.selection.SelectionTab;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.clipboard.ClipboardCopyActionContainer;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.expansion.ExpansionActionContainer;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.pomloading.PomLoadingActionContainer;
import com.braintribe.devrock.greyface.views.dependency.tabs.capability.selection.GlobalSelectionActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;
import com.braintribe.plugin.commons.views.tabbed.ActiveTabProvider;
import com.braintribe.plugin.commons.views.tabbed.TabProvider;

public class GreyfaceView extends ViewPart implements Scanner, Uploader, TabItemImageListener, ProcessControl, ScanProcessNotificator, UploadProcessNotificator, 
	ActiveTabProvider<GenericViewTab>,
	TabProvider<GenericViewTab> {
	private IWorkbenchWindow workbenchWindow;
	@SuppressWarnings("unused")
	private IViewSite site;	
	private Display display;
	
	protected Map<GenericViewTab, CTabItem> tabToItemMap = new HashMap<GenericViewTab, CTabItem>();
	protected Map<CTabItem, GenericViewTab> itemToTabMap = new HashMap< CTabItem, GenericViewTab>();
	protected Map<Integer, GenericViewTab> indexToItemMap = new HashMap<Integer, GenericViewTab>();
	protected CTabFolder tabFolder = null;
	
	private List<ScanProcessListener> scanProcessListeners = new ArrayList<ScanProcessListener>();
	private List<UploadProcessListener> uploadProcessListeners = new ArrayList<UploadProcessListener>();
	
	private ScanContext scanContext;
	private ParameterTab parameterTab;

	private IProgressMonitor activeScanMonitor;
	private IProgressMonitor activeUploadMonitor;
	
	protected IMenuManager menuManager;
	protected IToolBarManager toolbarManager;
	protected Set<ViewActionController<GenericViewTab>> actionControllers = new HashSet<ViewActionController<GenericViewTab>>();
	protected IActionBars actionBars;
	
	private int currentSelection = -1;
	
	@Override
	public void init(IViewSite site) throws PartInitException {		
		super.init(site);
		
		this.site = site; 
		workbenchWindow = site.getWorkbenchWindow();
		display = workbenchWindow.getShell().getDisplay();
		
		actionBars = site.getActionBars();
		menuManager = actionBars.getMenuManager();
		toolbarManager = actionBars.getToolBarManager();
		
		// set process control
		GreyfacePlugin.getInstance().setProcessControl( this);
	}
	
	

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout( new FillLayout());
		
		Composite composite = new Composite(parent, SWT.NONE);
	
		int nColumns= 4;		
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
           				
		composite.setLayout( new FillLayout());
     	
		tabFolder = new CTabFolder( composite, SWT.NONE);
		tabFolder.setBackground( parent.getBackground());
		tabFolder.setSimple( false);		
		tabFolder.setLayout( new FillLayout());
		int index = 0;	
		
		parameterTab = new ParameterTab(display, this);						
		CTabItem item = new CTabItem( tabFolder, SWT.NONE);
		Composite pageComposite = parameterTab.createControl( tabFolder);
		pageComposite.setBackground( composite.getBackground());
		item.setControl( pageComposite);
		item.setText( "Parameter");
		item.setToolTipText( "Parameters for scanning");
		tabToItemMap.put( parameterTab, item);
		itemToTabMap.put( item, parameterTab);			
		indexToItemMap.put( index++, parameterTab);
		parameterTab.setProcessControl(this);
		addScanProcessListener(parameterTab);
		
		
		SelectionTab selectionTab = new SelectionTab(display, this);						
		item = new CTabItem( tabFolder, SWT.NONE);
		pageComposite = selectionTab.createControl( tabFolder);
		pageComposite.setBackground( composite.getBackground());
		item.setControl( pageComposite);
		item.setText( "Scan result");
		item.setToolTipText( "Scan result to be selected");
		tabToItemMap.put( selectionTab, item);
		itemToTabMap.put( item, selectionTab);			
		indexToItemMap.put( index++, selectionTab);
		addScanProcessListener(selectionTab);
		addUploadProcessListener(selectionTab);
		selectionTab.setImageListener( this);		
		
		ResultTab resultTab = new ResultTab(display);						
		item = new CTabItem( tabFolder, SWT.NONE);
		pageComposite = resultTab.createControl( tabFolder);
		pageComposite.setBackground( composite.getBackground());
		item.setControl( pageComposite);
		item.setText( "Upload result");
		item.setToolTipText( "Uploaded artifacts");
		tabToItemMap.put( resultTab, item);
		itemToTabMap.put( item, resultTab);			
		indexToItemMap.put( index++, resultTab);
		addUploadProcessListener(resultTab);
		resultTab.setImageListener( this);
		
		parent.layout();
		parent.setFocus();
						
		
		for (GenericViewTab tab : tabToItemMap.keySet()) {			
			tab.adjustSize();
		}
		
		//
		// wire
		// 
		// user parameter changes -> selection tab 
		parameterTab.setSelectionContextListener(selectionTab);
		// automatic parameter changes (due to failed uploads) -> parameter tab
		selectionTab.addSelectionContextListener( parameterTab);
		
		// broadcast initial context from parameter tab
		parameterTab.broadCastSelectionContext();
		
		
		// add action containers
		// expand / condense
		ExpansionActionContainer expansionContainer = new ExpansionActionContainer();
		initViewActionContainer(expansionContainer);
		actionControllers.add(expansionContainer.create());
		
		GlobalSelectionActionContainer globalSelectionContainer = new GlobalSelectionActionContainer();
		initViewActionContainer(globalSelectionContainer);
		actionControllers.add(globalSelectionContainer.create());
		
		PomLoadingActionContainer pomActionContainer = new PomLoadingActionContainer();
		initViewActionContainer(pomActionContainer);
		actionControllers.add(pomActionContainer.create());
		
		ClipboardCopyActionContainer clipboardActionContainer = new ClipboardCopyActionContainer();
		initViewActionContainer(clipboardActionContainer);
		actionControllers.add( clipboardActionContainer.create());
		
		tabFolder.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selection = tabFolder.getSelectionIndex();				
				if (selection < 0) {
					return;
				}
				if (currentSelection >= 0) {
					GenericViewTab invisibleTab = indexToItemMap.get( currentSelection);
					invisibleTab.acknowledgeDeactivation();
					
				}
				GenericViewTab tab = indexToItemMap.get( selection);
				acknowledgeTabSelection(tab);	
				tab.acknowledgeActivation();
				currentSelection = selection;
			}
			
		});
		
		tabFolder.setSelection(0);
		GenericViewTab tab = indexToItemMap.get( 0);
		acknowledgeTabSelection(tab);	
		tab.acknowledgeActivation();
		currentSelection = 0;
		
	}

	private void initViewActionContainer( ViewActionContainer<GenericViewTab> actionContainer) {
		actionContainer.setDisplay( display);
		actionContainer.setMenuManager( menuManager);
		actionContainer.setToolbarManager( toolbarManager);
		actionContainer.setSelectionProvider( this);
		actionContainer.setTabProvider(this);
	}

	public void setArtifactExpression( String expression) {
		parameterTab.setArtifactExpression(expression);
	}
	
	@Override
	public void setFocus() {		
	}

	@Override
	public void dispose() {	
		for (GenericViewTab tab : tabToItemMap.keySet()) {
			tab.dispose();
		}
		super.dispose();
	}
	
	

	@Override
	public void setItemImage(GenericViewTab tab, final Image image) {
		final CTabItem item = tabToItemMap.get( tab);
		if (item != null) {
			display.asyncExec( new Runnable() { 
				@Override			
				public void run() {	
					item.setImage( image);
				}				
			});
		}
	}
	
	

	@Override
	public void setItemLabel(GenericViewTab tab, final String label) {
		final CTabItem item = tabToItemMap.get( tab);
		if (item != null) {
			display.asyncExec( new Runnable() { 
				@Override			
				public void run() {	
					item.setText( label);
				}				
			});
		}		
	}



	@Override
	public void upload(IProgressMonitor dummy, final UploadContext context) {
		final UploaderImpl uploader = new UploaderImpl();
		for (UploadProcessListener uploadProcessListener : uploadProcessListeners) {
			uploader.addUploadProcessListener(uploadProcessListener);
		}
		context.setTarget( scanContext.getTargetRepository());
		context.getSources().addAll(scanContext.getSourceRepositories());
		WorkspaceJob job = new WorkspaceJob("Running greyface upload") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				activeUploadMonitor = monitor;
				uploader.upload(monitor, context);
				activeUploadMonitor = null;
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
		
	}

	@Override
	public void scan(IProgressMonitor dummy, final ScanContext context) {
		scanContext = context;
		
		// 
		final AbstractScannerImpl scanner = GreyfacePlugin.getInstance().getGreyfacePreferences(false).getAsyncScanMode() ? new AsynchronousScannerImpl() : new SynchronousScannerImpl();
		for (ScanProcessListener scanProcessListener : scanProcessListeners) {
			scanner.addScanProcessListener(scanProcessListener);
		}
		
		WorkspaceJob job = new WorkspaceJob("Running greyface scan") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				activeScanMonitor = monitor;
				scanner.scan(monitor, context);
				activeScanMonitor = null;
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}


	@Override
	public void cancelCurrentProcess(ProcessId id) {
		switch (id) {
			case scan:
				if (activeScanMonitor != null) {
					activeScanMonitor.setCanceled(true);
				}
				break;
			case upload:
				if (activeUploadMonitor != null) {
					activeUploadMonitor.setCanceled(true);
				}
				break;
			default:
				break;
		}		
	}
	

	@Override
	public void cancelAllProcesses() {
		cancelCurrentProcess( ProcessId.scan);
		cancelCurrentProcess( ProcessId.upload);		
	}

	@Override
	public void addUploadProcessListener(UploadProcessListener listener) {	
		uploadProcessListeners.add(listener);
	}

	@Override
	public void removeUploadProcessListener(UploadProcessListener listener) {
		uploadProcessListeners.remove( listener);
		
	}

	@Override
	public void addScanProcessListener(ScanProcessListener listener) {
		scanProcessListeners.add(listener);
		
	}

	@Override
	public void removeScanProcessListener(ScanProcessListener listener) {
		scanProcessListeners.remove(listener);
		
	}

	@Override
	public Collection<GenericViewTab> provideTabs() {	
		return tabToItemMap.keySet();
	}

	@Override
	public GenericViewTab provideActiveTab() {
		int selection = tabFolder.getSelectionIndex();				
		if (selection < 0) {
			return null;
		}
		return indexToItemMap.get(selection);
	}	
	
	protected void acknowledgeTabSelection(final GenericViewTab tab) {
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {
				for (ViewActionController<GenericViewTab> controller : actionControllers) {
					controller.controlAvailablity( tab);
				}
			}
		});
	}

	
	
}
