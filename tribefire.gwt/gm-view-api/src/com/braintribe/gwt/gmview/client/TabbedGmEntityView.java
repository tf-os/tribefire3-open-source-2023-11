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
package com.braintribe.gwt.gmview.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gxt.gxtresources.orangeflattab.client.OrangeFlatTabPanelAppearance;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.AccessStack;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.PlainTabPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.tips.QuickTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

/**
 * Tabs with display views used as detail panel.
 */
public class TabbedGmEntityView extends ContentPanel implements InitializableBean, GmEntityView, GmCheckSupport, GmInteractionSupport,
		GmActionSupport, HasAddtionalWidgets, GmViewIdProvider, DisposableBean, GmDetailViewSupport {
	
	private static final String DETAILS_PANEL_ROOT_ID = "gmDetailsPanel";
	
	private List<TabbedGmEntityViewContext> tabbedGmEntityViewContexts;
	private List<TabbedWidgetContext> additionalWidgets;
	private Map<TabbedGmEntityViewContext, Widget> tabItemsPerEntityContext = new HashMap<>();
	private Map<TabbedWidgetContext, Widget> tabItemsPerWidgetContext = new HashMap<>();
	private ModelAction action;
	private PersistenceGmSession gmSession;
	private Timer updateTimer;
	private SimplePanel simplePanel;
	private TabItemConfig tabIconConfig;
	private boolean actionConfigured;
	private HasAddtionalWidgets link;
	private PlainTabPanel tabPanel;
	private GmContentView headerPanel;
	private ViewSituationResolver<TabbedWidgetContext> tabbedGmEntityViewContextResolver;
	private List<Widget> tabbedGmEntityViewContextResolverWidgets;
	private ModelPath modelPath;
	private String useCase;
	private boolean disableDefaultContexts = false;
	private List<Widget> allViews;
	private List<Widget> removedWidgets = new ArrayList<>();
	
	public TabbedGmEntityView() {
		setHeaderVisible(false);
		setBodyBorder(false);
		setBorders(false);
		
		XElement xe = XElement.as(getElement());
		xe.applyStyles("borderLeft: 1px solid #dfdfdf");
		
		tabPanel = new PlainTabPanel(GWT.<OrangeFlatTabPanelAppearance>create(OrangeFlatTabPanelAppearance.class)) {
			@Override
			public void setActiveWidget(Widget item, boolean fireEvents) {
				if (item == null)
					return;
				
				if (action == null || item != simplePanel) {
					if (tabbedGmEntityViewContextResolverWidgets != null && tabbedGmEntityViewContextResolverWidgets.contains(item)
							&& item instanceof GmContentView) {
						GmContentView view = (GmContentView) item;
						view.configureUseCase(useCase);
						view.configureGmSession(gmSession);
						view.setContent(modelPath);
					}
					
					super.setActiveWidget(item, fireEvents);
					return;
				}
				
				if (item.getParent() != getContainer() || item == getActiveWidget())
					return;

				if (fireEvents) {
					BeforeSelectionEvent<Widget> event = BeforeSelectionEvent.fire(this, item);
					if (event != null && event.isCanceled())
						return;
				}

				getContainer().setActiveWidget(item);

				AccessStack<Widget> stack = getStack(tabPanel);
				if (stack == null) {
					stack = new AccessStack<>();
					setStack(tabPanel, stack);
				}
				stack.add(item);

				if (fireEvents)
					SelectionEvent.fire(this, item);
				tabPanelDelegateUpdates(tabPanel);
			}
			
			@Override
			protected void onClick(Event event) {
				if (action == null) {
					super.onClick(event);
					return;
				}
				
				XElement target = event.getEventTarget().cast();
				Element item = findItem(target);
				if (item != null) {
					Widget w = getWidget(itemIndex(item));
					if (w == simplePanel) {
						action.perform(null);
						return;
					}
				}
				
				super.onClick(event);
			}
			
			@Override
			public void onBrowserEvent(Event event) {
				XElement target = event.getEventTarget().cast();
				Element item = null;
				if (target != null)
					item = findItem(target);
				
				Widget widget = null;
				int index = -1;
				if (item != null) {
					index = itemIndex(item);
					try {
						widget = getWidget(index); //TODO: why is this needed?
					} catch(Exception ex) {
						widget = null;
					}
				}
				
				if (action != null) {
					tabIconConfig.setEnabled(true);
					if (widget != null && simplePanel == widget) {
						if (event.getTypeInt() == Event.ONMOUSEOVER)
							tabIconConfig.setEnabled(false);
					}
				}
				
				if (index > -1 && index < getWidgetCount())
					super.onBrowserEvent(event);
			}
		};
		tabPanel.setBodyBorder(false);
		tabPanel.setBorders(false);
		tabPanel.addStyleName("tabbedEntityView");
		//tabPanel.setVisible(false);
		
		addAttachHandler(event -> {
			if (actionConfigured || !event.isAttached() || action == null)
				return;
			
			actionConfigured = true;
			action.configureGmContentView(this);
			QuickTip quickTip = new QuickTip(this);
			ToolTipConfig config = new ToolTipConfig();
			config.setDismissDelay(0);
			quickTip.update(config);
			StringBuilder builder = new StringBuilder();
			builder.append("<div class='").append(GmViewActionResources.INSTANCE.css().actionPosition()).append("'>");
			if (!action.getHidden()) {
				builder.append("<img class='").append(GmViewActionResources.INSTANCE.css().actionIcon());
				builder.append("' qtip='").append(action.getName()).append("' src='").append(action.getIcon().getSafeUri().asString()).append("'/>");
			}
			builder.append("</div>");
			
			tabIconConfig = new TabItemConfig();
			tabIconConfig.setHTML(SafeHtmlUtils.fromTrustedString(builder.toString()));
			simplePanel = new SimplePanel();
			tabPanel.add(simplePanel, tabIconConfig);
			
			action.addPropertyListener((source, property) -> getUpdateTimer().schedule(100));
		});
	}
	
	/**
	 * Configures the {@link GmContentView} to be displayed as header.
	 */
	@Configurable
	public void setHeaderPanel(GmContentView headerPanel) {
		this.headerPanel = headerPanel;
	}
	
	/**
	 * Configures the resolver which will add or remove tabs depending on the current {@link ModelPathElement}.
	 */
	@Configurable
	public void setTabbedGmEntityViewContextResolver(ViewSituationResolver<TabbedWidgetContext> tabbedGmEntityViewContextResolver) {
		this.tabbedGmEntityViewContextResolver = tabbedGmEntityViewContextResolver;
		tabbedGmEntityViewContextResolverWidgets = new ArrayList<>();
		tabbedGmEntityViewContextResolver.setMappedSituationSelectorListener(using -> disableDefaultContexts = using);
	}
	
	@Configurable
	public void setTabbedGmEntityViewContexts(List<TabbedGmEntityViewContext> tabbedGmEntityViewContexts) {
		this.tabbedGmEntityViewContexts = tabbedGmEntityViewContexts;
	}
	
	/**
	 * Configures an action to be added in the right side of the tabs.
	 */
	@Configurable
	public void setAction(ModelAction action) {
		this.action = action;
	}
	
	@Override
	public String getRootId() {
		return DETAILS_PANEL_ROOT_ID;
	}
	
	public GmContentView getHeaderPanel() {
		return headerPanel;
	}
	
	public void configureTabPanelVisibility(boolean visible) {
		tabPanel.setVisible(visible);
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		for (Widget widget : allViews) {
			if (widget instanceof GmActionSupport)
				((GmActionSupport) widget).configureActionGroup(actionGroup);
		}
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		for (Widget widget : allViews) {
			if (widget instanceof GmActionSupport)
				((GmActionSupport) widget).configureExternalActions(externalActions);
		}
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		return null;
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		for (Widget widget : allViews) {
			if (widget instanceof GmActionSupport)
				((GmActionSupport) widget).setActionManager(actionManager);
		}
	}

	@Override
	public void setContent(ModelPath modelPath) {
		this.modelPath = modelPath;
		
		if (headerPanel != null) {
			headerPanel.configureUseCase(getUseCase());
			headerPanel.setContent(modelPath);
		}
		
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				((GmContentView) widget).setContent(modelPath);
		}
		
		boolean widgetCountChanged = false;
		if (tabbedGmEntityViewContextResolver != null) {
			if (modelPath == null)
				widgetCountChanged = clearContextResolverTabs(tabbedGmEntityViewContextResolverWidgets);
			else {
				List<TabbedWidgetContext> possibleTabs = tabbedGmEntityViewContextResolver.getPossibleContentViews(modelPath.last());
				Map<Widget, TabbedWidgetContext> widgetsAdded = new LinkedHashMap<>();
				possibleTabs.forEach(context -> widgetsAdded.put(context.getWidget(), context));
				List<Widget> widgetsRemoved = new ArrayList<>(tabbedGmEntityViewContextResolverWidgets);
				widgetsRemoved.removeAll(widgetsAdded.keySet());
				if (!widgetsRemoved.isEmpty())
					widgetCountChanged = clearContextResolverTabs(widgetsRemoved);
				
				tabbedGmEntityViewContextResolverWidgets.forEach(widget -> widgetsAdded.remove(widget));
				if (!widgetsAdded.isEmpty()) {
					possibleTabs.clear();
					possibleTabs.addAll(widgetsAdded.values());
					possibleTabs.forEach(t -> tabbedGmEntityViewContextResolverWidgets.add(t.getWidget()));
					addAdditionalWidgets(possibleTabs, false);
					widgetCountChanged = true;
				}
			}
		}
		
		if (disableDefaultContexts && !tabbedGmEntityViewContexts.isEmpty()) {
			for (TabbedGmEntityViewContext tabbedGmEntityViewContext : tabbedGmEntityViewContexts) {
				Widget item = tabItemsPerEntityContext.remove(tabbedGmEntityViewContext);
				if (item != null) {
					widgetCountChanged = true;
					tabPanel.remove(item);
					allViews.remove(item);
					
					if (item instanceof DisposableBean) {
						try {
							((DisposableBean) item).disposeBean();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			tabbedGmEntityViewContexts.clear();
			tabItemsPerEntityContext.clear();
		}
		
		if (widgetCountChanged) {
			XElement bar = tabPanel.getAppearance().getBar(tabPanel.getElement());
			boolean isVisible = bar.isVisible();
			boolean newVisible = getVisibleWidgetCount() > 1 || headerPanel == null;
			if (isVisible != newVisible) {
				bar.setVisible(newVisible);
				forceLayout();
			}
		}
	}
	
	private int getVisibleWidgetCount(/*Set<Widget> widgetsJustAdded*/) {
		//if (tabPanel.getWidgetCount() == 1)
			//return 1;
		
		return tabPanel.getWidgetCount();
		
		/*int visibleWidgets = 0;
		for (int i = 0; i < tabPanel.getWidgetCount(); i++) {
			Widget widget = tabPanel.getWidget(i);
			if (widgetsJustAdded.contains(widget) || widget.isVisible())
				visibleWidgets++;
		}
		
		return visibleWidgets;*/
	}
	
	private boolean clearContextResolverTabs(List<Widget> widgetsToRemove) {
		if (widgetsToRemove == null || widgetsToRemove.isEmpty())
			return false;
		
		allViews.removeAll(widgetsToRemove);
		widgetsToRemove.forEach(t -> tabPanel.remove(t));
		tabbedGmEntityViewContextResolverWidgets.removeAll(widgetsToRemove);
		
		removedWidgets.addAll(widgetsToRemove);
		
		return true;
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				((GmContentView) widget).addSelectionListener(sl);
		}
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				((GmContentView) widget).removeSelectionListener(sl);
		}
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return null;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				((GmContentView) widget).select(index, keepExisting);
		}
	}

	@Override
	public GmContentView getView() {
		return null;
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		for (Widget widget : allViews) {
			if (widget instanceof GmInteractionSupport)
				((GmInteractionSupport) widget).addInteractionListener(il);
		}
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		for (Widget widget : allViews) {
			if (widget instanceof GmInteractionSupport)
				((GmInteractionSupport) widget).removeInteractionListener(il);
		}
	}

	@Override
	public void addCheckListener(GmCheckListener cl) {
		for (Widget widget : allViews) {
			if (widget instanceof GmCheckSupport)
				((GmCheckSupport) widget).addCheckListener(cl);
		}
	}
	
	@Override
	public void removeCheckListener(GmCheckListener cl) {
		for (Widget widget : allViews) {
			if (widget instanceof GmCheckSupport)
				((GmCheckSupport) widget).removeCheckListener(cl);
		}
	}
	
	@Override
	public void addDetailViewListener(GmDetailViewListener dl) {
		for (Widget widget : allViews) {
			if (widget instanceof GmDetailViewSupport)
				((GmDetailViewSupport) widget).addDetailViewListener(dl);
		}
	}
	
	@Override
	public void removeDetailViewListener(GmDetailViewListener dl) {
		for (Widget widget : allViews) {
			if (widget instanceof GmDetailViewSupport)
				((GmDetailViewSupport) widget).removeDetailViewListener(dl);
		}
	}
	
	@Override
	public GmDetailViewSupportContext getGmDetailViewSupportContext() {
		return null;
	}

	@Override
	public ModelPath getFirstCheckedItem() {
		return null;
	}

	@Override
	public List<ModelPath> getCurrentCheckedItems() {
		return null;
	}

	@Override
	public boolean isChecked(Object element) {
		return false;
	}

	@Override
	public boolean uncheckAll() {
		return false;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				((GmContentView) widget).configureGmSession(gmSession);
		}
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}

	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				((GmContentView) widget).configureUseCase(useCase);
		}
	}

	@Override
	public String getUseCase() {
		for (Widget widget : allViews) {
			if (widget instanceof GmContentView)
				return ((GmContentView) widget).getUseCase();
		}
		
		if (tabbedGmEntityViewContexts != null && !tabbedGmEntityViewContexts.isEmpty())
			return tabbedGmEntityViewContexts.get(0).getEntityView().getUseCase();
		
		return this.useCase;
	}
	
	public void clearWidgets() {
		clearWidgets(true);
	}
	
	private void clearWidgets(boolean forceLayout) {
		for (Widget widget : tabItemsPerEntityContext.values())
			close(tabPanel, widget);
		
		for (Widget widget : tabItemsPerWidgetContext.values())
			close(tabPanel, widget);
		
		if (forceLayout) {
			if (headerPanel instanceof Widget)
				((Widget) headerPanel).setVisible(false);
			forceLayout();
		}
	}

	@Override
	public void intializeBean() throws Exception {
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		if (headerPanel != null)
			vlc.add((Widget) headerPanel, new VerticalLayoutData(1, -1));
		vlc.add(tabPanel, new VerticalLayoutData(1, 1));
		add(vlc);
		
		clearWidgets(false);
		
		if (simplePanel != null)
			close(tabPanel, simplePanel);
		
		tabPanel.setAutoSelect(true);
		
		allViews = new ArrayList<>();
		
		for (final TabbedGmEntityViewContext tabbedGmEntityViewContext : tabbedGmEntityViewContexts) {
			Widget item = tabItemsPerEntityContext.get(tabbedGmEntityViewContext);
			if (item == null && tabbedGmEntityViewContext.getEntityView() instanceof Widget) {
				item = (Widget) tabbedGmEntityViewContext.getEntityView();				
				tabItemsPerEntityContext.put(tabbedGmEntityViewContext, item);
			}
			tabPanel.add(item, tabbedGmEntityViewContext.getDescription());
			allViews.add(item);
		}
		
		if (actionConfigured)
			tabPanel.add(simplePanel, tabIconConfig);
		
		tabPanel.getAppearance().getBar(tabPanel.getElement()).setVisible(tabPanel.getWidgetCount() > 1 || headerPanel == null);
		
		forceLayout();
	}

	@Override
	public void configureAdditionalWidgets(List<TabbedWidgetContext> additionalWidgets) {
		this.additionalWidgets = additionalWidgets;
		tabPanel.setAutoSelect(false);
		addAdditionalWidgets(additionalWidgets, true);
		((Widget) headerPanel).setVisible(true);
		forceLayout();
	}
	
	public ModelAction getAction() {
		return action;
	}

	private void addAdditionalWidgets(List<TabbedWidgetContext> additionalWidgets, boolean activateFirstInList) {
		if (additionalWidgets == null || additionalWidgets.isEmpty())
			return;
		
		Widget firstWidgetInList = null;
		for (final TabbedWidgetContext additionalWidget : additionalWidgets) {
			Widget item = tabItemsPerWidgetContext.get(additionalWidget);
			if (item == null) {
				item = additionalWidget.getWidget();					
				tabItemsPerWidgetContext.put(additionalWidget, item);
			}
			
			if (firstWidgetInList == null)
				firstWidgetInList = item;
			if (getWidgetIndex(item) == -1) { //RVE check if item not already added
				int index = additionalWidget.getIndex();
				String description = additionalWidget.getDescription();
				if (description == null)
					description = additionalWidget.getName();
				if (index == -1) {
					tabPanel.add(item, description);
					allViews.add(item);
				} else {
					tabPanel.insert(item, index, new TabItemConfig(description));
					allViews.add(index, item);
				}
				
				removedWidgets.remove(item);
			}
		}

		if (activateFirstInList && firstWidgetInList != null)
			tabPanel.setActiveWidget(firstWidgetInList);
		else
			tabPanel.setActiveWidget(tabPanel.getWidget(0));
	}

	@Override
	public List<TabbedWidgetContext> getTabbedWidgetContexts() {
		return this.additionalWidgets;
	}
	
	@Override
	public void setActiveTabbedWidget(String key) {
		tabbedGmEntityViewContexts.forEach(context -> {
			if (context.getName().equals(key) && context.getEntityView() instanceof Widget) {
				tabPanel.setActiveWidget((Widget) context.getEntityView());
				return;
			}
		});
		
		additionalWidgets.forEach(context -> {
			if (context.getName().equals(key)) {
				tabPanel.setActiveWidget(context.getWidget());
				return;
			}
		});
	}
	
	private Timer getUpdateTimer() {
		if (updateTimer != null)
			return updateTimer;
		
		updateTimer = new Timer() {
			@Override
			public void run() {
				StringBuilder builder = new StringBuilder();
				builder.append("<div class='").append(GmViewActionResources.INSTANCE.css().actionPosition()).append("'>");
				if (!action.getHidden()) {
					builder.append("<img class='").append(GmViewActionResources.INSTANCE.css().actionIcon());
					builder.append("' qtip='").append(action.getName()).append("' src='").append(action.getIcon().getSafeUri().asString()).append("'/>");
				}
				builder.append("</div>");
				tabIconConfig.setHTML(SafeHtmlUtils.fromTrustedString(builder.toString()));
				
				tabPanel.update(simplePanel, tabIconConfig);
			}
		};
		return updateTimer;
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (tabbedGmEntityViewContexts != null) {
			for (TabbedGmEntityViewContext context : tabbedGmEntityViewContexts) {
				if (context.getEntityView() instanceof DisposableBean)
					((DisposableBean) context.getEntityView()).disposeBean();
			}
			tabbedGmEntityViewContexts = null;
		}
		
		if (additionalWidgets != null) {
			for (TabbedWidgetContext context : additionalWidgets) {
				if (context.getWidgetIfProvided() instanceof DisposableBean)
					((DisposableBean) context.getWidget()).disposeBean();
			}
			additionalWidgets = null;
		}
		
		if (tabItemsPerEntityContext != null)
			tabItemsPerEntityContext = null;
		
		if (tabItemsPerWidgetContext != null)
			tabItemsPerWidgetContext = null;
		
		if (tabbedGmEntityViewContextResolverWidgets != null)
			tabbedGmEntityViewContextResolverWidgets = null;
		
		if (allViews != null) {
			for (Widget view : allViews) {
				if (view instanceof DisposableBean)
					((DisposableBean) view).disposeBean();
			}
			allViews.clear();
		}
		
		for (Widget widget : removedWidgets) {
			if (widget instanceof DisposableBean)
				((DisposableBean) widget).disposeBean();
		}
		removedWidgets.clear();
	}
	
	@Override
	public void setLink(HasAddtionalWidgets haw) {
		this.link = haw;
	}
	
	@Override
	public HasAddtionalWidgets getLink() {
		return link;
	}
	
	private native AccessStack<Widget> getStack(TabPanel tabPanel) /*-{
		return tabPanel.@com.sencha.gxt.widget.core.client.TabPanel::stack;
	}-*/;
	
	private native void setStack(TabPanel tabPanel, AccessStack<Widget> newStack) /*-{
		return tabPanel.@com.sencha.gxt.widget.core.client.TabPanel::stack = newStack;
	}-*/;
	
	private native void tabPanelDelegateUpdates(TabPanel tabPanel) /*-{
		tabPanel.@com.sencha.gxt.widget.core.client.TabPanel::delegateUpdates()();
	}-*/;
	
	private native void close(TabPanel tabPanel, Widget item) /*-{
		return tabPanel.@com.sencha.gxt.widget.core.client.TabPanel::close(Lcom/google/gwt/user/client/ui/Widget;)(item);
	}-*/;

}