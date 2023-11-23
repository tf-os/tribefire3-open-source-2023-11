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
package com.braintribe.devrock.importer.dependencies.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.model.version.VersionRange;

/**
 * content provider for the tree viewer in the page
 * 
 * requires the list of {@link RemoteCompiledArtifactIdentification} to be sorted acc
 * 	- groupid
 *  - artifactid
 *  - version
 *  
 * supports a 'getnext'/'getprevious/ version.
 * @author pit
 *
 */
public class ContentProvider implements ITreeContentProvider {
	
	private Map<String, List<RemoteCompiledDependencyIdentification>> groupToArtifactMap = new LinkedHashMap<>();
	private RemoteCompiledDependencyIdentification currentRcai;
	private RemoteCompiledDependencyIdentification firstRcai;
	private RemoteCompiledDependencyIdentification lastRcai;
	
	/**
	 * @param ecais - sets up the {@link ContentProvider} with the currently known {@link RemoteCompiledArtifactIdentification}
	 */
	public int setupFrom(List<RemoteCompiledDependencyIdentification> ecais, VersionModificationAction vma, int maxResult) {
		
		
		groupToArtifactMap.clear();
		if (ecais == null)
			return 0;
		
		// filter
		List<RemoteCompiledDependencyIdentification> filteredCdis = filter( ecais, vma);
		// cut to size
		int size = filteredCdis.size();
		if (size > maxResult) {
			filteredCdis = filteredCdis.subList(0, maxResult);
		}
		// setup
		for (RemoteCompiledDependencyIdentification ecai : filteredCdis) {
			if (firstRcai == null)
				firstRcai = ecai;
			lastRcai = ecai;
			String grpId = ecai.getGroupId();
			List<RemoteCompiledDependencyIdentification> ecaisOfGroup = groupToArtifactMap.computeIfAbsent( grpId, e -> new ArrayList<>());
			ecaisOfGroup.add( ecai);
		}
		return size;
	}
	
	private List<RemoteCompiledDependencyIdentification> filter( List<RemoteCompiledDependencyIdentification> raw, VersionModificationAction vma) {
		switch (vma) {
		case rangified:
			// build 'version ranges' from artifacts			
			return filterToRanges( raw);
		case referenced:
			// filter-out any duplicate '<groupid>:<artifactId> combinations.
			return filterToVariables( raw);
		case untouched:		
		default:
			return raw;						
		}
	}

	

	/**
	 * filters the input and leaves only one artifact (no matter what version)
	 * @param raw - a {@link List} of {@link RemoteCompiledArtifactIdentification} to filter
	 * @return - the filtered {@link List} of {@link RemoteCompiledArtifactIdentification} 
	 */
	private List<RemoteCompiledDependencyIdentification> filterToVariables( List<RemoteCompiledDependencyIdentification> raw) {
		Map<String, RemoteCompiledDependencyIdentification> map = new HashMap<>();
		for (RemoteCompiledDependencyIdentification rcai : raw) {
			String key = rcai.getGroupId() + ":" + rcai.getArtifactId();
			map.computeIfAbsent(key, k -> rcai);
		}
		List<RemoteCompiledDependencyIdentification> rcais = new ArrayList<>(map.values());
		rcais.sort( new Comparator<RemoteCompiledDependencyIdentification>() {

			@Override
			public int compare(RemoteCompiledDependencyIdentification o1, RemoteCompiledDependencyIdentification o2) {			
				return o1.compareTo(o2);
			}
			
		});
		
		return rcais;
	}
	
