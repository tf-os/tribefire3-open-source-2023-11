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
package com.braintribe.devrock.mj.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.braintribe.devrock.api.nature.CommonNatureIds;


/**
 * nature for GWT libraries
 * @author pit
 *
 */
public class GwtLibraryNature implements IProjectNature {

	public static final String NATURE_ID = CommonNatureIds.NATURE_GWT_LIBRARY; //$NON-NLS-1$
	private IProject project;
	
	@Override
	public void configure() throws CoreException {		
	}
	

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {		
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
		
	}

}
