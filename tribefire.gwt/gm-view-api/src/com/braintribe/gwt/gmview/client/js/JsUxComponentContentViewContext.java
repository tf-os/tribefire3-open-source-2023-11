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
package com.braintribe.gwt.gmview.client.js;

import java.util.function.Supplier;

import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Icon;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.resources.client.ImageResource;

import tribefire.extension.js.model.deployment.JsUxComponent;
import tribefire.extension.js.model.deployment.ViewWithJsUxComponent;

/**
 * Extension of the {@link GmContentViewContext} that uses the {@link ViewWithJsUxComponent} metadata for preparing its data.
 * @author michel.docouto
 *
 */
public class JsUxComponentContentViewContext extends GmContentViewContext {
	
	private ViewWithJsUxComponent viewWithJsUxComponent;
	private JsUxComponentWidgetSupplier jsUxComponentWidgetSupplier;
	private PersistenceGmSession gmSession;

	public JsUxComponentContentViewContext() {
		super(null, null, null);
	}
	
	/**
	 * Configures the required {@link JsUxComponentWidgetSupplier}.
	 */
	@Required
	public void setJsUxComponentWidgetSupplier(JsUxComponentWidgetSupplier jsUxComponentWidgetSupplier) {
		this.jsUxComponentWidgetSupplier = jsUxComponentWidgetSupplier;
	}
	
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public String getName() {
		ViewWithJsUxComponent withJsUxComponent = getViewWithJsUxComponent();
		LocalizedString displayName = withJsUxComponent.getDisplayName();
		return displayName != null ? I18nTools.getLocalized(displayName) : withJsUxComponent.getComponent().getModule().getName();
	}
	
	@Override
	public String getUseCase() {
		return getViewWithJsUxComponent().getComponent().getModule().getName();
	}
	
	@Override
	public ImageResource getHoverIcon() {
		return GmViewActionResources.INSTANCE.viewBig();
	}
	
	@Override
	public ImageResource getIcon() {
		Icon icon = getViewWithJsUxComponent().getIcon();
		return icon != null ? GMEIconUtil.transform(GMEIconUtil.getSmallestImageFromIcon(icon)) : GmViewActionResources.INSTANCE.view();
	}
	
	@Override
	public Supplier<? extends GmContentView> getContentViewProvider() {
		return () -> {
			ViewWithJsUxComponent viewWithJsUxComponent = getViewWithJsUxComponent();
			JsUxComponent component = viewWithJsUxComponent.getComponent();
			if (component == null)
				throw new RuntimeException("The ViewWithJsUxComponent metadata has no component configured on it. Please fix its configuration.");
			
			ExternalWidgetGmContentView view = jsUxComponentWidgetSupplier.apply(new JsUxComponentContext(component));
			view.setReadOnly(viewWithJsUxComponent.getReadOnly());
			return view;
		};
	}
	
	@Override
	public boolean isListView() {
		return getViewWithJsUxComponent().getListView();
	}
	
	private ViewWithJsUxComponent getViewWithJsUxComponent() {
		if (viewWithJsUxComponent != null)
			return viewWithJsUxComponent;
		
		ModelPathElement modelPathElement = getModelPathElement();
		GenericEntity entity = modelPathElement.getValue();
		if (entity != null)
			viewWithJsUxComponent = GmSessions.getMetaData(entity).entity(entity).meta(ViewWithJsUxComponent.T).exclusive();
		else {
			viewWithJsUxComponent = gmSession.getModelAccessory().getMetaData().lenient(true).entityType((EntityType<?>) modelPathElement.getType())
					.meta(ViewWithJsUxComponent.T).exclusive();
		}
		
		if (viewWithJsUxComponent != null)
			setShowDetails(!viewWithJsUxComponent.getHideDetails());
		
		return viewWithJsUxComponent;
	}

}
