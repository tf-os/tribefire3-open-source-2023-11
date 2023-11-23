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
package com.braintribe.gwt.querymodeleditor.client.queryform;

import java.util.function.Supplier;

import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanelListener;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.qc.api.client.QueryProviderActions;
import com.braintribe.gwt.querymodeleditor.client.resources.LocalizedText;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorResources;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifier;
import com.braintribe.model.query.Query;
import com.braintribe.model.template.Template;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class QueryFormDialog extends ClosableWindow implements DisposableBean {

	/********************************** Variables **********************************/

	private QueryProviderActions queryModelEditorActions = null;
	private QueryFormTemplate queryFormTemplate = null;

	private Supplier<PropertyPanel> propertyPanelSupplier;
	private PropertyPanel propertyPanel = null;
	private Object focusObject = null;
	private GenericEntity currentEntity;
	private ModelPath currentModelPath;
	private BorderLayoutContainer container;

	private boolean mouseOver = false;
	private boolean usePaging = false;
	private boolean initialized;
	private ToolBar toolBar;
	private int defaultHeight = 150;
	private int leftPosition;
	private int topPosition;

	/******************************* QueryFormDialog *******************************/

	public QueryFormDialog() {
		setHeaderVisible(false);
		setBodyBorder(false);
		setBorders(true);

		setResizable(true);
		setClosable(false);
		//this.setAutoHide(true);
		//this.setOnEsc(false);
		setModal(false);

		//this.setPredefinedButtons();
		//this.setHideOnButtonClick(false);
	}

	@Required
	public void setPropertyPanel(Supplier<PropertyPanel> propertyPanelSupplier) {
		this.propertyPanelSupplier = propertyPanelSupplier;
	}

	@Required
	public void setQueryFormTemplate(final QueryFormTemplate queryFormTemplate) {
		this.queryFormTemplate = queryFormTemplate;
	}

	public void configureEntity(GenericEntity entity) {
		BasicQueryStringifier stringifier = BasicQueryStringifier.create();
		if (currentEntity != null && entity != null && (stringifier.stringify(currentEntity).equals(stringifier.stringify(entity))))
			return;
		
		currentEntity = entity;
		
		if (currentEntity instanceof Template)
			currentModelPath = queryFormTemplate.prepareVariableWrapperEntity((Template) currentEntity);
		else if (currentEntity instanceof Query)
			currentModelPath = queryFormTemplate.prepareVariableWrapperEntity((Query) currentEntity);
		else {
			queryFormTemplate.clearVariables();
			currentModelPath = null;
		}		
	}
	
	/**
	 * Configures whether we should use entity references when handling entities in the {@link QueryFormTemplate}.
	 */
	public void configureUseEntityReferences(boolean useEntityReferences) {
		queryFormTemplate.configureUseEntityReferences(useEntityReferences);
	}

	@Required
	public void setQueryModelEditorActions(final QueryProviderActions queryModelEditorActions) {
		this.queryModelEditorActions = queryModelEditorActions;
	}

	@Required
	public void setUsePaging(final boolean usePaging) {
		this.usePaging = usePaging;
	}

	@Configurable
	public void configureFocusObject(final Object setFocusObject) {
		focusObject = setFocusObject;
		if (focusObject instanceof Widget)
			setFocusWidget((Widget) focusObject);
	}

	public boolean getMouseOver() {
		return mouseOver;
	}

	public QueryFormTemplate getQueryTemplate() {
		return queryFormTemplate;
	}

	public void setShowingPosition(int left, int top) {
		leftPosition = left;
		topPosition = top;
	}
	
	public String getSearchTextValue() {
		return queryFormTemplate.getCurrentVariableValue(QueryFormTemplate.SEARCH_TEXT_VARIABLE);
	}
	
	private void initialize() {
		toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.add(new FillToolItem());
		toolBar.add(getCloseButton());
		toolBar.add(getSearchButton());

		container = new BorderLayoutContainer();
		container.setSouthWidget(toolBar, new BorderLayoutData(30));

		addDomHandler(event -> mouseOver = true, MouseOverEvent.getType());
		addDomHandler(event -> mouseOver = false, MouseOutEvent.getType());

		addDomHandler(event -> {
			if (event.getNativeKeyCode() != KeyCodes.KEY_ENTER)
				return;
			
			event.preventDefault();
			event.stopPropagation();
			if (!propertyPanel.isEditing() && !propertyPanel.wasEditionFinishedByEnter())
				queryModelEditorActions.fireQueryPerform(!usePaging);
		}, KeyDownEvent.getType());

		add(container);
		
		initialized = true;
	}

	private TextButton getCloseButton() {
		TextButton closeButton = new TextButton(LocalizedText.INSTANCE.close());
		closeButton.setToolTip(LocalizedText.INSTANCE.close());
		closeButton.setIcon(QueryModelEditorResources.INSTANCE.cancel());
		closeButton.addSelectHandler(event -> hide());
		closeButton.addStyleName("gmeQueryFormClose");

		return closeButton;
	}

	private TextButton getSearchButton() {
		TextButton searchButton = new TextButton(LocalizedText.INSTANCE.search());
		searchButton.setToolTip(LocalizedText.INSTANCE.search());
		searchButton.setIcon(GmViewActionResources.INSTANCE.query());
		searchButton.addStyleName("gmeQueryFormSearch");

		searchButton.addSelectHandler(event -> {
			hide();
			queryModelEditorActions.fireQueryPerform(!usePaging);
		});

		return searchButton;
	}
	
	private PropertyPanel getPropertyPanel() {
		if (propertyPanel != null)
			return propertyPanel;
		
		propertyPanel = propertyPanelSupplier.get();
		propertyPanel.configureGmSession(queryFormTemplate.getWorkbenchPersistenceSession());
		propertyPanel.configureUseCase(KnownUseCase.queryEditorUseCase.getDefaultValue());
		container.setCenterWidget(propertyPanel);
		propertyPanel.addPropertyPanelListener(new PropertyPanelListener() {			
			@Override
			public void onEditorsReady() {
				// NOP				
			}
			
			@Override
			public void onEditingDone(boolean canceled) {
				if (!canceled)
					Scheduler.get().scheduleDeferred(() -> focus());
			}
		});
		
		return propertyPanel;
	}

	/******************************** Override Methods *****************************/

	@Override
	protected void onShow() {				
		if (!initialized)
			initialize();
		
		propertyPanel = getPropertyPanel();
		propertyPanel.setContent(currentModelPath);		
		
		//RVE hide before resize height (flickering)
		setPosition(-1000, -1000);
		
		super.onShow();			
		
		Scheduler.get().scheduleDeferred(() -> Scheduler.get().scheduleDeferred(() -> {
			int documentHeight = Math.max(propertyPanel.getGridContentHeight(), propertyPanel.getElement().getHeight(false));
			documentHeight = Math.min(documentHeight, 350);
			if (documentHeight > 20)
				setHeight(documentHeight + toolBar.getOffsetHeight() + 10);
			else
				setHeight(defaultHeight);		
			
			forceLayout();
			setPosition(leftPosition, topPosition);
			propertyPanel.startEditing();
		}));		
	}

	@Override
	protected void doFocus() {
		if (focusObject instanceof Element)
			((Element) focusObject).focus();
		else
			super.doFocus();
	}
	
	@Override
	protected void onKeyPress(Event event) {
		if (isOnEsc() && event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
			if (!propertyPanel.wasEditionFinishedByEsc())
				hide();
			return;
		}
		
		super.onKeyPress(event);
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (propertyPanel != null)
			propertyPanel.disposeBean();
		
		queryFormTemplate.disposeBean();
	}

	public int getDefaultHeight() {
		return defaultHeight;
	}

	public void setDefaultHeight(int defaultHeight) {
		this.defaultHeight = defaultHeight;
	}
}
