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
package com.braintribe.devrock.greyface.view.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.greyface.process.retrieval.TempFileHelper;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

public class TreeItemHelper implements HasTreeTokens{
	private static Logger log = Logger.getLogger(TreeItemHelper.class);
	
	private static final String METADATA_KEY = "maven-metadata.xml";	
	

	/**
	 * recursively get all tree items starting from a tree item	
	 */
	private static List<TreeItem> collectTreeItems( TreeItem treeItem) {
		List<TreeItem> items = new ArrayList<TreeItem>();
		TreeItem [] treeItems = treeItem.getItems();
		for (TreeItem item : treeItems) {
			items.add( item);
			items.addAll( collectTreeItems(item));
		}
		return items;
	}
	/**
	 * recursively get all tree items, start from a tree 	
	 */
	public static List<TreeItem> collectTreeItems( Tree tree) {
		List<TreeItem> items = new ArrayList<TreeItem>();
		TreeItem [] treeItems = tree.getItems();
		for (TreeItem item : treeItems) {
			items.add( item);
			items.addAll( collectTreeItems(item));
		}
		return items;
	}

	/**
	 * find tree item that has a certain stored {@link Identification} attached 	
	 */
	public static TreeItem findTreeItem( Collection<TreeItem> items, Identification artifact) {
		for (TreeItem item : items) {
			Identification suspect = (Identification) item.getData(KEY_ARTIFACT);
			if (
					artifact instanceof Artifact &&
					suspect instanceof Artifact
				) { 
				if (ArtifactProcessor.artifactEquals( (Artifact) suspect, (Artifact) artifact)) {
					return item;
				}
			}
			else if (
					artifact instanceof Dependency &&
					suspect instanceof Dependency
					) {
				if (ArtifactProcessor.coarseDependencyEquals((Dependency) artifact, (Dependency) suspect)) {
						return item;
				}
			}
		}
		return null;
	}
	
	/**
	 * check if the file's a maven metadata file 	
	 */
	private static boolean isMetaDataFile( String name) {
		if (
				name.endsWith( METADATA_KEY) ||
				name.endsWith( METADATA_KEY + ".md5") ||
				name.endsWith( METADATA_KEY + ".sha1")
			) {
			return true;
		}
		return false;			
	}
	
	/**
	 * build a tuple that contains all info to identify a part
	 * @param part - the part to extract it's information from 
	 * @return - the {@link PartMatchTuple} containing the retrieved information 
	 */
	public static PartMatchTuple buildMatchPartTuple( Part part) {
		PartMatchTuple tuple = new PartMatchTuple();
		tuple.part = part;		
		tuple.partVersionAsString = VersionProcessor.toString( part.getVersion());
		
		
		tuple.location = part.getLocation().replace('\\', '/');
		tuple.isTempFile = TempFileHelper.isATempFile( tuple.location);
		if (tuple.isTempFile) {
			tuple.extractedLocation = TempFileHelper.extractFilenameFromTempFile( tuple.location);
			tuple.partFileName = tuple.extractedLocation.substring( tuple.extractedLocation.lastIndexOf( '/') + 1);
			tuple.isMetaData = isMetaDataFile( tuple.partFileName);
			if (tuple.isMetaData) {
				tuple.isGroupMetaData = tuple.partFileName.startsWith(".grp.");
			}
		}
		else {
			tuple.partFileName = tuple.location.substring( tuple.location.lastIndexOf( '/') + 1);
			tuple.isMetaData = isMetaDataFile( tuple.partFileName);
			if (tuple.isMetaData) {
				if (tuple.partVersionAsString != null) {
					tuple.isGroupMetaData = !tuple.location.endsWith( tuple.partVersionAsString + "/" + tuple.partFileName);
				}
				else {
					log.warn("cannot determine group metdata by analyzing location");
				}
			}
		}
		return tuple;
	}
	
	/**
	 * find the tree item referencing the passed {@link Part}	
	 */
	public static TreeItem findTreeItem( Collection<TreeItem> items, Part artifact) {
		PartMatchTuple matchTuple = buildMatchPartTuple(artifact);
		
		for (TreeItem item : items) {
			Part suspect = (Part) item.getData(KEY_PART);
			if (suspect == null)
				continue;
			if (suspect == artifact)
				return item;
			
			PartMatchTuple suspectMatchTuple = buildMatchPartTuple(suspect);
			
			if (matchTuple.partFileName.equalsIgnoreCase(suspectMatchTuple.partFileName)) {
				// matching file names
				if (matchTuple.isMetaData && suspectMatchTuple.isMetaData) {
					// both are metadata, if either are both group metadata or both not group meta data, it's a match 
					if (
							(matchTuple.isGroupMetaData && suspectMatchTuple.isGroupMetaData) ||
							(!matchTuple.isGroupMetaData && !suspectMatchTuple.isGroupMetaData)
						) { 
						return item;
					}
					// different types .. can't be the same 
					continue;	
				}
				// same name, no meta data involved, so it's a match 
				return item;
			}
		}
		// not match found
		return null;
	}
	
