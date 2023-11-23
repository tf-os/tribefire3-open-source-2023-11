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
package com.braintribe.gwt.ioc.gme.client.expert;

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gme.workbench.client.Workbench;
import com.braintribe.gwt.gme.workbench.client.WorkbenchListenerAdapter;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.ioc.gme.client.Runtime;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.workbench.WorkbenchConfiguration;
import com.google.gwt.dom.client.Style.Visibility;
import com.sencha.gxt.core.client.Style.LayoutRegion;
import com.sencha.gxt.widget.core.client.SplitBar;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;

/**
 * This controller is responsible for controlling some of the behavior of the {@link Workbench} used within the {@link ExplorerConstellation}
 * @author michel.docouto
 *
 */
public class ExplorerWorkbenchController implements InitializableBean {
	private ExplorerConstellation explorerConstellation;
	private SpotlightPanel quickAccessPanel;
	private boolean workbenchHidden = false;
	private double lastWorkbenchSize;
	private SplitBar workbenchSplitBar;
	
	/**
	 * Configures the required {@link ExplorerConstellation}, with the Workbench that will be linked to the
	 * {@link SpotlightPanel} configured via {@link #setQuickAccessPanel(SpotlightPanel)}.
	 */
	@Required
	public void setExplorerConstellation(final ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
		
		if (explorerConstellation.isUseWorkbenchWithinTab() || explorerConstellation.getWorkbench() == null)
			return;
		
		Workbench workbench = explorerConstellation.getWorkbench();
		workbench.addWorkbenchListener(new WorkbenchListenerAdapter() {
			@Override
			public void onWorkenchRefreshed(boolean containsData) {
				BorderLayoutData westBorderLayoutData = explorerConstellation.getWestBorderLayoutData();
				if (containsData && workbenchHidden) {
					workbenchHidden = false;
					westBorderLayoutData.setSize(lastWorkbenchSize);
					westBorderLayoutData.setSplit(true);
					workbench.setData("splitBar", workbenchSplitBar);
					workbenchSplitBar.getElement().getStyle().clearVisibility();
					explorerConstellation.doLayout();
				} else if (!containsData && !workbenchHidden) {
					lastWorkbenchSize = westBorderLayoutData.getSize();
					workbenchHidden = true;
					westBorderLayoutData.setSize(0);
					westBorderLayoutData.setSplit(false);
					workbenchSplitBar = explorerConstellation.getSplitBar(LayoutRegion.WEST);
					workbenchSplitBar.getElement().getStyle().setVisibility(Visibility.HIDDEN);
					workbench.setData("splitBar", null);
					explorerConstellation.doLayout();
				} else {
					workbenchSplitBar = explorerConstellation.getSplitBar(LayoutRegion.WEST);
					workbenchSplitBar.getElement().getStyle().clearVisibility();
				}
			}
			
			@Override
			public void onModelEnvironmentChanged(ModelEnvironment modelEnvironment) {
				quickAccessPanel.onModelEnvironmentChanged();
				WorkbenchConfiguration workbenchConfiguration = modelEnvironment.getWorkbenchConfiguration();
				Runtime.useGlobalSearchPanel = workbenchConfiguration == null ? false : workbenchConfiguration.getUseGlobalSearch();
			}
		});
	}
	
	/**
	 * Configures the required {@link SpotlightPanel}, which will have its events listened by the {@link ExplorerConstellation}'s {@link Workbench}.
	 */
	@Required
	public void setQuickAccessPanel(SpotlightPanel quickAccessPanel) {
		this.quickAccessPanel = quickAccessPanel;
	}
	
	/**
	 * Configures the required {@link QuickAccessTriggerField}, which will have its events listened.
	 */
	@Required
	public void setQuickAccessTriggerField(QuickAccessTriggerField quickAccessTriggerField) {
		quickAccessTriggerField.addQuickAccessTriggerFieldListener(result -> {
			if (result != null && isUseServiceRequestPanel(result.getObjectAndType()))
				explorerConstellation.handleServiceRequestPanel(result);
			else
				explorerConstellation.getWorkbench().handleQuickAccessResult(result);
		});
	}
	
	@Override
	public void intializeBean() {
		quickAccessPanel.setLoadExistingValues(false);
		quickAccessPanel.setDisplayValuesSection(false);
		quickAccessPanel.setUseCase(explorerConstellation.getWorkbench() != null ? explorerConstellation.getWorkbench().getUseCase() : "");
	}
	
	private boolean isUseServiceRequestPanel(ObjectAndType objectAndType) {
		if (objectAndType == null)
			return false;
		
		if (!objectAndType.isServiceRequest())
			return false;
		
		EntityType<?> entityType = GMF.getTypeReflection().getEntityType(objectAndType.getType().getTypeSignature());
		if (entityType.isAbstract())
			return false;
		
		ViewWithJsUxComponent viewWithJsUxComponent = explorerConstellation.getTransientGmSession().getModelAccessory().getMetaData().lenient(true)
				.entityType(entityType).meta(ViewWithJsUxComponent.T).exclusive();
		
		return viewWithJsUxComponent == null;
	}

}
