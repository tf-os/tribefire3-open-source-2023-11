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
package com.braintribe.gwt.genericmodel.client.tracking;

import java.util.Stack;

import com.braintribe.model.generic.tracking.AbstractStackingManipulationTracker;
import com.braintribe.model.generic.tracking.ManipulationCollector;

public class GwtManipulationTracking extends AbstractStackingManipulationTracker {
	private Stack<ManipulationCollector> collectorStack = new Stack<ManipulationCollector>();
	
	@Override
	protected Stack<ManipulationCollector> getCollectorStack() {
		return collectorStack;
	}
}
