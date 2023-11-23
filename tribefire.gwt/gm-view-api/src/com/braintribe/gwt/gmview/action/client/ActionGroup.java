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
package com.braintribe.gwt.gmview.action.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.model.resource.Icon;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType (namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class ActionGroup {
	
	private Action action;
	private List<ActionGroup> actionList;
	private Icon icon;
	private ActionTypeAndName actionTypeAndName;
	private boolean complete;
	private String displayName;
	private Set<String> tags = new HashSet<>();
	
	@JsIgnore
	public ActionGroup(Action action) {
		this(action, null, null, null, null, true);
	}
	
	@JsIgnore
	public ActionGroup(Action action, List<ActionGroup> actionList) {
		this(action, actionList, null, null, null, true);
	}
	
	@JsIgnore
	public ActionGroup(Action action, List<ActionGroup> actionList, Icon icon) {
		this(action, actionList, icon, null, null, true);
	}
	
	@JsIgnore
	public ActionGroup(Action action, List<ActionGroup> actionList, Icon icon, ActionTypeAndName actionTypeAndName) {
		this(action, actionList, icon, actionTypeAndName, null, true);
	}
	
	public ActionGroup(Action action, List<ActionGroup> actionList, Icon icon, ActionTypeAndName actionTypeAndName, String displayName, boolean complete) {
		setAction(action);
		setActionList(actionList);
		setIcon(icon);
		setActionTypeAndName(actionTypeAndName);
		setDisplayName(displayName);
		setComplete(complete);
	}
	
	public Action getAction() {
		return action;
	}
	
	public List<ActionGroup> getActionList() {
		return actionList;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public void setActionList(List<ActionGroup> actionList) {
		this.actionList = actionList;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	public ActionTypeAndName getActionTypeAndName() {
		return actionTypeAndName;
	}
	
	public void setActionTypeAndName(ActionTypeAndName actionTypeAndName) {
		this.actionTypeAndName = actionTypeAndName;
	}
	
	public String getActionName() {
		return actionTypeAndName == null ? null : actionTypeAndName.getActionName();
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Set<String> getTags() {
		return this.tags;
	}	
}
