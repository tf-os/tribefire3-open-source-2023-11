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
package com.braintribe.devrock.artifactcontainer.validator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.braintribe.devrock.artifactcontainer.views.dependency.ValidationResultContentProvider;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;

public class ArtifactContainerPluginValidatorTab {
	private ValidationResult result;
	private CommonTableViewer commonTableViewer;
	private Table repositoryTable;
	
	public ArtifactContainerPluginValidatorTab(ValidationResult result) {
		super();
		this.result = result;
	}


	public Composite createControl( Composite parent) {		
		
		Composite tableComposite = new Composite( parent, SWT.BORDER);	
		tableComposite.setLayout( new GridLayout());
		//tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1));
		
    	commonTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[1];
		columnData[0] = new CommonTableColumnData("message", 100, 100, result.getTooltip(), new NameColumnLabelProvider());
		
		commonTableViewer.setup(columnData);
		
		repositoryTable = commonTableViewer.getTable();
		repositoryTable.setHeaderVisible(false);
		repositoryTable.setLinesVisible(true);
		
	
		ValidationResultContentProvider contentProvider = new ValidationResultContentProvider();    
		contentProvider.setResults( result.getMessages());
		commonTableViewer.setContentProvider(contentProvider);
		commonTableViewer.setInput( result.getMessages());
	
		return tableComposite;			
	}
}
