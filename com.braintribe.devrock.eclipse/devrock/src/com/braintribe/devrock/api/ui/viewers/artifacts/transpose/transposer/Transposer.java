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
package com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.eclipse.model.reason.devrock.ProjectLocation;
import com.braintribe.devrock.eclipse.model.reason.devrock.ProjectNonPerfectMatch;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DeclaratorNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DependerNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.resolution.nodes.NodeArchetype;
import com.braintribe.devrock.eclipse.model.resolution.nodes.NodeFunction;
import com.braintribe.devrock.eclipse.model.resolution.nodes.PartNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ProjectNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ReasonClassification;
import com.braintribe.devrock.eclipse.model.resolution.nodes.ReasonNode;
import com.braintribe.devrock.eclipse.model.storage.TranspositionContext;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.devrock.model.mc.reason.McReason;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
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
	public static final String CONTEXT_PROJECTS = "projects";
	
	private  List<PartIdentification> relevantPartIdentifications; {
		relevantPartIdentifications = new ArrayList<>();
		
		relevantPartIdentifications.add(PartIdentifications.pom);
		relevantPartIdentifications.add(PartIdentifications.jar);
	}

	private Set<String> visited;

	private Map<AnalysisArtifact, AnalysisNode> analysisArtifactToNode = new ConcurrentHashMap<>();
	private Map<AnalysisArtifact, DependerNode> analysisArtifactToDependerNode = new ConcurrentHashMap<>();
	private Map<AnalysisDependency, AnalysisNode> analysisDependencyToNode = new ConcurrentHashMap<>();
	private static final NodeComparator nodeComparator = new NodeComparator();

	private AnalysisArtifactResolution resolution;
	private Map<EqProxy<ArtifactIdentification>, IProject> projectDependencies;
	private Map<AnalysisArtifact, IProject> rawProjectDependencies;

	@Required
	@Configurable
	public void setResolution(AnalysisArtifactResolution resolution) {
		this.resolution = resolution;
	}

	@Required
	@Configurable
	public void setProjectDependencies(Map<AnalysisArtifact, IProject> rawProjectDependencies) {
		if (rawProjectDependencies == null) {
			return;
		}
		this.rawProjectDependencies = rawProjectDependencies;
		projectDependencies = new HashMap<>(rawProjectDependencies.size());
		for (Map.Entry<AnalysisArtifact, IProject> entry : rawProjectDependencies.entrySet()) {
			projectDependencies.put(HashComparators.artifactIdentification.eqProxy(entry.getKey()), entry.getValue());
		}
	}

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
	 * 
	 * @param origination - the {@link Origination} to transpose
	 * @return - the origination as {@link Node}
	 */
	public Node transpose(Origination origination) {
		return transpose(origination, ReasonClassification.origination);
	}

	/**
	 * transpose a reason
	 * 
	 * @param reason - the {@link Reason}
	 * @return - the reason as {@link Node}
	 */
	public Node transpose(Reason reason) {
		return transpose(reason, ReasonClassification.failure);
	}

	/**
	 * extracts and transposes all parents (and attached) from the resolution
	 * (traversing down from the terminals)
	 * 
	 * @param tcC        - the {@link TranspositionContext} to use
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a {@link List} of transposed {@link Node}s
	 */
	public List<Node> transposeParents(TranspositionContext tcC) {
		List<AnalysisArtifact> artifactsFromTerminalStructure = extractAllArtifactsFromTerminalStructure(
				resolution.getTerminals());
		List<AnalysisArtifact> parents = artifactsFromTerminalStructure.stream()
				.filter(aa -> aa.getParentDependers().size() > 0).collect(Collectors.toList());
		return transposeParents(tcC, parents);
	}

	/**
	 * transposes the passed parents (and attached) from the resolution
	 * 
	 * @param tcC        - the {@link TranspositionContext} to use
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a {@link List} of transposed {@link Node}s
	 */
	private List<Node> transposeParents(TranspositionContext tcC, List<AnalysisArtifact> parents) {
		clearCaches();
		List<Node> nodes = new ArrayList<>(parents.size());
		for (AnalysisArtifact parent : parents) {
			AnalysisNode parentNode = from(tcC, parent);
			parentNode.setTopLevel(true);
			// grab and attach import dependencies with this parent as declarator to this
			// parent
			if (!nodes.contains(parentNode)) {
				nodes.add(parentNode);
			}
		}
		return nodes;
	}

	/**
	 * transposes the clashes
	 * 
	 * @param tcC     - the {@link TranspositionContext} to use
	 * @param clashes - a {@link List} of {@link DependencyClash} supplied by the
	 *                {@link AnalysisArtifactResolution}
	 * @return - the transposed {@link List} of {@link AnalysisNode}
	 */
	public List<Node> transposeClashes(TranspositionContext tcC) {
		List<DependencyClash> clashes = resolution.getClashes();
		clearCaches();
		if (clashes.size() == 0) {
			return Collections.emptyList();
		}
		List<Node> nodes = new ArrayList<>(clashes.size());

		for (DependencyClash dc : clashes) {
			AnalysisDependency ad = dc.getSelectedDependency();

			AnalysisNode cn = AnalysisNode.T.create();
			cn.setDependencyIdentification(ad);
			cn.setBackingDependency(ad);
			cn.setFunction(NodeFunction.clash);
			cn.setTopLevel(true);

			AnalysisNode winnerNode = null;
			List<AnalysisDependency> involvedDependencies = dc.getInvolvedDependencies();
			for (AnalysisDependency involved : involvedDependencies) {

				AnalysisNode node = from(tcC, involved);
				// in this case, we don't want any standard dependers added
				node.getChildren().clear();
				boolean merged = false;
				if (involved.compareTo(ad) == 0) {
					if (winnerNode == null) {
						winnerNode = node;
						node.setFunction(NodeFunction.clashWinner);
					} else {
						node = winnerNode;
						merged = true;
					}
					// standard depender path
					if (tcC.getShowDependers()) {
						attachDependers(tcC, Collections.singletonList(involved), node);
					}

				} else {
					node.setFunction(NodeFunction.clashLoser);

					// node is created from the data of the dependency, but actually clash resolving
					// has interfered,
					// so we must get the 'original' analysis artifact and replace that link
					AnalysisArtifact replacedAnalysisArtifact = dc.getReplacedSolutions().get(involved);
					node.setBackingSolution(replacedAnalysisArtifact);
					node.setSolutionIdentification(replacedAnalysisArtifact);

					// dependency has already been modified, has the wrong depender?
					AnalysisDependency clone = involved.clone(new StandardCloningContext());
					clone.setSolution(replacedAnalysisArtifact);

					if (tcC.getShowDependers()) {
						attachDependers(tcC, Collections.singletonList(clone), node);
					}
				}
				// only add the node if it hasn't been reused (multiple winners)
				if (!merged) {
					cn.getChildren().add(node);
				}
			}

			nodes.add(cn);
		}

		return nodes;
	}

	/**
	 * transposes the filtered dependencies
	 * 
	 * @param tcC     - the {@link TranspositionContext} to use
	 * @param clashes - a {@link Set} of {@link AnalysisDependency} supplied by the
	 *                {@link AnalysisArtifactResolution}
	 * @return - the transposed {@link List} of {@link Node}
	 */
	public List<Node> transposeFiltered(TranspositionContext tcF) {
		Set<AnalysisDependency> filteredDependencies = resolution.getFilteredDependencies();
		clearCaches();
		if (filteredDependencies.size() == 0) {
			return Collections.emptyList();
		}

		List<Node> nodes = new ArrayList<>();
		for (AnalysisDependency ad : filteredDependencies) {
			AnalysisNode analysisNode = from(tcF, ad);
			analysisNode.setTopLevel(true);
			analysisNode.setFunction(NodeFunction.dependency);
			nodes.add(analysisNode);
		}

		// coalesce
		if (tcF.getCoalesce()) {
			nodes = coalesceNodes(nodes);
		}

		nodes.sort(new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				AnalysisNode a1 = (AnalysisNode) o1;
				AnalysisNode a2 = (AnalysisNode) o2;
				return a1.getDependencyIdentification().compareTo(a2.getDependencyIdentification());
			}
		});
		return nodes;

	}

	/**
	 * transpose incomplete artifacts
	 * 
	 * @param tcU                 - {@link TranspositionContext} to use
	 * @param incompleteArtifacts - a {@link Set} of {@link AnalysisArtifact} that
	 *                            are incomplete
	 * @return - a {@link List} of {@link Node}
	 */
	public List<Node> transposeIncomplete(TranspositionContext tcU) {
		Set<AnalysisArtifact> incompleteArtifacts = resolution.getIncompleteArtifacts();
		clearCaches();
		if (incompleteArtifacts.size() == 0) {
			return Collections.emptyList();
		}
		return transposeSolutions(tcU, incompleteArtifacts);
	}

	/**
	 * transpose unresolved dependencies
	 * 
	 * @param tcU                 - {@link TranspositionContext} to use
	 * @param incompleteArtifacts - a {@link Set} of {@link AnalysisArtifact} that
	 *                            are incomplete
	 * @return - a {@link List} of {@link Node}
	 */
	public List<Node> transposeUnresolved(TranspositionContext tcU) {
		Set<AnalysisDependency> unresolvedDependencies = resolution.getUnresolvedDependencies();
		clearCaches();
		if (unresolvedDependencies.size() == 0) {
			return Collections.emptyList();
		}
		List<Node> nodes = new ArrayList<>();
		for (AnalysisDependency ad : unresolvedDependencies) {
			nodes.add(from(tcU, ad));
		}
		return nodes;
	}

	/**
	 * transposes the {@link AnalysisNode} from a terminal section
	 * 
	 * @param tc         - the {@link TranspositionContext} to use
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a {@link List} of {@link AnalysisNode}
	 */
	public List<Node> transposeTerminals(TranspositionContext tc) {
		return transposeTerminals(tc, resolution.getTerminals());
	}

	/**
	 * transposes terminals
	 * 
	 * @param tc        - the {@link TranspositionContext} to use
	 * @param terminals - the {@link List} of {@link AnalysisTerminal}
	 * @return
	 */
	public List<Node> transposeTerminals(TranspositionContext tc, List<AnalysisTerminal> terminals) {
		clearCaches();
		List<Node> result = new ArrayList<>();
		for (AnalysisTerminal at : terminals) {
			if (at instanceof AnalysisArtifact) {
				AnalysisArtifact aa = (AnalysisArtifact) at;
				AnalysisNode analysisNode = from(tc, aa);
				analysisNode.setTopLevel(true);
				transposeEventualProject(tc, aa, analysisNode);
				result.add(analysisNode);
			} else {
				AnalysisDependency ad = (AnalysisDependency) at;
				AnalysisNode analysisNode = from(tc, ad);
				transposeEventualProject(tc, ad.getSolution(), analysisNode);
				analysisNode.setTopLevel(true);
				result.add(analysisNode);
			}
		}
		return result;
	}

	/**
	 * transposes the solutions' part of the {@link AnalysisArtifactResolution}
	 * 
	 * @param tc         - the {@link TranspositionContext}
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - the {@link List} of {@link Node}
	 */
	public List<Node> transposeSolutions(TranspositionContext tc) {
		return transposeSolutions(tc, resolution.getSolutions());
	}

	/**
	 * transposes the solutions
	 * 
	 * @param tc        - the {@link TranspositionContext}
	 * @param artifacts - a {@link Collection} of {@link AnalysisArtifact}
	 * @return - the {@link List} of {@link Node}
	 */
	public List<Node> transposeSolutions(TranspositionContext tc, Collection<AnalysisArtifact> artifacts) {
		clearCaches();
		List<Node> result = new ArrayList<>();
		result.addAll(from(tc, artifacts));

		result.sort(nodeComparator);
		return result;
	}

	/**
	 * creates a {@link List} of {@link Node} from the terminal structure
	 * 
	 * @param tc         - the {@link TranspositionContext}
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return
	 */
	public List<Node> transposeAllArtifacts(TranspositionContext tc) {
		List<AnalysisTerminal> terminals = resolution.getTerminals();

		return transposeSolutions(tc, extractAllArtifactsFromTerminalStructure(terminals));
	}

	/**
	 * transposes the projects used in the resolution, backed with info of the
	 * container
	 * 
	 * @param tc                  - the {@link TranspositionContext}
	 * @param projectDependencies - a {@link Map} of {@link AnalysisArtifact} to
	 *                            {@link IProject} as returned by the container
	 * @param resolution          - the {@link AnalysisArtifactResolution} as
	 *                            returned by mc-core
	 * @return - a {@link List} of {@link Node}s, actually {@link ProjectNode}s
	 */
	public List<Node> transposeProjectDependencies(TranspositionContext tc) {
		List<AnalysisArtifact> solutions = resolution.getSolutions();
		if (solutions.size() == 0 || projectDependencies == null) {
			return Collections.emptyList();
		}
		clearCaches();
		List<Node> result = new ArrayList<>(solutions.size()); // can't be bigger than this
		for (AnalysisArtifact solution : solutions) {

			IProject project = projectDependencies.get(HashComparators.artifactIdentification.eqProxy(solution));
			if (project == null) {
				continue;
			}

			AnalysisNode node = from(tc, solution);
			node.setTopLevel(true);
			result.add(node);
		}

		return result;
	}

	/**
	 * creates new nodes from the node passed - for detail views
	 * 
	 * @param tc   - the {@link TranspositionContext}
	 * @param node - the {@link Node} to be built the tree from
	 * @return - the new root {@link Node}
	 */
	public Node transposeDetailNode(TranspositionContext tc, Node node) {
		if (node instanceof AnalysisNode) {
			AnalysisNode aNode = (AnalysisNode) node;
			AnalysisArtifact backingSolution = aNode.getBackingSolution();
			clearCaches();
			AnalysisNode from = from(tc, backingSolution);
			return from;
		} else if (node instanceof DependerNode) {
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
	 * 
	 * @param tc        - the {@link TranspositionContext}
	 * @param terminals - a {@link List} of {@link AnalysisTerminal}
	 * @return - a {@link List} of all {@link AnalysisArtifact} involved
	 */
	private List<AnalysisArtifact> extractAllArtifactsFromTerminalStructure(List<AnalysisTerminal> terminals) {
		visited = new HashSet<>();
		List<AnalysisArtifact> result = new ArrayList<>();
		for (AnalysisTerminal terminal : terminals) {
			if (terminal instanceof AnalysisArtifact) {
				AnalysisArtifact aa = (AnalysisArtifact) terminal;
				result.addAll(extract(aa));
			} else if (terminal instanceof AnalysisDependency) {
				AnalysisDependency ad = (AnalysisDependency) terminal;
				AnalysisArtifact solution = ad.getSolution();
				result.addAll(extract(solution));
			}
		}
		result.sort(new Comparator<AnalysisArtifact>() {

			@Override
			public int compare(AnalysisArtifact o1, AnalysisArtifact o2) {
				return o1.compareTo(o2);
			}
		});
		return result;
	}

	/**
	 * extracts all relevant {@link AnalysisArtifact} starting from the
	 * {@link AnalysisArtifact} passed
	 * 
	 * @param tc - the {@link TranspositionContext}
	 * @param aa - the {@link AnalysisArtifact} as a starting point
	 * @return - a {@link List} of all {@link AnalysisArtifact} (itself, the
	 *         parents/imports, the dependencies)
	 */
	private List<AnalysisArtifact> extract(AnalysisArtifact aa) {
		if (!visited.add(aa.asString())) {
			return Collections.emptyList();
		}
		List<AnalysisArtifact> result = new ArrayList<>();
		result.add(aa);

		// parents and imports
		result.addAll(extractParentChain(aa));

		for (AnalysisDependency dep : aa.getImports()) {
			AnalysisArtifact solution = dep.getSolution();
			if (solution != null) {
				result.addAll(extract(solution));
			}
		}

		// dependencies
		for (AnalysisDependency dep : aa.getDependencies()) {
			AnalysisArtifact solution = dep.getSolution();
			if (solution != null) {
				result.addAll(extract(solution));
			}
		}
		return result;
	}

	/**
	 * @param aa - {@link AnalysisArtifact}
	 * @return - {@link List} of {@link AnalysisArtifact}
	 */
	private List<AnalysisArtifact> extractParentChain(AnalysisArtifact aa) {
		List<AnalysisArtifact> result = new ArrayList<>();
		AnalysisDependency parentDependency = aa.getParent();
		if (parentDependency != null) {
			AnalysisArtifact parentSolution = parentDependency.getSolution();
			if (parentSolution != null) {
				if (visited.add(parentSolution.asString())) {
					result.add(parentSolution);
					result.addAll(extractParentChain(parentSolution));
				}
				/*
				 * List<AnalysisDependency> imports = parentSolution.getImports(); for
				 * (AnalysisDependency dep : imports) { AnalysisArtifact importSolution =
				 * dep.getSolution(); if (importSolution != null) { if (visited.add(
				 * importSolution.asString())) { result.add( importSolution); result.addAll(
				 * extractParentChain( importSolution)); } } }
				 */
			}
		}
		return result;
	}

	/**
	 * transposes {@link Node} from the passed {@link Collection} of
	 * {@link AnalysisArtifact}
	 * 
	 * @param context   - the {@link TranspositionContext}
	 * @param artifacts - the {@link Collection} of {@link AnalysisArtifact}
	 * @return - the transposed {@link Node}
	 */
	private List<Node> from(TranspositionContext context, Collection<AnalysisArtifact> artifacts) {
		List<Node> nodes = new ArrayList<>();
		for (AnalysisArtifact artifact : artifacts) {
			AnalysisNode node = from(context, artifact);
			nodes.add(node);
			node.setTopLevel(true);
		}
		return nodes;
	}
	
  
	/**
	 * determines the 'relevant' origin of the owner of these parts,
	 * if all share the same origin, it will be used.
	 * if they differ, the pom's original is determined to be relevant and used 
	 * @param partNodes
	 * @return
	 */
	private String determineOrginOfAnalysisNodeByPartNodes( List<PartNode> partNodes) {
		Map<String, PartIdentification> map = new HashMap<>();
		String first = null;
		for (PartNode node : partNodes) {
			Part part = node.getPart();			
			if (first == null) {
				first = part.getRepositoryOrigin();
			}
			map.put( part.getRepositoryOrigin(), part);		
		}
		// no parts : should never happen
		if (first == null) {
			return null;
		}
		final String toMatch = first; 
		// standard case : all from one repository
		boolean allFromInstall = map.keySet().stream().allMatch( p -> p.equals( toMatch));
		if (allFromInstall) {
			return toMatch;
		}
		// not all parts have the same origin, so it's the pom? 
		if (!allFromInstall) {
			Optional<Entry<String, PartIdentification>> optional = map.entrySet().stream().filter( p -> p.getValue().equals( PartIdentifications.pom)).findFirst();
			if (optional.isPresent()) {
				return optional.get().getKey();
			}
			else {
				// TODO : check that in release mode
				System.out.println("cannot access pom to determine origin");
				return null;
			}
		}
		return null;
	}
	
	
	
	Object monitor = new Object();

	/**
	 * transposes a single {@link AnalysisArtifact} to an {@link AnalysisNode}
	 * 
	 * @param context  - the {@link TranspositionContext}
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - the {@link AnalysisNode}
	 */
	private AnalysisNode from(TranspositionContext context, AnalysisArtifact artifact) {
		AnalysisNode node = analysisArtifactToNode.get(artifact);
		if (node != null) {
			return node;
		}
		node = AnalysisNode.T.create();
		node.setSolutionIdentification(artifact);
		node.setBackingSolution(artifact);

		String packaging = artifact.getPackaging();
		if (packaging == null) {
			packaging = "jar";
		}
		switch (packaging) {
		case "pom":
			node.setArchetype(NodeArchetype.pom);
			break;
		default:
		case "jar":
			node.setArchetype(NodeArchetype.jar);
			break;
		}

		// project reference? add the two possible nodes..
		transposeEventualProject(context, artifact, node);

		//
		// basic linkage
		//

		// dependencies
		if (context.getShowDependencies()) {
			List<AnalysisDependency> dependencies = artifact.getDependencies();
			List<AnalysisNode> dependencyNodes = dependencies.stream().map(ad -> from(context, ad))
					.collect(Collectors.toList());
			dependencyNodes.sort(TranspositionCommons.analysisNodeComparator);
			node.getChildren().addAll(dependencyNodes);
		}

		// depender
		if (context.getShowDependers()) {
			attachDependers(context, artifact, node);
		}
		
		
		//determine the repository origin of the 'main' parts..  
		List<PartNode> parts = extractParts(artifact);
		String originOfRelevantParts = determineOrginOfAnalysisNodeByPartNodes(parts);
		node.setRelevantResourceOrigin( originOfRelevantParts);
		// attach the parts if requested 
		if (context.getShowParts()) {
			node.getChildren().addAll(parts);
		}

		if (artifact.hasFailed()) {
			node.getChildren().add(0, transpose(artifact.getFailure())); // add as first
		}

		// if this is a parent artifact, add the dependers
		Set<AnalysisDependency> parentDependers = artifact.getParentDependers();
		if (parentDependers.size() > 0) {
			node.setFunction(NodeFunction.parent);
			
			if (context.getShowParentDependers()) {
				// attach parent dependers as depender nodes
				attachDependers(context, parentDependers, node);
				synchronized (monitor) {
					
					// mark
					List<DependerNode> toMark = node.getChildren().stream()
							.filter( n -> n instanceof DependerNode)
							.map( n -> (DependerNode) n)
							.collect( Collectors.toList());
	
					for (DependerNode dn : toMark) {
						synchronized (dn) {
							dn.setIsParentDepender(true);
						}
					}
				}
			}
			// check dep mgt
		}
		// if this is an import artifact, add dependers
		if (artifact.getImporters().size() > 0) {
			node.setFunction(NodeFunction.imports);

			if (context.getShowImportDependers()) {
				// attach imports as dependencies
				attachDependers(context, artifact.getImporters(), node);

				// attachDeclarator(context, artifact, node);
			}
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
		 * Map<CompiledDependencyIdentification,CompiledDependencyIdentification>
		 * artifactRedirects = ca.getArtifactRedirects();
		 */

		analysisArtifactToNode.put(artifact, node);
		return node;
	}

	private void transposeEventualProject(TranspositionContext context, AnalysisArtifact artifact, Node node) {		
		if (!context.getDetectProjects())
			return;
		BasicWorkspaceProjectInfo projectInfo;
		IProject iProject = projectDependencies != null
				? projectDependencies.get(HashComparators.artifactIdentification.eqProxy(artifact))
				: null;
		boolean isProjectDependency = false;
		if (iProject == null) {
			projectInfo = DevrockPlugin.instance().getWorkspaceProjectView().getProjectInfo(artifact);
		} else { // is a dependency of a node
			projectInfo = DevrockPlugin.instance().getWorkspaceProjectView().getProjectInfo(iProject);
			isProjectDependency = true;
		}

		if (projectInfo != null) {
			node.setIsAProject(true);
			iProject = projectInfo.getProject();
				
			boolean hasReasoning =  node.getChildren().stream().filter( n -> n instanceof ReasonNode).findFirst().isPresent();
			if (!hasReasoning) {			
				Reason location = TemplateReasons.build(ProjectLocation.T).assign(ProjectLocation::setLocation, iProject.getLocation().toOSString()).toReason();
				ReasonNode locationNode = transpose(location, ReasonClassification.origination);
				node.getChildren().add(locationNode);
			}
			

			if (isProjectDependency) {
				String projectVersion = projectInfo.getVersionedArtifactIdentification().getVersion();
				String solutionVersion = artifact.getVersion();

				if (projectVersion.compareTo(solutionVersion) != 0) {
					Reason nonPerfectMatchReason = TemplateReasons.build(ProjectNonPerfectMatch.T)
							.assign(ProjectNonPerfectMatch::setActualVersion, projectVersion)
							.assign(ProjectNonPerfectMatch::setRequestedVersion, solutionVersion).toReason();
					ReasonNode versionOverrideNode = transpose(nonPerfectMatchReason, ReasonClassification.warning);
					node.getChildren().add(versionOverrideNode);
				}
			}
		}
	}

	

	/**
	 * attaches a parent node to artifact passed - if any
	 * 
	 * @param context  - the {@link TranspositionContext}
	 * @param artifact - the {@link AnalysisArtifact} whose parent we want
	 * @param node     - the {@link AnalysisNode} of the {@link AnalysisArtifact}
	 */
	private synchronized void attachParent(TranspositionContext context, AnalysisArtifact artifact, AnalysisNode node) {
		if (artifact == null)
			return;
		AnalysisDependency parentDependency = artifact.getParent();
		if (parentDependency != null) {
			AnalysisNode parentNode = from(context, parentDependency);
			if (parentNode != null) {
				parentNode.setFunction(NodeFunction.parent);
				node.setParentNode(parentNode);
				node.getChildren().add(parentNode);
				
				// parents have no parts currently
				//Map<String,Part> parts = parentDependency.getSolution().getParts();

				if (context.getShowParentDependers()) {
					synchronized (monitor) {
						
						AnalysisArtifact parent = parentNode.getBackingSolution();
						if (parent != null) {
							attachDependers(context, parent.getParentDependers(), parentNode);
							// mark nodes 												
							List<DependerNode> toMark = parentNode.getChildren().stream()
														.filter( n -> n instanceof DependerNode)
														.map( n -> (DependerNode) n)
														.collect( Collectors.toList());
							
							for (DependerNode dn : toMark) {
								synchronized (dn) {
									dn.setIsParentDepender(true);
								}
							}														
						}
					}
				}
			}
		}
	}

	/**
	 * attaches any 'imports' of the artifact to its node
	 * 
	 * @param context  - the {@link TranspositionContext}
	 * @param artifact - the {@link AnalysisArtifact}
	 * @param node     - the {@link AnalysisNode}
	 */
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
			}
		}
	}

	/**
	 * attaches all the dependers of the given {@link AnalysisArtifact} to the
	 * passed {@link Node}
	 * 
	 * @param context  - the {@link TranspositionContext}
	 * @param artifact - the {@link AnalysisArtifact} who's dependers to use
	 * @param node     - the {@link Node} to attach to
	 */
	private void attachDependers(TranspositionContext context, AnalysisArtifact artifact, Node node) {
		Set<AnalysisDependency> dependers = artifact.getDependers();
		attachDependers(context, dependers, node);
	}

	/**
	 * attaches a number of {@link AnalysisDependency} as dependers to a
	 * {@link Node}
	 * 
	 * @param context   - the {@link TranspositionContext}
	 * @param dependers - a {@link Collection} of {@link AnalysisDependency} to
	 *                  attach as dependers
	 * @param node      - the {@link Node} to attach to
	 */
	private void attachDependers(TranspositionContext context, Collection<AnalysisDependency> dependers, Node node) {
		//

		List<Node> currentChildren = new ArrayList<>(node.getChildren());
		List<DependerNode> nodes = new ArrayList<>(dependers.size());
		for (AnalysisDependency dependerDependency : dependers) {
			AnalysisArtifact dependerArtifact = dependerDependency.getDepender();
			if (dependerArtifact == null) {
				continue;
			}

			synchronized (currentChildren) {

				Node alreadyPresentNode = currentChildren.stream().filter(n -> n instanceof DependerNode)
						.map(n -> (DependerNode) n).filter(n -> n.getDependerArtifact() != null)
						.filter(n -> n.getDependerArtifact().compareTo(dependerArtifact) == 0).findFirst().orElse(null);

				if (alreadyPresentNode != null) {
					continue;
				}

			}
			//
			DependerNode dependerNode = DependerNode.T.create();

			dependerNode.setDependency(dependerDependency);
			dependerNode.setBackingDependency(dependerDependency);

			dependerNode.setDependerArtifact(dependerArtifact);
			dependerNode.setBackingArtifact(dependerArtifact);
			transposeEventualProject(context, dependerArtifact, dependerNode);
			
			// check if it's a terminal depender ... 
			if (dependerArtifact.getDependers().size() == 0) {
				dependerNode.setIsTerminal(true);
			}

			dependerNode.setFunction(NodeFunction.depender);
			List<DependerNode> nextNodes = getDependerNode(context, dependerArtifact);
			nextNodes.sort(TranspositionCommons.dependencyNodeComparator);
			dependerNode.getChildren().addAll(nextNodes);
			nodes.add(dependerNode);
		}
		nodes.sort(TranspositionCommons.dependencyNodeComparator);
		node.getChildren().addAll(nodes);
	}

	/**
	 * creates the depender {@link DependerNode} from the dependers of a
	 * {@link AnalysisArtifact}
	 * 
	 * @param context
	 * @param artifact
	 * @return
	 */
	private List<DependerNode> getDependerNode(TranspositionContext context, AnalysisArtifact artifact) {
		Set<AnalysisDependency> dependers = artifact.getDependers();
		//

		List<DependerNode> nodes = new ArrayList<>(dependers.size());
		for (AnalysisDependency dependerDependency : dependers) {
			AnalysisArtifact dependerArtifact = dependerDependency.getDepender();
			if (dependerArtifact == null) {
				continue;
			}
			// already processed
			DependerNode dependerNode = analysisArtifactToDependerNode.get(dependerArtifact);
			if (dependerNode != null) {
				nodes.add(dependerNode);
				continue;
			}
			// process
			dependerNode = DependerNode.T.create();
			analysisArtifactToDependerNode.put(dependerArtifact, dependerNode);
			dependerNode.setDependency(dependerDependency);
			dependerNode.setDependerArtifact(dependerArtifact);
			if (dependerArtifact.getDependers().size() == 0) {
				dependerNode.setIsTerminal(true);
			}
			dependerNode.setFunction(NodeFunction.depender);
			transposeEventualProject(context, dependerArtifact, dependerNode);

			Node alreadyPresentNode = nodes.stream().filter(n -> n instanceof DependerNode).map(n -> (DependerNode) n)
					.filter(n -> n.getDependerArtifact() != null)
					.filter(n -> n.getDependerArtifact().compareTo(dependerArtifact) == 0).findFirst().orElse(null);

			if (alreadyPresentNode == null) {
				List<DependerNode> nextNodes = getDependerNode(context, dependerArtifact);

				nextNodes.sort(TranspositionCommons.dependencyNodeComparator);
				dependerNode.getChildren().addAll(nextNodes);
			}
			nodes.add(dependerNode);
		}
		nodes.sort(TranspositionCommons.dependencyNodeComparator);
		return nodes;
	}

	/**
	 * extract all parts of an {@link AnalysisArtifact}
	 * 
	 * @param artifact - the {@link AnalysisArtifact}
	 * @return - the parts as a {@link List} of {@link Node}
	 */
	private List<PartNode> extractParts(AnalysisArtifact artifact) {
		if (artifact == null) {
			return Collections.emptyList();
		}
		Map<String, Part> parts = artifact.getParts();
		List<PartNode> result = new ArrayList<>(parts.size());
		for (Map.Entry<String, Part> entry : parts.entrySet()) {
			PartNode partNode = PartNode.T.create();

			Part part = entry.getValue();
			String name = entry.getKey();
			String type = part.getType();
			if (name.contains(":")) {
				name = type;
			}
			switch (name) {
				case "pom":
					partNode.setArchetype(NodeArchetype.pom);
					break;
				case "jar":
					partNode.setArchetype(NodeArchetype.jar);
					break;
				case "sources":
					partNode.setArchetype(NodeArchetype.sources);
					break;
				case "javadoc":
					partNode.setArchetype(NodeArchetype.javadoc);
					break;
				case "man":
					partNode.setArchetype(NodeArchetype.man);
					break;
				case "zip":
					partNode.setArchetype(NodeArchetype.zip);
					break;
				default:
					partNode.setArchetype(NodeArchetype.other);
					break;
			}

			partNode.setOwnerIdentification(artifact);
			partNode.setPart(part);
			partNode.setSymbolicName(name);
			result.add(partNode);
		}
		return result;
	}

	/**
	 * creates an {@link AnalysisNode} from an {@link AnalysisDependency}
	 * 
	 * @param context - the {@link TranspositionContext}
	 * @param ad      - the {@link AnalysisDependency}
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
			node.getChildren().add(0, transpose(ad.getFailure())); // add as first child
		}
		analysisDependencyToNode.put(ad, node);

		AnalysisArtifact solution = ad.getSolution();
		if (solution != null) {
			node.setSolutionIdentification(solution);
			node.setBackingSolution(solution);

			// check for project reference
			// project reference? add the two possible nodes..
			transposeEventualProject(context, solution, node);

			for (AnalysisDependency add : solution.getDependencies()) {
				AnalysisNode adNode = from(context, add);
				adNode.setFunction(NodeFunction.standard);
				node.getChildren().add(adNode);
			}
			//
			if (context.getShowImports()) {
				attachImport(context, solution, node);
			}
		}
		if (context.getShowDependers()) {
			if (solution != null) {
				attachDependers(context, solution, node);
			} else {
				// attaching dependers of the dependency
				AnalysisArtifact depender = ad.getDepender();
				if (depender != null) {
					//
					DependerNode dependerNode = analysisArtifactToDependerNode.get(depender);
					if (dependerNode == null) {
						dependerNode = DependerNode.T.create();
						analysisArtifactToDependerNode.put(depender, dependerNode);
						dependerNode.setDependency(ad);
						if (depender.getDependers().size() == 0) {
							dependerNode.setIsTerminal(true);
						}
						dependerNode.setDependerArtifact(depender);
						transposeEventualProject( context, depender, dependerNode);
						dependerNode.setFunction(NodeFunction.depender);
						attachDependers(context, depender, dependerNode);
					}
					node.getChildren().add(dependerNode);
				}
			}
		}
		// determine the repository origin of the 'main' parts..
		List<PartNode> parts = extractParts(solution);
		String originOfRelevantParts = determineOrginOfAnalysisNodeByPartNodes(parts);
		node.setRelevantResourceOrigin( originOfRelevantParts);
		
		// attach the parts if requested
		if (context.getShowParts()) {
			node.getChildren().addAll(parts);
		}
		if (context.getShowParents()) {
			attachParent(context, solution, node);
		}

		AnalysisArtifact declarator = ad.getDeclarator();
		if (declarator != null) {
			attachDeclarator(context, declarator, node);
		}

		return node;
	}

	/**
	 * attaches a declarator node to the analysis node passed
	 * 
	 * @param context    - the {@link TranspositionContext}
	 * @param declarator - the {@link AnalysisArtifact} that declares the dependency
	 *                   in the {@link AnalysisNode}
	 * @param node       - the {@link AnalysisNode}
	 */
	private void attachDeclarator(TranspositionContext context, AnalysisArtifact declarator, AnalysisNode node) {
		DeclaratorNode declaratorNode = DeclaratorNode.T.create();

		declaratorNode.setDependingDependency(node.getDependencyIdentification());
		declaratorNode.setBackingDependingDependency(node.getBackingDependency());

		if (context.getShowImportDependers()) {
			attachDependers(context, Collections.singletonList(node.getBackingDependency()), declaratorNode);
		}

		declaratorNode.setDeclaratorArtifact(declarator);
		declaratorNode.setBackingDeclaratorArtifact(declarator);

		AnalysisNode declaringNode = from(context, declarator);
		// check if we need to add the import to the parent...
		if (context.getShowParentDependers()) {
			attachDependers(context, Collections.singletonList(node.getBackingDependency()), declaringNode);
		}

		declaratorNode.getChildren().add(declaringNode);
		node.getChildren().add(declaratorNode);
	}

	/**
	 * combines several instances of the same {@link Node}s (multiple nodes, same
	 * logical dependers) of the filtered {@link AnalysisDependency}
	 * 
	 * @param allDependers - the {@link Collection} of {@link Node} that needs to be
	 *                     identity managed
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
				unCoalescingNodes.add(node);
				continue;
			}
			String key = dependencyIdentification.asString();
			List<AnalysisNode> nodes = idToNodes.computeIfAbsent(key, k -> new ArrayList<AnalysisNode>());
			nodes.add(analysisNode);
		}
		List<Node> result = new ArrayList<>(idToNodes.size());
		for (Map.Entry<String, List<AnalysisNode>> entry : idToNodes.entrySet()) {
			List<AnalysisNode> nodes = entry.getValue();
			AnalysisNode prototype = nodes.get(0);
			if (nodes.size() == 1) {
				result.add(prototype);
				continue;
			}
			AnalysisNode commonNode = AnalysisNode.T.create();
			commonNode.setSolutionIdentification(prototype.getSolutionIdentification());
			commonNode.setDependencyIdentification(prototype.getDependencyIdentification());
			commonNode.setFunction(prototype.getFunction());
			commonNode.setTopLevel(true);
			List<Node> nodesToAttach = new ArrayList<>();
			for (AnalysisNode found : nodes) {
				nodesToAttach.addAll(found.getChildren());
			}
			nodesToAttach.sort(TranspositionCommons.nodeComparator);
			commonNode.setChildren(nodesToAttach);
			result.add(commonNode);
		}
		result.addAll(unCoalescingNodes);
		return result;
	}

	/**
	 * transposes a reason
	 * 
	 * @param reason        - the {@link Reason}
	 * @param asOrigination - true if it's an origination (no error) or a standard
	 *                      reason (error)
	 * @return - the newly created {@link ReasonNode}
	 */
	private ReasonNode transpose(Reason reason, ReasonClassification classification) {
		ReasonNode node = ReasonNode.T.create();
		node.setClassification(classification);
		node.setBackingReason(reason);
		EntityType<GenericEntity> entityType = reason.entityType();
		node.setType(entityType.getShortName());
		String text = reason.getText();
		if (text == null) {
			text = "<n/a>";
		}
		node.setText(text);

		Set<String> standardReasonProperties = null;
		switch (classification) {
		case origination:
			standardReasonProperties = Origination.T.getProperties().stream().map(p -> p.getName())
					.collect(Collectors.toSet());
			break;
		default:
			standardReasonProperties = McReason.T.getProperties().stream().map(p -> p.getName())
					.collect(Collectors.toSet());
			break;
		}

		// properties
		List<Property> properties = entityType.getProperties();
		for (Property property : properties) {
			String name = property.getName();

			// filter any irrelevant - any standard properties of the base class is ignored
			if (standardReasonProperties.contains(name)) {
				continue;
			}
			//
			// Object value = property.get(reason);
			// transpose property value?? how?? toString()?
		}

		// children
		for (Reason child : reason.getReasons()) {
			node.getChildren().add(transpose(child, classification));
		}
		return node;
	}

	public Transposer from() {
		Transposer transposer = new Transposer();
		transposer.setProjectDependencies( this.rawProjectDependencies);
		transposer.setResolution(this.resolution);
		return transposer;
	}
}
