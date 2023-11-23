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

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

public class ScanTuple {
	public Dependency dependency;
	public int level;
	public int index;
	public Solution importParent;
	
	
	public ScanTuple(Dependency dependency, int level, int index, Solution importParent) {
		this.dependency = dependency;
		this.level = level;
		this.index = index;
		this.importParent = importParent;
	}
}
