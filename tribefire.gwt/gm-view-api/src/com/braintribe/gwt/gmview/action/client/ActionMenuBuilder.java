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

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;

public interface ActionMenuBuilder extends GmSessionHandler, Function<Folder, Future<Void>> {
	
	Menu getContextMenu(GmContentView view, List<Pair<String, ? extends Widget>> externalComponents, ActionGroup actionGroup, boolean filterExternal);
	ActionGroup prepareActionGroup(List<Pair<ActionTypeAndName, ModelAction>> knownActions, GmContentView gmContentView);
	void onSelectionChanged(ActionGroup actionGroup, GmSelectionSupport gmSelectionSupport);
	Set<ActionTypeAndName> updateActionGroup(ActionGroup actionGroup, List<Pair<ActionTypeAndName, ModelAction>> externalActions);
	List<Item> addExternalActionsToMenu(GmContentView view, Widget actionMenuWidget, List<ModelAction> externalActions);
	List<Item> updateMenu(GmContentView view, Widget actionMenuWidget, List<Pair<ActionTypeAndName, ModelAction>> externalActions, ActionGroup actionGroup);
	void setWorkbenchSession(PersistenceGmSession workbenchSession);
	void notifyDisposedView(Widget viewMenu);
	
}