	public static Set<TreeItem> findTreeItems( Collection<TreeItem> items, Identification artifact) {
		
		Set<TreeItem> result = new HashSet<TreeItem>();
		
		for (TreeItem item : items) {
			Identification suspect = (Identification) item.getData(KEY_ARTIFACT);
			if (
					artifact instanceof Artifact &&
					suspect instanceof Artifact
				) { 
				if (ArtifactProcessor.artifactEquals( (Artifact) suspect, (Artifact) artifact)) {
					result.add(item);
				}
			}
			else if (
					artifact instanceof Dependency &&
					suspect instanceof Dependency
					) {
				if (ArtifactProcessor.coarseDependencyEquals((Dependency) artifact, (Dependency) suspect)) {
						result.add( item);
				}
			}
		}
		return result;
	}
	
	public static void fillTreeItem( TreeItem item, RepositorySetting setting, Identification artifact, boolean shortName) {		
		String name;  
		String condensedName = "";
	
		if (artifact instanceof Solution) {
			condensedName = NameParser.buildName((Solution) artifact);
			name = artifact.getArtifactId() + "-" + VersionProcessor.toString( ((Solution) artifact).getVersion());			
		} 
		else {
			condensedName = NameParser.buildName((Dependency) artifact);
			name = artifact.getArtifactId() + "-" + VersionRangeProcessor.toString( ((Dependency) artifact).getVersionRange());				
		}
			
		if (!shortName) { 
			name = name + " (" +  artifact.getGroupId() + ")";
		} 
	
		item.setText( name);
		item.setData( KEY_ARTIFACT, artifact);					
		item.setData( KEY_NAME, name);
		if (setting != null) {
			item.setData( KEY_REPOSITORY, setting);
			item.setData( KEY_TOOLTIP, condensedName + " @ " + setting.getName());
		} 
		else {
			item.setData( KEY_TOOLTIP, condensedName + " @ <not available");
		}
	}
	
	public static void fillTreeItem( TreeItem item, RepositorySetting setting, Solution artifact) {
		fillTreeItem(item, setting, artifact, false);
	}
	
	
	public static TreeItem attachParts( TreeItem item, Solution artifact) {
		if (artifact.getParts().size() > 0) {			
			TreeItem deferredItem = new TreeItem( item, SWT.NONE);
			deferredItem.setText(KEY_DEFERRED);
			deferredItem.setData( KEY_ARTIFACT, artifact);
			return deferredItem;
		}	
		return null;
	}
	
	public static TreeItem createItem(Object parent, TreeSet<String> treeSet, Map<String, Set<TreeItem>> duplicates, RepositorySetting setting, Identification artifact) {
		String key = artifact.getArtifactId();
		if (artifact instanceof Solution) {		
			key += VersionProcessor.toString( ((Solution) artifact).getVersion());		
		}
		else if (artifact instanceof Dependency) {
			key += VersionRangeProcessor.toString( ((Dependency) artifact).getVersionRange());
		}
		
		SortedSet<String> set = treeSet.headSet( key);
		int p = set.size();
		treeSet.add(key);		
		return createItem(parent, duplicates, setting, artifact, key, p);
	}

	public static TreeItem createItem(Object parent, TreeSet<String> treeSet, Map<String, Set<TreeItem>> duplicates, RepositorySetting setting, Solution artifact) {
		String key;
	
		key = artifact.getArtifactId() + VersionProcessor.toString( artifact.getVersion());
		SortedSet<String> set = treeSet.headSet( key);
		int p = set.size();
		treeSet.add(key);	
		return createItem(parent, duplicates, setting, artifact, key, p);
	}

