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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;

public class RepositoryKindColumnLabelProvider extends ColumnLabelProvider {
	private Image svnImage;
	private Image gitImage;
	
	public RepositoryKindColumnLabelProvider() {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "repo_svn.gif");
		svnImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( RepositoryKindColumnLabelProvider.class, "repo_git.gif");
		gitImage = imageDescriptor.createImage();
	}

	@Override
	public Image getImage(Object element) {
		SourceRepositoryPairing pairing = (SourceRepositoryPairing) element;
		switch (pairing.getRemoteRepresentationKind()) {
		case git:
			return gitImage;		
		case svn:			
		default:
			return svnImage;		
		}			
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public String getToolTipText(Object element) {	
		SourceRepositoryPairing pairing = (SourceRepositoryPairing) element;
		switch (pairing.getRemoteRepresentationKind()) {
		case git:
			return "GIT repository";		
		case svn:			
		default:
			return "SVN repository";		
		}		
	}

	@Override
	public void dispose() {
		svnImage.dispose();
		gitImage.dispose();
		super.dispose();
	}

	
}
