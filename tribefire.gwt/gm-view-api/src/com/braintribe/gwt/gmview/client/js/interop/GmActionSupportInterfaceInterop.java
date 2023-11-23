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

import java.util.List;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.ModelAction;

import jsinterop.annotations.JsMethod;

/**
 * Interface used for exporting {@link GmActionSupport} via JsInterop.
 */
@SuppressWarnings("unusable-by-js")
public interface GmActionSupportInterfaceInterop extends GmActionSupport {
	
	@Override
	@JsMethod
	public void setActionManager(GmContentViewActionManager actionManager);
	
	@Override
	@JsMethod
	public void configureActionGroup(ActionGroup actionGroup);
	
	@Override
	@JsMethod
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions);
	
	@Override
	@JsMethod
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions();

}
