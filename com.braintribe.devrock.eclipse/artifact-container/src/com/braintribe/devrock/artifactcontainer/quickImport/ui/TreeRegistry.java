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
package com.braintribe.devrock.artifactcontainer.quickImport.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.build.quickscan.agnostic.SourceArtifactWrapperCodec;
import com.braintribe.build.quickscan.agnostic.SourceArtifactWrapperCodecMode;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportAction;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.model.panther.SourceRepository;

/**
 * a simple helper class to support the tree creation 
 * 
 * @author pit
 *
 */
public class TreeRegistry implements HasQuickImportTokens {


	
	
	private Tree tree;
	private Map<String, TreeItem> groupToItemMap = new HashMap<String, TreeItem>();
	private Map<SourceArtifact, TreeItem> sourceArtifactToItemMap = CodingMap.createHashMapBased( new SourceArtifactWrapperCodec( SourceArtifactWrapperCodecMode.complexIdentification));
	private Map<Integer, TreeItem> indexToItemMap = new HashMap<Integer, TreeItem>();
	
	private DisplayMode mode;
	
	private Font italicFont;
	private Font boldFont;
	private Font boldItalicFont;
	private int index = 0;
	
	private IWorkingSet activeWorkingSet;
	private ProjectWorkspaceRelation relationFilter = ProjectWorkspaceRelation.notLoaded;
	
	/**
	 * sets the mode, either {@literal DisplayMode.condensed} or {@literal DisplayMode.explict}
	 * @param mode - the {@link DisplayMode}
	 */
	@Configurable @Required
	public void setMode(DisplayMode mode) {
		this.mode = mode;
	}
	
	@Configurable
	public void setActiveWorkingSet(IWorkingSet activeWorkingSet) {
		this.activeWorkingSet = activeWorkingSet;
	}
	
	@Configurable
	public void setRelationFilter(ProjectWorkspaceRelation relationFilter) {
		this.relationFilter = relationFilter;
	}
	
	public TreeRegistry(Tree tree) {
		this.tree = tree;
		Font initialFont = tree.getFont();
		FontData [] fontDataItalic = initialFont.getFontData();
		
		for (FontData data : fontDataItalic) {
			data.setStyle( data.getStyle() | SWT.ITALIC);		
		}
		italicFont = new Font( tree.getDisplay(), fontDataItalic);
		
		FontData [] fontDataBold = initialFont.getFontData();
		for (FontData data : fontDataBold) {
			data.setStyle( data.getStyle() | SWT.BOLD);		
		}
		boldFont = new Font( tree.getDisplay(), fontDataBold);
		
		FontData [] fontDataItalicBold = initialFont.getFontData();
		for (FontData data : fontDataItalicBold) {
			data.setStyle( data.getStyle() | SWT.BOLD | SWT.ITALIC);		
		}
		boldItalicFont = new Font( tree.getDisplay(), fontDataItalicBold);
	}
	
	public void dispose() {
		italicFont.dispose();
		boldFont.dispose();
		boldItalicFont.dispose();
	}
	
	/**
	 * acquires a {@link TreeItem} for the group
	 * @param groupId - the GroupId as a {@link String}
	 * @return - the {@link TreeItem} associated or created 
	 */
	public TreeItem acquireTreeItemForGroup( String groupId) {
		TreeItem result = groupToItemMap.get( groupId);
		
		if (result == null) {
			result = createGroupTreeItem(tree, groupId, groupId);
		}
		return result;
	}
	
	/**
	 * acquires a {@link TreeItem} for a {@link SourceArtifact}
	 * @param artifact - the {@link SourceArtifact}
	 * @return - the {@link TreeItem} associated or created
	 */
	public TreeItem acquireTreeItemForArtifact( SourceArtifact artifact, QuickImportAction importAction) {
		TreeItem result = null;
		
		switch (mode) {
			case condensed:
				result = sourceArtifactToItemMap.get(artifact);
				
				if (result == null) {
					TreeItem parent = acquireTreeItemForGroup( artifact.getGroupId());
					result = createArtifactTreeItem(parent, artifact, false);
					if (
							importAction == QuickImportAction.importProject && 
							!Boolean.TRUE.equals(result.getData(MARKER_AVAILABLE))
						) {					
						result.setFont(italicFont);
						result.setData(MARKER_TOOLTIP, NO_IMPORT);
					} else {
						result.setFont(boldFont);
					}
				} else {
					result.setFont( tree.getFont());			
					String versions = result.getText(1);
					versions += ", " + artifact.getVersion();
					result.setText(1, versions);				
				}
				break;
			case explicit:
				TreeItem parent = acquireTreeItemForGroup( artifact.getGroupId());
				result = createArtifactTreeItem(parent, artifact, true);
				if (
						importAction == QuickImportAction.importProject && 
						!Boolean.TRUE.equals(result.getData(MARKER_AVAILABLE))
					) {
					result.setFont(italicFont);
					result.setData(MARKER_TOOLTIP, NO_IMPORT);
				}
				else {
					result.setFont(boldFont);
				}
				break;
			}
		return result;
	}
	
