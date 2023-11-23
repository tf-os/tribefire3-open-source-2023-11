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

import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.user.client.ui.Widget;

public class SimpleQueryActionHandler extends AbstractQueryActionHandler<SimpleQueryAction> {
	
	@Override
	public void perform(final WorkbenchActionContext<SimpleQueryAction> workbenchActionContext) {
		Widget parentPanel = getParentPanel(workbenchActionContext.getPanel());
		ExplorerConstellation explorerConstellation = null;
		SelectionConstellation selectionConstellation = null;
		if (parentPanel instanceof ExplorerConstellation)
			explorerConstellation = (ExplorerConstellation) parentPanel;
		else if (parentPanel instanceof SelectionConstellation)
			selectionConstellation = (SelectionConstellation) parentPanel;
		
		if (explorerConstellation == null && selectionConstellation == null)
			return;
		
		String name = getName(workbenchActionContext);
		SimpleQueryAction simpleQueryAction = workbenchActionContext.getWorkbenchAction();
		EntityQuery query = prepareEntityQuery(simpleQueryAction.getTypeSignature());
		
		if (wasHandledInSameTab(workbenchActionContext, explorerConstellation, name, query))
			return;
		
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(query.getEntityTypeSignature());
		
		ModelMdResolver modelMdResolver = explorerConstellation != null
				? explorerConstellation.getGmSession().getModelAccessory().getMetaData() : selectionConstellation.getMetaDataResolver();
		String useCase = explorerConstellation != null ? explorerConstellation.getUseCase() : selectionConstellation.getUseCase();
		
		String description = entityType.getTypeSignature();
		Name nameMD = GMEMetadataUtil.getName(entityType, null, modelMdResolver, useCase);
		if (nameMD != null) {
			String localizedName = I18nTools.getLocalized(nameMD.getName());
			if (localizedName != null)
				description = localizedName;
		}
		
		if (explorerConstellation != null) {
			explorerConstellation.maybeCreateVerticalTabElement(workbenchActionContext, name, description,
					explorerConstellation.provideBrowsingConstellation(name, query), GMEIconUtil.getSmallIcon(workbenchActionContext), query, false) //
					.andThen(result -> handleVerticalTabElement(result, workbenchActionContext)).onError(e -> {
						ErrorDialog.show("Error while creating tab element.", e);
						e.printStackTrace();
					});
		} else if (selectionConstellation != null) {
			VerticalTabElement verticalTabElement = selectionConstellation.maybeCreateVerticalTabElement(workbenchActionContext, name, name,
					description, GMEIconUtil.getSmallIcon(workbenchActionContext),
					selectionConstellation.provideSelectionBrowsingConstellation(name, query, false), false, true, false);
			
			handleVerticalTabElement(verticalTabElement, workbenchActionContext);
		}
	}
	
	private boolean wasHandledInSameTab(WorkbenchActionContext<?> workbenchActionContext, ExplorerConstellation explorerConstellation, String name,
			Query query) {
		if (workbenchActionContext.isHandleInNewTab() || explorerConstellation == null)
			return false;
		
		BrowsingConstellation browsingConstellation = getParentBrowsingConstellation(workbenchActionContext.getPanel());
		if (browsingConstellation == null)
			return false;
		
		explorerConstellation.maybeCreateQueryTetherBarElement(name, query, null, browsingConstellation);
		
		return true;
	}
	
	private BrowsingConstellation getParentBrowsingConstellation(Object panel) {
		if (panel instanceof BrowsingConstellation)
			return (BrowsingConstellation) panel;
		
		if (panel instanceof Widget)
			return getParentBrowsingConstellation(((Widget) panel).getParent());
		
		return null;
	}

	private EntityQuery prepareEntityQuery(String typeSignature) {
		EntityQuery entityQuery = EntityQuery.T.create();
		entityQuery.setEntityTypeSignature(typeSignature);
		return entityQuery;
	}
	
}
