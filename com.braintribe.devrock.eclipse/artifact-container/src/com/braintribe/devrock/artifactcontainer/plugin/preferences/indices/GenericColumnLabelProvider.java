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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.indices;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public class GenericColumnLabelProvider extends ColumnLabelProvider {

	private int columnId;
	
	public GenericColumnLabelProvider(int id) {
		this.columnId = id;
	}

	@Override
	public String getText(Object element) {
		RavenhurstBundle bundle = (RavenhurstBundle) element;
		switch ( columnId) {
			case 0: // name
				return bundle.getRepositoryId();
			case 1: // last updated 
				return bundle.getDate().toString();
			case 2: // url
				return bundle.getRepositoryUrl();
			case 3:
				return bundle.getDynamicRepository() ? "dynamic" : "maven";								
		}
		return super.getText(element);
	}

	@Override
	public String getToolTipText(Object element) {		
		switch ( columnId) {
			case 0: // name
				return "the id of the repository";
			case 1: // last updated 
				return "last date as sent by RH";
			case 2: // url
				return "the url the repository is connected to";
			case 3:
				return "what kind of update policy the repository supports";								
		}
		return super.getToolTipText(element);
	}
	
	
}
