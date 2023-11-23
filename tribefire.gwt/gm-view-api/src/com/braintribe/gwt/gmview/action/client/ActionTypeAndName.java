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

import com.braintribe.gm.model.uiaction.ActionFolderContent;
import com.braintribe.gwt.gmview.client.js.interop.InteropConstants;
import com.braintribe.model.generic.reflection.EntityType;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType (namespace = InteropConstants.VIEW_NAMESPACE)
public class ActionTypeAndName {
	
	private EntityType<? extends ActionFolderContent> denotationType;
	private String actionName;

	public ActionTypeAndName(EntityType<? extends ActionFolderContent> denotationType, String actionName) {
		this.denotationType = denotationType;
		this.actionName = actionName;
	}

	@JsIgnore
	public ActionTypeAndName() {
		this(null, null);
	}
	
	@JsIgnore
	public ActionTypeAndName(String actionName) {
		this(null, actionName);
	}
	
	@JsIgnore
	public ActionTypeAndName(ActionTypeAndName other) {
		this(other.getDenotationType(), other.getActionName());
	}

	public EntityType<? extends ActionFolderContent> getDenotationType() {
		return denotationType;
	}
	
	public void setDenotationType(EntityType<? extends ActionFolderContent> denotationType) {
		this.denotationType = denotationType;
	}
	
	public String getActionName() {
		return actionName;
	}
	
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionName == null) ? 0 : actionName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionTypeAndName other = (ActionTypeAndName) obj;
		
		if (actionName != null && other.actionName != null)
			return actionName.equals(other.actionName);
		
		if (denotationType != null && other.denotationType != null)
			return denotationType.equals(other.denotationType);
		
		if (actionName == null) {
			if (other.actionName != null)
				return false;
		} else if (!actionName.equals(other.actionName))
			return false;
		
		if (denotationType == null) {
			if (other.denotationType != null)
				return false;
		} else if (!denotationType.equals(other.denotationType))
			return false;
		
		return true;
	}
	
}
