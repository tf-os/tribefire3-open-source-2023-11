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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.project;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceRepository;

public class PathColumnLabelProvider extends ColumnLabelProvider {
	

	private boolean local;
	private VirtualPropertyResolver virtualPropertyResolver;

	public PathColumnLabelProvider(boolean local) {
		super();
		this.local = local;
		virtualPropertyResolver = ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver();
	}

	@Override
	public String getText(Object element) {
		SourceRepositoryPairing pairing = (SourceRepositoryPairing) element;
		
		SourceRepository repository;
		if (local) {
			repository = pairing.getLocalRepresentation();
			return repository.getRepoUrl();
		} else {
			repository = pairing.getRemoteRepresentation();
			return repository.getRepoUrl();						
		}
		
	}

	@Override
	public String getToolTipText(Object element) {
		SourceRepositoryPairing pairing = (SourceRepositoryPairing) element;
		SourceRepository repository;
		if (local) {
			repository = pairing.getLocalRepresentation();
			return "path [" + repository.getRepoUrl() + "]";
		} else {
			repository = pairing.getRemoteRepresentation();
			return "path [" + repository.getRepoUrl() + "]";
			
		}
	
	}

}
