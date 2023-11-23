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
package com.braintribe.gwt.gmview.client.js.interop;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.actionbar.client.ActionProviderConfiguration;
import com.braintribe.gwt.gmview.actionbar.client.GmViewActionProvider;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.ModelAction;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Class needed for being able to export the following classes via JsInterop: {@link GmContentView},
 * {@link GmActionSupport} and {@link GmViewActionProvider}.
 */
@JsType(name = "GmActionContentView", namespace = InteropConstants.VIEW_NAMESPACE)
@SuppressWarnings("unusable-by-js")
public class GmActionContentViewInterop extends GmContentViewInterop implements GmActionSupportInterfaceInterop, GmViewActionProviderInterfaceInterop {
	private GmContentViewActionManager actionManager;
	private ActionProviderConfiguration actionProviderConfiguration;
	private List<Pair<ActionTypeAndName, ModelAction>> externalActions;
	
	@JsConstructor
	public GmActionContentViewInterop() {
		super();
	}

	@Override
	@JsMethod
	public void setActionManager(GmContentViewActionManager actionManager) {
		this.actionManager = actionManager;
	}

	@Override
	@JsMethod
	public void configureActionGroup(ActionGroup actionGroup) {
		//NOP
	}

	@Override
	@JsMethod
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		this.externalActions = externalActions;
		
		if (externalActions != null) {
			if (actionProviderConfiguration != null)
				actionProviderConfiguration.addExternalActions(externalActions);
			if (actionManager != null) //Already initialized
				actionManager.addExternalActions(getViewWidget(), externalActions);
		}
	}

	@Override
	@JsMethod
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return externalActions;
	}
	
	@Override
	@JsMethod
	public ActionProviderConfiguration getActions() {
		if (actionProviderConfiguration != null)
			return actionProviderConfiguration;
		
		actionProviderConfiguration = new ActionProviderConfiguration();
		actionProviderConfiguration.setGmContentView(getViewWidget());
		
		List<Pair<ActionTypeAndName, ModelAction>> knownActions = null;
		if (actionManager != null)
			knownActions = actionManager.getKnownActionsList(getViewWidget());
		
		if (knownActions != null || externalActions != null) {
			List<Pair<ActionTypeAndName, ModelAction>> allActions = new ArrayList<>();
			if (knownActions != null)
				allActions.addAll(knownActions);
			if (externalActions != null)
				allActions.addAll(externalActions);

			actionProviderConfiguration.addExternalActions(allActions);
		}			
		
		return actionProviderConfiguration;
	}
	
	private GmContentView getViewWidget() {
		return viewWidget != null ? viewWidget : this;
	}

	@Override
	@JsMethod
	public boolean isFilterExternalActions() {
		return false;
	}

}
