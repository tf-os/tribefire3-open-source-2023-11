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

import java.util.function.Function;

import com.braintribe.gwt.gme.constellation.client.expert.AbstractPreviewOpenerActionHandler;
import com.braintribe.gwt.gmview.client.GmContentViewWindow;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.js.ExternalWidgetGmContentView;
import com.braintribe.gwt.gmview.client.js.JsUxComponentContext;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.extension.js.model.deployment.JsUxComponent;
import tribefire.extension.js.model.deployment.JsUxPreviewOpenerAction;

/**
 * Handler for handling {@link JsUxPreviewOpenerAction} within the workbench.
 * @author michel.docouto
 *
 */
public class JsUxPreviewOpenerActionHandler extends AbstractPreviewOpenerActionHandler {
	
	private ExternalWidgetGmContentView externalView;
	private Function<JsUxComponentContext, ExternalWidgetGmContentView> jsUxComponentWidgetSupplier;
	private PersistenceGmSession gmSession;
	
	public JsUxPreviewOpenerActionHandler() {
		maskPreview = false;
		handleBeforeHide = true;
	}
	
	/**
	 * Configures the required supplier for a given {@link JsUxComponent}.
	 */
	@Required
	public void setJsUxComponentWidgetSupplier(Function<JsUxComponentContext, ExternalWidgetGmContentView> jsUxComponentWidgetSupplier) {
		this.jsUxComponentWidgetSupplier = jsUxComponentWidgetSupplier;
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession} which will be configured to the external view.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	protected GmListView getView() {
		if (externalView != null)
			return externalView;
		
		JsUxComponent jsUxComponent = ((JsUxPreviewOpenerAction) action).getComponent();
		
		externalView = jsUxComponentWidgetSupplier.apply(new JsUxComponentContext(jsUxComponent, getContentViewWindow()));
		externalView.configureGmSession(gmSession); //TODO: is this needed?
		externalView.addContentSpecificationListener(this);
		return externalView;
	}
	
	private GmContentViewWindow getContentViewWindow() {
		return new GmContentViewWindow() {
			@Override
			public void restore() {
				if (documentWindow != null)
					documentWindow.restore();
			}
			
			@Override
			public void maximize() {
				if (documentWindow != null)
					documentWindow.maximize();
			}
			
			@Override
			public boolean isMaximized() {
				return documentWindow != null ? documentWindow.isMaximized() : false;
			}
			
			@Override
			public void close() {
				if (documentWindow != null)
					documentWindow.hide();
			}
		};
	}

}
