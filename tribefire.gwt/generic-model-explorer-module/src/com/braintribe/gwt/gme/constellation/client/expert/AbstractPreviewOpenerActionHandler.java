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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.gme.constellation.client.BrowsingConstellation;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.constellation.client.JsUxComponentOpenerActionHandler;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gmview.action.client.LocalizedText;
import com.braintribe.gwt.gmview.action.client.WorkWithEntityExpert;
import com.braintribe.gwt.gmview.client.ContentSpecificationListener;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmListContentSupplier;
import com.braintribe.gwt.gmview.client.GmListView;
import com.braintribe.gwt.gmview.client.HasFullscreenSupport;
import com.braintribe.gwt.gmview.client.PreviewRefreshHandler;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.aspects.VariableProviderAspect;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.uicommand.RefreshPreview;
import com.braintribe.model.workbench.KeyConfiguration;
import com.braintribe.model.workbench.PreviewOpenerAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.KeyNav;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;

/**
 * Abstract implementation for the {@link PreviewOpenerAction}, which should be extended by actual implementations.
 * {@link #getView()} is the method which should be implemented by them.
 * 
 * @author michel.docouto
 *
 */
public abstract class AbstractPreviewOpenerActionHandler
		implements WorkbenchActionHandler<PreviewOpenerAction>, ContentSpecificationListener, PreviewRefreshHandler, DisposableBean {
	
	private ExplorerConstellation explorerConstellation;
	private ModelPath modelPath;
	private List<ModelPath> selectionModelPathList;
	protected PreviewOpenerAction action;
	private StandardMatcher matcher;
	private boolean disableNavigation;
	private boolean documentInitialized = false;
	private Integer width;
	private Integer height;
	protected Window documentWindow;
	private boolean maximized;
	private TextButton maximizeButton;
	private SafeHtml safeHeading;
	private WorkWithEntityExpert workWithEntityExpert;
	protected boolean maskPreview = true;
	protected boolean handleBeforeHide = false;
	private KeyNav documentWindowKeyNav;
	private KeyNav previewWindowKeyNav;
	
	/**
	 * Configures the required {@link ExplorerConstellation}.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures the required expert for handling opening the document externally.
	 */
	@Required
	public void setWorkWithEntityExpert(WorkWithEntityExpert workWithEntityExpert) {
		this.workWithEntityExpert = workWithEntityExpert;
	}
	
	@Override
	public void perform(WorkbenchActionContext<PreviewOpenerAction> workbenchActionContext) {
		BrowsingConstellation currentBrowsingConstellation = explorerConstellation.getCurrentBrowsingConstellation();
		if (currentBrowsingConstellation == null)
			return;
		
		GmContentView currentContentView = currentBrowsingConstellation.getCurrentContentView();
		if (currentContentView == null)
			return;
		
		selectionModelPathList = currentContentView.getCurrentSelection();
		modelPath = currentContentView.getFirstSelectedItem();
		if (modelPath == null)
			return;
		
		if (action != workbenchActionContext.getWorkbenchAction()) {
			matcher = null;
			action = workbenchActionContext.getWorkbenchAction();
			if (action.getKeyConfiguration() != null) {
				Window window = getDocumentWindow();
				documentWindowKeyNav = new KeyNav(window) {
					@Override
					public void onKeyPress(NativeEvent evt) {
						EventTarget eventTarget = evt.getEventTarget();
						if (Element.is(eventTarget)) {
							Element element = eventTarget.cast();
							String tagName = element.getTagName();
							if ("input".equalsIgnoreCase(tagName) || "textarea".equalsIgnoreCase(tagName))
								return;
							
							String contentEditable = element.getAttribute("contenteditable");
							if (contentEditable != null && "true".equalsIgnoreCase(contentEditable.toLowerCase()))
								return;
							
							EventListener eventListener = DOM.getEventListener(element);
							if (eventListener instanceof IgnoreKeyConfigurationDialog)
								return;
						}
						
						if (evt.getKeyCode() == KeyCodes.KEY_RIGHT || evt.getKeyCode() == KeyCodes.KEY_LEFT) {
							Console.log("JsUxPreviewOpenerActionHandler onKeyPress KEY_RIGHT or KEY_LEFT");
							if (!disableNavigation && handleNavigation(evt.getKeyCode() == KeyCodes.KEY_RIGHT)) {
								evt.stopPropagation();
								evt.preventDefault();
							}
							
							return;
						}

						if (!disableNavigation && evt.getKeyCode() == KeyCodes.KEY_UP || evt.getKeyCode() == KeyCodes.KEY_DOWN) {
							Console.log("JsUxPreviewOpenerActionHandler onKeyPress KEY_UP or KEY_DOWN");
							if (handleVerticalNavigation(evt.getKeyCode() == KeyCodes.KEY_DOWN)) {
								evt.stopPropagation();
								evt.preventDefault();
							}
							
							return;
						}
						
						KeyConfiguration keyConfiguration = action.getKeyConfiguration();
						if (evt.getKeyCode() != keyConfiguration.getKeyCode() || evt.getAltKey() != keyConfiguration.getAlt()
								|| evt.getShiftKey() != keyConfiguration.getShift() || evt.getCtrlKey() != keyConfiguration.getCtrl()
								|| evt.getMetaKey() != keyConfiguration.getMeta())
							return;
						
						evt.stopPropagation();
						evt.preventDefault();
						smoothHide(window);
					}
				};
			}
		}
		
		GenericEntity entity = modelPath.last().getValue();
		openPreview(null, entity.toSelectiveInformation(), action.getWidth(), action.getHeight(), getDocumentWindow(), false, action.getKeyConfiguration());
	}

	@Override
	public void onPreviewRefresh(RefreshPreview refreshPreview) {
		if (documentWindow != null && documentWindow.isVisible())
			updateViewContent();
	}

	@Override
	public void onHandleContentSize(double contentWidth, double contentHeight) {
		if (documentInitialized)
			return;
		
		documentInitialized = true;
		
		if (contentWidth == 0)
			width = width != null ? width : (Document.get().getClientWidth() < 1280 ? Document.get().getClientWidth() : 1280);
		else
			width = ((Double) contentWidth).intValue();
		
		if (contentHeight == 0)
			height = height != null ? height : Document.get().getClientHeight() - 25 /*defined padding? */;
		else
			height = ((Double) contentHeight).intValue();
		
		Window window = getDocumentWindow();
		window.setSize(width + "px", height + "px");
		window.center();
		if (maskPreview)
			window.getElement().unmask();
		
		Scheduler.get().scheduleFixedDelay(() -> {
			if (getView() instanceof RequiresResize)
				((RequiresResize) getView()).onResize();
			return false;
		}, 50);
	}
	
	/**
	 * Despite being a {@link GmListView}, the returned view must be a {@link Widget}, a {@link RequiresResize} and a {@link HasFullscreenSupport}.
	 */
	protected abstract GmListView getView();
	
	private Window getDocumentWindow() {
		if (documentWindow != null)
			return documentWindow;
		
		documentWindow = new DocumentWindowDialog() {
			@Override
			public void hide() {
				if (maximized)
					handleMaximizeButton(maximizeButton);
				getView().setContent(null);  //RVE - to simplify the view to standard Interfaces "cancel()" replaced with setContent(null)
				super.hide();
			}
			
			@Override
			public void maximize() {
				super.maximize();
				Scheduler.get().scheduleDeferred(() -> {
					safeHeading = documentWindow.getHeading();
					documentWindow.setHeading("");
					GmListView view = getView();
					if (view instanceof HasFullscreenSupport)
						((HasFullscreenSupport) view).onMaximize();
					view.sendUxMessage("Window", "onMaximize", documentWindow.getOffsetWidth() + "," + documentWindow.getOffsetHeight());
				});
			}
			
			@Override
			public void restore() {
				super.restore();
				Scheduler.get().scheduleDeferred(() -> {
					GmListView view = getView();
					if (view instanceof HasFullscreenSupport)
						((HasFullscreenSupport) view).onMinimize();
					if (safeHeading != null)
						documentWindow.setHeading(safeHeading);
					view.sendUxMessage("Window", "onRestore", documentWindow.getOffsetWidth() + "," + documentWindow.getOffsetHeight());
				});
			}
		};
		documentWindow.setModal(true);
		documentWindow.setClosable(false);
		documentWindow.setOnEsc(true);
		//documentWindow.setAutoHide(true);   //RVE disabled - because on using action inside the preview, is auto closed
		
		documentWindow.addStyleName("gmeDialog");
		documentWindow.addStyleName("gmePreviewDialog");
		
		documentWindow.setBodyBorder(false);
		documentWindow.setBorders(false);
		documentWindow.setShadow(false);
		documentWindow.getHeader().setHeight(15);
		
		GmListView view = getView();
		
		documentWindow.addShowHandler(event -> documentInitialized = false);
		
		documentWindow.addResizeHandler(event -> {
			if (view instanceof RequiresResize)
				((RequiresResize) view).onResize();
			view.sendUxMessage("Window", "onResize", documentWindow.getOffsetWidth() + "," + documentWindow.getOffsetHeight());
		});
		
		if (handleBeforeHide) {
			documentWindow.addBeforeHideHandler(event -> {
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
						documentWindow.hide();
					}
				});
			});
		}
		
		documentWindow.add((Widget) view);
		
		TextButton openNewBrowserButton = new TextButton();
		openNewBrowserButton.setToolTip(LocalizedText.INSTANCE.openWebReader());
		openNewBrowserButton.setIcon(ConstellationResources.INSTANCE.open());
		openNewBrowserButton.addSelectHandler(event -> {
			WorkbenchAction action = workWithEntityExpert.getActionToPerform(modelPath);
			if (action == null)
				openNewBrowserButton.setVisible(false);
			else {
				workWithEntityExpert.performAction(modelPath, action, view, true);
				documentWindow.hide();
			}
		});
		documentWindow.getHeader().addTool(openNewBrowserButton);
		
		maximizeButton = new TextButton();
		maximizeButton.setIcon(ConstellationResources.INSTANCE.maximizeSmall());
		maximizeButton.setToolTip(LocalizedText.INSTANCE.maximize());
		maximizeButton.addSelectHandler(event -> handleMaximizeButton(maximizeButton));
		documentWindow.getHeader().addTool(maximizeButton);
		
		TextButton closeButton = new TextButton();
		closeButton.setIcon(ConstellationResources.INSTANCE.cancelSmall());
		closeButton.setToolTip(LocalizedText.INSTANCE.close());
		closeButton.addSelectHandler(event -> documentWindow.hide());
		documentWindow.getHeader().addTool(closeButton);
		
		return documentWindow;
	}
	
	private class DocumentWindowDialog extends ClosableWindow implements IgnoreKeyConfigurationDialog {
		public DocumentWindowDialog() {
			setId(JsUxComponentOpenerActionHandler.EXTERNAL_COMPONENT_ID_WINDOW_PREFIX);
		}
	}
	
	private boolean handleNavigation(boolean next) {
		BrowsingConstellation currentBrowsingConstellation = explorerConstellation.getCurrentBrowsingConstellation();
		if (currentBrowsingConstellation == null)
			return false;
		
		GmContentView view = currentBrowsingConstellation.getCurrentContentView();
		if (view == null)
			return false;
		
		if (!(view instanceof GmListView) && !(view instanceof GmListContentSupplier))
			return false;
		
		int index = view.getFirstSelectedIndex();
		int newIndex = -1;
		if (next)
			newIndex = getNextContent(index, view);
		else
			newIndex = getPreviousContent(index, view);
		
		if (newIndex == -1)
			return false;
		
		view.select(newIndex, false);
		selectionModelPathList = view.getCurrentSelection();
		modelPath = view.getFirstSelectedItem();
		
		GenericEntity entity = modelPath.last().getValue();
		getDocumentWindow().setHeading(entity.toSelectiveInformation());
		updateViewContent();
		
		return true;
	}
	
	/**
	 * Opens the preview for the given parameters.
	 */
	public void openPreview(String documentId, String title, Integer width, Integer height, KeyConfiguration keyConfiguration) {
		openPreview(documentId, title, width, height, getDocumentWindow(), true, keyConfiguration);
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (documentWindowKeyNav != null)
			documentWindowKeyNav.bind(null);
		
		if (previewWindowKeyNav != null)
			previewWindowKeyNav.bind(null);
		
		if (selectionModelPathList != null)
			selectionModelPathList.clear();
	}
	
	private void openPreview(String documentId, String title, Integer width, Integer height, Window window, boolean disableNavigation,
			KeyConfiguration keyConfiguration) {
		this.disableNavigation = disableNavigation;
		this.width = width != null ? width : (Document.get().getClientWidth() < 1280 ? Document.get().getClientWidth() : 1280);
		this.height = height != null ? height : Document.get().getClientHeight() - 25 /*defined padding? */;
		
		window.setWidth(this.width);
		window.setHeight(this.height);
		window.getElement().getStyle().setProperty("opacity", "0");
		
		if (title != null)
			window.setHeading(title);
		
		if (documentId == null)
			updateViewContent();
		else {
			ModelPath modelPath = new ModelPath();
			ModelPathElement modelPathElement = new RootPathElement(GMF.getTypeReflection().getBaseType().getActualType(documentId), documentId);
			modelPath.add(modelPathElement);
			getView().setContent(modelPath);
		}
		
		window.show();
		window.setZIndex(900);
		window.center();
		if (maskPreview)
			window.getElement().mask(com.braintribe.gwt.gme.constellation.client.LocalizedText.INSTANCE.loadingPreview());
		
		Window finalWindow = window;
		int delay = 5;
		if (selectionModelPathList.size() > 1) //RVE multiselection - instead of preview show complete (actions+tab) maximized window
			delay = 500;
		maximizeButton.setVisible(selectionModelPathList.size() == 1);
		
		Scheduler.get().scheduleFixedDelay(() -> {
			finalWindow.getElement().getStyle().setProperty("opacity", "1");
			if (selectionModelPathList.size() > 1)
				Scheduler.get().scheduleFixedDelay(() -> {
					if (!maximized)
						handleMaximizeButton(maximizeButton);
					return false;
				}, 5);

			return false;
		}, delay);
		
		if (keyConfiguration == null)
			return;
		
		previewWindowKeyNav = new KeyNav(window) {
			@Override
			public void onKeyPress(NativeEvent evt) {
				if (evt.getKeyCode() != keyConfiguration.getKeyCode() || evt.getAltKey() != keyConfiguration.getAlt()
						|| evt.getShiftKey() != keyConfiguration.getShift() || evt.getCtrlKey() != keyConfiguration.getCtrl()
						|| evt.getMetaKey() != keyConfiguration.getMeta())
					return;

				evt.stopPropagation();
				evt.preventDefault();
				smoothHide(finalWindow);
			}
		};
	}
	
	private int getNextContent(int index, GmContentView view) {
		Collection<?> collection = getCollection(view);
		if (collection == null || collection.size() == index + 1)
			return -1;

		TraversingCriterion inplaceContextCriterion = action.getInplaceContextCriterion();
		if (inplaceContextCriterion == null)
			return index + 1;
		
		List<?> list;
		if (collection instanceof List)
			list = (List<?>) collection;
		else
			list = new ArrayList<>(collection);
		
		int size = list.size();
		
		for (int i = index + 1; i < size; i++) {
			Object collectionElement = list.get(i);
			
			ModelPath elementModelPath = prepareModelPath(collectionElement);
			if (elementModelPath == null)
				continue;
			
			if (getMatcher().matches(elementModelPath.asTraversingContext()))
				return i;
		}
		
		return -1;
	}
	
	private int getPreviousContent(int index, GmContentView view) {
		if (index == 0)
			return -1;
		
		Collection<?> collection = getCollection(view);
		if (collection == null)
			return -1;
		
		TraversingCriterion inplaceContextCriterion = action.getInplaceContextCriterion();
		if (inplaceContextCriterion == null)
			return index - 1;
		
		List<?> list;
		if (collection instanceof List)
			list = (List<?>) collection;
		else
			list = new ArrayList<>(collection);
		
		for (int i = index - 1; i >= 0; i--) {
			Object collectionElement = list.get(i);
			
			ModelPath newModelPath = prepareModelPath(collectionElement);
			if (newModelPath == null)
				continue;
			
			if (getMatcher().matches(newModelPath.asTraversingContext()))
				return i;
		}
		
		return -1;
	}
	
	private boolean handleVerticalNavigation(boolean next) {
		BrowsingConstellation currentBrowsingConstellation = explorerConstellation.getCurrentBrowsingConstellation();
		if (currentBrowsingConstellation == null)
			return false;

		GmContentView view = currentBrowsingConstellation.getCurrentContentView();
		if (view == null)
			return false;
		
		if (!(view instanceof GmListView) && !(view instanceof GmListContentSupplier))
			return false;
				
		if (!view.selectVertical(next, false))
			return false;
		
		selectionModelPathList = view.getCurrentSelection();
		modelPath = view.getFirstSelectedItem();
		
		GenericEntity entity = modelPath.last().getValue();
		getDocumentWindow().setHeading(entity.toSelectiveInformation());
		updateViewContent();
		
		return true;
	}
	
	private void smoothHide(Window window) {
		window.getElement().getStyle().setProperty("opacity", "0");
		Scheduler.get().scheduleFixedDelay(() -> {
			window.hide();
			return false;
		}, 200);
	}
	
	private void updateViewContent() {
		getView().setContent(modelPath);
		for (ModelPath addModelPath : selectionModelPathList) {
			if (addModelPath == null)
				continue;
			
			if (addModelPath.equals(modelPath))
				continue;
			
			getView().addContent(addModelPath);
		}
	}
	
	private Collection<?> getCollection(GmContentView view) {
		ModelPath modelPath = view.getContentPath();
		
		if (modelPath != null) {
			Object value = modelPath.last().getValue();
			if (!(value instanceof Collection))
				return null;
			
			return (Collection<?>) value;
		}
		
		List<ModelPath> modelPathList = null;
		if (view instanceof GmListView)
			modelPathList = ((GmListView) view).getAddedModelPaths();
		else if (view instanceof GmListContentSupplier)
			modelPathList = ((GmListContentSupplier) view).getContents();
		
		if (modelPathList == null || modelPathList.isEmpty() || modelPathList.size() > 1)
			return modelPathList;
		
		Object value = modelPathList.get(0).last().getValue();
		if (!(value instanceof Collection))
			return null;
		
		return (Collection<?>) value;
	}
	
	private ModelPath prepareModelPath(Object value) {
		if (value instanceof ModelPath)
			return (ModelPath) value;
		
		if (!(value instanceof GenericEntity))
			return null;
		
		ModelPath modelPath = new ModelPath();
		RootPathElement element = new RootPathElement((GenericEntity) value);
		modelPath.add(element);
		return modelPath;
	}
	
	private StandardMatcher getMatcher() {
		if (matcher == null) {
			matcher = new StandardMatcher();
			matcher.setCheckOnlyProperties(false);
			matcher.setCriterion(action.getInplaceContextCriterion());
			matcher.setPropertyValueComparisonResolver(this::evaluate);
		}
		
		return matcher;
	}
	
	private Object evaluate(Object object) {
		//@formatter:off
		return VDE.evaluate()
				.withEvaluationMode(VdeEvaluationMode.Preliminary)
				.with(VariableProviderAspect.class, Variable::getDefaultValue)
				.forValue(object);
		//@formatter:on
	}
	
	private void handleMaximizeButton(TextButton maximizeButton) {
		if (!maximized)
			documentWindow.maximize();
		else
			documentWindow.restore();
		
		maximized = !maximized;
		maximizeButton.setIcon(maximized ? ConstellationResources.INSTANCE.restoreSmall() : ConstellationResources.INSTANCE.maximizeSmall());
		maximizeButton.setToolTip(maximized ? LocalizedText.INSTANCE.restore() : LocalizedText.INSTANCE.maximize());
	}

}
