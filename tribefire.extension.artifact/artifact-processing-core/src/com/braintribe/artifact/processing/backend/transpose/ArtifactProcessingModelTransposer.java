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
package com.braintribe.artifact.processing.backend.transpose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.ResolvedArtifactPart;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionSortOrder;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * a transposer between MC's domain and the extension's domain, i.e. artifact-model (et al) -> artifact-processing model
 * 
 * @author pit
 *
 */
public class ArtifactProcessingModelTransposer extends TransposerCommons {
	private static Logger log = Logger.getLogger(ArtifactProcessingModelTransposer.class);

	private Map<String, ResolvedArtifact> transposedMap = new HashMap<>();
	private Map<String, Solution> rawMap = new HashMap<>();
		
	@Configurable @Required
	public void setPopulation( Collection<Solution> population) {
		population.stream().forEach( s -> {
			String key = s.getGroupId() + ":" + s.getArtifactId();
			rawMap.put( key, s);
		});
	}
	
	public static ArtifactResolution transpose( RepositoryReflection reflection, Solution terminal, Collection<Solution> solutions, ResolutionSortOrder mode) {
		ArtifactProcessingModelTransposer transposer = new ArtifactProcessingModelTransposer();
		transposer.setPopulation(solutions);
		transposer.setRepositoryReflection(reflection);
				
		ArtifactResolution result = ArtifactResolution.T.create();
		result.setResolvedArtifact( transposer.transpose(terminal));
		result.setDependencies( transposer.transpose( mode));
		return result;
	}
	/**
	 * transposed the terminal into it's counterpart, direct dependencies 
	 * recursively linked with the solutions passed by the constructor
	 * @param terminal - the terminal of the walk as {@link Solution} 
	 * @return - the corresponding {@link ResolvedArtifact}
	 */
	public ResolvedArtifact transpose( Solution terminal) {
		return acquire( terminal);
	}
	
	/**
	 * transpose the flat list of solutions from the constructor into a flat list of their corresponding parts, 
	 * auto-link them 
	 * @return - a {@link List} of {@link ResolvedArtifact}
	 */
	public List<ResolvedArtifact> transpose(ResolutionSortOrder mode) {	
		Collection<Solution> values = rawMap.values();
		List<Solution> solutions = new ArrayList<>( values);
		if (mode == null) {
			mode = ResolutionSortOrder.buildOrder;
		}
		switch (mode) {
			case alphabetically:
				solutions.sort( new Comparator<Solution>() {
					@Override
					public int compare(Solution o1, Solution o2) {
						// same artifact, then same group
						String s1 = o1.getArtifactId()  + ":" + o1.getGroupId();  
						String s2 = o2.getArtifactId()  + ":" + o2.getGroupId();
						return s1.compareTo(s2);
					}
				});
				break;			
		
			case buildOrder:
				default:
					solutions.sort( new Comparator<Solution>() {
						@Override
						public int compare(Solution o1, Solution o2) {				
							return o1.getOrder().compareTo( o2.getOrder()) * -1;
						}
					});
					break;
		}
		List<ResolvedArtifact> result = solutions.stream().map( s -> {
			return acquire(s);
		}).collect( Collectors.toList());
		// sort 
		return result;
	}
	
	/**
	 * acquire a solution's resolved artifact
	 * @param solution - the {@link Solution}
	 * @return - the corresponding {@link ResolvedArtifact}
	 */
	private ResolvedArtifact acquire( Solution solution) {
		String key = solution.getGroupId() + ":" + solution.getArtifactId();
		ResolvedArtifact resolved = transposedMap.get( key);
		if (resolved == null) {
			resolved = _transpose( solution);
			transposedMap.put(key, resolved);			
				
			// dependencies
			resolved.setDependencies(acquireDependencies(solution));
						
			resolved.setRepositoryOrigins( getRepositoryOrigins(solution));			
		}
		return resolved;
	}

	/**
	 * acquire the resolved artifacts representing the direct dependencies of the solution. 
	 * NOTE: no all dependencies have to be found amongst the list, as MC's walk may have filtered the list
	 * as declared in the pom.
	 * NOTE: dependencies inherited from parents ARE present
	 * @param solution - the {@link Solution}
	 * @return - a {@link List} of {@link ResolvedArtifact} that represent the dependencies 
	 */
	private List<ResolvedArtifact> acquireDependencies(Solution solution) {
		List<ResolvedArtifact> result = new ArrayList<>();
		solution.getDependencies().stream().forEach( d -> {
			Solution ds = rawMap.get( d.getGroupId() + ":" + d.getArtifactId());
					 
			if (ds == null) {
				// no solution resolved by MC for this dependency.. 
				// actually, if MC doesn't resolve a dependency, i.e. it doesn't appear in the solution list, the MC's configuration
				// made it so, so don't worry.. 				
				String msg = "no raw solution stored for dependency [" + d.getGroupId() + ":" + d.getArtifactId() + "], classifier [" + d.getClassifier() + "], scope [" + d.getScope() + "]";
				log.debug( msg);						
			}
			else {
				ResolvedArtifact acquired= acquire(ds);
				result.add(acquired);
			}		
		});
		return result;
	}
	
	/**
	 * transpose a {@link Solution} and its {@link Part} into a {@link ResolvedArtifact} with its {@link ResolvedArtifactPart}
	 * @param solution - the {@link Solution}
	 * @return - the representing {@link ResolvedArtifact}
	 */
	private  ResolvedArtifact _transpose( Solution solution) {
		ResolvedArtifact artifact = ResolvedArtifact.T.create();
		artifact.setGroupId( solution.getGroupId());
		artifact.setArtifactId( solution.getArtifactId());
		artifact.setVersion( VersionProcessor.toString( solution.getVersion()));
		
		
		for (Part part : solution.getParts()) { 
			ResolvedArtifactPart resolvedPart = transpose( part);
			artifact.getParts().add(resolvedPart);			
		}
		return artifact;
	}
	

}
