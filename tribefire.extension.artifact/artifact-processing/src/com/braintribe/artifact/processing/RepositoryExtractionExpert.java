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
package com.braintribe.artifact.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifacts.mc.wire.buildwalk.space.FilterConfigurationSpace;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;

public class RepositoryExtractionExpert {
	private static Logger log = Logger.getLogger(RepositoryExtractionExpert.class);
	private List<Pattern> globalExclusions;
	
	private boolean isExcluded(Solution solution) {
		return isExcluded(NameParser.buildName(solution));
	}
	
	private boolean isExcluded(String artifactId) {
		if (globalExclusions != null && !globalExclusions.isEmpty()) {
			for (Pattern p : globalExclusions) {
				if (p.matcher(artifactId).matches()) {
					log.debug("Excluding artifact "+artifactId+" because it is globally excluded.");
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isIncluded(Dependency dependency) {
		String scope = dependency.getScope();
		
		boolean excluded = dependency.getOptional() || (scope != null && (scope.equalsIgnoreCase("provided") || scope.equalsIgnoreCase("test")));
		return !excluded;
	}
	
	private List<Pattern> parseExclusionPatterns(List<String> lines) {
		List<Pattern> exclusions = new ArrayList<>();
		for (String line : lines) {
			if (line.trim().length() > 0 && !line.startsWith("#")) {
				try {
					exclusions.add(Pattern.compile(line));
				} catch(Exception e) {
					log.warn("Could not compile pattern [" + line + "]", e);
				}
			}
		}
		return exclusions;		
	}

	private class RepositoryExtractFilterConfiguration extends FilterConfigurationSpace {
		private List<Pattern> globalExclusions;
		
		public RepositoryExtractFilterConfiguration( List<Pattern> exclusions) {
			this.globalExclusions = exclusions;
		}
		
		@Override
		public Predicate<? super Solution> solutionFilter() {
			// filter the solutions with the configured global exclusions
			return RepositoryExtractionExpert.this::isExcluded;
		}

		@Override
		public Predicate<? super Dependency> dependencyFilter() {
			// filter the dependency with the correct scope and optional flag
			return RepositoryExtractionExpert.this::isIncluded;
		}

		@Override
		public Predicate<? super PartTuple> partFilter() {
			// every part should be included as this is a full export of the artifact
			return p -> true;
		}
	}

}
