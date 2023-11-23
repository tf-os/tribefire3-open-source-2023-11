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
package com.braintribe.devrock.importer.scanner.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.devrock.eclipse.model.identification.SelectableEnhancedCompiledArtifactIdentification;

/**
 * content provider for the tree viewer in the page
 * requires the list of {@link SelectableEnhancedCompiledArtifactIdentification} to be sorted acc
 * 	- groupid
 *  - artifactid
 *  - version
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {
	
	private Map<String, List<SelectableEnhancedCompiledArtifactIdentification>> groupToArtifactMap = new LinkedHashMap<>();
	private SelectableEnhancedCompiledArtifactIdentification currentSecai;
	private SelectableEnhancedCompiledArtifactIdentification firstSecai;
	private SelectableEnhancedCompiledArtifactIdentification lastSecai;
	
	public void setupFrom(List<SelectableEnhancedCompiledArtifactIdentification> ecais) {
		groupToArtifactMap.clear();
		if (ecais == null)
			return;
		for (SelectableEnhancedCompiledArtifactIdentification ecai : ecais) {
			if (firstSecai == null)
				firstSecai = ecai;
			lastSecai = ecai;
			String grpId = ecai.getGroupId();
			List<SelectableEnhancedCompiledArtifactIdentification> ecaisOfGroup = groupToArtifactMap.computeIfAbsent( grpId, e -> new ArrayList<>());
			ecaisOfGroup.add( ecai);
		}				
	}
	

	@Override
	public Object[] getChildren(Object obj) {
		if (obj instanceof SelectableEnhancedCompiledArtifactIdentification)
			return null;
		String grp = (String) obj;
		List<SelectableEnhancedCompiledArtifactIdentification> list = groupToArtifactMap.get(grp);
		if (list == null)
			return null;
		return list.toArray();
	}

	@Override
	public Object[] getElements(Object obj) {		
		return groupToArtifactMap.keySet().toArray();
	}

	@Override
	public Object getParent(Object obj) {
		if (obj instanceof SelectableEnhancedCompiledArtifactIdentification) {			
			return ((SelectableEnhancedCompiledArtifactIdentification) obj).getGroupId();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object obj) {
		if (obj instanceof SelectableEnhancedCompiledArtifactIdentification) {			
			return false;
		}		
		return true;
	}
	public SelectableEnhancedCompiledArtifactIdentification getCurrent() {
		return currentSecai;
	}
	
	
	
	private String getNextGroup( String grp) {
		List<String> sortedGroups = new ArrayList<>( groupToArtifactMap.keySet());
		int i = sortedGroups.indexOf(grp);
		int j = i+1;
		if (j > sortedGroups.size() - 1) {
			return sortedGroups.get(0);
		}
		else {
			return sortedGroups.get( j);
		}
	}
	
	private String getPreviusGroup( String grp) {
		List<String> sortedGroups = new ArrayList<>( groupToArtifactMap.keySet());
		int i = sortedGroups.indexOf(grp);
		int j = i-1;
		if (j < 0) {
			return sortedGroups.get( sortedGroups.size()-1);
		}
		else {
			return sortedGroups.get( j);
		}
	}
	
	
	public SelectableEnhancedCompiledArtifactIdentification getNext() {
		if (currentSecai == null) {
			// first
			currentSecai = firstSecai;
			return firstSecai;
		}
		if (currentSecai == lastSecai) {		
			currentSecai = firstSecai;
			return firstSecai;
		}
		
		String grp = currentSecai.getGroupId();
		List<SelectableEnhancedCompiledArtifactIdentification> members = groupToArtifactMap.get(grp);
		int i = members.indexOf( currentSecai);
		int j = i+1;
		if (j > members.size() - 1) {
			String grpNext = getNextGroup(grp);
			currentSecai = groupToArtifactMap.get( grpNext).get(0);
			return currentSecai;
		}
		else {
			currentSecai = members.get( j);
			return currentSecai;
		}				
	}
	
	
	
	public SelectableEnhancedCompiledArtifactIdentification getPrevious() {
		if (currentSecai == null) {
			// last
			currentSecai = lastSecai;
			return lastSecai;
		}
		if (currentSecai == firstSecai) {
			currentSecai = lastSecai;
			return lastSecai;
		}
		
		String grp = currentSecai.getGroupId();
		List<SelectableEnhancedCompiledArtifactIdentification> members = groupToArtifactMap.get(grp);
		int i = members.indexOf( currentSecai);
		int j = i-1;
		if (j < 0) {
			String grpPrevious = getPreviusGroup(grp);
			List<SelectableEnhancedCompiledArtifactIdentification> list = groupToArtifactMap.get( grpPrevious);
			currentSecai = list.get( list.size()-1);
			return currentSecai;
		}
		else {
			currentSecai = members.get( j);
			return currentSecai;
		}		
	}
	
	
	

}
