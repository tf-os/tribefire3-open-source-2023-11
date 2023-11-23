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
package tribefire.cortex.initializer.support.impl;

import com.braintribe.wire.api.scope.InstanceQualification;

public class GlobalIdGenerator {
	private int seq = 0;
	private StringBuilder globalIdBuilder = new StringBuilder();
	
	public void initialize(InstanceQualification qualification, String globalIdPrefix) {
		// reset id suffix sequence
		seq = 0;
		
		// reset globalId prefixing
		globalIdBuilder.setLength(0);
		globalIdBuilder
			.append("wire://")
			.append(globalIdPrefix)
			.append('/')
			.append(qualification.space().getClass().getSimpleName())
			.append('/')
			.append(qualification.name())
			.append('/');
	}
	
	public String nextGlobalId() {
		int trimBackTo = globalIdBuilder.length();
		globalIdBuilder.append(seq++);
		String globalIdResult = globalIdBuilder.toString();
		globalIdBuilder.setLength(trimBackTo);
		return globalIdResult;
	}
}
