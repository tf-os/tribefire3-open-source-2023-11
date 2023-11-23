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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;

public class RepositoryContentProvider implements IStructuredContentProvider {

	private List<RavenhurstBundle> bundles = null;

	
	public List<RavenhurstBundle> getBundles() {
		return bundles;
	}

	public void setBundles(List<RavenhurstBundle> pairings) {
		this.bundles = pairings;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {	
	}

	@Override
	public Object[] getElements(Object arg0) {	
		return bundles.toArray();
	}

	
	public List<RavenhurstBundle> getBundlesAtPositions( int ... positions) {
		if (positions == null) {
			return Collections.emptyList();
		}
		List<RavenhurstBundle> result = new ArrayList<>();
		for (int i : positions) {
			result.add( bundles.get(i));
		}		
		return result;
	}

}
