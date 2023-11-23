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
package com.braintribe.gwt.querymodeleditor.client.panels.editor;

import java.util.List;

import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.QueryConstellation;
import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessTriggerField;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar;
import com.braintribe.gwt.gme.tetherbar.client.TetherBar.TetherBarListener;
import com.braintribe.gwt.gme.tetherbar.client.TetherBarElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.qc.api.client.QueryProviderActions;
import com.braintribe.gwt.qc.api.client.QueryProviderContext;
import com.braintribe.gwt.qc.api.client.QueryProviderView;
import com.braintribe.gwt.qc.api.client.QueryProviderViewListener;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;

/**
 * Panel used as a replacement of both the QueryModelEditor and the SpotlightPanel.
 * @author michel.docouto
 *
 */
public class GlobalSearchPanel extends ContentPanel implements InitializableBean, VerticalTabListener, TetherBarListener, QueryProviderViewListener, DisposableBean {
	
	private QueryProviderView<GenericEntity> queryProviderView;
	private QuickAccessTriggerField quickAccessTriggerField;
	private boolean hasContext;
	private ExplorerConstellation explorerConstellation;
	private TetherBar currentTetherBar;
	private String currentContextName;
	private QueryConstellation currentQueryConstellation;
	
	public GlobalSearchPanel() {
		setStyleName("gmeGlobalSearchPanel");
		setHeaderVisible(false);
		setDeferHeight(false);
		setBodyBorder(false);
		setBorders(false);
		setWidth(800);
	}
	
	/**
	 * Configures the required {@link ExplorerConstellation}. We will listen for tab changes, and tetherBar changes within those tabs.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
		explorerConstellation.getVerticalTabPanel().addVerticalTabListener(this);
	}
	
	/**
	 * Configures the required panel which will be shown and used as default search field.
	 */
	@Required
	public void setQueryProviderView(QueryProviderView<GenericEntity> queryProviderView) {
		this.queryProviderView = queryProviderView;
		
		queryProviderView.addQueryProviderViewListener(this);
		queryProviderView.setCurrentContext(true, null);
	}
	
	/**
	 * Configures the required {@link QuickAccessTriggerField} to be used when there is no context or the context is disabled.
	 */
	@Required
	public void setQuickAccessTriggerField(QuickAccessTriggerField quickAccessTriggerField) {
		this.quickAccessTriggerField = quickAccessTriggerField;
	}
	
	@Override
	public void intializeBean() throws Exception {
		add(queryProviderView.getWidget());
		quickAccessTriggerField.configureExternalField((Component) queryProviderView);
	}
	
	@Override
	public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
		if (currentTetherBar != null) {
			currentTetherBar.removeTetherBarListener(this);
			currentTetherBar = null;
		}
		
