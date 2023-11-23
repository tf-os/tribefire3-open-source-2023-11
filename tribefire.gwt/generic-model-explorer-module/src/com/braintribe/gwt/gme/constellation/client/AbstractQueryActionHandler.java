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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gm.storage.api.StorageHandle;
import com.braintribe.gwt.gm.storage.expert.api.QueryStorageExpert;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.qc.api.client.QueryProviderContext;
import com.braintribe.gwt.qc.api.client.QueryProviderStorageHandle;
import com.braintribe.gwt.qc.api.client.QueryProviderView;
import com.braintribe.gwt.qc.api.client.QueryProviderViewListener;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract implementation of the {@link WorkbenchActionHandler}, with Util methods.
 * 
 * @author michel.docouto
 */
public abstract class AbstractQueryActionHandler<T extends WorkbenchAction> implements WorkbenchActionHandler<T> {
	private QueryStorageExpert queryStorageExpert;
	private Supplier<? extends QueryStorageExpert> queryStorageExpertSupplier;

	@Required
	public void setQueryStorageExpert(Supplier<? extends QueryStorageExpert> queryStorageExpertSupplier) {
		this.queryStorageExpertSupplier = queryStorageExpertSupplier;
	}

	protected String getName(WorkbenchActionContext<T> context) {
		//if (!context.isHandleInNewTab()) {
		//RVE changed the getting of the name - did not see reason to get different name if opened at new tab or at tether element -> name should be same
		List<com.braintribe.model.generic.path.ModelPath> listModelPath = context.getModelPaths();
		if (listModelPath != null && listModelPath.size() > 0) {
			ModelPathElement element = listModelPath.get(0).last();
			if (element.getType().isEntity()) {
				EntityType<?> entityType = element.getType();
				GenericEntity entity = element.getValue();
				String useCase = null;
				if (context.getPanel() instanceof GmContentView)
					useCase = ((GmContentView) context.getPanel()).getUseCase();
				return SelectiveInformationResolver.resolve(entityType, entity, (ModelMdResolver) null, useCase);
			}
		}
		
		LocalizedString displayName = context.getWorkbenchAction().getDisplayName();
		if (displayName != null)
			return I18nTools.getLocalized(displayName);

		Folder folder = context.getFolder();
		if (folder != null) { 
			displayName = folder.getDisplayName();
			if (displayName != null)
				return I18nTools.getLocalized(displayName);
		}

		return folder != null ? folder.getName() : null;
	}

	protected void handleVerticalTabElement(final VerticalTabElement verticalTabElement, final WorkbenchActionContext<T> workbenchActionContext) {
		verticalTabElement.setTabElementActions(Arrays.asList(prepareNewAction(workbenchActionContext), prepareReloadAction(verticalTabElement)));
		configureStorageHandle(verticalTabElement, workbenchActionContext);
	}

	protected static Widget getParentPanel(Object panel) {
		if (panel instanceof ExplorerConstellation || panel instanceof SelectionConstellation)
			return (Widget) panel;
		else if (panel instanceof Widget)
			return getParentPanel(((Widget) panel).getParent());

		return null;
	}

	protected void configureStorageHandle(VerticalTabElement tabElement, final WorkbenchActionContext<T> workbenchActionContext) {
		BrowsingConstellation browsingConstellation = (BrowsingConstellation) tabElement.getWidget();
		TetherBar tetherBar = browsingConstellation.getTetherBar();
		GmContentView view = tetherBar.getElementAt(0).getContentViewIfProvided();
		
		if (!(view instanceof QueryConstellation))
			return;
		
		QueryProviderView<?> queryProviderView = ((QueryConstellation) view).getQueryProviderView();
		queryProviderView.addQueryProviderViewListener(new QueryProviderViewListener() {
			@Override
			public void onModeChanged(QueryProviderView<GenericEntity> newQueryProviderView, boolean advanced) {
				newQueryProviderView.removeQueryProviderViewListener(this);
				newQueryProviderView.addQueryProviderViewListener(this);

				if (!(newQueryProviderView instanceof QueryProviderStorageHandle))
					return;
				
				QueryStorageExpert queryStorageExpert = getQueryStorageExpert();
				if (queryStorageExpert == null)
					return;
				
				QueryProviderStorageHandle queryProviderStorageHandle = (QueryProviderStorageHandle) newQueryProviderView;

				// Set expert before handle
				queryProviderStorageHandle.setQueryStorageExpert(queryStorageExpert);

				Folder folder = workbenchActionContext.getFolder();
				if (folder != null && (folder.getTags() == null || !folder.getTags().contains(Workbench.FAKE_FOLDER))) {
					StorageHandle storageHandle = queryStorageExpert.prepareStorageHandle(folder);
					queryProviderStorageHandle.setStorageHandle(storageHandle);
				}
			}

			@Override
			public void onQueryPerform(QueryProviderContext queryProviderContext) {
				// Nothing to do
			}

			@Override
			public void configureEntityType(EntityType<?> entityType, boolean configureEntityTypeForCheck) {
				// Nothing to do
			}
		});

		QueryStorageExpert queryStorageExpert = getQueryStorageExpert();
		if (!(queryProviderView instanceof QueryProviderStorageHandle) || queryStorageExpert == null)
			return;
		
		QueryProviderStorageHandle queryProviderStorageHandle = (QueryProviderStorageHandle) queryProviderView;
		// Set expert before handle
		queryProviderStorageHandle.setQueryStorageExpert(queryStorageExpert);
		
		Folder folder = workbenchActionContext.getFolder();
		if (folder != null && (folder.getTags() == null || !folder.getTags().contains(Workbench.FAKE_FOLDER))) {
			StorageHandle storageHandle = queryStorageExpert.prepareStorageHandle(folder);
			queryProviderStorageHandle.setStorageHandle(storageHandle);
		}
	}

	private Action prepareNewAction(final WorkbenchActionContext<T> context) {
		Action newAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				AbstractQueryActionHandler.this.perform(context);
			}
		};
		newAction.setName(LocalizedText.INSTANCE.newEntry());

		return newAction;
	}

	private static Action prepareReloadAction(final VerticalTabElement verticalTabElement) {
		Action reloadAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				BrowsingConstellation browsingConstellation = (BrowsingConstellation) verticalTabElement.getWidget();
				TetherBar tetherBar = browsingConstellation.getTetherBar();
				GmContentView view = tetherBar.getElementAt(0).getContentViewIfProvided();
				if (!(view instanceof QueryConstellation))
					return; // TODO: should we do something?
				
				if (tetherBar.getElementsSize() > 1) {
					List<TetherBarElement> elements = new ArrayList<>();
					for (int i = 1; i < tetherBar.getElementsSize(); i++)
						elements.add(tetherBar.getElementAt(i));
					tetherBar.removeTetherBarElements(elements);
				}
				((QueryConstellation) view).reload();
			}
		};
		reloadAction.setName(LocalizedText.INSTANCE.reload());

		return reloadAction;
	}
	
	private QueryStorageExpert getQueryStorageExpert() {
		if (queryStorageExpert != null || queryStorageExpertSupplier == null)
			return queryStorageExpert;
		
		queryStorageExpert = queryStorageExpertSupplier.get();
		return queryStorageExpert;
	}
}
