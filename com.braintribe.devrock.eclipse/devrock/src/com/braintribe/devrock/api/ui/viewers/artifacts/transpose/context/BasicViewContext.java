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

public class BasicViewContext implements ViewContextBuilder {
	private boolean shortRanges;
	private boolean showGroups;
	private boolean showDependencies;
	private boolean showNatures;

	@Override
	public ViewContextBuilder shortRanges(boolean shortRanges) {
		this.shortRanges = shortRanges;
		return this;
	}

	@Override
	public ViewContextBuilder showGroups(boolean showGroups) {
		this.showGroups = showGroups;
		return this;
	}
	
	

	@Override
	public ViewContextBuilder showDependencies(boolean showDependencies) {
		this.showDependencies = showDependencies;
		return this;
	}
	
	
	@Override
	public ViewContextBuilder showNatures(boolean showNatures) {
		this.showNatures = showNatures;
		return this;
	}

	@Override
	public ViewerContext done() {
		ViewerContext vc = ViewerContext.T.create();
		vc.setShowGroupIds(showGroups);
		vc.setShowShortNotation(shortRanges);
		vc.setShowDependencies(showDependencies);
		vc.setShowNature(showNatures);
		return vc;
	}
	
	

	
	public static ViewContextBuilder build() {
		return new BasicViewContext();
	}

}
