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
package com.braintribe.gwt.gmview.client.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.gwt.gmview.client.TabbedWidgetContext;
import com.braintribe.gwt.gmview.client.js.ExternalWidgetGmContentView;
import com.braintribe.gwt.gmview.client.js.JsUxComponentContext;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.i18n.I18nTools;

import tribefire.extension.js.model.deployment.DetailWithUiComponent;
import tribefire.extension.js.model.deployment.JsUxComponent;
import tribefire.extension.js.model.deployment.PropertyPanelUxComponent;

/**
 * Function responsible for preparing a list of {@link TabbedWidgetContext} based on the given {@link ModelPathElement},
 * by checking the {@link DetailWithUiComponent} and getting the components configured there. When no component is set,
 * then we use the {@link PropertyPanelUxComponent} as default.
 * 
 * @author michel.docouto
 *
 */
public class UiComponentSelectorSupplier implements Function<ModelPathElement, List<TabbedWidgetContext>> {
	
	private PersistenceGmSession gmSession;
	private Set<String> useCases;
	private Map<EntityType<? extends JsUxComponent>, TabbedWidgetContext> wellKnownComponentsContextMap;
	private Function<JsUxComponentContext, ExternalWidgetGmContentView> externalWidgetSupplier;
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the required supplier which supplies an external widget.
	 */
	@Required
	public void setExternalWidgetSupplier(Function<JsUxComponentContext, ExternalWidgetGmContentView> externalWidgetSupplier) {
		this.externalWidgetSupplier = externalWidgetSupplier;
	}
	
	/**
	 * Configures a list of well known components and their contexts.
	 */
	@Configurable
	public void setWellKnownComponentsContextMap(Map<EntityType<? extends JsUxComponent>, TabbedWidgetContext> wellKnownComponentsContextMap) {
		this.wellKnownComponentsContextMap = wellKnownComponentsContextMap;
	}
	
	/**
	 * Use cases used when resolving the {@link DetailWithUiComponent} metadata.
	 */
	@Configurable
	public void setUseCases(Set<String> useCases) {
		this.useCases = useCases;
	}

	@Override
	public List<TabbedWidgetContext> apply(ModelPathElement element) {
		EntityMdResolver entityMdResolver = getEntityMdResolver(element);
		
		if (useCases != null)
			entityMdResolver = entityMdResolver.useCases(useCases);
		
		List<TabbedWidgetContext> contexts = new ArrayList<>();
		
		List<DetailWithUiComponent> list = entityMdResolver.meta(DetailWithUiComponent.T).list();
		int counter = 0;
		for (DetailWithUiComponent md : list) {
			TabbedWidgetContext context = null;
			
			if (wellKnownComponentsContextMap != null) {
				EntityType<? extends JsUxComponent> entityType = null;
				if (md.getComponent() != null)
					entityType = md.getComponent().entityType();
				else
					entityType = PropertyPanelUxComponent.T;
				
				context = wellKnownComponentsContextMap.get(entityType);
			}
			
			if (context == null) {
				String name = md.getComponent().getModule().getName();
				String display = I18nTools.getLocalized(md.getDisplayName());
				//if the DetailWithUiComponent has priority > 0.5, then it will be added first in the list. Otherwise, it will be added in the end
				int index = -1;
				
				double conflictPriority = (md.getConflictPriority() != null) ? md.getConflictPriority() : 0; 				
				if (conflictPriority > 0.5)
					index = counter++;
				context = new TabbedWidgetContext(name, display,() -> {
					ExternalWidgetGmContentView view = externalWidgetSupplier.apply(new JsUxComponentContext(md.getComponent()));
					view.setReadOnly(md.getReadOnly());
					return view;
				}, index, md.getHideDefaultDetails());
			}
			
			contexts.add(context);
		}
		
		return contexts;
	}
	
	private EntityMdResolver getEntityMdResolver(ModelPathElement element) {
		if (element.getValue() instanceof GenericEntity) {
			GenericEntity entity = element.getValue();
			return GmSessions.getMetaData(entity).lenient(true).entity(entity);
		}
		
		return gmSession.getModelAccessory().getMetaData().lenient(true).entityType((EntityType<?>) element.getType());
	}

}
