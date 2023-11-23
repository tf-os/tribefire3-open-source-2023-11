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
package com.braintribe.devrock.model.transposition.resolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.devrock.eclipse.model.resolution.AnalysisArtifactResolutionViewerModel;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DependerNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.resolution.nodes.NodeArchetype;
import com.braintribe.devrock.eclipse.model.resolution.nodes.NodeFunction;
import com.braintribe.devrock.eclipse.model.resolution.nodes.PartNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ReasonNode;
import com.braintribe.devrock.eclipse.model.storage.TranspositionContext;
import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.devrock.model.mc.reason.McReason;
import com.braintribe.devrock.model.transposition.resolution.common.TranspositionCommons;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;

public class Transposer {
	public static final String CONTEXT_SOLUTIONS = "terminal-solutions";
	public static final String CONTEXT_TERMINAL = "terminal-context";
	public static final String CONTEXT_ALL = "terminal-all";
	public static final String CONTEXT_CLASHES = "clashes";
	public static final String CONTEXT_INCOMPLETE = "incomplete";
	public static final String CONTEXT_UNRESOLVED = "unresolved";
	public static final String CONTEXT_FILTERED = "filtered";
	public static final String CONTEXT_DETAIL = "detail";
	public static final String CONTEXT_DEFAULT = "default";
	public static final String CONTEXT_PARENTS = "parent";
		
	private Set<String> visited;
	
	private Map<AnalysisArtifact, AnalysisNode> analysisArtifactToNode = new HashMap<>();
	private Map<AnalysisArtifact, DependerNode> analysisArtifactToDependerNode = new HashMap<>();
	private Map<AnalysisDependency, AnalysisNode> analysisDependencyToNode = new HashMap<>();
	
		
	/**
	 * clears the caches
	 */
	public void clearCaches() {
		analysisArtifactToNode = new HashMap<>();
		analysisArtifactToDependerNode = new HashMap<>();
		analysisDependencyToNode = new HashMap<>();			
	}
	
	/**
	 * transpose an origination 
	 * @param origination - the {@link Origination} to transpose
	 * @return - the origination as {@link Node}
	 */
	public Node transpose( Origination origination) {
		return transpose(origination, true);
	}
	
	/**
	 * transpose a reason 
	 * @param reason - the {@link Reason}
	 * @return - the reason as {@link Node}
	 */
	public Node transpose( Reason reason) {
		return transpose(reason, false);
	}
	
	/**
	 * transforms a {@link AnalysisArtifactResolution} to an instance of {@link AnalysisArtifactResolutionViewerModel}
	 * @param contexts - a {@link Map} of key to {@link TranspositionContext} to be used for the sections (see CONTEXT-* statics)
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - the {@link AnalysisArtifactResolutionViewerModel}
	 */
	public AnalysisArtifactResolutionViewerModel from( Map<String, TranspositionContext> contexts, AnalysisArtifactResolution resolution) {
		AnalysisArtifactResolutionViewerModel vm = AnalysisArtifactResolutionViewerModel.T.create();
				
		// terminal transposition 
		TranspositionContext tcT = getContext(CONTEXT_TERMINAL, contexts);		
		vm.setTerminals( transposeTerminals(tcT, resolution.getTerminals()));
		
		// solution transposition 
		TranspositionContext tcS =  getContext(CONTEXT_SOLUTIONS, contexts);
		vm.setSolutions( transposeSolutions( tcS, resolution.getSolutions()));
		
		// all
		TranspositionContext tcA =  getContext(CONTEXT_SOLUTIONS, contexts);
		List<AnalysisArtifact> artifactsFromTerminalStructure = extractAllArtifactsFromTerminalStructure( resolution.getTerminals());
		vm.setPopulation( transposeSolutions( tcA, artifactsFromTerminalStructure));
		
		// unresolved deps
		TranspositionContext tcU = getContext(CONTEXT_UNRESOLVED, contexts);		
		vm.setUnresolvedDependencies( transposeUnresolved(tcU, resolution.getUnresolvedDependencies()));
		
		// incomplete 
		TranspositionContext tcI = getContext(CONTEXT_INCOMPLETE, contexts);		
		vm.setIncompleteArtifacts(transposeIncomplete(tcI, resolution.getIncompleteArtifacts()));
		
		// filtered dependencies 
		TranspositionContext tcF = getContext(CONTEXT_FILTERED, contexts);		
		vm.setFilteredDependencies( transposeFiltered(tcF, resolution.getFilteredDependencies()));
		
		// clashes
		TranspositionContext tcC = getContext(CONTEXT_CLASHES, contexts);		
		vm.setClashes( transposeClashes(tcC, resolution.getClashes()));
		
		// parents
		TranspositionContext tcP = getContext(CONTEXT_PARENTS, contexts);		
		vm.setParents( transposeParents(tcP, artifactsFromTerminalStructure.stream().filter( aa -> aa.getParentDependers().size() > 0).collect( Collectors.toList())));
		
		return vm;
	}

