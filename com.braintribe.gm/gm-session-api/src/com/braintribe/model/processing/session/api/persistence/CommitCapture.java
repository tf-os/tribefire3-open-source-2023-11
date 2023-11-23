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
package com.braintribe.model.processing.session.api.persistence;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.session.exception.GmSessionException;

public class CommitCapture {

	private static class ManipulationCollectingListener implements CommitListener {
		
		private final List<Manipulation> manipulations;
		
		public ManipulationCollectingListener(List<Manipulation> manipulations) {
			this.manipulations = manipulations;
		}
	
		@Override
		public void onBeforeCommit(PersistenceGmSession session, Manipulation manipulation) {
			// noop
		}
	
		@Override
		public void onAfterCommit(PersistenceGmSession session,	Manipulation manipulation, Manipulation inducedManipluation) {
			if (manipulation != null) {
				this.manipulations.add(manipulation);
			}
			if (inducedManipluation != null) {
				this.manipulations.add(inducedManipluation);	
			}
		}
	}

	public static Manipulation commitAndCaptureManipulations( PersistenceGmSession session) throws GmSessionException {
		List<Manipulation> collectedManipulations = new ArrayList<Manipulation>();			
		ManipulationCollectingListener commitListener = new ManipulationCollectingListener(collectedManipulations);
		session.listeners().add(commitListener);
		
		session.commit();

		switch (collectedManipulations.size()) {
			case 0:
				return null;
			case 1: 
				return collectedManipulations.get(0);
			default:
				return CompoundManipulation.create(collectedManipulations);
		}				
	}
}
