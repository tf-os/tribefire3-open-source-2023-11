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

import java.util.Collections;
import java.util.List;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionMenuBuilder;
import com.braintribe.gwt.gmview.action.client.ActionPerformanceListener;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.google.gwt.user.client.ui.Widget;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType (namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public interface GmContentViewActionManager extends GmSelectionListener {
	
	void connect(GmContentView view);
	void addActionPeformanceListener(ActionPerformanceListener listener);
	void removeActionPeformanceListener(ActionPerformanceListener listener);
	
	@JsIgnore
	Widget getActionMenu(GmContentView view, List<Pair<String, ? extends Widget>> externalComponents, boolean filterExternal);
	
	void resetActions(GmContentView view);
	
	@JsIgnore
	List<? extends Widget> addExternalActions(GmContentView view, List<Pair<ActionTypeAndName, ModelAction>> externalActions);
	
	@JsIgnore
	Widget getActionMenuByGroup(GmContentView view, ActionGroup actionGroup);
	
	@JsIgnore
	void addExternalComponents(GmContentView view, List<Pair<String, ? extends Widget>> externalComponents);
	
	@JsIgnore
	List<Pair<String, ? extends Widget>> getExternalComponentsForView(GmContentView view);
	
	void notifyDisposedView(GmContentView view);
	List<Pair<ActionTypeAndName, ModelAction>> getKnownActionsList(GmContentView view);
	boolean isActionAvailable(ActionTypeAndName actionTypeAndName);
	ActionGroup getActionGroup(GmContentView view);
	
	@JsIgnore
	ActionMenuBuilder getActionMenuBuilder();
	
	/**
	 * @param view - the view for the action.
	 */
	default ModelAction getWorkWithEntityAction(GmContentView view) {
		return null;
	}
	
	/**
	 * @param view - the view for getting the actions.
	 */
	default List<Action> getActionsForView(GmContentView view) {
		return Collections.emptyList();
	}

}
