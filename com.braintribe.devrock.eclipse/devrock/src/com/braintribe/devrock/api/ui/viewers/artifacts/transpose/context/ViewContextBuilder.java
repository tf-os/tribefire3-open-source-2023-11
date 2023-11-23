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
package com.braintribe.devrock.api.ui.viewers.artifacts.transpose.context;

import com.braintribe.devrock.eclipse.model.storage.ViewerContext;

public interface ViewContextBuilder {
	
	/**
	 * @param shortRanges - true to show ranges in 'js-style' notation rather than 'maven-style'
	 * @return - itself
	 */
	ViewContextBuilder shortRanges( boolean shortRanges);
	/**
	 * @param showGroups - true to show at least one groupid per node
	 * @return
	 */
	ViewContextBuilder showGroups( boolean showGroups);
	/**
	 * @param showDependencies - true to show dependency information or to concentrate on artifacts only
	 * @return - itself
	 */
	ViewContextBuilder showDependencies( boolean showDependencies);

	/**
	 * @param showNatures - true to show artifact natures (as images for artifact/project/parent)
	 * @return - itself
	 */
	ViewContextBuilder showNatures( boolean showNatures);
	
	/**
	 * @return - close and produce a {@link ViewerContext}
	 */
	
	ViewerContext done();
}
