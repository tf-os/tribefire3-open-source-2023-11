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
package com.braintribe.gwt.workbenchaction.processing.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.workbench.KeyConfiguration;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.event.dom.client.KeyCodes;

public class WorkbenchActionHandlerRegistry implements Function<WorkbenchActionContext<?>, ModelAction> {
	
	private Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> workbenchActionHandlerRegistryMap;
	private Function<? super Resource, String> resourceUrlProvider;
	private PersistenceGmSession gmSession;
	private List<ModelActionPosition> modelActionPositions = Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu);
	private Supplier<String> userNameProvider;
	
	@Required
	public void setWorkbenchActionHandlerRegistryMap(Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> workbenchActionHandlerRegistryMap) {
		this.workbenchActionHandlerRegistryMap = workbenchActionHandlerRegistryMap;
	}
	
	@Required
	@Configurable
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	
	/**
	 * Configures the session used for handling resources.
	 * If this is not set, then {@link #resourceUrlProvider} is required.
	 */
	@Configurable
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the URL provider for resources.
	 * If this is not set, then {@link #gmSession} is required.
	 */
	@Configurable
	public void setResourceUrlProvider(Function<? super Resource, String> resourceUrlProvider) {
		this.resourceUrlProvider = resourceUrlProvider;
	}
	
	/**
	 * Configures the list of positions where the created ModelAction will be placed.
	 * Defaults to both the ActionBar and the ContextMenu.
	 */
	@Configurable
	public void setModelActionPositions(List<ModelActionPosition> modelActionPositions) {
		this.modelActionPositions = modelActionPositions;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ModelAction apply(WorkbenchActionContext<?> workbenchActionContext) {
		WorkbenchAction workbenchAction = workbenchActionContext.getWorkbenchAction();
		EntityType<WorkbenchAction> actionEntityType = workbenchAction.entityType();
		Folder folder = workbenchActionContext.getFolder();

		WorkbenchActionHandler handler = null;
		for (Map.Entry<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> entry : workbenchActionHandlerRegistryMap.entrySet()) {
			if (entry.getKey().isAssignableFrom(actionEntityType)) {
				handler = entry.getValue().get();
				break;
			}
		}
		
		if (handler == null)
			return null;
		
		WorkbenchActionHandler finalHandler = handler;
		ModelAction modelAction = new AbstractWorkbenchModelAction() {
			{
				setIcon(GmViewActionResources.INSTANCE.defaultActionIconSmall());
				setHoverIcon(GmViewActionResources.INSTANCE.defaultActionIconLarge());
				setUserNameProvider(userNameProvider);
			}
			
			@Override
			public void perform(TriggerInfo triggerInfo) {
				finalHandler.perform(workbenchActionContext);
			}
			
			@Override
			public TraversingCriterion getInplaceContextCriterion() {
				return workbenchActionContext.getWorkbenchAction().getInplaceContextCriterion();
			}
			
			@Override
			public boolean getMultiSelectionSupport() {
				return workbenchActionContext.getWorkbenchAction().getMultiSelectionSupport();
			}
			
			@Override
			public WorkbenchAction getWorkbenchAction() {
				return workbenchActionContext.getWorkbenchAction();
			}
		};
		
		modelAction.put(ModelAction.PROPERTY_POSITION, modelActionPositions);
		
		if (workbenchAction.getDisplayName() != null || (folder != null && folder.getDisplayName() != null))
			modelAction.setName(I18nTools.getLocalized(workbenchAction.getDisplayName() != null ? workbenchAction.getDisplayName() : folder.getDisplayName()));
		
		if (workbenchAction.getIcon() != null || (folder != null && folder.getIcon() != null)) {
			Icon icon = workbenchAction.getIcon() != null ? workbenchAction.getIcon() : folder.getIcon();
			
			//GSC: 2019-03-16 - Changed 
			// 1. Use the largest image available since the stylesheet is prepared to scale to proper action-size
			// 2. removed duplicated code (small vs. medium size images).
			Resource image = GMEIconUtil.getLargestImageFromIcon(icon);
			if (image != null) {
				GmImageResource gmImageResource;
				if (resourceUrlProvider != null)
					gmImageResource = new GmImageResource(image, resourceUrlProvider);
				else
					gmImageResource = new GmImageResource(image, gmSession.resources().url(image).asString());
				gmImageResource.addGmImageResourceListener(modelAction);
				modelAction.setIcon(gmImageResource);
			} else
				modelAction.setIcon(GmViewActionResources.INSTANCE.defaultActionIconSmall());
			
		} else {
			modelAction.setIcon(GmViewActionResources.INSTANCE.defaultActionIconSmall());
			modelAction.setHoverIcon(GmViewActionResources.INSTANCE.defaultActionIconLarge());
		}
		
		if (workbenchAction.getKeyConfiguration() == null)
			return modelAction;
		
		StringBuilder tooltip = new StringBuilder();
		tooltip.append("(");
		KeyConfiguration config = workbenchAction.getKeyConfiguration();
		if (config.getMeta())
			tooltip.append("META + ");
		if (config.getCtrl())
			tooltip.append("CTRL + ");
		if (config.getAlt())
			tooltip.append("ALT + ");
		
		if (config.getKeyCode() != null) {
			if (KeyCodes.KEY_SPACE == config.getKeyCode())
				tooltip.append("SPACE");
			if (KeyCodes.KEY_ENTER == config.getKeyCode())
				tooltip.append("ENTER");
			if (KeyCodes.KEY_END == config.getKeyCode())
				tooltip.append("END");
			if (KeyCodes.KEY_HOME == config.getKeyCode())
				tooltip.append("HOME");
			if (KeyCodes.KEY_INSERT == config.getKeyCode())
				tooltip.append("INSERT");
			if (KeyCodes.KEY_TAB == config.getKeyCode())
				tooltip.append("TAB");
			else if (KeyCodes.KEY_BACKSPACE == config.getKeyCode())
				tooltip.append("BACKSPACE");
			else if (KeyCodes.KEY_DELETE == config.getKeyCode())
				tooltip.append("DELETE");
			else if (KeyCodes.KEY_DOWN == config.getKeyCode())
				tooltip.append("DOWN");
			else if (KeyCodes.KEY_UP == config.getKeyCode())
				tooltip.append("UP");
			else if (KeyCodes.KEY_LEFT == config.getKeyCode())
				tooltip.append("LEFT");
			else if (KeyCodes.KEY_RIGHT == config.getKeyCode())
				tooltip.append("RIGHT");
			else {
				char c = (char) config.getKeyCode().intValue();
				tooltip.append(c);
			}
		}
		
		tooltip.append(")");
		modelAction.setTooltip(tooltip.toString());
		
		return modelAction;
	}

}
