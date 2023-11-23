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
package tribefire.extension.artifact.management.processing;

import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.model.artifact.analysis.AnalysisDependency;

import tribefire.extension.artifact.management.api.model.request.ResolutionScope;

public class DownloadDependencyFilter implements Predicate<AnalysisDependency> {
	private Set<ResolutionScope> scopes;
	private boolean includeOptional;
	
	public DownloadDependencyFilter(Set<ResolutionScope> scopes, boolean includeOptional) {
		super();
		this.scopes = scopes;
		this.includeOptional = includeOptional;
	}

	@Override
	public boolean test(AnalysisDependency dependency) {
		if (!includeOptional && dependency.getOptional())
			return false;
		
		for (ResolutionScope scope: scopes) {
			String depScope = dependency.getScope();
			
			switch (depScope) {
			case "parent":
			case "import":
				return true;
			}
			
			switch (scope) {
			case compile:
				switch(depScope) {
				case "compile":
				case "provided":
					return true;
				}
				break;
			case runtime:
				switch(depScope) {
				case "compile":
				case "runtime":
					return true;
				}
				break;
			case test:
				switch(depScope) {
				case "compile":
				case "test":
					return true;
				}
				break;
			default:
				break;
			}
		}
		return false;
	}
}
