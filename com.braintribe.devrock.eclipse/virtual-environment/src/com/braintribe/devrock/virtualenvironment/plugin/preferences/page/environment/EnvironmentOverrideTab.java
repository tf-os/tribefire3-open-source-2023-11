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
package com.braintribe.devrock.virtualenvironment.plugin.preferences.page.environment;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.braintribe.devrock.commons.tableviewer.CommonTableColumnData;
import com.braintribe.devrock.commons.tableviewer.CommonTableViewer;
import com.braintribe.model.malaclypse.cfg.preferences.ve.EnvironmentOverride;
import com.braintribe.model.malaclypse.cfg.preferences.ve.OverrideType;

public class EnvironmentOverrideTab {
	
	private Button addOverrideButton;
	private Button removeOverrideButton;
	
	private Set<EnvironmentOverride> overrides;
	
	private OverrideType nature;

	private CommonTableViewer tableViewer;
	private OverrideContentProvider contentProvider;
	
	public EnvironmentOverrideTab(OverrideType nature) {
		this.nature = nature;
	}
	
	public void setOverrides(Set<EnvironmentOverride> overrides) {
		this.overrides = overrides;
		if (contentProvider != null) {
			contentProvider.setEnvironmentOverrides(overrides);			
		}
		if (tableViewer != null) {
			tableViewer.setInput(overrides);
		}
	}
	
	public Set<EnvironmentOverride> getOverrides() {
		return overrides;
	}

	public Composite createControl(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);
		
		// table with overrides 
     	Composite overridesGroup = new Composite( composite, SWT.NONE);
		overridesGroup.setLayout( layout);
		overridesGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));

		Composite tableComposite = new Composite(overridesGroup, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;	
		tableComposite.setLayout(gridLayout);
		GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4);
		tableComposite.setLayoutData( gridData);
		
		tableViewer = new CommonTableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columData = new CommonTableColumnData[4];
		String nameTooltip, currentValueTooltip, overrideValueTooltip, activatedTooltip;
		switch (nature) {
			case environment:
				nameTooltip = "name of the environment variable";
				currentValueTooltip ="current value of the environment variable";
				overrideValueTooltip = "value of the environment variable when override is activated";
				activatedTooltip ="override activation state";				
				break;
			case property:						
			default:
				nameTooltip = "Name of the system property";
				currentValueTooltip ="current value of the system property";
				overrideValueTooltip = "value of the system property when override is activated";
				activatedTooltip ="override activation state";
				break;		
		}
		
		columData[0] = new CommonTableColumnData("name", 100, 150, nameTooltip, new NameColumLabelProvider(), new NameColumnEditingSupport(tableViewer));
		columData[1] = new CommonTableColumnData("variable", 100, 150, currentValueTooltip, new ValueColumnLabelProvider());
		columData[2] = new CommonTableColumnData("value", 100, 150, overrideValueTooltip, new OverrideColumnLabelProvider(), new OverrideColumnEditingSupport(tableViewer));
		columData[3] = new CommonTableColumnData("on", 10, 20, activatedTooltip, new ActiveColumnLabelProvider(), new ActiveColumnEditingSupport(tableViewer));
		
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData layoutData = gridData;    	
    	int ht = (table.getItemHeight() * 10) + table.getHeaderHeight();
    	layoutData.heightHint = table.computeSize(SWT.DEFAULT, ht).y;
    		
		tableViewer.setup(columData);
		
		contentProvider = new OverrideContentProvider();
		contentProvider.setEnvironmentOverrides(overrides);
		tableViewer.setContentProvider(contentProvider);
		tableViewer.setInput(overrides);
				
    	
    	//
    	// buttons
    	//
		// table with overrides      
    
    	addOverrideButton = new Button( overridesGroup, SWT.NONE);
    	addOverrideButton.setText( "Add override");
    	addOverrideButton.setLayoutData( new GridData( SWT.LEFT,SWT.CENTER, true, true, 2, 1));
    	
    	removeOverrideButton = new Button( overridesGroup, SWT.NONE);
    	removeOverrideButton.setText( "Remove override");
    	removeOverrideButton.setLayoutData( new GridData( SWT.RIGHT,SWT.CENTER, true, true, 2, 1));
    	    			
		addOverrideButton.addSelectionListener( new SelectionListener() {		
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// add 
				EnvironmentOverride override = EnvironmentOverride.T.create();
				override.setName("name");
				override.setValue("override");
				override.setOverrideNature(nature);
				overrides.add( override);		
				contentProvider.setEnvironmentOverrides(overrides);
				tableViewer.setInput(overrides);
				tableViewer.refresh();			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		removeOverrideButton.addSelectionListener( new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// remove
				TableItem [] items = tableViewer.getTable().getSelection();
				for (TableItem item : items) {
					EnvironmentOverride override = (EnvironmentOverride) item.getData();
					overrides.remove( override);
				}
				contentProvider.setEnvironmentOverrides(overrides);
				tableViewer.setInput(overrides);
				tableViewer.refresh();				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
	    	    
		tableViewer.refresh();
 		
 		return overridesGroup; 		
	}
	
	public void refresh() {	
		tableViewer.refresh();
	}
}
