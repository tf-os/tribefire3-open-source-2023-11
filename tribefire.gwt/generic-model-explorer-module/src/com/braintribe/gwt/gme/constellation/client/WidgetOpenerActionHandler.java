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
package com.braintribe.gwt.gme.constellation.client;

import java.util.function.Supplier;

import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.workbench.WidgetOpenerAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.user.client.ui.Widget;

public class WidgetOpenerActionHandler implements WorkbenchActionHandler<WidgetOpenerAction> {

	@Override
	public void perform(WorkbenchActionContext<WidgetOpenerAction> workbenchActionContext) {
		ExplorerConstellation explorerConstellation = getParentPanel(workbenchActionContext.getPanel());
		if (explorerConstellation != null) {
			WidgetOpenerAction widgetOpenerAction = workbenchActionContext.getWorkbenchAction();
		
			Supplier<Widget> widgetProvider = (Supplier<Widget>) widgetOpenerAction.getWidgetProvider();
			explorerConstellation.maybeCreateVerticalTabElement(workbenchActionContext, getName(workbenchActionContext),
					widgetOpenerAction.getDescription(), widgetProvider, GMEIconUtil.getSmallIcon(workbenchActionContext), null, false);
		}
	}
	
	private String getName(WorkbenchActionContext<WidgetOpenerAction> context) {
		LocalizedString displayName = context.getWorkbenchAction().getDisplayName();
		if (displayName != null)
			return I18nTools.getLocalized(displayName);
		
		return context.getWorkbenchAction().getName();
	}
	
	public static ExplorerConstellation getParentPanel(Object panel) {
		if (panel instanceof ExplorerConstellation)
			return (ExplorerConstellation) panel;
		else if (panel instanceof Widget)
			return getParentPanel(((Widget) panel).getParent());
		
		return null;
	}

}
