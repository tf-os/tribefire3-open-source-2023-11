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
package com.braintribe.devrock.zed.forensics.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.zarathud.model.data.Artifact;

/**
 * an entry in the {@link DependencyStructureRegistry}, containing all tags a dependency/solution has been adorned with
 * @author pit
 *
 */
public class DependencyStructureEntry {
	private String name;
	private Map<String, List<String>> requestorToTagMap = new HashMap<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, List<String>> getRequestorToTagMap() {
		return requestorToTagMap;
	}
	public void setRequestorToTagMap(Map<String, List<String>> requestorToTagMap) {
		this.requestorToTagMap = requestorToTagMap;
	}
	
	public DependencyStructureEntry(String key) {
		name = key;
	}
	
	
	/**	  
	 * @param requester - the name of the requesting solution (as condensed name) 
	 * @return - a {@link List} with all collected tags of this requester
	 */
	public List<String> getDependencyTagViaRequestor( String requester) {
		return requestorToTagMap.get(requester);
	}
	
	/**
	 * @param artifact - the requesting artifact 
	 * @return - a {@link List} with all collected tags of this requester 
	 */
	public List<String> getDependencyTagViaRequestor( Artifact artifact) {
		return requestorToTagMap.get( artifact.toVersionedStringRepresentation());
	}
	
	/**
	 * @return - a {@link List} with *all* tags combined 
	 */
	public List<String> getAllDependencyTags() {
		Set<String> result = new HashSet<>();
		for (List<String> toAdd : requestorToTagMap.values()) {
			result.addAll( toAdd);
		}
		return new ArrayList<>(result);
	}
	
	
}
