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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingCapable;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.malaclypse.container.SolutionContainer;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;

public class SolutionClashesTab extends AbstractDependencyViewTab implements ViewExpansionCapable,
																				PomLoadingCapable  {
	private Image requesterImage;
	private boolean condensed = true;

	public SolutionClashesTab(Display display) {
		super(display);
		setColumnNames( new String [] {"Artifact", "Version", "Group" });
		setColumnWeights( new int [] {200, 100, 300,} );
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DependencyClashesTab.class, "requester.png");
		requesterImage = imageDescriptor.createImage();		
	}
	
	

	@Override
	public void dispose() {
		requesterImage.dispose();
		super.dispose();
	}

	

	@Override
	protected Collection<String> getRelevantDataKeys() {
		Set<String> result = new HashSet<String>();		
		result.add( DATAKEY_POM);
		return result;
	}



	@Override
	protected void buildContents(boolean interactive) {
		Map<Solution, SolutionContainer> solutionClashes = monitoringResult.getSolutionClashes();
		if (solutionClashes.size()>0) {
			setTabState( DependencyViewTabState.validState);
			
			for (Entry<Solution, SolutionContainer> entry : solutionClashes.entrySet()) {
				Solution winner = entry.getKey();
				
				Set<Solution> loosers = entry.getValue().getSolutions();
				Set<Dependency> looserDependencies = CodingSet.createHashSetBased( new DependencyWrapperCodec());
				for (Solution looser : loosers) {
					looserDependencies.addAll( looser.getRequestors());
				}
				
				TreeItem winnerItem = buildEntry(tree, winner, looserDependencies);
				for (Solution looser : loosers) {
					buildEntry( winnerItem, looser, null);
				}
			}
		}
	}

	private TreeItem buildEntry( Object parent, Solution solution, Set<Dependency> looserRequesters){
		TreeItem item;
		if (parent instanceof Tree) {
			item = new TreeItem( (Tree) parent, SWT.NONE);
		}
		else {
			item = new TreeItem( (TreeItem) parent, SWT.NONE);
		}
		List<String> texts = new ArrayList<String>();
		texts.add( solution.getArtifactId());		
		texts.add( VersionProcessor.toString(solution.getVersion()));		
		texts.add( solution.getGroupId());				
		item.setText( texts.toArray( new String[0]));
		
		PomLoader.attachPomToTreeItem(item, solution);
		attachRequesters(item, solution, looserRequesters);
		
		return item;
		
	}
	
	private void attachRequesters( TreeItem parent, Solution solution, Set<Dependency> blockedRequesters) {
		Set<Dependency> requesters = solution.getRequestors();
		for (Dependency dependency : requesters) {
			if (blockedRequesters != null && blockedRequesters.contains(dependency))
				continue;
			
			TreeItem item = new TreeItem( parent, SWT.NONE);
			List<String> texts = new ArrayList<String>();
			texts.add( dependency.getArtifactId());			
			texts.add( VersionRangeProcessor.toString(dependency.getVersionRange()));			
			texts.add( dependency.getGroupId());				
			item.setText( texts.toArray( new String[0]));
			item.setImage(requesterImage);
			item.setData(PainterKey.image.toString(), requesterImage);
		}
	}



	@Override
	public void importPom() {
		TreeItem [] items = tree.getSelection();
		if (items != null) {
			PomLoader.loadPom(items);
		}		
	}
	
	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		if (monitoringResult.getSolutionClashes().size() > 0) {
			setTabState( DependencyViewTabState.validState);
		}
		else {
			super.broadcastTabState();
		}
	}
	@Override
	public void expand() {
		if (!condensed) {
			return;
		}
		int count = tree.getItemCount();
		if (count > 100) {
			String msg = "Message from [" + SolutionTab.class.getName() + "]: Size of tree is at [" + count + "], too big to auto expand";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);	
			return;
		}
		condensed = false;
		if (deferUpdate)
			return;
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {				
				new TreeExpander().expand(tree);
			}
		});
	}


	@Override
	public void condense() {
		if (condensed) {
			return;
		}
		condensed = true;
		if (deferUpdate)
			return;
		display.asyncExec( new Runnable() {			
			@Override
			public void run() {
				new TreeExpander().collapse(tree);
			}
		});
	}


	@Override
	public boolean isCondensed() {	
		return condensed;
	}	
}