	public static TreeItem appendItem(Object parent, Map<String, Set<TreeItem>> duplicates, RepositorySetting setting, Identification artifact) {
		TreeItem [] items;
		if (parent instanceof Tree) {
			items = ((Tree) parent).getItems();
		}
		else {
			items = ((TreeItem)parent).getItems();
		}
		int p = 1;
		if (items == null || items.length == 0) {
			p = 0;
		}
		else {
			// sort
			List<String> names = new ArrayList<String>( items.length);
			for (TreeItem item : items) {
				names.add( item.getText());
			}
			names.add( artifact.getArtifactId());
			Collections.sort(names);
			for (int i=0; i < names.size(); i++) {
				if (names.get(i).equalsIgnoreCase( artifact.getArtifactId())) {
					if (i == 0)
						p = 1;
					else
						p = i;
					
					break;
				}
			}
		}
		// protect the parent	
		String key = artifact.getArtifactId();
		return createItem(parent, duplicates, setting, artifact, key, p);
	}
	
	
	private static TreeItem createItem(Object parent, Map<String, Set<TreeItem>> duplicates, RepositorySetting setting, Identification artifact, String key, int p) {
		TreeItem item;
		if (parent instanceof Tree) {
			Tree tree = (Tree) parent;
			if (p < tree.getItemCount())
					item = new TreeItem( (Tree) parent, SWT.NONE, p);
			else {
				item = new TreeItem( (Tree) parent, SWT.NONE);
			}
		} 
		else {
			TreeItem treeItem = (TreeItem) parent;
			if (p < treeItem.getItemCount()) {
				item = new TreeItem( (TreeItem) parent, SWT.NONE, p);
			}
			else {
				item = new TreeItem( (TreeItem) parent, SWT.NONE);
			}
		}
					
		Set<TreeItem> duplicateTreeItems = duplicates.get(key);		
		if (duplicateTreeItems == null) {		
			TreeItemHelper.fillTreeItem(item, setting, artifact, true);						
			Set<TreeItem> dups = new HashSet<TreeItem>();
			dups.add( item);
			duplicates.put( key, dups);
		}
		else { 
			// check whether we really must rename
			boolean collides = false;
			for (TreeItem duplicate : duplicateTreeItems) {
				if (checkCollision( (Identification) duplicate.getData( KEY_ARTIFACT), artifact)) {
					collides = true;
					break;
				}
			}
			if (collides) {
				// only one in there? must rename (otherwise it already has been renamed) 
				if (duplicateTreeItems.size() == 1) {
					TreeItem duplicate = duplicateTreeItems.toArray( new TreeItem[0])[0];
					Identification identification = (Identification) duplicate.getData( KEY_ARTIFACT);
					if (identification instanceof Solution) {
						Solution duplicateSolution = (Solution) identification;					
						duplicate.setText( duplicateSolution.getArtifactId() + "-" + VersionProcessor.toString( duplicateSolution.getVersion()) + " (" + duplicateSolution.getGroupId()  + ")");					
					}
					else {
						Dependency duplicateDependency = (Dependency) identification;
						duplicate.setText( duplicateDependency.getArtifactId() + "-" + VersionRangeProcessor.toString( duplicateDependency.getVersionRange()) + " (" + duplicateDependency.getGroupId()  + ")");
					}
				}
				TreeItemHelper.fillTreeItem(item, setting, artifact, false);
			}
			else {
				// no collision? use short name 
				TreeItemHelper.fillTreeItem(item, setting, artifact, true);	
			}
		
			duplicateTreeItems.add(item);
		}
		return item;
	}
	
	private static boolean checkCollision(Identification suspect, Identification comparison) {	
		if (suspect instanceof Solution && comparison instanceof Solution) {
			if (ArtifactProcessor.artifactEquals( (Artifact) suspect, (Artifact) comparison))
				return false;
		}
		if (suspect instanceof Dependency && comparison instanceof Dependency) {
			if (ArtifactProcessor.coarseDependencyEquals( (Dependency) suspect, (Dependency) comparison))
				return false;
		}
		if (suspect.getArtifactId().equalsIgnoreCase( comparison.getArtifactId()))
			return false;
		//
		return true;
		
	}
	
	public static Set<Solution> selectArtifactsByHierarchy( Solution solution ){				
		return getRelevantSolutions( solution);		
	}
	
	private static Set<Solution> getRelevantSolutions( Solution solution){
		Set<Solution> result = new HashSet<Solution>();
		result.add(solution);
		Solution parent = solution.getResolvedParent();
		if (parent != null) {
			result.addAll( getRelevantSolutions(parent));
		}
		for (Solution imported : solution.getImported()) {
			result.addAll( getRelevantSolutions(imported));
		}
		for (Dependency dependency : solution.getDependencies()) {
			for (Solution dependentSolution : dependency.getSolutions()) {
				result.addAll( getRelevantSolutions(dependentSolution));
			}
		}
		return result;
	}
	
	/**
	 * build an name for the part with special treatment of maven-metadata.xml files 
	 */
	public static String getName( Part part) {
		PartMatchTuple tuple = TreeItemHelper.buildMatchPartTuple(part);
		
		String name = tuple.partFileName;
		if (tuple.isGroupMetaData) {
			if (name.startsWith( ".grp.")) {
				name = name.substring(4);
			}
			name = "../" + name;
		}
		return name;
	}
	
}
