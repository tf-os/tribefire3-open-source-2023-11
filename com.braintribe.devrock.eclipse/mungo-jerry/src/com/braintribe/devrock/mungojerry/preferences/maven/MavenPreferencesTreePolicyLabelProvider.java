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
package com.braintribe.devrock.mungojerry.preferences.maven;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.braintribe.model.maven.settings.Repository;

public class MavenPreferencesTreePolicyLabelProvider extends LabelProvider implements IStyledLabelProvider, ToolTipProvider {
	private Image dynamicUpdatePolicyImage; 
	private Image nonDynamicUpdatePolicyImage;
	private MavenPreferencesTreeRegistry registry;
	

	public MavenPreferencesTreePolicyLabelProvider(MavenPreferencesTreeRegistry registry) {
		this.registry = registry;
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile(getClass(), "arrow_refresh_small.png");
		dynamicUpdatePolicyImage = imageDescriptor.createImage();
		imageDescriptor = ImageDescriptor.createFromFile( getClass(), "find.png");
		nonDynamicUpdatePolicyImage = imageDescriptor.createImage();
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof Repository) {
			Repository repository = (Repository) element;
			if (registry.getDynamicUpdatePolicySupport(repository)) {
				StyledString styledString = new StyledString( "dynamic");
				return styledString;
			}
			else {
				StyledString styledString = new StyledString( "maven");
				return styledString;
			}
		}
		return new StyledString();
			
	}

	@Override
	public Image getImage(Object element) {	
		if (element instanceof Repository) {
			Repository repository = (Repository) element;
			if (registry.getDynamicUpdatePolicySupport(repository)) {
				return dynamicUpdatePolicyImage;				
			}
			else {
				return nonDynamicUpdatePolicyImage;
			}
		}
		return super.getImage(element);
	}

	@Override
	public void dispose() {
		if (dynamicUpdatePolicyImage != null)
			dynamicUpdatePolicyImage.dispose();
		if (nonDynamicUpdatePolicyImage != null)
			nonDynamicUpdatePolicyImage.dispose();
		super.dispose();
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof Repository) {
			Repository repository = (Repository) element;
			if (registry.getDynamicUpdatePolicySupport(repository)) {
				return new String("dynamic update policy via ravenhurst");
			}
			else {
				return new String("maven update policy " + repository.getReleases().getUpdatePolicy());
			}
		}
		return null;
	}
	

	
}
