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
package com.braintribe.gwt.gmview.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.gmview.action.client.ActionPerformanceContext;
import com.braintribe.gwt.gmview.action.client.ActionPerformanceListener;

public abstract class PerformanceListenerBoundModelAction extends ModelAction {

	private List<ActionPerformanceListener> actionPerformanceListeners = new ArrayList<ActionPerformanceListener>();
	
	public void addActionPeformanceListener(ActionPerformanceListener listener) {
		actionPerformanceListeners.add(listener);
	}
	
	public void removeActionPerformanceListener(ActionPerformanceListener listener) {
		actionPerformanceListeners.remove(listener);
	}
	
	public void setActionPerformanceListeners(List<ActionPerformanceListener> actionPerformanceListeners) {
		this.actionPerformanceListeners = actionPerformanceListeners;
	}
	
	public void fireOnBeforePerformAction(ActionPerformanceContext actionPerformanceContext) {
		for (ActionPerformanceListener listener : actionPerformanceListeners) {
			listener.onBeforePerformAction(actionPerformanceContext);
		}
	}
	
	public void fireOnAfterPerformAction(ActionPerformanceContext actionPerformanceContext) {
		for (ActionPerformanceListener listener : actionPerformanceListeners) {
			listener.onAfterPerformAction(actionPerformanceContext);
		}
	}

}
