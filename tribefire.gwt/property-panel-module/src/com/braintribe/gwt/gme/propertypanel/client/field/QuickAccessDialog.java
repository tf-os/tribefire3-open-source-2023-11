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
package com.braintribe.gwt.gme.propertypanel.client.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.propertypanel.client.LocalizedText;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.EntitiesProviderResult;
import com.braintribe.gwt.gmview.action.client.GmTypeOrAction;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.GmEnumTypeResult;
import com.braintribe.gwt.gmview.action.client.SpotlightPanelListener;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.input.InputFocusHandler;
import com.braintribe.gwt.gmview.client.parse.ParserArgument;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.NonModalEditorWindow;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.folder.FolderContent;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.BaseEventPreview;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.form.Field;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class QuickAccessDialog extends ClosableWindow implements NonModalEditorWindow, InitializableBean, DisposableBean {
	private static final int DEFAULT_HEIGHT = 700;
	private static final int DEFAULT_WIDTH = 600;
	
	private Future<QuickAccessResult> future;
	private Supplier<SpotlightPanel> quickAccessPanelProvider;
	private SpotlightPanel quickAccessPanel;
	private TextButton backButton;
	private TextButton frontButton;
	private TextButton instantiateButton;
	private TextButton applyButton;
	private final List<TypeCondition> previousTypeConditions = new ArrayList<>();
	private final List<String> previousEntityTypes = new ArrayList<>();
	private TypeCondition currentTypeCondition;
	private String useCase;
	private BorderLayoutContainer borderLayoutContainer;
	
	private String instantiateButtonLabel = LocalizedText.INSTANCE.instantiate();
	private final ImageResource instantiateButtonIcon = PropertyPanelResources.INSTANCE.instantiate();
	private boolean useApplyButton = true;
	private boolean useNavigationButtons = true;
	private boolean useInstantiateButton = true;
	private boolean initialized = false;
	private Function<ParserArgument, Future<EntitiesProviderResult>> entitiesFutureProvider;
	private Function<ParserArgument, Future<EntitiesProviderResult>> defaultEntitiesFutureProvider;
	
	private Timer hideTimer;
	private AsyncCallback<Boolean> hideCallback;
	private boolean supressHide = true;
	private ToolBar toolBar;
	private boolean addingToCollection = false;
	private boolean usingForSelect = false;
	
	public QuickAccessDialog() {
		setSize(DEFAULT_WIDTH + "px", DEFAULT_HEIGHT + "px");
		setOnEsc(false);
		setClosable(false);
		setModal(false);
		setMinWidth(400);
		setBodyBorder(false);
		setBorders(false);
		setAutoHide(true);
		getHeader().setHeight(20);
		
		setHeaderVisible(false);
		
		borderLayoutContainer = new BorderLayoutContainer();
		borderLayoutContainer.setBorders(false);
		this.add(borderLayoutContainer);
		
		BaseEventPreview eventPreview = new BaseEventPreview() {
			@Override
			protected boolean onAutoHide(NativePreviewEvent pe) {
				if (isResizing(QuickAccessDialog.this))
					return false;
				
				Element target = Element.as(pe.getNativeEvent().getEventTarget());
				Object textField = quickAccessPanel.getTextField();
				if (textField instanceof ValueBaseField) {
					ValueBaseField<?> field = (ValueBaseField<?>) textField;
					if (target == field.getCell().getInputElement(field.getElement()))
						return false;
				} else if (textField instanceof InputFocusHandler) {
					if (isElementRelatedToInputField(target, (InputFocusHandler) textField))
						return false;
				} else if (textField instanceof Element && target == textField)
					return false;
				
				if (pe.getTypeInt() == Event.ONMOUSEUP)
					return false;
				
				hide();
				return true;
			}
		};
			
		eventPreview.getIgnoreList().add(getElement());
		setEventPreview(eventPreview, this);
	}
	
	@Override
	public void intializeBean() throws Exception {
		toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.add(new FillToolItem());
		if (useApplyButton)
			toolBar.add(getApplyButton());
		if (useInstantiateButton)
			toolBar.add(getInstantiateButton());
		if (useNavigationButtons){
			toolBar.add(getBackButton());
			toolBar.add(getFrontButton());
		}
		
		toolBar.add(getCloseButton());
		
		borderLayoutContainer.setSouthWidget(toolBar, new BorderLayoutData(30));
		initialized = true;
	}
	
	@Override
	public void disposeBean() {
		future = null;
		hide();
	}
	
	@Configurable
	public void setUseApplyButton(boolean useApplyButton) {
		this.useApplyButton = useApplyButton;
	}
	
	@Configurable
	public void setUseNavigationButtons(boolean useNavigationButtons) {
		this.useNavigationButtons = useNavigationButtons;
	}
	
	@Configurable
	public void setInstantiateButtonLabel(String instantiateButtonLabel) {
		this.instantiateButtonLabel = instantiateButtonLabel;
		if (initialized)
			getInstantiateButton().setText(instantiateButtonLabel);
	}
	
	@Configurable
	public void setUseInstantiateButton(boolean useInstantiateButton) {
		this.useInstantiateButton = useInstantiateButton;
	}
	
	/**
	 * Configures the required provider for the {@link SpotlightPanel}.
	 */
	@Required
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
	}
	
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	public void configureEntitiesFutureProvider(Function<ParserArgument, Future<EntitiesProviderResult>> entitiesFutureProvider) {
		if (quickAccessPanel == null)
			this.entitiesFutureProvider = entitiesFutureProvider;
		else {
			if (defaultEntitiesFutureProvider == null)
				defaultEntitiesFutureProvider = quickAccessPanel.getEntitiesFutureProvider();
			quickAccessPanel.setEntitiesFutureProvider(entitiesFutureProvider);
		}
	}
	
	public void configureEnumConstantRenderer(Codec<GmEnumConstant, String> enumConstantRenderer) {
		if (quickAccessPanel != null)
			quickAccessPanel.configureEnumConstantRenderer(enumConstantRenderer);
	}
	
	public void configureDefaultEntitiesFutureProvider() {
		if (quickAccessPanel != null && defaultEntitiesFutureProvider != null)
			quickAccessPanel.setEntitiesFutureProvider(defaultEntitiesFutureProvider);
		entitiesFutureProvider = null;
	}
	
	public void configureCurrentEnumTypes(Set<GmEnumType> enumTypes) {
		quickAccessPanel.configureCurrentEnumTypes(enumTypes);
	}
	
	public void configureDefaultEnumTypes() {
		if (quickAccessPanel != null)
			quickAccessPanel.configureDefaultEnumTypes();
	}
	
	public void configureHandleKeyPress(boolean handleKeyPress) {
		if (quickAccessPanel != null)
			quickAccessPanel.configureHandleKeyPress(handleKeyPress);
	}
	
	public SpotlightPanel getQuickAccessPanel() {
		if (quickAccessPanel != null)
			return quickAccessPanel;
		
		quickAccessPanel = quickAccessPanelProvider.get();
		borderLayoutContainer.setCenterWidget(quickAccessPanel);
		if (useCase != null)
			quickAccessPanel.setUseCase(useCase);
		
		if (entitiesFutureProvider != null) {
			if (defaultEntitiesFutureProvider == null)
				defaultEntitiesFutureProvider = quickAccessPanel.getEntitiesFutureProvider();
			quickAccessPanel.setEntitiesFutureProvider(entitiesFutureProvider);
		}
		
		quickAccessPanel.addDomHandler(event -> {
			if (hideCallback != null)
				hideCallback.onSuccess(false);
		}, MouseDownEvent.getType());
		
		quickAccessPanel.addSpotlightPanelListener(new SpotlightPanelListener() {
			@Override
			public void onValueOrTypeSelected(ObjectAndType objectAndType) {
				handleObjectAndType(objectAndType, true);
			}
			
			@Override
			public void onTypeSelected(GmType modelType) {
				resetTypeCondition(quickAccessPanel.prepareTypeCondition(modelType));
			}
			
			@Override
			public void onNotEnoughCharsTyped() {
				//handleObjectAndType(null, true);
				//quickAccessPanel.focusField();
			}
			
			@Override
			public void onCancel() {
				hide();
			}
		});
		
		quickAccessPanel.getGrid().getSelectionModel().addSelectionChangedHandler(event -> {
			GmTypeOrAction modelTypeOrAction = quickAccessPanel.prepareModelTypeOrAction();
			GmType modelType = modelTypeOrAction != null ? modelTypeOrAction.getType() : null;
			ObjectAndType objectAndType = quickAccessPanel.prepareObjectAndType();
			if (useNavigationButtons) {
				if (modelType instanceof GmEntityType)
					frontButton.setVisible(isValidAbstractType((GmEntityType) modelType));
				else
					frontButton.setVisible(false);
			}
			
			if (useInstantiateButton) {
				if (modelType instanceof GmEntityType && !((GmEntityType) modelType).getIsAbstract() && objectAndType.getObject() == null) {
					if (objectAndType.isServiceRequest())
						instantiateButton.setVisible(false);
					else {
						instantiateButton.setVisible(quickAccessPanel.getCMDResolver().entityTypeSignature(modelType.getTypeSignature())
								.useCase(useCase).is(Instantiable.T));
					}
				} else if (modelType instanceof GmEnumType && quickAccessPanel.getEnumTypeResult() == GmEnumTypeResult.type) {
					instantiateButton.setVisible(true);
				} else
					instantiateButton.setVisible(false);
			}
			
			if (useApplyButton)
				updateApplyButton(objectAndType);
			toolBar.forceLayout();
		});
		
		if (quickAccessPanel.getTextField() instanceof ValueBaseField) {
			((ValueBaseField<?>) quickAccessPanel.getTextField()).addKeyUpHandler(event -> {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
					handleEscape();
			});
		}
		
		return quickAccessPanel;
	}
	
	/**
	 * May be called when handling the Escape key when no textField is configured.
	 */
	public void handleEscape() {
		handleObjectAndType(null, true);
	}
	
	private void updateApplyButton(ObjectAndType objectAndType) {
		boolean visible = objectAndType != null;
		applyButton.setVisible(visible);
		
		if (!visible)
			return;
		
		if (addingToCollection) {
			applyButton.setText(LocalizedText.INSTANCE.addToCollection());
			applyButton.setToolTip(LocalizedText.INSTANCE.addDescription());
			applyButton.setIcon(PropertyPanelResources.INSTANCE.instantiate());
		} else if (usingForSelect) {
			applyButton.setText(LocalizedText.INSTANCE.select(""));
			applyButton.setToolTip(LocalizedText.INSTANCE.selectDescription());
			applyButton.setIcon(PropertyPanelResources.INSTANCE.apply());
		} else {
			if (objectAndType.getObject() == null && objectAndType.getType() instanceof GmEntityType) {
				applyButton.setText(LocalizedText.INSTANCE.query());
				applyButton.setToolTip(LocalizedText.INSTANCE.queryDescription());
				applyButton.setIcon(GmViewActionResources.INSTANCE.query());
			} else if (objectAndType.getObject() instanceof FolderContent) {
				applyButton.setText(LocalizedText.INSTANCE.execute());
				applyButton.setToolTip(LocalizedText.INSTANCE.executeDescription());
				applyButton.setIcon(PropertyPanelResources.INSTANCE.run());
			} else {
				applyButton.setText(LocalizedText.INSTANCE.open());
				applyButton.setToolTip(LocalizedText.INSTANCE.openDescription());
				applyButton.setIcon(PropertyPanelResources.INSTANCE.open());
			}
		}
	}
	
	/**
	 * Displays the dialog, and gets the {@link QuickAccessResult}.
	 */
	public Future<QuickAccessResult> getQuickAccessResult(TypeCondition typeCondition, Widget widget) {
		return getQuickAccessResult(typeCondition, widget, null);
	}
	
	/**
	 * Displays the dialog, and gets the {@link QuickAccessResult}.
	 */
	public Future<QuickAccessResult> getQuickAccessResult(TypeCondition typeCondition, Widget widget, boolean addingToCollection) {
		return getQuickAccessResult(typeCondition, widget, null, addingToCollection, false);
	}
	
	/**
	 * Displays the dialog, and gets the {@link QuickAccessResult}.
	 */
	public Future<QuickAccessResult> getQuickAccessResult(TypeCondition typeCondition, Widget widget, boolean addingToCollection, boolean usingForSelect) {
		return getQuickAccessResult(typeCondition, widget, null, addingToCollection, usingForSelect);
	}
	
	/**
	 * Displays the dialog, and gets the {@link QuickAccessResult}.
	 */
	public Future<QuickAccessResult> getQuickAccessResult(TypeCondition typeCondition, Widget widget, String initialText) {
		return getQuickAccessResult(typeCondition, widget, initialText, false, false);
	}
	
	public Future<QuickAccessResult> getQuickAccessResult(TypeCondition typeCondition, Object widget, String initialText) {
		return getQuickAccessResult(typeCondition, widget, initialText, false, false);
	}
	
	/**
	 * Displays the dialog, and gets the {@link QuickAccessResult}.
	 */
	public Future<QuickAccessResult> getQuickAccessResult(TypeCondition typeCondition, Object widget, String initialText, boolean addingToCollection, boolean usingForSelect) {
		this.addingToCollection = addingToCollection;
		this.usingForSelect = usingForSelect;
		future = new Future<>();
		
		try {
			getQuickAccessPanel().configureTypeCondition(typeCondition, false, initialText);
			getQuickAccessPanel().forceLayout();
		} catch (RuntimeException e) {
			e.printStackTrace();
			future.onFailure(e);
			return future;
		}
		currentTypeCondition = typeCondition;
		
		this.show();
		
		adaptPosition(widget);
		
		try {
			getQuickAccessPanel().forceLayout();
		} catch (RuntimeException e) {
			e.printStackTrace();
			future.onFailure(e);
			return future;
		}
		
		return future;
	}
	
	public void adaptPosition(Object widget){
		int top = 0;
		if (widget instanceof Widget) {
			top = ((Widget) widget).getAbsoluteTop() + ((Widget) widget).getOffsetHeight();
			this.setPosition(((Widget) widget).getAbsoluteLeft(), top);
		} else if (widget instanceof Element) {
			top = ((Element) widget).getAbsoluteTop() + ((Element) widget).getOffsetHeight();
			this.setPosition(((Element) widget).getAbsoluteLeft(), top);
		}
		
		if (top + DEFAULT_HEIGHT > com.google.gwt.user.client.Window.getClientHeight())
			setHeight(DEFAULT_HEIGHT - (top + DEFAULT_HEIGHT - com.google.gwt.user.client.Window.getClientHeight()));
		else if (getElement().getHeight(false) != DEFAULT_HEIGHT)
			setHeight(DEFAULT_HEIGHT);
	}
	
	/**
	 * Updates the filter text.
	 */
	public void updateFilterText(String initialText) {
		try {
			getQuickAccessPanel().configureTypeCondition(currentTypeCondition, false, initialText);
		} catch (RuntimeException e) {
			e.printStackTrace();
			future.onFailure(e);
		}
	}
	
	private void resetTypeCondition(TypeCondition typeCondition) {
		previousTypeConditions.add(currentTypeCondition);
		if (currentTypeCondition instanceof IsAssignableTo)
			previousEntityTypes.add(((IsAssignableTo) currentTypeCondition).getTypeSignature());
		quickAccessPanel.configureTypeCondition(typeCondition);
		currentTypeCondition = typeCondition;
		if (useNavigationButtons)
			backButton.setVisible(true);
		
		toolBar.forceLayout();
	}
	
	private void hide(QuickAccessResult result) {
		if (future != null) {
			if (quickAccessPanel != null)
				quickAccessPanel.cancelLoading();
			future.onSuccess(result);
		}
		previousTypeConditions.clear();
		previousEntityTypes.clear();
		if (useNavigationButtons)
			backButton.setVisible(false);
		if (quickAccessPanel != null && quickAccessPanel.getTextField() instanceof Field)
			((Field<?>) quickAccessPanel.getTextField()).clear();
		
		toolBar.forceLayout();
		
		super.hide();
	}
	
	private TextButton getBackButton() {
		backButton = new TextButton(LocalizedText.INSTANCE.back());
		backButton.setToolTip(LocalizedText.INSTANCE.backDescription());
		backButton.setIcon(PropertyPanelResources.INSTANCE.back());
		backButton.addSelectHandler(event -> {
			currentTypeCondition = previousTypeConditions.remove(previousTypeConditions.size() - 1);
			if (currentTypeCondition instanceof IsAssignableTo)
				previousEntityTypes.remove(previousEntityTypes.size() - 1);
			quickAccessPanel.configureTypeCondition(currentTypeCondition);
			backButton.setVisible(!previousTypeConditions.isEmpty());
			
			toolBar.forceLayout();
		});
		backButton.setVisible(false);
		
		return backButton;
	}
	
	private TextButton getFrontButton() {
		frontButton = new TextButton(LocalizedText.INSTANCE.subType());
		frontButton.setToolTip(LocalizedText.INSTANCE.frontDescription());
		frontButton.setIcon(PropertyPanelResources.INSTANCE.front());
		frontButton.addSelectHandler(event -> resetTypeCondition(quickAccessPanel.prepareTypeCondition(quickAccessPanel.prepareModelTypeOrAction().getType())));
		frontButton.setVisible(false);
		return frontButton;
	}
	
	private TextButton getInstantiateButton() {
		instantiateButton = new TextButton(instantiateButtonLabel);
		instantiateButton.setToolTip(LocalizedText.INSTANCE.instantiateDescription());
		instantiateButton.setIcon(instantiateButtonIcon);
		instantiateButton.addSelectHandler(event -> handleObjectAndType(quickAccessPanel.prepareObjectAndType(), false));
		instantiateButton.setVisible(false);
		return instantiateButton;
	}
	
	private TextButton getCloseButton() {
		TextButton closeButton = new TextButton(LocalizedText.INSTANCE.close());
		closeButton.setToolTip(LocalizedText.INSTANCE.closeDescription());
		closeButton.setIcon(PropertyPanelResources.INSTANCE.cancel());
		closeButton.addSelectHandler(event -> hide());
		return closeButton;
	}
	
	@Override
	public void hide() {
		handleObjectAndType(null, true);
	}
	
	public void forceHide(){
		super.hide();
	}
	
	private TextButton getApplyButton() {
		applyButton = new TextButton(LocalizedText.INSTANCE.ok());
		applyButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		applyButton.setIcon(PropertyPanelResources.INSTANCE.apply());
		applyButton.addSelectHandler(event -> handleObjectAndType(quickAccessPanel.prepareObjectAndType(), true));
		applyButton.setVisible(false);
		return applyButton;
	}
	
	private void handleObjectAndType(ObjectAndType objectAndType, boolean query) {
		if (objectAndType == null) {
			hide((QuickAccessResult) null);
			return;
		}
		
		if (objectAndType.isServiceRequest() && objectAndType.getType() instanceof GmEntityType) {
			GmEntityType entityType = (GmEntityType) objectAndType.getType();
			if (entityType.getIsAbstract()) {
				if (isValidAbstractType(entityType))
					resetTypeCondition(quickAccessPanel.prepareTypeCondition(entityType));
				return;
			}
		}
		
		hide(new QuickAccessResult(objectAndType, query, quickAccessPanel.getFilterText(), quickAccessPanel.getUseCase()));
	}
	
	public void setSupressHide(boolean supressHide) {
		this.supressHide = supressHide;
	}
	
	public boolean getSupressHide() {
		return supressHide;
	}
	
	@Override
	public void focus() {
		supressHide = true;
		super.focus();
//		suppressBlur = false;
	}
	
	public AsyncCallback<Boolean> triggerHideCallback() {
		if (supressHide) {
			supressHide = false;
			return null;
		}

		hideCallback = AsyncCallbacks.of(result -> {
			hideTimer.cancel();
			supressHide = !result;
			if (result)
				hide();
		}, e -> {
			hideTimer.cancel();
			supressHide = false;
		});
		
		hideTimer = new Timer() {				
			@Override
			public void run() {
				if (hideCallback != null)
					hideCallback.onSuccess(true);
			}
		};
		hideTimer.schedule(500);
		return hideCallback;
	}
	
	private boolean isValidAbstractType(GmEntityType entityType) {
		String currentTypeSignature = currentTypeCondition instanceof IsAssignableTo ? currentTypeSignature = ((IsAssignableTo) currentTypeCondition).getTypeSignature() : null;
		
		String typeSignature = entityType.getTypeSignature();
		if (entityType.getIsAbstract() && (typeSignature.equals(currentTypeSignature) || previousEntityTypes.contains(typeSignature)))
			return false;
		
		return true;
	}
	
	private boolean isElementRelatedToInputField(Element element, InputFocusHandler input) {
		if (element == input.getInputField())
			return true;
		
		Element inputTrigger = input.getInputTrigger();
		if (inputTrigger == null)
			return false;
		
		return inputTrigger.isOrHasChild(element);
	}
	
	public static class QuickAccessResult {
		private ObjectAndType objectAndType;
		private boolean query;
		private String filterText;
		private String useCase;
		
		public QuickAccessResult(ObjectAndType objectAndType, boolean query, String useCase) {
			this(objectAndType, query, null, useCase);
		}
		
		public QuickAccessResult(ObjectAndType objectAndType, boolean query, String filterText, String useCase) {
			this.objectAndType = objectAndType;
			this.query = query;
			this.filterText = filterText;
			this.useCase = useCase;
		}
		
		public void setObjectAndType(ObjectAndType objectAndType) {
			this.objectAndType = objectAndType;
		}
		
		public ObjectAndType getObjectAndType() {
			return objectAndType;
		}
		
		public void setQuery(boolean query) {
			this.query = query;
		}
		
		public boolean isQuery() {
			return query;
		}
		
		public Object getObject() {
			return objectAndType == null ? null : objectAndType.getObject();
		}
		
		public GmType getType() {
			return objectAndType == null ? null : objectAndType.getType();
		}
		
		public String getFilterText() {
			return filterText;
		}
		
		public void setFilterText(String filterText) {
			this.filterText = filterText;
		}
		
		public String getUseCase() {
			return useCase;
		}
		
		public void setUseCase(String useCase) {
			this.useCase = useCase;
		}
	}
	
	@Override
	protected void onWindowResize(int width, int height) {
		try {
			super.onWindowResize(width, height);
		} catch(Exception ex) {
			//NOP
		}
	}
	
	private static native void setEventPreview(BaseEventPreview eventPrev, Window window) /*-{
		window.@com.sencha.gxt.widget.core.client.Window::eventPreview = eventPrev;
	}-*/;
	
	private static native boolean isResizing(Window window) /*-{
		return window.@com.sencha.gxt.widget.core.client.Window::resizing;
	}-*/;

}
