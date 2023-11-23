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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs;

import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

public class DependencyTab extends AbstractDependencyTab {
	
	public DependencyTab(Display display) {
		super(display);
		setColumnNames( new String [] { "Artifact", "Assigned Version", "Requested Version",  "Group", "Classifier", "Scope"});
		setColumnWeights( new int [] { 150, 50, 50, 150, 50, 50 });
	}
						
	@Override
	protected void buildContents(boolean interactive) {
		super.buildContents(interactive);
		buildEntry( terminalSolution, tree, interactive);		
	}
			
	@Override
	protected void buildEntriesForDependencyItem( TreeItem parent, Dependency dependency, boolean interactive) {
		Set<Solution> solutions = dependency.getSolutions();
		for (Solution solution : solutions) {
			buildEntry( solution, parent, interactive);
		}
	}

	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		if (terminalSolution.getDependencies().size() > 0) {
			setTabState( DependencyViewTabState.validState);
		}
		else {
			super.broadcastTabState();
		}
	}	
	
}