	/**
	 * returns the {@link TreeItem} stored at index 
	 * @param index - the position in the map
	 */
	public TreeItem getTreeItemForIndex( int index) {
		return indexToItemMap.get( index);
	}
	
	public Collection<TreeItem> getTreeItems() {
		return indexToItemMap.values();
	}
	/**
	 * returns the number of items in the list 
	 */
	public int getArtifactCount(){
		return indexToItemMap.size();
	}
	
	
	/**
	 * create a {@link TreeItem} for a group 
	 * @param parent - the parent, either a {@link Tree} or a {@link TreeItem}
	 * @param value - the name of group 
	 * @return - the created {@link TreeItem}
	 */
	private TreeItem createGroupTreeItem( Object parent, String value, String fullName){
		TreeItem item = null;
		if (parent instanceof Tree) {
			item = new TreeItem( (Tree) parent, SWT.NONE);
		} else {
			item = new TreeItem( (TreeItem) parent, SWT.NONE);
		}
		List<String> labels = new ArrayList<String>();
		labels.add(value);
		labels.add("");
		item.setText( labels.toArray( new String[0]));
		item.setFont(italicFont);
		groupToItemMap.put( fullName, item);
		return item;
	}
	
	
	/**
	 * create a {@link TreeItem} for a {@link SourceArtifact}
	 * @param parent - the {@link TreeItem} that is the parent 
	 * @param artifact - the {@link SourceArtifact} that is to be reflected 
	 * @return - the {@link TreeItem} that was created 
	 */
	private TreeItem createArtifactTreeItem( TreeItem parent, SourceArtifact artifact, boolean attachSource) {			
		TreeItem item = new TreeItem( parent, SWT.None);
		List<String> texts = new ArrayList<String>();
		String artifactId = artifact.getArtifactId();
		SourceRepository repository = artifact.getRepository();
		if (repository != null) {
			texts.add( artifactId + " (" + repository.getName() + ")");
		}
		else {		
			texts.add( artifactId);
		}
		
		texts.add( artifact.getVersion());		
		item.setText( texts.toArray( new String[0]));
		
		item.setData( MARKER_ARTIFACT, artifact);

		// check if we can load that artifact		
		ProjectWorkspaceRelation projectWorkspaceRelation = isArtifactLoaded(artifact);
		switch (relationFilter) {
			case notLoaded: // must be not loaded 
				if (projectWorkspaceRelation == ProjectWorkspaceRelation.notLoaded) { 
					item.setData( MARKER_AVAILABLE, new Boolean(true));
				}
				else {
					item.setData( MARKER_AVAILABLE, new Boolean(false));
				}
				break;
			case presentInWorkspace: // can be either not loaded or in the workspace, but NOT in the current working set
				if (projectWorkspaceRelation != ProjectWorkspaceRelation.presentInCurrentWorkingSet) { 
					item.setData( MARKER_AVAILABLE, new Boolean(true));
				}
				else {
					item.setData( MARKER_AVAILABLE, new Boolean(false));
					item.setData(MARKER_TOOLTIP, NO_IMPORT);
				}
				break;
			default:
				item.setData( MARKER_AVAILABLE, new Boolean(false));
				item.setData(MARKER_TOOLTIP, NO_IMPORT);
				break;
			
		}
		 
		item.setData( DATA_AVAILABLITY_STATE, projectWorkspaceRelation);
		indexToItemMap.put( new Integer( index++), item);
			
		//artifactToItemMap.put( artifact.getGroupId() + ":" + artifactId, item);
		sourceArtifactToItemMap.put(artifact, item);
		return item;
	}

	/**
	 * checks how an artifact's present in the workspace 
	 * @param sourceArtifact - the {@link SourceArtifact}
	 * @return - the {@link ProjectWorkspaceRelation} showing the loading state
	 */
	private ProjectWorkspaceRelation isArtifactLoaded( SourceArtifact sourceArtifact) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( sourceArtifact.getGroupId());
		artifact.setArtifactId( sourceArtifact.getArtifactId());
		try {
			artifact.setVersion( VersionProcessor.createFromString( sourceArtifact.getVersion()));
		} catch (VersionProcessingException e) {
			String msg =  "cannot look up source artifact as the version is invalid ([" + sourceArtifact.getVersion() +"]";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);			
			return ProjectWorkspaceRelation.notLoaded;
		}
				
		IProject project = ArtifactContainerPlugin.getWorkspaceProjectRegistry().getProjectForArtifact(artifact);
		if (project != null) {					
			if (activeWorkingSet != null) {
				for (IAdaptable adaptable : activeWorkingSet.getElements()) {
					IJavaProject javaProject = adaptable.getAdapter(IJavaProject.class);					
					if (javaProject != null && project == javaProject.getProject()) {
						return ProjectWorkspaceRelation.presentInCurrentWorkingSet;							
					}
				}
			}						
			return ProjectWorkspaceRelation.presentInWorkspace;
		}		
		return ProjectWorkspaceRelation.notLoaded;		
	}

}
