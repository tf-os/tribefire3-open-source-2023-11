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
package com.braintribe.plugin.commons;

import java.util.Comparator;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class SolutionSortComparator implements Comparator<Solution> {

	@Override
	public int compare(Solution s1, Solution s2) {
		int r = s1.getArtifactId().compareToIgnoreCase(s2.getArtifactId());
		if (r == 0) {
			r = s1.getGroupId().compareToIgnoreCase( s2.getGroupId());
			if (r == 0) {			
				r = VersionProcessor.toString(s1.getVersion()).compareTo( VersionProcessor.toString(s2.getVersion()));
			}
		}
		return r;
	}


}
