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
package com.braintribe.plugin.commons.views.tabbed;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.braintribe.model.malaclypse.WalkMonitoringResult;
import com.braintribe.plugin.commons.views.AbstractView;
import com.braintribe.plugin.commons.views.TabItemImageListener;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionController;
import com.braintribe.plugin.commons.views.tabbed.listener.TabbedViewNotificationBroadcaster;
import com.braintribe.plugin.commons.views.tabbed.listener.TabbedViewNotificationListener;
import com.braintribe.plugin.commons.views.tabbed.tabs.AbstractTreeViewTab;
import com.braintribe.plugin.commons.views.tabbed.tabs.AbstractViewTab;

public abstract class AbstractTabbedView<T extends AbstractTreeViewTab> extends AbstractView implements TabItemImageListener, 
																										TabbedViewNotificationBroadcaster,
																										ActiveTabProvider<T>,
																										TabProvider<T>{
	protected CTabFolder tabFolder = null;
	protected Map<T, CTabItem> tabToItemMap = new HashMap<T, CTabItem>();
	protected Map<CTabItem, T> itemToTabMap = new HashMap< CTabItem, T>();
	protected Map<Integer, T> indexToTabMap = new HashMap<Integer, T>();
	protected Map<T, Integer> tabToIndexMap = new HashMap<T, Integer>();
	//private Set<TabbedViewNotificationListener> listeners = new HashSet<TabbedViewNotificationListener>();
	private int currentSelection = -1;
	protected Set<ViewActionController<T>> actionControllers = new HashSet<ViewActionController<T>>();
	private int currentTabIndex = 0;

	
	@Override
	public void addListener(TabbedViewNotificationListener listener) {
		//listeners.add(listener);		
	}

	@Override
	public void removeListener(TabbedViewNotificationListener listener) {
		//listeners.remove(listener);		
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
		
		addTabs( composite);
		addActions();
		
		parent.layout();
		parent.setFocus();	
				
		tabFolder.setSelection(0);
		tabFolder.addSelectionListener( new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent event) {		
				int selection = tabFolder.getSelectionIndex();				
				if (selection < 0) {
					return;
				}
				if (currentSelection >= 0) {
					AbstractTreeViewTab invisibleTab = indexToTabMap.get( currentSelection);
					invisibleTab.acknowledgeDeactivation();
					
				}
				T tab = indexToTabMap.get( selection);
				acknowledgeTabSelection(tab);	
				tab.acknowledgeActivation();
				currentSelection = selection;				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});
		
		T tab = indexToTabMap.get(0);
		if (tab != null) {
			acknowledgeTabSelection( tab);
			tab.acknowledgeActivation();
		}
	}	

	/**
	 * create and add (wire) the tabs 
	 * @param composite - the tabfolder parent composite
	 */
	protected abstract void addTabs(Composite composite);
	
	/**
	 * create and add actions 
	 */
	protected abstract void addActions();
	
	/**
	 * react to a tab selection change (switch focus, actions)	
	 */
	protected void acknowledgeTabSelection(final T tab) {
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {
				for (ViewActionController<T> controller : actionControllers) {
					controller.controlAvailablity( tab);
				}
			}
		});
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void acknowledgeVisibility(String key) {
		super.acknowledgeVisibility(key);
	}

	@Override
	public void acknowledgeInvisibility(String key) {
		super.acknowledgeInvisibility(key);
	}

	@Override
	public void acknowledgeProjectChanged(IProject project) {
		super.acknowledgeProjectChanged(project);
	}
	

	@Override
	public void acknowledgeLockTerminal(boolean lock) {
		super.acknowledgeLockTerminal( lock);		
	}
	
	@Override
	public void acknowledgeExternalMonitorResult( WalkMonitoringResult result) {
		super.acknowledgeExternalMonitorResult( result);
	}

	@Override
	public void setItemImage(AbstractViewTab tab, final Image image) {
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
	public void dispose() {
		for (T tab : tabToItemMap.keySet()) {
			tab.dispose();
		}
		super.dispose();
	}

	@Override
	public Collection<T> provideTabs() {
		return tabToItemMap.keySet();
	}

	@Override
	public T provideActiveTab() {
		int selection = tabFolder.getSelectionIndex();				
		if (selection < 0) {
			return null;
		}
		return indexToTabMap.get(selection);
	}	
	
	protected void initViewActionContainer( ViewActionContainer<T> actionContainer) {
		actionContainer.setDisplay(display);
		actionContainer.setMenuManager(menuManager);
		actionContainer.setToolbarManager(toolbarManager);
		actionContainer.setSelectionProvider( this);
		actionContainer.setTabProvider(this);
	}
	
	protected void initAndTabToFolder( T tab, String title, String tooltip, Color background){
		CTabItem item = new CTabItem( tabFolder, SWT.NONE);
		Composite pageComposite = tab.createControl( tabFolder);
		pageComposite.setBackground( background);
		item.setControl( pageComposite);
		item.setText( title);
		item.setToolTipText( tooltip);
		tabToItemMap.put( tab, item);
		itemToTabMap.put( item, tab);
		tab.setTabImageListener( this);		
		indexToTabMap.put( currentTabIndex++, tab);
		addListener(tab);			
	}

	protected void teardown() {
		for (CTabItem item : itemToTabMap.keySet()) {
			item.dispose();
		}
		tabToIndexMap.clear();
		indexToTabMap.clear();
		
		tabToItemMap.clear();
		itemToTabMap.clear();
		
		currentSelection = 0;
		currentTabIndex = 0;
	}
	
	protected void teardown( T tab) {
		CTabItem item = tabToItemMap.get(tab);
		tabToItemMap.remove( tab);
		itemToTabMap.remove( item);		
		// index 		
		int index = tabToIndexMap.get( tab);
		tabToIndexMap.remove( tab);
		indexToTabMap.remove( index);

		currentSelection = -1;
		currentTabIndex = indexToTabMap.size();
	 
		// rebuild index  
		for (Entry<T, Integer> entry : tabToIndexMap.entrySet()) {
			if (entry.getValue() < index) {								
				continue;
			} 
			int relocatedValue = entry.getValue() - 1;
			entry.setValue( relocatedValue);
			indexToTabMap.put( relocatedValue, tab);
		}
		
		item.dispose();
				
		
		
	}
}
