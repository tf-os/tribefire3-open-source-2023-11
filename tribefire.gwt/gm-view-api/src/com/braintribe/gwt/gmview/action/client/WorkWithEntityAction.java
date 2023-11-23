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
package com.braintribe.gwt.gmview.action.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.action.client.TriggerKnownProperties;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.SplitAction;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.WorkWithEntityActionListener;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.workbench.ExclusiveWorkbenchAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * This class is responsible for triggering the {@link WorkWithEntityActionListener} event when performed.
 * @author michel.docouto
 *
 */
public class WorkWithEntityAction extends ModelAction implements SplitAction {
	private static int idCounter = 0;
	private static final int ELEMENT_HEIGHT = 57;
	private static final int ELEMENT_WIDTH = 74;
	
	private ModelPath modelPath;
	private Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier;
	private List<GmContentViewContext> externalContentViewContexts;
	private WorkWithEntityExpert workWithEntityExpert;
	private boolean handleInTab = false;
	private boolean useWorkWithEntityExpert = true;
	private StandardMatcher matcher;
	
	public WorkWithEntityAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.workWithEntity());
		setIcon(GmViewActionResources.INSTANCE.open());
		setHoverIcon(GmViewActionResources.INSTANCE.openBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configures the resolver used for knowing which views are available.
	 */
	public void configureViewSituationResolver(Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolverSupplier) {
		this.viewSituationResolverSupplier = viewSituationResolverSupplier;
	}
	
	/**
	 * Configures content view contexts that are always valid.
	 */
	public void configureExternalContentViewContexts(List<GmContentViewContext> contentViewContexts) {
		this.externalContentViewContexts = contentViewContexts;
	}
	
	/**
	 * Configures whether to use the expert if it is available.
	 * Defaults to true.
	 */
	public void configureUseWorkWithEntityExpert(boolean useWorkWithEntityExpert) {
		this.useWorkWithEntityExpert = useWorkWithEntityExpert;
	}
			
	@Override
	public void perform(TriggerInfo triggerInfo) {
		boolean splitClicked = false;
		Boolean readOnly = false;
		if (triggerInfo != null) {
			Boolean clicked = triggerInfo.get(SplitAction.SPLIT_CLICKED_PROPERTY);
			readOnly = triggerInfo.get(ModelAction.PROPERTY_READONLY);
			if (readOnly == null)
				readOnly = false;
			if (clicked != null && clicked)
				splitClicked = true;
		}
		
		if (splitClicked) {
			Element clickedElement = triggerInfo.get(TriggerKnownProperties.PROPERTY_CLICKEDELEMENT);
			Window menuWindow = getMenuWindow();
			menuWindow.show();
			menuWindow.alignTo(clickedElement, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_LEFT), 0, 0);
		} else {
			WorkbenchAction actionToPerform = useWorkWithEntityExpert ? workWithEntityExpert.getActionToPerform(modelPath) : null;
			
			if (actionToPerform != null) {
				workWithEntityExpert.setSelectModelPath(true);
				workWithEntityExpert.performAction(modelPath, actionToPerform, gmContentView, handleInTab);
			} else
				fireWorkWithEntity(gmContentView.getUseCase(), false, readOnly);
		}
	}
	
	@Override
	public Menu getMenu(GmContentView view) {
		if (modelPath == null)
			return null;
		
		WorkbenchAction actionToPerform = useWorkWithEntityExpert ? workWithEntityExpert.getActionToPerform(modelPath) : null;
		if (actionToPerform != null) {
			ExclusiveWorkbenchAction exclusiveWorkbenchAction = workWithEntityExpert.getExclusiveWorkbenchAction();
			if (exclusiveWorkbenchAction != null && exclusiveWorkbenchAction.getInplaceContextCriterion() != null) {
				StandardMatcher matcher = getMatcher();
				matcher.setCriterion(exclusiveWorkbenchAction.getInplaceContextCriterion());
				if (!matcher.matches(modelPath.asTraversingContext()))
					return null;
			}
		}
		
		Menu menu = new Menu();
		List<GmContentViewContext> contentViewContexts = getPossibleContentViews();
		for (final GmContentViewContext contentViewContext : contentViewContexts) {
			MenuItem menuItem = new MenuItem(contentViewContext.getName(), contentViewContext.getIcon());
			menuItem.addSelectionHandler(event -> fireWorkWithEntity(contentViewContext.getUseCase(), true, contentViewContext.isReadOnly()));
			menu.add(menuItem);
		}
		
		return menu;
	}
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		modelPath = null;
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true);
			return;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath modelPath : selection) {
			if (modelPath != null && (modelPath.last().getValue() instanceof GenericEntity || modelPath.last().getValue() instanceof Collection
					|| modelPath.last().getValue() instanceof Map)) {
				this.modelPath = modelPath;
				setHidden(!workWithEntityExpert.checkWorkWithAvailable(modelPath, gmContentView, useWorkWithEntityExpert));
				return;
			}
		}
		
		setHidden(true);
	}
	
	private StandardMatcher getMatcher() {
		if (matcher != null)
			return matcher;
		
		matcher = new StandardMatcher();
		matcher.setCheckOnlyProperties(false);
		
		return matcher;
	}	
	
	private void fireWorkWithEntity(String preferredUseCase, boolean forcePreferred, boolean readOnly) {
		if (gmContentView != null) {
			WorkWithEntityActionListener listener = GMEUtil.getWorkWithEntityActionListener(gmContentView);
			if (listener != null)
				listener.onWorkWithEntity(modelPath, null, preferredUseCase, forcePreferred, readOnly);
		}
	}
	
	private Window getMenuWindow() {
		final Window menuWindow = new Window();
		menuWindow.setClosable(false);
		menuWindow.setAutoHide(true);
		menuWindow.setShadow(false);
		menuWindow.setHeaderVisible(false);
		menuWindow.setResizable(false);
		
		HorizontalLayoutContainer layoutContainer = new HorizontalLayoutContainer();
		
		List<GmContentViewContext> contentViewContexts = getPossibleContentViews();
		int contentViewsSize = contentViewContexts.size();
		
		int factor = contentViewsSize == 0 ? 1 : contentViewsSize % 3 == 0 ? contentViewsSize / 3 : (contentViewsSize / 3) + 1;
		int columns = contentViewsSize <= 1 ? 0 : contentViewsSize == 2 ? 2 : 3;
		layoutContainer.add(
				getViewHtmlPanel(menuWindow, contentViewContexts, factor),
				new HorizontalLayoutData(columns * ELEMENT_WIDTH + 8, -1));
		
		menuWindow.add(layoutContainer);
		menuWindow.setWidth((columns * ELEMENT_WIDTH) + 22);
		menuWindow.setHeight((factor * ELEMENT_HEIGHT) + 21);
		return menuWindow;
	}
	
	private HTMLPanel getViewHtmlPanel(final Window menuWindow, final List<GmContentViewContext> contentViewContexts, int factor) {
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<div class='").append(GmViewActionResources.INSTANCE.css().toolBarParentStyle()).append("' style='height: ");
		htmlString.append((factor * ELEMENT_HEIGHT) + 4).append("px;'><ul class='gxtReset ").append(GmViewActionResources.INSTANCE.css().toolBarStyle());
		htmlString.append("'>");
		
		for (GmContentViewContext contentViewContext : contentViewContexts) {
			htmlString.append("<li>");
			htmlString.append("<div class='").append(GmViewActionResources.INSTANCE.css().toolBarElement());
			htmlString.append("' style='position: relative;'");
			htmlString.append(" id='").append(contentViewContext.getName()).append("gmContentViewContext").append(idCounter++).append("'");
			htmlString.append("><img src='").append(contentViewContext.getHoverIcon().getSafeUri().asString()).append("' class='");
			htmlString.append(GmViewActionResources.INSTANCE.css().toolBarElementImage()).append("'/>");
			htmlString.append("<div class='").append(GmViewActionResources.INSTANCE.css().toolBarElementText()).append("'");
			htmlString.append(">").append(contentViewContext.getName()).append("</div></div>");
			htmlString.append("</li>");
		}
		
		htmlString.append("</ul></div>");
		htmlString.append("<div class='").append(GmViewActionResources.INSTANCE.css().toolTip()).append("' style='top: ");
		htmlString.append((factor * ELEMENT_HEIGHT) + 7);
		htmlString.append("px'></div>");
		
		HTMLPanel viewHtmlPanel = new HTMLPanel(htmlString.toString());
		viewHtmlPanel.setHeight((factor * ELEMENT_HEIGHT) + 13 + "px");
		viewHtmlPanel.sinkEvents(Event.ONCLICK);
		viewHtmlPanel.addHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
			
			Element actionElement = getActionElement(Element.as(target), 3, GmViewActionResources.INSTANCE.css().toolBarElement());
			if (actionElement != null) {
				for (GmContentViewContext contentViewContext : contentViewContexts) {
					if (actionElement.getId().startsWith(contentViewContext.getName())) {
						fireWorkWithEntity(contentViewContext.getUseCase(), true, contentViewContext.isReadOnly());
						menuWindow.hide();
						break;
					}
				}
			}
		}, ClickEvent.getType());
		
		return viewHtmlPanel;
	}
	
	private Element getActionElement(Element clickedElement, int depth, String className) {
		if (depth <= 0 || clickedElement == null)
			return null;
		
		if (className.equals(clickedElement.getClassName()))
			return clickedElement;
		else
			return getActionElement(clickedElement.getParentElement(), --depth, className);
	}
	
	private List<GmContentViewContext> getPossibleContentViews() {
		List<GmContentViewContext> contentViewContexts = new ArrayList<>();
		contentViewContexts = viewSituationResolverSupplier.get().getPossibleContentViews((modelPath.last()));
		contentViewContexts.addAll(externalContentViewContexts);
		return contentViewContexts;
	}
							
	public WorkWithEntityExpert getWorkWithEntityExpert() {
		return workWithEntityExpert;
	}

	public void configureWorkWithEntityExpert(WorkWithEntityExpert workWithEntityExpert) {
		this.workWithEntityExpert = workWithEntityExpert;
	}
}
