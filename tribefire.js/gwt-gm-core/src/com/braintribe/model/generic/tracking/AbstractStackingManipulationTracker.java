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

import java.util.Stack;

import com.braintribe.model.generic.manipulation.Manipulation;

public abstract class AbstractStackingManipulationTracker implements ManipulationTracker {
	
	protected abstract Stack<ManipulationCollector> getCollectorStack();
	
	@Override
	public ManipulationCollector getCurrentManipulationCollector() {
		Stack<ManipulationCollector> contextStack = getCollectorStack();
		return !contextStack.isEmpty()? contextStack.peek(): null;
	}
	
	@Override
	public ManipulationCollector begin() {
		ManipulationCollector manipulationCollector = new StandardManipulationCollector();
		getCollectorStack().push(manipulationCollector);
		return manipulationCollector;
	}
	
	@Override
	public ManipulationCollector stop() throws ManipulationTrackingException {
		try {
			return getCollectorStack().pop();
		} catch (Exception e) {
			throw new ManipulationTrackingException("error while stopping tracking", e);
		}
	}
	
	@Override
	public void begin(ManipulationCollector manipulationContext)
			throws ManipulationTrackingException {
		getCollectorStack().push(manipulationContext);
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		ManipulationCollector manipulationContext = getCurrentManipulationCollector();
		if (manipulationContext != null)
			manipulationContext.noticeManipulation(manipulation);
	}
}
