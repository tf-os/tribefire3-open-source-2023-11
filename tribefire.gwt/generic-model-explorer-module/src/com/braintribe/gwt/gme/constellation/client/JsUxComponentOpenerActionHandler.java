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

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gmview.client.GmContentViewWindow;
import com.braintribe.gwt.gmview.client.js.ExternalWidgetGmContentView;
import com.braintribe.gwt.gmview.client.js.JsUxComponentContext;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;

import tribefire.extension.js.model.deployment.JsUxComponent;
import tribefire.extension.js.model.deployment.JsUxComponentOpenerAction;

/**
 * Handler for handling {@link JsUxComponentOpenerAction} configured within the workbench.
 * @author michel.docouto
 *
 */
public class JsUxComponentOpenerActionHandler implements WorkbenchActionHandler<JsUxComponentOpenerAction> {
	public static String EXTERNAL_COMPONENT_ID_WINDOW_PREFIX = "gmeExternalComponentWindow";
	
	private Function<JsUxComponentContext, ExternalWidgetGmContentView> jsUxComponentWidgetSupplier;
	private PersistenceGmSession gmSession;
	private boolean handleBeforeHide = true;
	
	/**
	 * Configures the required supplier for a given {@link JsUxComponent}.
	 */
	@Required
	public void setJsUxComponentWidgetSupplier(Function<JsUxComponentContext, ExternalWidgetGmContentView> jsUxComponentWidgetSupplier) {
		this.jsUxComponentWidgetSupplier = jsUxComponentWidgetSupplier;
	}
		
	@Required
	public void setPersistenceSession(PersistenceGmSession persistenceSession) {
		this.gmSession = persistenceSession;
	}
	
	@Override
	public void perform(WorkbenchActionContext<JsUxComponentOpenerAction> workbenchActionContext) {
		JsUxComponentOpenerAction jsUxComponentOpenerAction = workbenchActionContext.getWorkbenchAction();
		List<ModelPath> modelPaths = workbenchActionContext.getModelPaths();
		if (jsUxComponentOpenerAction.getOpenAsModal()) {
			handleOpenAsModal(jsUxComponentOpenerAction, modelPaths);
			return;
		}
		
		ExplorerConstellation explorerConstellation = getParentPanel(workbenchActionContext.getPanel());
		if (explorerConstellation != null) {
			JsUxComponent jsUxComponent = jsUxComponentOpenerAction.getComponent();
			ExternalWidgetGmContentView view = jsUxComponentWidgetSupplier.apply(new JsUxComponentContext(jsUxComponent));
			
			explorerConstellation.maybeCreateVerticalTabElement(workbenchActionContext, getName(workbenchActionContext),
					jsUxComponentOpenerAction.getDescription(), getViewSupplier(view, modelPaths, jsUxComponentOpenerAction.getReadOnly()),
					GMEIconUtil.getSmallIcon(workbenchActionContext), null, false, true);
		}
	}
	
	private Supplier<ExternalWidgetGmContentView> getViewSupplier(ExternalWidgetGmContentView view, List<ModelPath> modelPaths, boolean readOnly) {
		return () -> {
			view.configureGmSession(gmSession);
			view.setReadOnly(readOnly);
			handleContent(view, modelPaths);
			return view;
		};
	}
	
	private static ExplorerConstellation getParentPanel(Object panel) {
		if (panel instanceof ExplorerConstellation)
			return (ExplorerConstellation) panel;
		else if (panel instanceof Widget)
			return getParentPanel(((Widget) panel).getParent());
		
		return null;
	}
	
	private String getName(WorkbenchActionContext<JsUxComponentOpenerAction> context) {
		LocalizedString displayName = context.getWorkbenchAction().getDisplayName();
		if (displayName != null)
			return I18nTools.getLocalized(displayName);
		
		return context.getWorkbenchAction().getComponent().getModule().getName();
	}
	
	private void handleOpenAsModal(JsUxComponentOpenerAction action, List<ModelPath> modelPaths) {
		ExternalComponentWindow window = new ExternalComponentWindow();
		
		JsUxComponent jsUxComponent = action.getComponent();
		ExternalWidgetGmContentView view = jsUxComponentWidgetSupplier.apply(new JsUxComponentContext(jsUxComponent, window));
		view.configureGmSession(gmSession);
		handleContent(view, modelPaths);
		
		String heading = null;
		if (action.getDescription() != null)
			heading = action.getDescription();
		
		if (heading == null) {
			String name = jsUxComponent.getModule().getName();
			if (name != null)
				heading = name;
		}
		
		window.setView(view);
		if (heading != null)
			window.setHeading(heading);
		window.add(view);
		window.show();
		window.center();
	}
	
	private void handleContent(ExternalWidgetGmContentView view, List<ModelPath> modelPaths) {
		if (modelPaths == null || modelPaths.isEmpty()) {
			// In this scenario, we won't have any content at all, but still, the external component must be
			// notified. Thus, we are initializing the content with null
			view.setContent(null);
			view.addContent(null);
		} else {
			if (modelPaths.size() == 1)
				view.setContent(modelPaths.get(0));
			else {
				view.setContent(null);
				modelPaths.forEach(modelPath -> view.addContent(modelPath));
			}
		}
	}
	
	private class ExternalComponentWindow extends Window implements IgnoreKeyConfigurationDialog, GmContentViewWindow {
		private ExternalWidgetGmContentView view;
		
		public ExternalComponentWindow() {
			setId(EXTERNAL_COMPONENT_ID_WINDOW_PREFIX);
		}
		
		public void setView(ExternalWidgetGmContentView view) {
			this.view = view;
			
			int width = Document.get().getClientWidth() < 1024 ? Document.get().getClientWidth() : 1024;
			int height = Document.get().getClientHeight() - 25 < 768 ? Document.get().getClientHeight() - 25 : 768;
			setPixelSize(width, height);
			
			setModal(true);
			setMaximizable(true);
			//setClosable(true);
			//setOnEsc(true);
			addStyleName("gmeDialog");
			addStyleName("gmeComponentDialog");
			setBodyBorder(false);
			setBorders(false);
			addBeforeHideHandler(event -> {
				if (!handleBeforeHide) {
					handleBeforeHide = true;
					return;
				}

				Future<Boolean> future = view.waitReply();
				if (future == null)
					return;
				
				event.setCancelled(true);
				future.andThen(close -> {
					if (!close)
						handleBeforeHide = true;
					else {
						handleBeforeHide = false;
						hide();
					}
				});
			});
			
			addResizeHandler(event -> {
				view.onResize();
				view.sendUxMessage("Window", "onResize", getOffsetWidth() + "," + getOffsetHeight());
			});
			//setShadow(false);
			//getHeader().setHeight(15);
		}
		
		@Override
		public void close() {
			hide();
		}
		
		@Override
		public void maximize() {
			super.maximize();
			Scheduler.get().scheduleDeferred(() -> {
				view.onMaximize();
				view.sendUxMessage("Window", "onMaximize", getOffsetWidth() + "," + getOffsetHeight());
			});
		}
		
		@Override
		public void restore() {
			super.restore();
			Scheduler.get().scheduleDeferred(() -> {
				view.onMinimize();
				view.sendUxMessage("Window", "onRestore", getOffsetWidth() + "," + getOffsetHeight());
			});
		}
	}

}
