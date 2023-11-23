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
package com.braintribe.devrock.api.ui.viewers.artifacts.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer.NodeComparator;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.importer.CamelCasePatternExpander;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

public class NodeFilter {
	private static Logger log = Logger.getLogger(NodeFilter.class);
	private CamelCasePatternExpander patternExpander = new CamelCasePatternExpander();
	private enum Notation { group,artifact,version, all}
	private static final NodeComparator nodeComparator = new NodeComparator();
	
	/**
	 * filters the nodes via a contains("<expression>") 
	 * @param expression
	 * @param nodes
	 * @return
	 */
	public List<Node> filterNodesViaContains(final String expression, List<Node> nodes) {
		List<Node> result = new ArrayList<>(nodes.size());
		Map<VersionedArtifactIdentification, Node> vaiToNode = createFilterMap(nodes);
		
		Predicate<VersionedArtifactIdentification> predicate = new Predicate<VersionedArtifactIdentification>() {
			@Override
			public boolean test(VersionedArtifactIdentification t) {
				String s = t.asString();
				return s.contains(expression);
			}			
		};
		List<VersionedArtifactIdentification> resultingVais = vaiToNode.keySet().parallelStream().filter(predicate).collect( Collectors.toList());

		for (VersionedArtifactIdentification vai : resultingVais) {
			result.add( vaiToNode.get(vai));
		}		
		result.sort( nodeComparator);
		return result;
	}	
	
	/**
	 * filters the node via CamelCase to '<camel>-<case>' algo as in QI
	 * @param expression
	 * @param nodes
	 * @return
	 */
	public List<Node> filterNodes( String expression, List<Node> nodes) {
		List<Node> result = new ArrayList<>(nodes.size());
		Map<VersionedArtifactIdentification, Node> vaiToNode = createFilterMap(nodes);
		
		Predicate<VersionedArtifactIdentification> predicate = processExpression(expression);
		List<VersionedArtifactIdentification> resultingVais = vaiToNode.keySet().parallelStream().filter(predicate).collect( Collectors.toList());
		for (VersionedArtifactIdentification vai : resultingVais) {
			result.add( vaiToNode.get(vai));
		}		
		result.sort( nodeComparator);
		return result;
	}

	/**
	 * creates a 'filterable' map the contains the nodes
	 * @param nodes - the {@link Node}s from a view
	 * @return - a {@link Map} of the {@link VersionedArtifactIdentification} to the {@link Node}
	 */
	private Map<VersionedArtifactIdentification, Node> createFilterMap(List<Node> nodes) {
		Map<VersionedArtifactIdentification, Node> vaiToNode = new HashMap<>();
		nodes.stream().forEach( n -> {
			AnalysisNode an = (AnalysisNode) n;
			VersionedArtifactIdentification artifactVai = an.getSolutionIdentification();
			VersionedArtifactIdentification dependencyVai = an.getDependencyIdentification();
			if (artifactVai != null) {
				vaiToNode.put(artifactVai, n);
			}
			else if (dependencyVai != null) {
				vaiToNode.put(dependencyVai, n);
			}
			else {
				log.error( "node passed has neither artifact nor dependency information, skipped");
			}
		});
		return vaiToNode;
	}
	
	/**
	 * build a predicate from the value of the QuickImporter's edit box,
	 * analyze the expression and derive the level of the query's predicate strictness
	 * - possible strictnes :
	 * 	all : groupid, artifactid, version
	 *  group : groupId, artifactId
	 *  version : artifactId, version  
	 * @param txt - the expression as {@link String}
	 * @return - the matching {@link Predicate} 
	 */
	private Predicate<VersionedArtifactIdentification> processExpression( String txt) {
		Notation notation = Notation.artifact;
			
		int groupIndex = txt.indexOf(":");	
		int versionIndex = txt.indexOf("#");
		
		if (groupIndex < 0 && versionIndex < 0) {
			notation = Notation.artifact;
		} else {
			if (groupIndex >= 0) {
				notation=Notation.group;
				if (versionIndex > 0)
					notation=Notation.all;
			} else {
				if (versionIndex >= 0) {
					notation = Notation.version;
				}
			}			
		}
		
		switch (notation) {
			case all: {
				final String grp = patternExpander.expand( txt.substring(0, groupIndex));
				final String vrsn = patternExpander.expand( txt.substring( versionIndex + 1));
				final String artf = patternExpander.expand( txt.substring(groupIndex+1, versionIndex));		
				
				return new Predicate<VersionedArtifactIdentification>() {

					@Override
					public boolean test(VersionedArtifactIdentification t) {
						if (!t.getGroupId().toLowerCase().matches( grp))
							return false;
						if (!t.getArtifactId().toLowerCase().matches( artf))
							return false;
						if (!t.getVersion().matches( vrsn))
							return false;				
						return true;
					}
					
				};
			}
			case group: {
				final String grp = patternExpander.expand(txt.substring(0, groupIndex));
				final String artf = patternExpander.expand(txt.substring(groupIndex +1));				
				
				return new Predicate<VersionedArtifactIdentification>() {

					@Override
					public boolean test(VersionedArtifactIdentification t) {
						if (!t.getGroupId().toLowerCase().matches( grp))
							return false;
						if (!t.getArtifactId().toLowerCase().matches( artf))
							return false;							
						return true;
					}
					
				};
			}
			case version : {
				final String artf = patternExpander.expand( txt.substring( 0, versionIndex));
				final String vrsn = patternExpander.expand(txt.substring( versionIndex + 1));
				
				return new Predicate<VersionedArtifactIdentification>() {

					@Override
					public boolean test(VersionedArtifactIdentification t) {
						if (!t.getArtifactId().toLowerCase().matches( artf))
							return false;
						if (!t.getVersion().matches( vrsn))
							return false;				
						return true;
					}
					
				};
			}				
			case artifact: 					
			default: {
				if (
						txt.contains( "*") == false &&
						patternExpander.isPrecise(txt) == false
					)					
					txt = patternExpander.expand(txt);
				else 
					txt = patternExpander.sanitize(txt);
				
				final String v = txt;
				return new Predicate<VersionedArtifactIdentification>() {
					
					@Override
					public boolean test(VersionedArtifactIdentification t) {
						if (!t.getArtifactId().toLowerCase().matches( v))
							return false;
						return true;
					}						
				};					
			}
		
		}			
	}


}