	/**
	 * scans the input, builds ranges and assigns artifacts to the ranges 
	 * @param raw - a {@link List} of {@link RemoteCompiledArtifactIdentification} to filter
	 * @return - the filtered {@link List} of {@link RemoteCompiledArtifactIdentification} 
	 */
	private List<RemoteCompiledDependencyIdentification> filterToRanges(List<RemoteCompiledDependencyIdentification> raw) {	
		Map<String, RemoteCompiledDependencyIdentification> map = new HashMap<>();
		for (RemoteCompiledDependencyIdentification rcai : raw) {					
			String id = rcai.getGroupId() + ":" + rcai.getArtifactId();
			VersionExpression versionExpression = rcai.getVersion();
			VersionRange range; 
			if (versionExpression instanceof VersionRange) {
				range = (VersionRange) versionExpression;
			}
			else if (versionExpression instanceof Version) { 			
				Version version = (Version) versionExpression;
				Integer minor = version.getMinor();
				Version lowerBound = Version.create( version.getMajor(), minor != null ? 0 : 0);
				range = VersionRange.toStandardRange( lowerBound); 
			}
			else {
				// TODO: what now?
				System.out.println("unexpected");
				continue;
			}
			RemoteCompiledDependencyIdentification ranged = RemoteCompiledDependencyIdentification.create( rcai.getGroupId(), rcai.getArtifactId(), range.toString(), rcai.getRepositoryOrigin(), rcai.getSourceOrigin());
			String key = id + "#" + range.asString();			
			map.computeIfAbsent(key, k -> ranged);
		}
		
		List<RemoteCompiledDependencyIdentification> rcais = new ArrayList<>(map.values());
		rcais.sort( new Comparator<RemoteCompiledDependencyIdentification>() {

			@Override
			public int compare(RemoteCompiledDependencyIdentification o1, RemoteCompiledDependencyIdentification o2) {			
				return o1.compareTo(o2);
			}			
		});
		
		return rcais;
	}

	@Override
	public Object[] getChildren(Object obj) {
		if (obj instanceof RemoteCompiledDependencyIdentification)
			return null;
		String grp = (String) obj;
		List<RemoteCompiledDependencyIdentification> list = groupToArtifactMap.get(grp);
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
		if (obj instanceof RemoteCompiledDependencyIdentification) {			
			return ((RemoteCompiledDependencyIdentification) obj).getGroupId();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object obj) {
		if (obj instanceof RemoteCompiledDependencyIdentification) {			
			return false;
		}		
		return true;
	}
	
	/**
	 * @return - the current selected {@link RemoteCompiledArtifactIdentification}
	 */
	public RemoteCompiledDependencyIdentification getCurrent() {
		return currentRcai;
	}
	
	
	
	/**
	 * @param grp - the current group 
	 * @return - the next group
	 */
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
	
	/**
	 * @param grp - the current group
	 * @return - the previous group
	 */
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
	
	
	/**
	 * @return - the next {@link RemoteCompiledDependencyIdentification} from the currently selected one 
	 */
	public RemoteCompiledDependencyIdentification getNext() {
		if (currentRcai == null) {
			// first
			currentRcai = firstRcai;
			return firstRcai;
		}
		if (currentRcai == lastRcai) {		
			currentRcai = firstRcai;
			return firstRcai;
		}
		
		String grp = currentRcai.getGroupId();
		List<RemoteCompiledDependencyIdentification> members = groupToArtifactMap.get(grp);
		int i = members.indexOf( currentRcai);
		int j = i+1;
		if (j > members.size() - 1) {
			String grpNext = getNextGroup(grp);
			currentRcai = groupToArtifactMap.get( grpNext).get(0);
			return currentRcai;
		}
		else {
			currentRcai = members.get( j);
			return currentRcai;
		}				
	}
	
	
	
	/**
	 * @return - the previous {@link RemoteCompiledDependencyIdentification} from the currently selected one
	 */
	public RemoteCompiledDependencyIdentification getPrevious() {
		if (currentRcai == null) {
			// last
			currentRcai = lastRcai;
			return lastRcai;
		}
		if (currentRcai == firstRcai) {
			currentRcai = lastRcai;
			return lastRcai;
		}
		
		String grp = currentRcai.getGroupId();
		List<RemoteCompiledDependencyIdentification> members = groupToArtifactMap.get(grp);
		int i = members.indexOf( currentRcai);
		int j = i-1;
		if (j < 0) {
			String grpPrevious = getPreviusGroup(grp);
			List<RemoteCompiledDependencyIdentification> list = groupToArtifactMap.get( grpPrevious);
			currentRcai = list.get( list.size()-1);
			return currentRcai;
		}
		else {
			currentRcai = members.get( j);
			return currentRcai;
		}		
	}
	
}
