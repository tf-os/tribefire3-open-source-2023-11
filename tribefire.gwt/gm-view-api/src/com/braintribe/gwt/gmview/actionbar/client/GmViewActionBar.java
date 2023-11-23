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
package com.braintribe.gwt.gmview.actionbar.client;

import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.utils.client.RootKeyNavExpert.RootKeyNavListener;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.TemplateBasedAction;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface defining the operations for the ActionBar.
 * @author michel.docouto
 *
 */
public interface GmViewActionBar extends Loader<Void>, Supplier<List<TemplateBasedAction>>, RootKeyNavListener {
	
	Widget getView();
	void prepareActionsForView(GmViewActionProvider view);
	void setWorkbenchSession(PersistenceGmSession workbenchSession);
	void navigateToAction(ActionTypeAndName actionTyeAndName);
	void navigateToAction(Action action);
    void setToolBarVisible(boolean toolBarVisible);
}
