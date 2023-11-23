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
package com.braintribe.artifact.processing.backend;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.processing.cfg.resolution.FilterScope;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionScope;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.scopes.Scope;

/**
 * converts a modeled walker configuration into MC-wiring's configuration
 * @author pit
 *
 */
public class ArtifactProcessingWalkConfigurationExpert {

	/**
	 * transpose a {@link WalkerContext} from a {@link WalkConfiguration}, or create an empty (standard) one
	 * @param walkCfg - the {@link WalkConfiguration} or null
	 * @return - the resulting {@link WalkerContext}
	 */
	public static WalkerContext acquireWalkerContext( ResolutionConfiguration walkCfg) {
		WalkerContext walkerContext = new WalkerContext();
		if (walkCfg == null) {
			walkerContext.setSkipOptionals( true);
			return walkerContext;
		}
		
		//
		// part tuples
		//
		
		// overriding : completely replace the relevant part tuples		
		List<PartTuple> overridingRelevants = walkCfg.getParts().stream().map( p -> {
			return PartTupleProcessor.fromString(p);
		}).collect( Collectors.toList());				 
		walkerContext.setRelevantPartTuples( overridingRelevants);
		
		//
		// scopes 
		//
		Set<Scope> scopes = new HashSet<>();
		// filter scopes  (may be null)
		Set<FilterScope> filterScopes = walkCfg.getFilterScopes();
		if (filterScopes != null && filterScopes.size() > 0) {
			//  
			List<String> scopeNames = filterScopes.stream().map( fs -> {
				return fs.toString();
			}).collect(Collectors.toList());		
			scopes.addAll( Scopes.buildScopes(scopeNames));
		}

		// resolution scope (may be null)  
		ResolutionScope resolutionScope = walkCfg.getResolutionScope();
		if (resolutionScope != null) { 			
			switch (resolutionScope) {
				case compile:
					scopes.addAll( Scopes.compileScopes());								
					break;
				case runtime:
					scopes.addAll(  Scopes.runtimeScopes());				
					break;
				case test:
					scopes.addAll(  Scopes.testScopes());
					break;
				default:
				case all:
					walkerContext.setIgnoreDependencyScopes( true);
					break;			
			}
		}
		if (scopes.size() > 0) { 
			// any scopes configured (via filter or resolution)
			walkerContext.setScopes( scopes);
		}
		else {
			// no scopes collected -> switch to ignore
			walkerContext.setIgnoreDependencyScopes( true);
		}

		//			
		// optionals
		//
		walkerContext.setSkipOptionals( !walkCfg.getIncludeOptionals());
		
		//
		// tag rule
		//
		String tagRule = walkCfg.getTagRule();	
		if (tagRule != null) {
			// transpose if required
			walkerContext.setTagRule(tagRule);
		}
		
		//
		// type rule
		//
		String typeRule = walkCfg.getTypeRule();
		if (typeRule != null) {
			// transpose if required
			walkerContext.setTypeRule(typeRule);
		}
		
		//	
		// abort for unresolved
		//
		walkerContext.setAbortOnUnresolvedDependency( walkCfg.getAbortOnUnresolvedDependency());
		return walkerContext;
	}
}
