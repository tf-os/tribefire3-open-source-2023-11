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
package com.braintribe.devrock.api.ui.viewers.artifacts.selector.editors.artifact;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

public class ContentProvider implements ITreeContentProvider {

	private List<CompiledArtifactIdentification> matches;

	public ContentProvider() {
	}
	
	@Override
	public Object[] getChildren(Object arg0) {
		return null;
	}

	@Override
	public Object[] getElements(Object arg0) {
		return matches.toArray();
	}

	@Override
	public Object getParent(Object arg0) {
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		return false;
	}

	public void setInput(List<CompiledArtifactIdentification> matchingArtifactIdentifications) {
		this.matches = matchingArtifactIdentifications;		
	}
	
	

}
