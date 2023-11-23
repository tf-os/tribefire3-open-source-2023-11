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
package com.braintribe.model.generic.tracking;

import static com.braintribe.model.generic.manipulation.util.ManipulationBuilder.compound;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.VoidManipulation;

public class StandardManipulationCollector implements ManipulationCollector {
	private final List<Manipulation> manipulations = new ArrayList<Manipulation>();
	private final List<ManipulationListener> manipulationListeners = new ArrayList<ManipulationListener>();
	private boolean collectCompoundManipulations = true;

	public void setCollectCompoundManipulations(boolean collectCompoundManipulations) {
		this.collectCompoundManipulations = collectCompoundManipulations;
	}

	public void addManipulationListener(ManipulationListener listener) {
		manipulationListeners.add(listener);
	}

	public void removeManipulationListener(ManipulationListener listener) {
		manipulationListeners.remove(listener);
	}

	@Override
	public List<Manipulation> getManipulations() {
		return manipulations;
	}

	public Manipulation toManipulation() {
		switch (manipulations.size()) {
			case 0:
				return VoidManipulation.T.createPlain();
			case 1:
				return manipulations.get(0);
			default:
				return compound(manipulations);
		}
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (collectCompoundManipulations || manipulation.manipulationType() != ManipulationType.COMPOUND) {
			manipulations.add(manipulation);
		}

		for (ManipulationListener listener: manipulationListeners) {
			listener.noticeManipulation(manipulation);
		}
	}
}
