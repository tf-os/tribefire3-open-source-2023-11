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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.cache.SolutionWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.ArtifactWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.clipboard.ClipboardContentsProviderMode;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.expansion.ViewExpansionCapable;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoader;
import com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.pom.PomLoadingCapable;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.artifact.SolutionProcessor;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.ParentContainer;
import com.braintribe.model.malaclypse.container.DependencyTraversingEvent;
import com.braintribe.model.malaclypse.container.SolutionTraversingEvent;
import com.braintribe.model.malaclypse.container.TraversingEvent;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;


public class TraversingProtocalTab extends AbstractDependencyViewTab implements ViewExpansionCapable,																				
																				PomLoadingCapable,
																				ClipboardContentsProviderCapable 
																				{
	private static final String DATAKEY_DEPENDENCY = "DEPENDENCY";
	private static final String DATAKEY_SOLUTION = "SOLUTION";
	private static final String DATAKEY_END_POINT = "ENDPOINT";
	private static final String DATAKEY_LEVEL = "LEVEL";
	private Image redirectionImage;
	private Image validPomImage;
	private Image invalidPomImage;
	private Image endPointImage;
	private Image parentPomImage;
	private Image importPomImage;
	
	private enum TraversingType { pom, parent, imported}
	
	private boolean condensed = true;	
	private Map<Artifact, TreeItem> artifactToTreeItemMap;	
	private Map<Artifact, SolutionTraversingEvent> parentToTraversingEventMap;
	private Map<Artifact, SolutionTraversingEvent> importToTraversingEventMap;
	private Set<Solution> insertImports = CodingSet.createHashSetBased( new SolutionWrapperCodec());
	private Map<TreeItem, Set<Solution>> treeItemsToImports = new HashMap<TreeItem, Set<Solution>>();
	
	private Map<Dependency, TreeItem> nonEndPointDependencyTreeItems;
	private Map<Artifact, TreeItem> nonEndPointArtifactTreeItems;

	public TraversingProtocalTab(Display display) {
		super(display);	
		setColumnNames( new String [] {"Artifact","Index" });
		setColumnWeights( new int [] {100, 25} );
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( AbstractDependencyViewTab.class, "redirected.pom.gif");
		redirectionImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AbstractDependencyViewTab.class, "validation-valid-document.png");
		validPomImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AbstractDependencyViewTab.class, "validation-invalid-document.png");
		invalidPomImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AbstractDependencyViewTab.class, "terminal.png");
		endPointImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AbstractDependencyViewTab.class, "parent_pom.gif");
		parentPomImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( AbstractDependencyViewTab.class, "import_pom.png");
		importPomImage = imageDescriptor.createImage();
		
	}

	@Override
	protected void handleTreeEvent(Event event) {
		super.handleTreeEvent(event);
	}

	@Override
	public void dispose() {
		redirectionImage.dispose();
		validPomImage.dispose();
		invalidPomImage.dispose();		
		endPointImage.dispose();
		parentPomImage.dispose();
		importPomImage.dispose();
		
		super.dispose();
	}
	
	
	
	@Override
	protected Collection<String> getRelevantDataKeys() {
		Set<String> result = new HashSet<String>();
		result.add( DATAKEY_SOLUTION);
		result.add( DATAKEY_END_POINT);
		result.add( DATAKEY_LEVEL);
		result.add( DATAKEY_DEPENDENCY);
		result.add( DATAKEY_POM);
		return result;
	}

	

	@Override
	protected void buildContents(boolean interactive) {
		// 
		List<TraversingEvent> traversingEvents = monitoringResult.getTraversingEvents();
		if (traversingEvents == null || traversingEvents.size() == 0) {
			setTabState( DependencyViewTabState.noState);
		}
		setTabState( DependencyViewTabState.validState);
		
		artifactToTreeItemMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		Map<Dependency, TreeItem> dependencyToTreeItemMap = CodingMap.createHashMapBased( new DependencyWrapperCodec());
		
		Map<Artifact, Artifact> partToSolutionMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		partToSolutionMap.putAll( monitoringResult.getRedirectionMap());
		
		//
		// build redirection registry
		// 
		List<SolutionTraversingEvent> redirections = new ArrayList<SolutionTraversingEvent>();		
		parentToTraversingEventMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		importToTraversingEventMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		
		
		nonEndPointDependencyTreeItems = CodingMap.createHashMapBased( new DependencyWrapperCodec());
		nonEndPointArtifactTreeItems = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
	
		for (TraversingEvent event : traversingEvents) {
			if (event instanceof SolutionTraversingEvent) {
				SolutionTraversingEvent stEvent = (SolutionTraversingEvent) event;

				// do parents later 
				if (stEvent.getParentNature())  {
					parentToTraversingEventMap.put( stEvent.getArtifact(), stEvent);
					continue;
				}
				if (stEvent.getImportNature()) {
					importToTraversingEventMap.put( stEvent.getArtifact(), stEvent);
					continue;
				}
				
				// do redirected later 
				Artifact redirected = partToSolutionMap.get( stEvent.getArtifact());
				if (redirected != null) {
					redirections.add(stEvent);
					continue;
				}
				
				TreeItem parent = dependencyToTreeItemMap.get(stEvent.getParent());				
				buildEntryForSolution( parent, stEvent, TraversingType.pom);
				
			}
			else {
				DependencyTraversingEvent dtEvent = (DependencyTraversingEvent) event;
				Dependency dependency = dtEvent.getDependency();
				String tag = NameParser.buildName( dependency);
				int index = dtEvent.getIndex();			
				boolean endPoint = event.getEndpoint();
				TreeItem parent = artifactToTreeItemMap.get( dtEvent.getParent());
				
				buildEntryForDependency(dependencyToTreeItemMap, dependency, tag, index, endPoint, parent);
			}					
		}	
		
	
		
		
				
		// parent -> parent

		Map<Artifact, ParentContainer> parentAssociationMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		parentAssociationMap.putAll( monitoringResult.getParentContainerAssociationMap());
		
		Map<Artifact, Solution> childToParentAssociationMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec()); 
		childToParentAssociationMap.putAll( monitoringResult.getParentAssociationMap());
		
		Set<Artifact> processedArtifacts = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
		boolean foundAny;
		do {
			foundAny = false;
			for (Entry<Artifact, Solution> entry : childToParentAssociationMap.entrySet()) {
				if (processedArtifacts.contains(entry.getKey())) {
					continue;
				}
				// find any tree item with the artifact
				TreeItem item = nonEndPointArtifactTreeItems.get(entry.getKey());
				if (item != null) {			
					foundAny = true;
					System.out.println("item found for [" + NameParser.buildName( entry.getKey()) + "]");
					// attach parent  
					//
					ParentContainer container = parentAssociationMap.get( entry.getValue());
					if (container == null) {
						System.out.println("no container found for [" + NameParser.buildName( entry.getValue()) + "]");					
					}
					else {
						buildEntryForParent(item,  container.getParent(), container.getImports());
						processedArtifacts.add(entry.getKey());
					}
				}
			}
		} while (foundAny);
		if (processedArtifacts.size() != parentAssociationMap.size()) {
			System.out.println("not all parents were processed !");
			Set<Artifact> expected = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
			expected.addAll( parentAssociationMap.keySet());
			expected.removeAll( nonEndPointArtifactTreeItems.keySet());
			for (Artifact artifact : expected) {
				System.out.println("\t" + NameParser.buildName(artifact));
			}
		}
	
		/*
		
		// handle parents
		Map<Artifact, Set<TreeItem>> parentRepresentationMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		Map<Artifact, Artifact> parentToGrandParentMap = CodingMap.createHashMapBased( new ArtifactWrapperCodec());
		
		for (Entry<Artifact, Solution> entry : monitoringResult.getParentAssociationMap().entrySet()) {
			Artifact child = entry.getKey();
			Solution parent = entry.getValue();
			
			TreeItem childItem = artifactToTreeItemMap.get(child);
			if (childItem == null) {
				// no child found in tree - dropped? 
				continue;
			}
			// parent to grandparent relation only appears once, needs to be propagated later 
			SolutionTraversingEvent solutionTraversingEvent = parentToTraversingEventMap.get( child);
			if (solutionTraversingEvent != null) {				
				parentToGrandParentMap.put( child, parent);
				continue;
			}
			
			SolutionTraversingEvent event = parentToTraversingEventMap.get(parent);
			Set<TreeItem> items = parentRepresentationMap.get( parent);
			if (items == null) {
				items = new HashSet<TreeItem>();
				parentRepresentationMap.put(parent, items);
			}
			TreeItem parentItem = buildEntryForSolution(childItem, event, TraversingType.parent);
			items.add( parentItem);					
			
		}
		//
		if (parentToGrandParentMap.size() > 0) {
			for (Entry<Artifact, Artifact> entry : parentToGrandParentMap.entrySet()) {
				Set<TreeItem> items = parentRepresentationMap.get( entry.getKey());
				for (TreeItem item : items) {
					buildEntryForSolution(item, parentToTraversingEventMap.get( entry.getValue()), TraversingType.imported);
				}
			}
		}
		*/
		// handle redirections
		for (SolutionTraversingEvent event : redirections) {
			Artifact artifact = event.getArtifact();
			Artifact redirected = partToSolutionMap.get( artifact);
			// 
			TreeItem targetItem = artifactToTreeItemMap.get(redirected);
			String [] texts = new String[2];
			texts[0] = targetItem.getText(0) + " <- " + NameParser.buildName(artifact);
			texts[1] = targetItem.getText(1);
			targetItem.setText(texts);
			targetItem.setImage( redirectionImage);
			targetItem.setData( PainterKey.image.toString(), redirectionImage);
			// attach dependency information ..
			
		}
	}

	private void buildEntryForParent(TreeItem parentItem, Solution parentSolution, Collection<Solution> imports) {
		if (parentItem.getItemCount() > 0) {
			TreeItem [] items = parentItem.getItems();
			for (TreeItem suspect : items) {
				
				Solution suspectedSolution = (Solution) suspect.getData( DATAKEY_SOLUTION);
				if (suspectedSolution != null && ArtifactProcessor.artifactEquals( parentSolution, suspectedSolution))
					return;						
			}
		}
		
		TreeItem item = new TreeItem( parentItem, SWT.NONE);
		
						
		item.setText(  new String [] { NameParser.buildName(parentSolution), "", ""});
		
		item.setData(DATAKEY_SOLUTION, parentSolution);
		
				
		Integer index = 0;	
		Solution solution = (Solution) parentItem.getData(DATAKEY_SOLUTION);
		if (solution != null) {
			 Integer level = solution.getHierarchyLevel();
			 index = level + 1;
		}
		else {		
			index += 1;
		}
		item.setData( DATAKEY_LEVEL, index);
		item.setImage(parentPomImage);
		item.setData(PainterKey.image.toString(), parentPomImage);
		// 
		PartTuple pomTuple = PartTupleProcessor.createPomPartTuple();
		Part pomPart = SolutionProcessor.getPart(parentSolution, pomTuple);
		if (pomPart != null)
			item.setData( DATAKEY_POM, new File(pomPart.getLocation()));
		
		nonEndPointArtifactTreeItems.put(parentSolution, item);
		
		Set<Solution> codingSolutions = CodingSet.createHashSetBased( new SolutionWrapperCodec());
		codingSolutions.addAll( imports);
		
		for (Solution importSolution : codingSolutions) {
			buildEntryForImport(item, pomTuple, importSolution);			
		}			
	}

	private void buildEntryForImport(TreeItem item, PartTuple pomTuple, Solution importSolution) {
		TreeItem importItem = new TreeItem( item, SWT.NONE);
		importItem.setText( new String [] { NameParser.buildName(importSolution), "", ""});
		importItem.setImage(importPomImage);
		importItem.setData(PainterKey.image.toString(), importPomImage);
		
		nonEndPointArtifactTreeItems.put( importSolution, importItem);
		Part importPomPart = SolutionProcessor.getPart(importSolution, pomTuple);
		if (importPomPart != null) {
			item.setData( DATAKEY_POM, new File(importPomPart.getLocation()));
		}
		// set the same index as the parent + 1
		int index = ((Solution) item.getData(DATAKEY_SOLUTION)).getHierarchyLevel() + 1;
		importItem.setData(DATAKEY_LEVEL, index);
		Solution importParent = importSolution.getResolvedParent();
		if (importParent != null) {			
			buildEntryForParent(importItem, importParent,  importParent.getImported());
		}
		
		// 
		Set<Solution> importsOfImport = CodingSet.createHashSetBased( new SolutionWrapperCodec());
		importsOfImport.addAll( importSolution.getImported());
		for (Solution importOfImport : importsOfImport) {
			buildEntryForImport(importItem, pomTuple, importOfImport);
			
		}
	}
	
	
	
	
 
	private void buildEntryForDependency(Map<Dependency, TreeItem> dependencyToTreeItemMap, Dependency dependency, String tag, int index, boolean endPoint, TreeItem parent) {
		TreeItem item;
		if (parent != null) {
			item = new TreeItem( parent, SWT.NONE);
		}
		else {				
			item = new TreeItem( tree, SWT.NONE);
		}
		item.setData( DATAKEY_DEPENDENCY, dependency);
		dependencyToTreeItemMap.put(dependency, item);			
		List<String> texts = new ArrayList<String>();
		texts.add( tag);
		texts.add( "");
		texts.add( "" + index);				
		item.setText( texts.toArray( new String[0]));
		if (endPoint) {
			Image image = endPointImage;
			item.setImage(image);
			item.setData(PainterKey.image.toString(), image);
			item.setData( DATAKEY_END_POINT, true);
		}			
		else {
			nonEndPointDependencyTreeItems.put(dependency, item);
		}
		item.setData( DATAKEY_LEVEL, index);
	}
		
	private TreeItem buildEntryForSolution( TreeItem parent, SolutionTraversingEvent stEvent, TraversingType traversingType) {
		Artifact artifact = stEvent.getArtifact();
		String tag = NameParser.buildName( artifact);
		int index = stEvent.getIndex();
		boolean valid = stEvent.getValidity();			
		
		boolean endPoint = stEvent.getEndpoint();
		
		TreeItem item;
		if (parent != null) {
			item = new TreeItem( parent, SWT.NONE);
		}
		else {
			item = new TreeItem( tree, SWT.NONE);
		}
		item.setData( DATAKEY_SOLUTION, artifact);
		
		artifactToTreeItemMap.put( artifact, item);					
		if (!endPoint) {
			nonEndPointArtifactTreeItems.put( artifact, item);
		}
		
		// attach pom file
		String location = stEvent.getLocation();
		if (location == null) {
			PartTuple pomPartTuple = PartTupleProcessor.createPomPartTuple();
			for (Part part : stEvent.getArtifact().getParts()) {
				if (PartTupleProcessor.equals(pomPartTuple, part.getType())) {
					location = part.getLocation();
					break;
				}
			}
		}
				
		if (location != null) {
			item.setData( DATAKEY_POM, new File( location));
		}
		item.setData( DATAKEY_LEVEL, index);
					
		Image image;
		
		switch (traversingType) {
				
			case parent: {
				image = parentPomImage;
				break;
			}
			case imported: {
				image = importPomImage;
				break;
			}
			default:
			case pom : 
				if (valid == false) {
					image = invalidPomImage;
				}
				else if (endPoint) {
					image = endPointImage;
					item.setData( DATAKEY_END_POINT, true);
				}
				else {
					image = validPomImage;				
				}
				break;
		}		
		
		item.setImage(image);
		item.setData(PainterKey.image.toString(), image);		
		if (location!= null) {
			item.setData( PainterKey.tooltip.toString(), location);
		}

		List<String> texts = new ArrayList<String>();
		texts.add( tag);				
		
		texts.add( "" + index);				
		item.setText( texts.toArray( new String[0]));

		
		for (Solution imported : artifact.getImported()) {		
			Set<Solution> alreadyAttachedImports = treeItemsToImports.get( parent);
			if (alreadyAttachedImports == null) {
				alreadyAttachedImports = CodingSet.createHashSetBased( new SolutionWrapperCodec());
				treeItemsToImports.put( parent, alreadyAttachedImports);
			}
			else {
				if (alreadyAttachedImports.contains( imported))
					continue;
				alreadyAttachedImports.add(imported);
				
			}
			SolutionTraversingEvent importedTraversingEvent = importToTraversingEventMap.get(imported);
			TreeItem importItem = buildEntryForSolution( item, importedTraversingEvent, TraversingType.imported);		
			// already attached? 
			Dependency parentDependency = imported.getParent();
			if (parentDependency != null) {
				Solution parentSolution = null;
				Set<Solution> solutions = parentDependency.getSolutions();//.toArray( new Solution[0]);
				if (solutions.size() > 0)  {
					parentSolution = solutions.toArray( new Solution[0])[0];
				}	
				else {
					System.out.println("no solution for parent dependency [" + NameParser.buildName( parentDependency) + "]");
				}
				if (parentSolution != null) {
					if (!insertImports.contains(parentSolution)) {
						// check if already inserted ... 
						buildEntryForSolution(importItem, parentToTraversingEventMap.get(parentSolution), TraversingType.parent);
						insertImports.add(parentSolution);
					}
					else {
						System.out.println("already inserted");
					}
				}				
			}
		}
		
		return item;
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


	@Override
	public void importPom() {
		TreeItem [] items = tree.getSelection();
		if (items != null) {
			PomLoader.loadPom(items);
		}		
	}

	@Override
	public String apply(ClipboardContentsProviderMode mode) throws RuntimeException {				
		TreeItem [] items = tree.getItems();
		
		StringBuilder builder = new StringBuilder();
		for (TreeItem item : items) {
			if (builder.length() > 0)
				builder.append( System.lineSeparator());
			builder.append( attachItemData(item, mode, 0));
		}		
		return builder.toString();
	}
		
	
	private String attachItemData( TreeItem item, ClipboardContentsProviderMode mode, int offset) {
		Artifact solution = (Artifact) item.getData( DATAKEY_SOLUTION);
		StringBuilder builder = new StringBuilder();		
		
		if (solution != null) {
			Integer level = (Integer) item.getData( DATAKEY_LEVEL);
			if (level == null) {
				level = 0;
			}
			for (int i = 0; i < level  + offset; i++) {
				builder.append("\t");
			}
			 switch (mode) {		 	
				case enhanced:
					File pom = (File) item.getData( DATAKEY_POM);
					builder.append( pom != null ? pom.getAbsolutePath() : "<unknown>");
					break;
				case standard:
					builder.append( NameParser.buildName(solution));
					break;
			 }
		}
		// end point, so search for continuation...
		if (Boolean.TRUE.equals(item.getData( DATAKEY_END_POINT))) {
		/*	
			Object obj = item.getData( DATAKEY_SOLUTION);
			if (obj != null) {
				TreeItem surrogate = nonEndPointArtifactTreeItems.get(obj);				
				if (surrogate != null) {
					int surrogateOffset = (int) surrogate.getData( DATAKEY_LEVEL);
					iterateOverChildren(surrogate, mode, surrogateOffset, builder);
					return builder.toString(); 
				}
			}
			else {
				obj = item.getData( DATAKEY_DEPENDENCY);
				TreeItem surrogate = nonEndPointDependencyTreeItems.get(obj);				
				if (surrogate != null) {
					int surrogateOffset = (int) surrogate.getData( DATAKEY_LEVEL);
					iterateOverChildren(surrogate, mode, surrogateOffset, builder);
					return builder.toString(); 
				}
			}
			*/
			return builder.toString();
		}
		
		iterateOverChildren(item, mode, offset, builder);
		return builder.toString();
	}

	private void iterateOverChildren(TreeItem item, ClipboardContentsProviderMode mode, int offset, StringBuilder builder) {
		TreeItem [] items = item.getItems();
		for (TreeItem child : items) {
			String childData = attachItemData(child, mode, offset);
			if (childData.length() > 0) {					
				if (builder.length() > 0) { 
					builder.append( System.lineSeparator());
				}
				builder.append( childData);
			}			 
		 }
	}

	

	@Override
	public boolean supportsMode(ClipboardContentsProviderMode mode) {
		switch (mode) {
			default:
			return true;
		}
	}

	@Override
	protected void broadcastTabState() {
		if (!ensureMonitorData()) {
			super.broadcastTabState();
			return;
		}
		List<TraversingEvent> traversingEvents = monitoringResult.getTraversingEvents();
		if (traversingEvents == null || traversingEvents.size() == 0) {
			super.broadcastTabState();
		}
		setTabState( DependencyViewTabState.validState);
	}
		

	
	

 	

}
