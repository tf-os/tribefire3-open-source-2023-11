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
package com.braintribe.model.access.impl;

import java.util.List;

import com.braintribe.model.access.AbstractDelegatingAccess;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.manipulation.Manipulation;

public class ManipulationCollectingAccess extends AbstractDelegatingAccess {
	
	private List<Manipulation> manipulations;
	
	public ManipulationCollectingAccess(IncrementalAccess delegate, List<Manipulation> manipulations) {
		this.manipulations = manipulations;
		setDelegate(delegate);
	}
	
	@Override
	public ManipulationResponse applyManipulation(ManipulationRequest request)	throws ModelAccessException {
		
		ManipulationResponse response = super.applyManipulation(request);

		Manipulation requestManipulation = request.getManipulation(); 
		if (requestManipulation != null) {
			this.manipulations.add(requestManipulation);
		}
		
		Manipulation inducedManipulation = response.getInducedManipulation();
		if (inducedManipulation != null) {
			this.manipulations.add(inducedManipulation);	
		}
		
		return response;
	}
	
}