		Widget widget = verticalTabElement.getWidgetIfSupplied();
		if (widget instanceof QueryConstellation)
			addQueryContext((QueryConstellation) widget);
		else if (widget instanceof BrowsingConstellation) {
			BrowsingConstellation browsingConstellation = (BrowsingConstellation) widget;
			GmContentView view = browsingConstellation.getTetherBar().getSelectedElement().getContentViewIfProvided();
			if (view instanceof QueryConstellation)
				addQueryContext((QueryConstellation) view);
			else
				removeQueryContext();
			
			currentTetherBar = browsingConstellation.getTetherBar();
			currentTetherBar.addTetherBarListener(this);
		} else
			removeQueryContext();
	}
	
	@Override
	public void onTetherBarElementSelected(TetherBarElement tetherBarElement) {
		GmContentView view = tetherBarElement.getContentViewIfProvided();
		if (view instanceof QueryConstellation)
			addQueryContext((QueryConstellation) view);
		else
			removeQueryContext();
	}
	
	@Override
	public void onModeChanged(QueryProviderView<GenericEntity> newQueryProviderView, boolean advanced) {
		exchangeCurrentQueryView(newQueryProviderView);
	}
	
	private void addQueryContext(QueryConstellation queryConstellation) {
		currentQueryConstellation = queryConstellation;
		
		if (queryProviderView instanceof HasText && !currentQueryConstellation.isCurrentQueryAdvanced())
			((HasText) queryProviderView).setText(currentQueryConstellation.getCurrentBasicText());
		else if (queryProviderView instanceof HasText)
			((HasText) queryProviderView).setText(null);
		
		VerticalTabElement verticalTabElement = explorerConstellation.getVerticalTabPanel().getSelectedElement();
		currentContextName = verticalTabElement.getText();
		
		queryProviderView.addQueryContext(currentContextName);
		queryProviderView.setCurrentContext(false, currentContextName);
		queryProviderView.configureGmSession(queryConstellation.getGmSession());
		
		queryProviderView.setEntityContent(queryConstellation.getEntity());
		
		queryConstellation.exchangeCurrentQueryProviderView(queryProviderView, false);
		hasContext = true;
		
		quickAccessTriggerField.configureExternalField(null);
		
		if (currentQueryConstellation.isCurrentQueryAdvanced() && queryProviderView instanceof QueryProviderActions)
			((QueryProviderActions) queryProviderView).fireModeChange();
	}
	
	@Override
	public void onContextChanged(boolean global) {
		if (global && queryProviderView instanceof HasText)
			((HasText) queryProviderView).setText(null);
		
		quickAccessTriggerField.configureExternalField(global ? (Component) queryProviderView : null);
		
		hasContext = !global;
		
		if (!global) {
			//Was this context already available before?
			if (queryProviderView instanceof HasText && !currentQueryConstellation.isCurrentQueryAdvanced())
				((HasText) queryProviderView).setText(currentQueryConstellation.getCurrentBasicText());
			
			if (currentQueryConstellation.isCurrentQueryAdvanced() && queryProviderView instanceof QueryProviderActions)
				((QueryProviderActions) queryProviderView).fireModeChange();
		}
	}
	
	private void removeQueryContext() {
		queryProviderView.removeQueryContext();
		queryProviderView.setCurrentContext(true, null);
		hasContext = false;
		
		quickAccessTriggerField.configureExternalField((Component) queryProviderView);
		
		currentContextName = null;
		
		if (queryProviderView instanceof QueryModelEditorAdvancedPanel)
			((QueryModelEditorAdvancedPanel) queryProviderView).fireModeChange();
		
		if (queryProviderView instanceof HasText)
			((HasText) queryProviderView).setText(null);
	}
	
	private void exchangeCurrentQueryView(QueryProviderView<GenericEntity> queryProviderView) {
		if (this.queryProviderView != null) {
			this.queryProviderView.getWidget().removeFromParent();
			this.queryProviderView.removeQueryProviderViewListener(this);
		}
		
		this.queryProviderView = queryProviderView;
		
		add(queryProviderView.getWidget());
		queryProviderView.addQueryProviderViewListener(this);
		
		if (currentContextName != null)
			queryProviderView.addQueryContext(currentContextName);
		else
			queryProviderView.removeQueryContext();
		
		if (hasContext)
			queryProviderView.setCurrentContext(false, currentContextName);
		else
			queryProviderView.setCurrentContext(true, null);
		
		doLayout();
	}
	
	@Override
	public void onQueryPerform(QueryProviderContext queryProviderContext) {
		if (!hasContext && queryProviderView instanceof HasText)
			((HasText) queryProviderView).setText(null);
	}
	
	@Override
	public void enable() {
		super.enable();
		
		if (queryProviderView instanceof Component)
			((Component) queryProviderView).unmask();
	}
	
	@Override
	public void disable() {
		super.disable();
		
		if (queryProviderView instanceof Component)
			((Component) queryProviderView).mask();
	}
	
	@Override
	public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
		//NOP
	}
	
	@Override
	public void onHeightChanged(int newHeight) {
		//NOP
	}
	
	@Override
	public void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded) {
		//NOP
	}
	
	@Override
	public void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved) {
		//NOP
	}
	
	@Override
	public void configureEntityType(EntityType<?> entityType, boolean configureEntityTypeForCheck) {
		//NOP
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (currentTetherBar != null) {
			currentTetherBar.removeTetherBarListener(this);
			currentTetherBar = null;
		}
		explorerConstellation.getVerticalTabPanel().removeVerticalTabListener(this);
		
		if (queryProviderView instanceof DisposableBean)
			((DisposableBean) queryProviderView).disposeBean();
		queryProviderView.getWidget().removeFromParent();
	}

}
