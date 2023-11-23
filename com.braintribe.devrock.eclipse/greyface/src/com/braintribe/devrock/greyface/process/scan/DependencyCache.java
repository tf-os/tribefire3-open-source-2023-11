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
package com.braintribe.devrock.greyface.process.scan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;

/**
 * @author Pit
 *
 */
public class DependencyCache {
	private Map<RepositorySetting, Set<Dependency>> cachedSettingToDependencyMap = new HashMap<RepositorySetting, Set<Dependency>>();
	
	
	public synchronized boolean isCached( RepositorySetting source, Dependency dependency) {
		Set<Dependency> cachedDependencies = cachedSettingToDependencyMap.get( source);
		if (cachedDependencies == null) {
			cachedDependencies = new HashSet<Dependency>(1);
			cachedSettingToDependencyMap.put( source, cachedDependencies);
		}
		boolean alreadyProcessed = false;
		for (Dependency suspect : cachedDependencies) {							
			if (ArtifactProcessor.coarsestDependencyEquals( dependency, suspect)) {
				alreadyProcessed = true;
				break;
			}							
		}
		if (!alreadyProcessed) {
			cachedDependencies.add(dependency);
		}
		return alreadyProcessed;
	}
	
	
}