	/**
	 * extracts and transposes all parents (and attached) from the resolution (traversing down from the terminals)
	 * @param tcC - the {@link TranspositionContext} to use
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a {@link List} of transposed {@link Node}s
	 */
	public List<Node> transposeParents(TranspositionContext tcC, AnalysisArtifactResolution resolution) {
		List<AnalysisArtifact> artifactsFromTerminalStructure = extractAllArtifactsFromTerminalStructure( resolution.getTerminals());
		List<AnalysisArtifact> parents = artifactsFromTerminalStructure.stream().filter( aa -> aa.getParentDependers().size() > 0).collect( Collectors.toList());		
		return transposeParents(tcC, parents);				
	}
	
	/**
	 * transposes the passed parents (and attached) from the resolution
	 * @param tcC - the {@link TranspositionContext} to use
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a {@link List} of transposed {@link Node}s
	 */
	private List<Node> transposeParents(TranspositionContext tcC, List<AnalysisArtifact> parents) {
		List<Node> nodes = new ArrayList<>( parents.size());
		for (AnalysisArtifact parent : parents) {
			AnalysisNode parentNode = from( tcC, parent);
			if (!nodes.contains( parentNode)) {
				nodes.add(parentNode);
			}
		}
		return nodes;
	}

	
	/**
	 * transposes the clashes 
	 * @param tcC - the {@link TranspositionContext} to use 
	 * @param clashes - a {@link List} of {@link DependencyClash} supplied by the {@link AnalysisArtifactResolution}
	 * @return - the transposed {@link List} of {@link AnalysisNode}
	 */
	public List<Node> transposeClashes(TranspositionContext tcC, List<DependencyClash> clashes) {
		clearCaches();
		if (clashes.size() == 0) {
			return Collections.emptyList();
		}
		List<Node> nodes = new ArrayList<>( clashes.size());
		
		for (DependencyClash dc : clashes) {
			AnalysisDependency ad = dc.getSelectedDependency();			
			
			AnalysisNode cn = AnalysisNode.T.create();
			cn.setDependencyIdentification(ad);
			cn.setBackingDependency( ad);
			cn.setFunction( NodeFunction.clash);
			
			AnalysisNode winnerNode = null;
			List<AnalysisDependency> involvedDependencies = dc.getInvolvedDependencies();
			for (AnalysisDependency involved : involvedDependencies) {
				
				AnalysisNode node = from( tcC, involved);
				// in this case, we don't want any standard dependers added
				node.getChildren().clear();
				boolean merged = false; 
				if (involved.compareTo( ad) == 0) { 
					if (winnerNode == null) {
						winnerNode = node;
						node.setFunction( NodeFunction.clashWinner);
					}
					else {
						node = winnerNode;
						merged = true;
					}
					// standard depender path 
					if (tcC.getShowDependers()) {
						attachDependers( tcC, Collections.singletonList( involved), node);
					}
					
				}
				else {
					node.setFunction( NodeFunction.clashLoser);

					// node is created from the data of the dependency, but actually clash resolving has interfered,
					// so we must get the 'original' analysis artifact and replace that link
					AnalysisArtifact replacedAnalysisArtifact = dc.getReplacedSolutions().get(involved);
					node.setBackingSolution(replacedAnalysisArtifact);
					node.setSolutionIdentification(replacedAnalysisArtifact);
					
					// dependency has already been modified, has the wrong depender?
					AnalysisDependency clone = involved.clone(new StandardCloningContext());
					clone.setSolution(replacedAnalysisArtifact);
					
					if (tcC.getShowDependers()) {						
						attachDependers( tcC, Collections.singletonList( clone), node);
					}
				}
				// only add the node if it hasn't been reused (multiple winners)
				if (!merged) {				
					cn.getChildren().add(node);
				}
			}
						
			nodes.add( cn);
		}
		
		return nodes;
	}
	/**
	 * transposes the filtered dependencies 
	 * @param tcC - the {@link TranspositionContext} to use 
	 * @param clashes - a {@link Set} of {@link AnalysisDependency} supplied by the {@link AnalysisArtifactResolution}
	 * @return - the transposed {@link List} of {@link Node}
	 */
	public List<Node> transposeFiltered(TranspositionContext tcF, Set<AnalysisDependency> filteredDependencies) {
		clearCaches();
		if (filteredDependencies.size() == 0) {
			return Collections.emptyList();
		}
		
		
		List<Node> nodes = new ArrayList<>();
		for (AnalysisDependency ad : filteredDependencies) {
			AnalysisNode analysisNode = from(tcF, ad);
			analysisNode.setFunction(NodeFunction.dependency);
			nodes.add( analysisNode);			
		}
		
		// coalesce
		if (tcF.getCoalesce()) {
			nodes = coalesceNodes(nodes);
		}
				
		nodes.sort( new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				AnalysisNode a1 = (AnalysisNode) o1;
				AnalysisNode a2 = (AnalysisNode) o2;
				return a1.getDependencyIdentification().compareTo( a2.getDependencyIdentification());
			}			
		});
		return nodes;

	}

	/**
	 * transpose incomplete artifacts
	 * @param tcU - {@link TranspositionContext} to use
	 * @param incompleteArtifacts - a {@link Set} of {@link AnalysisArtifact} that are incomplete 
	 * @return - a {@link List} of {@link Node}
	 */
	public List<Node> transposeIncomplete(TranspositionContext tcU, Set<AnalysisArtifact> incompleteArtifacts) {
		clearCaches();
		if (incompleteArtifacts.size() == 0) {
			return Collections.emptyList();
		}		
		return transposeSolutions(tcU, incompleteArtifacts);
	}
	/**
	 * transpose unresolved dependencies
	 * @param tcU - {@link TranspositionContext} to use
	 * @param incompleteArtifacts - a {@link Set} of {@link AnalysisArtifact} that are incomplete 
	 * @return - a {@link List} of {@link Node}
	 */
	public List<Node> transposeUnresolved(TranspositionContext tcU, Set<AnalysisDependency> unresolvedDependencies) {
		clearCaches();
		if (unresolvedDependencies.size() == 0) {
			return Collections.emptyList();
		}
		List<Node> nodes = new ArrayList<>();
		for (AnalysisDependency ad : unresolvedDependencies) {
			nodes.add( from(tcU, ad));
		}		
		return nodes;
	}

	/**
	 * transposes the {@link AnalysisNode} from a terminal section
	 * @param tc - the {@link TranspositionContext} to use
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a {@link List} of {@link AnalysisNode}
	 */
	public List<Node> transposeTerminals( TranspositionContext tc,  AnalysisArtifactResolution resolution) {		
						
		return transposeTerminals(tc, resolution.getTerminals());		
	}
	
	/**
	 * transposes terminals 
	 * @param tc - the {@link TranspositionContext} to use
	 * @param terminals - the {@link List} of {@link AnalysisTerminal}
	 * @return
	 */
	public List<Node> transposeTerminals( TranspositionContext tc,  List<AnalysisTerminal> terminals) {		
		clearCaches();
		List<Node> result = new ArrayList<>();
		for (AnalysisTerminal at : terminals) {
			if (at instanceof AnalysisArtifact) {
				result.add( from(tc, (AnalysisArtifact) at));
			}
			else {
				result.add( from( tc, (AnalysisDependency) at));
			}
		}		
		return result;		
	}
	
	/**
	 * transposes the solutions' part of the {@link AnalysisArtifactResolution}
	 * @param tc - the {@link TranspositionContext}
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - the {@link List} of {@link Node}
	 */
	public List<Node> transposeSolutions( TranspositionContext tc, AnalysisArtifactResolution resolution) {						
		return transposeSolutions(tc, resolution.getSolutions());
	}
	
	/**
	 * transposes the solutions 
	 * @param tc - the {@link TranspositionContext}
	 * @param artifacts - a {@link Collection} of {@link AnalysisArtifact} 
	 * @return - the {@link List} of {@link Node}
	 */
	public List<Node> transposeSolutions( TranspositionContext tc, Collection<AnalysisArtifact> artifacts) {
		clearCaches();
		List<Node> result = new ArrayList<>();	
		result.addAll( from(tc, artifacts));	
		// sort per name
		result.sort(new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				if (o1 instanceof AnalysisNode && o2 instanceof AnalysisNode) {
					// compare on artifactId, make sure they're reachable
					
					// first node
					AnalysisNode a1 = (AnalysisNode) o1;										
					VersionedArtifactIdentification identification1 = a1.getSolutionIdentification();
					if (identification1 == null) {
						return 0;
					}
					String aa1 = identification1.getArtifactId();
					if (aa1 == null) {
						return 0;
					}
					// second node
					AnalysisNode a2 = (AnalysisNode) o2;
					VersionedArtifactIdentification identification2 = a2.getSolutionIdentification();
					if (identification2 == null) {
						return 0;
					}
					String aa2 = identification2.getArtifactId();
					if (aa2 == null) {
						return 0;
					}
					// compare artifactId 
					return aa1.compareTo(aa2);
				}
				return 0;
			} 
			
		});
		return result;
	}
	
	/**
	 * creates a {@link List} of {@link Node} from the terminal structure
	 * @param tc - the {@link TranspositionContext} 
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return
	 */
	public List<Node> transposeAllArtifacts( TranspositionContext tc,  AnalysisArtifactResolution resolution) {
		List<AnalysisTerminal> terminals = resolution.getTerminals();
						
		return transposeSolutions(tc, extractAllArtifactsFromTerminalStructure( terminals));
	}
	
	/**
	 * @param tc
	 * @param node
	 * @return
	 */
	public Node transposeDetailNode( TranspositionContext tc, Node node) {
		if (node instanceof AnalysisNode) {
			AnalysisNode aNode = (AnalysisNode) node;
			AnalysisArtifact backingSolution = aNode.getBackingSolution();
			clearCaches();
			AnalysisNode from = from(tc, backingSolution);
			return from;
		}
		else if (node instanceof DependerNode) {
			DependerNode dNode = (DependerNode) node;
			AnalysisArtifact backingArtifact = dNode.getBackingArtifact();
			clearCaches();
			AnalysisNode from = from(tc, backingArtifact);
			return from;
		}
		return null;
			
	}
		
		
	/**
	 * gets all artifacts involved in the resolution...
	 * @param tc - the {@link TranspositionContext}
	 * @param terminals - a {@link List} of {@link AnalysisTerminal}
	 * @return - a {@link List} of all {@link AnalysisArtifact} involved
	 */
	private List<AnalysisArtifact> extractAllArtifactsFromTerminalStructure( List<AnalysisTerminal> terminals) {
		visited = new HashSet<>();
		List<AnalysisArtifact> result = new ArrayList<>();
		for (AnalysisTerminal terminal : terminals) {
			if (terminal instanceof AnalysisArtifact)  {
				AnalysisArtifact aa = (AnalysisArtifact) terminal;
				result.addAll( extract( aa));
			}
			else if (terminal instanceof AnalysisDependency){
				AnalysisDependency ad = (AnalysisDependency) terminal;
				AnalysisArtifact solution = ad.getSolution();
				result.addAll( extract( solution));
			}
		}
		result.sort( new Comparator<AnalysisArtifact>() {

			@Override
			public int compare(AnalysisArtifact o1, AnalysisArtifact o2) {
				return o1.compareTo(o2);
			}			
		});
		return result;
	}
	
	/**
	 * returns the stored {@link TranspositionContext} by its key 
	 * @param contextKey - the key as {@link String}
	 * @param contexts - the {@link Map} of 'key' to {@link TranspositionContext}
	 * @return - a matching context - either the stored one, the default. may throw an {@link IllegalStateException} if nothing's found
	 */
	private TranspositionContext getContext(String contextKey, Map<String, TranspositionContext> contexts) {		
		TranspositionContext tcT = contexts.get( contextKey);
		if (tcT == null)
			tcT = contexts.get( CONTEXT_DEFAULT);
		if (tcT == null) {
			throw new IllegalStateException("no context defined for [" + contextKey + "] nor for default [" + CONTEXT_DEFAULT + "]");
		}
		return tcT;
	}
	
		
	/**
	 * extracts all relevant {@link AnalysisArtifact} starting from the {@link AnalysisArtifact} passed
	 * @param tc - the {@link TranspositionContext}
	 * @param aa - the {@link AnalysisArtifact} as a starting point
	 * @return - a {@link List} of all {@link AnalysisArtifact} (itself, the parents/imports, the dependencies)
	 */
	private List<AnalysisArtifact> extract( AnalysisArtifact aa) {		
		if (!visited.add( aa.asString())) {
			return Collections.emptyList();
		}		
		List<AnalysisArtifact> result = new ArrayList<>();
		result.add( aa);
				
		// parents and imports  
		result.addAll( extractParentChain(aa)); 		
	
		// dependencies		
		for (AnalysisDependency dep : aa.getDependencies()) {
			AnalysisArtifact solution = dep.getSolution();
			if (solution != null) {
				result.addAll( extract( solution));
			}
		}
		return result;
	}
	
	
	/**
	 * @param aa - {@link AnalysisArtifact}
	 * @return - {@link List} of {@link AnalysisArtifact} 
	 */
	private List<AnalysisArtifact> extractParentChain( AnalysisArtifact aa) {
		List<AnalysisArtifact> result = new ArrayList<>();
		AnalysisDependency parentDependency = aa.getParent();
		if (parentDependency != null) {
			AnalysisArtifact parentSolution = parentDependency.getSolution();
			if (parentSolution != null) {
				if (visited.add( parentSolution.asString())) {
					result.add(parentSolution);
					result.addAll( extractParentChain( parentSolution));
				}
				List<AnalysisDependency> imports = parentSolution.getImports();
				for (AnalysisDependency dep : imports) {
					AnalysisArtifact importSolution = dep.getSolution();
					if (importSolution != null) {
						if (visited.add( importSolution.asString())) {
							result.add( importSolution);
							result.addAll( extractParentChain( importSolution));
						}
					}
				}
			}
		}
		return result;
	}
	/*
	private AnalysisArtifact buildAnalysisArtifact( CompiledArtifact compiledArtifact) {
		AnalysisArtifact analysisArtifact = AnalysisArtifact.of( compiledArtifact);
		compiledArtifact.getDependencies().stream()
											.map(this::buildAnalysisDependency)											
											.peek(analysisArtifact.getDependencies()::add)
											.forEach(d -> d.setDepender(analysisArtifact)
										);		
		return analysisArtifact;
	}

	private AnalysisDependency buildAnalysisDependency(CompiledDependency dependency) {
		// TODO : if lenient, the dependency may be flagged as invalid
		AnalysisDependency analysisDependency = AnalysisDependency.T.create();		
		analysisDependency.setOrigin(dependency);
		analysisDependency.setGroupId(dependency.getGroupId());
		analysisDependency.setArtifactId(dependency.getArtifactId());
		analysisDependency.setClassifier(dependency.getClassifier());
		analysisDependency.setType(dependency.getType());
		analysisDependency.setVersion(dependency.getVersion().asString());
		analysisDependency.setScope(dependency.getScope());
		analysisDependency.setOptional(dependency.getOptional());
		
		return analysisDependency;
	}
	*/
	/**
	 * transposes {@link Node} from the passed {@link Collection} of {@link AnalysisArtifact}
	 * @param context - the {@link TranspositionContext}
	 * @param artifacts - the {@link Collection} of {@link AnalysisArtifact}
	 * @return - the transposed {@link Node}
	 */
	private List<Node> from( TranspositionContext context,  Collection<AnalysisArtifact> artifacts) {			
		List<Node> nodes = new ArrayList<>();
		for (AnalysisArtifact artifact : artifacts) {
			AnalysisNode node = from( context, artifact);
			nodes.add(node);
		}		
		return nodes;
	}
			
	/**
	 * transposes a single {@link AnalysisArtifact} to an {@link AnalysisNode}
	 * @param context - the {@link TranspositionContext}
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - the {@link AnalysisNode}
	 */
	private AnalysisNode from (TranspositionContext context, AnalysisArtifact artifact) {
		AnalysisNode node = analysisArtifactToNode.get(artifact);
		if (node != null) {
			return node;
		}
		//System.out.println("from AA : " + artifact.asString());
		node = AnalysisNode.T.create();
		node.setSolutionIdentification( artifact);
		node.setBackingSolution(artifact);
		
		String packaging = artifact.getPackaging();
		if (packaging == null) {
			packaging = "jar";
		}
		switch (packaging) {
			case "pom" :
				node.setArchetype( NodeArchetype.pom);
				break;
			default:
			case "jar":
				node.setArchetype( NodeArchetype.jar);
				break;			
		}
		
		//
		// basic linkage
		//
		
		// dependencies		
		if (context.getShowDependencies()) {
			List<AnalysisDependency> dependencies = artifact.getDependencies();
			List<AnalysisNode> dependencyNodes = dependencies.stream().map( ad -> from(context, ad)).collect( Collectors.toList());
			dependencyNodes.sort( TranspositionCommons.analysisNodeComparator);
			node.getChildren().addAll( dependencyNodes);
		}
		
		// depender		
		if (context.getShowDependers()) {
			attachDependers( context, artifact, node);
		}
		
		if (context.getShowParts()) {
			node.getChildren().addAll(extractParts(artifact));
		}
		
		if (artifact.hasFailed()) {
			node.getChildren().add( 0, transpose( artifact.getFailure())); // add as first
		}
		
		// if this is a parent artifact, add the dependers
		if (context.getShowParentDependers() && artifact.getParentDependers().size() > 0) {
			node.setFunction( NodeFunction.parent);			
			// attach imports as dependencies 
			for (AnalysisDependency importDep :  artifact.getImports()) {
				AnalysisNode importNode = from( context, importDep);
				importNode.setFunction(NodeFunction.imports);
				node.getChildren().add(importNode);
			}
			// attach parent dependers as depender nodes 
			attachDependers(context, artifact.getParentDependers(), node);							

			// check dep mgt 			
		}
		// if this is an import artifact, add dependers
		if (context.getShowImportDependers() &&  artifact.getImporters().size() > 0) {
			node.setFunction( NodeFunction.imports);
			// attach imports as dependencies
			for (AnalysisDependency importDep :  artifact.getImports()) {
				AnalysisNode importNode = from( context, importDep);
				importNode.setFunction(NodeFunction.imports);
				node.getChildren().add(importNode);
			}
			// attach import dependers as depender nodes 
			attachDependers(context, artifact.getImporters(), node);			
			// check dep mgt
		}
		
		
		
		///
		// additional stuff, parents, imports, redirects
		//
		
		// if this artifact references a parent, attach it  
		if (context.getShowParents()) {
			attachParent(context, artifact, node);
		}	
		
		// if this artifact references an import, attach it 
		if (context.getShowImports()) {
			attachImport(context, artifact, node);
		}
		
		/*
		Map<CompiledDependencyIdentification,CompiledDependencyIdentification> artifactRedirects = ca.getArtifactRedirects();
		*/
		
		analysisArtifactToNode.put(artifact, node);
		return node;
	}

	private void attachParent(TranspositionContext context, AnalysisArtifact artifact, AnalysisNode node) {
		if (artifact == null)
			return;
		AnalysisDependency parentDependency = artifact.getParent();
		if (parentDependency != null) {				
			AnalysisNode parentNode = from(context, parentDependency);
			if (parentNode != null) {
				parentNode.setFunction(NodeFunction.parent);
				node.setParentNode(parentNode);
				node.getChildren().add(parentNode);				
				// attach the parent dependency's owner as depender  to his node
				attachDependers(context, Collections.singletonList( parentDependency), parentNode);
			}
		}
	}
	
	private void attachImport(TranspositionContext context, AnalysisArtifact artifact, AnalysisNode node) {
		if (artifact == null)
			return;
		List<AnalysisDependency> importDependencies = artifact.getImports();
		if (importDependencies.size() == 0)
			return;
		
		for (AnalysisDependency importDependency : importDependencies) {
			AnalysisNode importNode = from(context, importDependency);
			if (importNode != null) {
				importNode.setFunction(NodeFunction.imports);
				node.setParentNode(importNode);
				node.getChildren().add(importNode);				
				// attach the parent dependency's owner as depender to his node
				attachDependers(context, Collections.singletonList( importDependency), importNode);
			}
		}		
	}

	

	/**
	 * attaches all the dependers of the given {@link AnalysisArtifact} to the passed {@link Node} 
	 * @param context - the {@link TranspositionContext}
	 * @param artifact - the {@link AnalysisArtifact} who's dependers to use
	 * @param node - the {@link Node} to attach to
	 */
	private void attachDependers(TranspositionContext context, AnalysisArtifact artifact, Node node) {
		Set<AnalysisDependency> dependers = artifact.getDependers();
		attachDependers(context, dependers, node);
	}
	
	/**
	 * attaches a number of {@link AnalysisDependency} as dependers to a {@link Node}
	 * @param context - the {@link TranspositionContext}
	 * @param dependers - a {@link Collection} of {@link AnalysisDependency} to attach as dependers 
	 * @param node - the {@link Node} to attach to 
	 */
	private void attachDependers(TranspositionContext context, Collection<AnalysisDependency> dependers, Node node) {	
		// 
		
		List<DependerNode> nodes = new ArrayList<>( dependers.size());
		for (AnalysisDependency dependerDependency : dependers) {
			AnalysisArtifact dependerArtifact = dependerDependency.getDepender();
			if (dependerArtifact == null) {
				continue;
			}
			// 				
			DependerNode dependerNode = DependerNode.T.create();
			
			dependerNode.setDependency(dependerDependency);
			dependerNode.setBackingDependency(dependerDependency);
			
			dependerNode.setDependerArtifact(dependerArtifact);
			dependerNode.setBackingArtifact(dependerArtifact);
			
			dependerNode.setFunction(NodeFunction.depender);							
			List<DependerNode> nextNodes = getDependerNode( context, dependerArtifact);
			nextNodes.sort( TranspositionCommons.dependencyNodeComparator); 
			dependerNode.getChildren().addAll(nextNodes);
			nodes.add( dependerNode);
		}
		nodes.sort( TranspositionCommons.dependencyNodeComparator);
		node.getChildren().addAll( nodes);
	}
	

	
	/**
	 * creates the depender {@link DependerNode} from the dependers of a {@link AnalysisArtifact} 
	 * @param context
	 * @param artifact
	 * @return
	 */
	private List<DependerNode> getDependerNode(TranspositionContext context, AnalysisArtifact artifact) {
		Set<AnalysisDependency> dependers = artifact.getDependers();
		// 
		
		List<DependerNode> nodes = new ArrayList<>( dependers.size());
		for (AnalysisDependency dependerDependency : dependers) {
			AnalysisArtifact dependerArtifact = dependerDependency.getDepender();
			if (dependerArtifact == null) {
				continue;
			}
			// already processed  			
			DependerNode dependerNode = analysisArtifactToDependerNode.get(dependerArtifact);
			if (dependerNode != null) {
				nodes.add( dependerNode);
				continue;
			}					
			// process 
			dependerNode = DependerNode.T.create();
			analysisArtifactToDependerNode.put(dependerArtifact, dependerNode);
			dependerNode.setDependency(dependerDependency);
			dependerNode.setDependerArtifact(dependerArtifact);
			dependerNode.setFunction(NodeFunction.depender);							
			List<DependerNode> nextNodes = getDependerNode( context, dependerArtifact);
			nextNodes.sort( TranspositionCommons.dependencyNodeComparator);
			dependerNode.getChildren().addAll(nextNodes);
			nodes.add(dependerNode);
		}
		nodes.sort( TranspositionCommons.dependencyNodeComparator);
		return nodes;
	}

	/**
	 * extract all parts of an {@link AnalysisArtifact}
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - the parts as a {@link List} of {@link Node}
	 */
	private List<Node> extractParts(AnalysisArtifact artifact) {
		if (artifact == null) {
			return Collections.emptyList();
		}
		Map<String,Part> parts = artifact.getParts();
		List<Node> result = new ArrayList<>( parts.size()); 
		for (Map.Entry<String, Part> entry : parts.entrySet()) {
			PartNode partNode = PartNode.T.create();
			
			Part part = entry.getValue();
			String name = entry.getKey();
			String type = part.getType();
			if (name.contains( ":")) {
				name = type;
			}
			switch (name) {
				case "pom":
					partNode.setArchetype( NodeArchetype.pom);
					break;				
				case "jar":
					partNode.setArchetype( NodeArchetype.jar);
					break;
				case "sources":
					partNode.setArchetype( NodeArchetype.sources);
					break;
				case "javadoc":
					partNode.setArchetype( NodeArchetype.javadoc);
					break;
				default: 
					partNode.setArchetype( NodeArchetype.other);
					break;
			}
			
			partNode.setOwnerIdentification(artifact);
			partNode.setPart(part);			
			partNode.setSymbolicName( name);				
			result.add( partNode);				
		}
		return result;
	}
		
	
	/**
	 * creates an {@link AnalysisNode} from an {@link AnalysisDependency}
	 * @param context - the {@link TranspositionContext}
	 * @param ad - the {@link AnalysisDependency}
	 * @return - the new {@link AnalysisNode}
	 */
	private AnalysisNode from(TranspositionContext context, AnalysisDependency ad) {
		AnalysisNode node = analysisDependencyToNode.get(ad);
		if (node != null)
			return node;
		
		node = AnalysisNode.T.create();
		node.setFunction(NodeFunction.standard);
		
		node.setDependencyIdentification(ad);
		node.setBackingDependency(ad);
		// attach failure
		if (ad.hasFailed()) {
			node.getChildren().add( 0, transpose( ad.getFailure())); // add as first child
		}
		analysisDependencyToNode.put(ad, node);
				
		AnalysisArtifact solution = ad.getSolution();
		if (solution != null) {
			node.setSolutionIdentification(solution);
			node.setBackingSolution(solution);
		
			for (AnalysisDependency add : solution.getDependencies()) {
				AnalysisNode adNode = from( context, add);				
				adNode.setFunction( NodeFunction.standard);
				node.getChildren().add( adNode);
			}			
		}
		if (context.getShowDependers()) {			
			if (solution != null ) {								 		
				attachDependers(context, solution, node);
			}
			else {
				// attaching dependers of the dependency
				AnalysisArtifact depender = ad.getDepender();
				if (depender != null) {
				// 
					DependerNode dependerNode = analysisArtifactToDependerNode.get(depender);
					if (dependerNode == null) {
						dependerNode = DependerNode.T.create();
						analysisArtifactToDependerNode.put(depender, dependerNode);
						dependerNode.setDependency(ad);
						dependerNode.setDependerArtifact(depender);
						dependerNode.setFunction(NodeFunction.depender);	
						attachDependers(context, depender, dependerNode);
					}
					node.getChildren().add( dependerNode);
				}
			}
		}

		if (context.getShowParts()) {
			node.getChildren().addAll(extractParts( solution));
		}
		if (context.getShowParents()) {
			attachParent(context, solution, node);	
		}
					
		return node;
	}
	
	
	
	/**
	 * combines several instances of the same {@link Node}s (multiple nodes, same logical dependers) of the filtered {@link AnalysisDependency}
	 * @param allDependers - the {@link Collection} of {@link Node} that needs to be identity managed
	 * @return - the resulting {@link List} of {@link Node}
	 */
	private List<Node> coalesceNodes(Collection<Node> allDependers) {
		Map<String, List<AnalysisNode>> idToNodes = new HashMap<>();
		List<Node> unCoalescingNodes = new ArrayList<>();
		for (Node node : allDependers) {			
			AnalysisNode analysisNode = (AnalysisNode) node;
			VersionedArtifactIdentification dependencyIdentification = analysisNode.getDependencyIdentification();
			if (dependencyIdentification == null) {
				// injected terminal 
				unCoalescingNodes.add( node);
				continue;
			}
			String key = dependencyIdentification.asString();
			List<AnalysisNode> nodes = idToNodes.computeIfAbsent( key, k -> new ArrayList<AnalysisNode>());
			nodes.add( analysisNode);			
		}
		List<Node> result = new ArrayList<>( idToNodes.size());		
		for (Map.Entry<String, List<AnalysisNode>> entry : idToNodes.entrySet()) {		
			List<AnalysisNode> nodes = entry.getValue();
			AnalysisNode prototype = nodes.get(0);
			if (nodes.size() == 1) {
				result.add( prototype);
				continue;
			}
			AnalysisNode commonNode = AnalysisNode.T.create();
			commonNode.setSolutionIdentification( prototype.getSolutionIdentification());
			commonNode.setDependencyIdentification( prototype.getDependencyIdentification());
			commonNode.setFunction( prototype.getFunction());
			List<Node> nodesToAttach = new ArrayList<>();
			for (AnalysisNode found : nodes) {
				nodesToAttach.addAll( found.getChildren());
			}					
			nodesToAttach.sort( TranspositionCommons.nodeComparator);
			commonNode.setChildren(nodesToAttach);
			result.add( commonNode);
		}				
		result.addAll( unCoalescingNodes);
		return result;
	}
	

	/**
	 * transposes a reason 
	 * @param reason - the {@link Reason} 
	 * @param asOrigination - true if it's an origination (no error) or a standard reason (error)
	 * @return - the newly created {@link ReasonNode}
	 */
	private ReasonNode transpose( Reason reason, boolean asOrigination) {
		ReasonNode node = ReasonNode.T.create();
		
		node.setBackingReason(reason);
		EntityType<GenericEntity> entityType = reason.entityType();
		node.setType( entityType.getShortName());
		String text = reason.getText();
		if (text == null) {
			text = "<n/a>";
		}
		node.setText( text);
		
		Set<String> standardReasonProperties = null;
		if (asOrigination) {
			standardReasonProperties = Origination.T.getProperties().stream().map( p -> p.getName()).collect(Collectors.toSet());
		}
		else {
			standardReasonProperties = McReason.T.getProperties().stream().map( p -> p.getName()).collect(Collectors.toSet());	
		}
		
		// properties
		List<Property> properties = entityType.getProperties();
		for (Property property : properties) {
			String name = property.getName();
			
			// filter any irrelevant - any standard properties of the base class is ignored
			if (standardReasonProperties.contains( name)) {
				continue;
			}
			// 
			//Object value = property.get(reason);
			// transpose property value?? how?? toString()?					
		}
				
		// children 
		for (Reason child : reason.getReasons()) {
			node.getChildren().add( transpose(child, asOrigination));
		}	
		return node;
	}
}
