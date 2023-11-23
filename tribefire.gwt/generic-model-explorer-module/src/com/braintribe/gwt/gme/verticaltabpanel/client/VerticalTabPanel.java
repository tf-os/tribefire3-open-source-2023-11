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
package com.braintribe.gwt.gme.verticaltabpanel.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.vectomatic.dom.svg.ui.SVGResource;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.action.ExchangeContentViewAction;
import com.braintribe.gwt.gme.constellation.client.action.SeparatorAction;
import com.braintribe.gwt.gme.verticaltabpanel.client.action.DefaultVerticalTabManager;
import com.braintribe.gwt.gmresourceapi.client.GmImageResource;
import com.braintribe.gwt.gmview.client.DoubleStateAction;
import com.braintribe.gwt.gmview.client.GmAdditionalTextHandler;
import com.braintribe.gwt.gmview.client.HasAdditionalText;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.gmview.util.client.GMEIconUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Panel that will display {@link VerticalTabElement}s, in a vertical tab style.
 * Horizontal style is also available.
 * @author michel.docouto
 *
 */
public class VerticalTabPanel extends HTML implements DisposableBean, VerticalTab, GmAdditionalTextHandler {
	public static final int ELEMENT_HEIGHT = 33;
	
	private List<VerticalTabElement> tabElements;
	private Set<VerticalTabListener> tabListeners;
	private VerticalTabElement selectedElement;
	private OpenerDialog openerDialog;
	private RenameDialog renameDialog;
	private boolean useHorizontalTabs = false;
	private int maxNumberOfNonStaticElements = 3;
	private boolean closableItems = true;
	private boolean displayIconsForNonStaticElements = false;
	private int currentHeight = 0;
	private Menu contextMenu;
	private VerticalTabElement menuTabElement;
	private MenuItem renameTabItem;
	private MenuItem closeOtherItem;
	private MenuItem closeLeftItem;
	private MenuItem closeRightItem;
	private MenuItem closeAllItem;
	private List<MenuItem> tabElementItems;
	private boolean alwaysFireElement;

	private PersistenceGmSession gmSession;
	private Folder rootFolder;
	private DefaultVerticalTabManager verticalTabManager;
	private Boolean folderInitialized = false;
	protected PersistenceGmSession workbenchSession;
	private List<Folder> listFolder = new ArrayList<>();
	private String parentUseCase;
	private boolean reuseWorkbenchActionContextTabElement;
	private Map<Folder, VerticalTabElement> workbenchActionContextFolderTabElements;
	private Timer dragEnterTimer;
	private Element dragElement;
	private DropTarget dropTarget;
	private Set<VerticalTabElement> tabsWithReloadPending;
	
	public VerticalTabPanel() {
		tabElements = new ArrayList<>();
		this.getElement().getStyle().setBackgroundColor("white");
		sinkEvents(Event.ONCONTEXTMENU);
		
		Window.addResizeHandler(event -> updateHeight());
		
		addClickHandler(event -> {
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
						
			Element targetElement = Element.as(target);
			VerticalTabElement tabElement = getVerticalTabElement(targetElement);
			if (tabElement != null) {
				tabElement.setElement(targetElement);
				
				Widget widget = tabElement.getWidget();
				if (widget instanceof HasAdditionalText)
					((HasAdditionalText) widget).addAdditionalListener(this);
				
				if (widget != null && widget instanceof Menu) {
					int x = targetElement.getAbsoluteLeft();   //event.getClientX()
					int y = targetElement.getAbsoluteBottom() + 3;  //event.getClientY()

					((Menu) widget).setData("left", x);
					((Menu) widget).setData("top", y);
					
					//((Menu) widget).setPosition(event.getScreenX(), event.getScreenY());
				}
				
				if (targetElement.getClassName().equals("closeIcon") || targetElement.getClassName().equals("closeHorizontalIcon"))
					closeVerticalTabElement(tabElement);
				else
					setSelectedVerticalTabElement(tabElement);
			}
		});
		
		dropTarget = new DropTarget(this) {
			@Override
			protected void showFeedback(DndDragMoveEvent event) {
				event.getStatusProxy().setStatus(false);
			}
		};
		
		dropTarget.addDragEnterHandler(event -> {
			EventTarget eventTarget = event.getDragEnterEvent().getNativeEvent().getEventTarget();
			dragElement = Element.is(eventTarget) ? Element.as(eventTarget) : null;
			getDragEnterTimer().cancel();
			getDragEnterTimer().schedule(1000);
		});
		
		dropTarget.addDragCancelHandler(event -> getDragEnterTimer().cancel());
		dropTarget.addDragLeaveHandler(event -> getDragEnterTimer().cancel());
		dropTarget.addDropHandler(event -> getDragEnterTimer().cancel());
		dropTarget.addDragMoveHandler(event -> {
			EventTarget eventTarget = event.getDragMoveEvent().getNativeEvent().getEventTarget();
			dragElement = Element.is(eventTarget) ? Element.as(eventTarget) : null;
			getDragEnterTimer().cancel();
			getDragEnterTimer().schedule(1000);
		});
	}
	
	/**
	 * Configures whether to use this panel with horizontal tabs instead of vertical ones.
	 * Defaults to false.
	 */
	@Configurable
	public void setUseHorizontalTabs(boolean useHorizontalTabs) {
		this.useHorizontalTabs = useHorizontalTabs;
	}
	
	/**
	 * Configures the max number of non static elements that can be present in the {@link VerticalTabPanel}.
	 * Defaults to 3.
	 */
	@Configurable
	public void setMaxNumberOfNonStaticElements(int maxNumberOfNonStaticElements) {
		this.maxNumberOfNonStaticElements = maxNumberOfNonStaticElements;
	}
	
	/**
	 * Configures whether to add a close button in order to close tab elements.
	 * Defaults to true.
	 */
	@Configurable
	public void setClosableItems(boolean closableItems) {
		this.closableItems = closableItems;
	}
	
	/**
	 * Configures whether icons for non-static elements should be displayed.
	 * Defaults to false.
	 */
	@Configurable
	public void setDisplayIconsForNonStaticElements(boolean displayIconsForNonStaticElements) {
		this.displayIconsForNonStaticElements = displayIconsForNonStaticElements;
	}
	
	/**
	 * Configures whether icons for non-static elements should be displayed.
	 * Defaults to false.
	 */
	@Configurable
	public void setVerticalTabPanelLoaderManager(DefaultVerticalTabManager verticalTabManager) {
		this.verticalTabManager = verticalTabManager;
		if (verticalTabManager != null)
			verticalTabManager.addVerticalTabPanel(this); 
	}
	
	/**
	 * Configures owner name of component - used with dynamic Workbench config to differ which Actions/Items are configured
	 */
	@Configurable
	public void setParentUseCase(String parentUseCase) {
		this.parentUseCase = parentUseCase;
	}
	
	/**
	 * Configures whether opening a {@link WorkbenchActionContext} should lead to a new tab.
	 * Defaults to false: always a new tab is created. If true, then if there is an existing tab for a {@link WorkbenchActionContext}, it is selected instead.
	 */
	@Configurable
	public void setReuseWorkbenchActionContextTabElement(boolean reuseWorkbenchActionContextTabElement) {
		this.reuseWorkbenchActionContextTabElement = reuseWorkbenchActionContextTabElement;
	}
		
	/**
	 * Configures if always allow Fire already selected Element
	 */
	@Configurable
	public void setAlwaysFireElement(boolean alwaysFireElement) {
		this.alwaysFireElement = alwaysFireElement;
	}
	
	public void addVerticalTabListener(VerticalTabListener listener) {
		if (tabListeners == null)
			tabListeners = new LinkedHashSet<>();
		tabListeners.add(listener);
	}
	
	public void removeVerticalTabListener(VerticalTabListener listener) {
		if (tabListeners != null) {
			tabListeners.remove(listener);
			if (tabListeners.isEmpty())
				tabListeners = null;
		}
	}
	
	public void insertVerticalTabElement(final VerticalTabElement element) {
		insertVerticalTabElement(element, 0);
	}
	
	public void insertVerticalTabElement(final VerticalTabElement element, int index) {
		insertVerticalTabElement(element, index, false);
	}
	
	public void insertVerticalTabElement(final VerticalTabElement element, final boolean ignoreElementAddedEvent) {
		insertVerticalTabElement(element, 0, ignoreElementAddedEvent);
	}
	
	public void insertVerticalTabElement(final VerticalTabElement element, int index, final boolean ignoreElementAddedEvent) {
		if (element == null) 
			return;
					
		if (!folderInitialized) {
			if (verticalTabManager != null)
				rootFolder = verticalTabManager.getRootFolder();
			folderInitialized = true;
		}
		
		Widget widget = element.getWidgetIfSupplied();
		if (widget instanceof HasAdditionalText)
			((HasAdditionalText) widget).addAdditionalListener(this);
		
		updateElementFromFolders(element);
		putVerticalTabElement(element, index, ignoreElementAddedEvent);
	}
	
	/**
	 * Marks the {@link VerticalTabElement} which is the parent of the given widget as a reload pending.
	 */
	public void markVerticalTabElementAsReloadPending(Widget widget) {
		VerticalTabElement verticalTabElement = getVerticalTabElementByWidgetOrParent(widget);
		if (verticalTabElement != null && verticalTabElement.getWidgetIfSupplied() instanceof ReloadableGmView) {
			if (tabsWithReloadPending == null)
				tabsWithReloadPending = new HashSet<>();
			tabsWithReloadPending.add(verticalTabElement);
		}
	}
	
	private void putVerticalTabElement(final VerticalTabElement element, int index, final boolean ignoreElementAddedEvent) {
		//updateElement(element);
				
		if (index == -2) {	      //RVE - add as last STATIC element
			int newIndex = getLastStaticIndex() + 1;
			tabElements.add(newIndex, element);			
		} else if (index == -1)	  //add to end
			tabElements.add(element);
		else
			tabElements.add(index, element);
		
		if (!ignoreElementAddedEvent)
			fireElementAddedOrRemoved(true, Collections.singletonList(element));
		updateHtml();
	}

	private int getLastStaticIndex() {
		int i = -1;
		for (VerticalTabElement element : tabElements)
			if (element.isStatic())
				i = tabElements.indexOf(element);
				
		return i;
	}

	/*
	private void updateElement(final VerticalTabElement element) {
		//RVE - set Icon, Description from Workbench folder if configured
		
		ImageResource imageResource = mapImageResourceFolder.get(element.getName());
		if (imageResource != null)
			 element.setIcon(imageResource);
		String description = mapDescriptionFolder.get(element.getName());
		if (description != null && !description.isEmpty())
			 element.setDescription(SafeHtmlUtils.htmlEscape(description));
	}
	*/
			
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}

	@Override
	public PersistenceGmSession getGmSession() {
		if (gmSession == null && verticalTabManager != null)
			gmSession = verticalTabManager.getPersistenceSession();
		return gmSession;
	}

	@Override
	public Future<Void> apply(Folder folder) {
		folderInitialized = true;
		rootFolder = folder;
		if (rootFolder != null) {	
			updateFolderList(rootFolder);
			updateElementsFromFolder();
		}
		
		return new Future<>(null);
	}

	@Override
	public Widget getVerticalTab() {
		return this;
	}

	@Override
	public void setWorkbenchSession(PersistenceGmSession workbenchSession) {
		this.workbenchSession = workbenchSession;
	}
	
	public Future<VerticalTabElement> createAndInsertVerticalTabElement(WorkbenchActionContext<?> workbenchActionContext, String text, int index,
			String description, Supplier<? extends Widget> widgetSupplier, ImageResource icon, Object modelObject, boolean staticElement) {
		Future<VerticalTabElement> future = new Future<>();
		
		if (reuseWorkbenchActionContextTabElement && workbenchActionContextFolderTabElements == null && workbenchActionContext != null)
			workbenchActionContextFolderTabElements = new HashMap<>();
		
		if (getNotStaticElementsSize() < maxNumberOfNonStaticElements) {
			VerticalTabElement verticalTabElement = new VerticalTabElement("$" + text, text, description, widgetSupplier, icon, modelObject,
					staticElement, workbenchActionContext);
			if (reuseWorkbenchActionContextTabElement && workbenchActionContext != null)
				workbenchActionContextFolderTabElements.put(workbenchActionContext.getFolder(), verticalTabElement);
			
			insertVerticalTabElement(verticalTabElement, index);
			setSelectedVerticalTabElement(verticalTabElement);
			future.onSuccess(verticalTabElement);
			return future;
		}

		getOpenerDialog().apply(tabElements) //
				.andThen(verticalTabElementToRemove -> {
					int tabIndex = index;
					if (verticalTabElementToRemove != null)
						tabIndex = removeVerticalTabElement(verticalTabElementToRemove);

					VerticalTabElement verticalTabElement = new VerticalTabElement("$" + text, text, description, widgetSupplier, icon, modelObject,
							staticElement, workbenchActionContext);
					if (reuseWorkbenchActionContextTabElement && workbenchActionContext != null)
						workbenchActionContextFolderTabElements.put(workbenchActionContext.getFolder(), verticalTabElement);

					insertVerticalTabElement(verticalTabElement, tabIndex);
					setSelectedVerticalTabElement(verticalTabElement);
					future.onSuccess(verticalTabElement);
				}).onError(future::onFailure);
		
		return future;
	}
	
	public int removeVerticalTabElement(VerticalTabElement element) {
		return removeVerticalTabElement(element, true);
	}
	
	public int removeVerticalTabElement(VerticalTabElement element, boolean dispose) {
		int index = tabElements.indexOf(element);
		if (index < 0)
			return index;
		
		tabElements.remove(element);
		fireElementAddedOrRemoved(false, Collections.singletonList(element));
		updateHtml();
		if (dispose)
			disposeElement(element);
		
		return index;
	}
	
	/**
	 * Returns true if the {@link VerticalTabPanel} contains the given {@link VerticalTabElement}, and false otherwise.
	 */
	public boolean containsVerticalTabElement(VerticalTabElement element) {
		return tabElements.contains(element);
	}
	
	/**
	 * Returns the first not static {@link VerticalTabElement}.
	 * If no not static element is found, then the first static one is returned.
	 */
	public VerticalTabElement getFirstNotStaticVerticalTabElementIfAny() {
		VerticalTabElement staticElement = null;
		for (VerticalTabElement element : tabElements) {
			if (!element.isStatic())
				return element;
			else if (staticElement == null)
				staticElement = element;
		}
		
		return staticElement;
	}
	
	/**
	 * Removes all not static {@link VerticalTabElement}s.
	 */
	public void removeAllNotStaticVerticalTabElements() {
		removeOtherVerticalTabElements(null);
	}
	
	@Override
	public void onBrowserEvent(Event theEvent) {
		int anEventType = DOM.eventGetType(theEvent);
		if (anEventType != Event.ONCONTEXTMENU) {
			super.onBrowserEvent(theEvent);
			return;
		}
		
		EventTarget target = theEvent.getEventTarget();
		if (Element.is(target)) {
			Element targetElement = Element.as(target);
			VerticalTabElement tabElement = getVerticalTabElement(targetElement);
			if (tabElement != null && !tabElement.isStatic()) {
				menuTabElement = tabElement;
				getContextMenu().showAt(theEvent.getClientX(), theEvent.getClientY());
				theEvent.stopPropagation();
				theEvent.preventDefault();
			}
		}
	}
	
	private VerticalTabElement removeOtherVerticalTabElements(VerticalTabElement keptElement) {
		VerticalTabElement firstStaticElement = null;
		List<VerticalTabElement> elementsRemoved = new ArrayList<>();
		for (VerticalTabElement element : new ArrayList<>(tabElements)) {
			if (!element.isStatic() && element != keptElement) {
				tabElements.remove(element);
				elementsRemoved.add(element);
				disposeElement(element);
			} else if (element.isStatic() && firstStaticElement == null)
				firstStaticElement = element;
		}
		
		if (!elementsRemoved.isEmpty()) {
			fireElementAddedOrRemoved(false, elementsRemoved);
			updateHtml();
		}
		
		if (keptElement != null)
			setSelectedVerticalTabElement(keptElement);
		
		return firstStaticElement;
	}
	
	private void removeLeftVerticalTabElements(VerticalTabElement element) {
		List<VerticalTabElement> elementsRemoved = new ArrayList<>();
		int index = tabElements.indexOf(element);
		for (int i = 0; i < index; i++) {
			VerticalTabElement el = tabElements.get(i);
			if (!el.isStatic()) {
				//tabElements.remove(el);
				elementsRemoved.add(el);
				disposeElement(el);
			}
		}
		
		if (!elementsRemoved.isEmpty()) {
			tabElements.removeAll(elementsRemoved);
			fireElementAddedOrRemoved(false, elementsRemoved);
			updateHtml();
		}
		
		setSelectedVerticalTabElement(element);
	}
	
	private void removeRightVerticalTabElements(VerticalTabElement element) {
		List<VerticalTabElement> elementsRemoved = new ArrayList<>();
		int index = tabElements.indexOf(element);
		for (int i = tabElements.size() - 1; i > index; i--) {
			VerticalTabElement el = tabElements.get(i);
			if (!el.isStatic()) {
				tabElements.remove(el);
				elementsRemoved.add(el);
				disposeElement(el);
			}
		}
		
		if (!elementsRemoved.isEmpty()) {
			fireElementAddedOrRemoved(false, elementsRemoved);
			updateHtml();
		}
		
		setSelectedVerticalTabElement(element);
	}
	
	private Menu getContextMenu() {
		if (contextMenu == null) {
			MenuItem closeItem = new MenuItem(LocalizedText.INSTANCE.close());
			closeOtherItem = new MenuItem(LocalizedText.INSTANCE.closeOther());
			closeLeftItem = new MenuItem(LocalizedText.INSTANCE.closeLeft());
			closeRightItem = new MenuItem(LocalizedText.INSTANCE.closeRight());
			closeAllItem = new MenuItem(LocalizedText.INSTANCE.closeAll());
			renameTabItem =  new MenuItem(LocalizedText.INSTANCE.rename());
			
			SelectionHandler<Item> selectionHandler = event -> {
				if (event.getSelectedItem() == closeItem)
					closeVerticalTabElement(menuTabElement);
				else if (event.getSelectedItem() == closeOtherItem)
					removeOtherVerticalTabElements(menuTabElement);
				else if (event.getSelectedItem() == closeLeftItem)
					removeLeftVerticalTabElements(menuTabElement);
				else if (event.getSelectedItem() == closeRightItem)
					removeRightVerticalTabElements(menuTabElement);
				else if (event.getSelectedItem() == closeAllItem) {
					VerticalTabElement firstStaticElement = removeOtherVerticalTabElements(null);
					if (firstStaticElement != null)
						setSelectedVerticalTabElement(firstStaticElement);					
				}  else if (event.getSelectedItem() == renameTabItem)
					renameTab(menuTabElement);											
			};
			
			closeItem.addSelectionHandler(selectionHandler);
			closeOtherItem.addSelectionHandler(selectionHandler);
			closeLeftItem.addSelectionHandler(selectionHandler);
			closeRightItem.addSelectionHandler(selectionHandler);
			closeAllItem.addSelectionHandler(selectionHandler);
			renameTabItem.addSelectionHandler(selectionHandler);
			
			contextMenu = new Menu();
			contextMenu.add(closeItem);
			contextMenu.add(closeOtherItem);
			contextMenu.add(closeLeftItem);
			contextMenu.add(closeRightItem);
			contextMenu.add(closeAllItem);
			contextMenu.add(renameTabItem);
		}
		
		if (tabElementItems != null) {
			tabElementItems.forEach(item -> contextMenu.remove(item));
			tabElementItems.clear();
		}
		
		checkItemsVisibility();
		if (menuTabElement.getTabElementActions() != null) {
			int index = 0;
			for (Action action : menuTabElement.getTabElementActions()) {
				MenuItem item = new MenuItem();
				MenuItemActionAdapter.linkActionToMenuItem(action, item);
				contextMenu.insert(item, index++);
				if (tabElementItems == null)
					tabElementItems = new ArrayList<>();
				tabElementItems.add(item);
			}
		}
		
		return contextMenu;
	}
	
	private void renameTab(final VerticalTabElement menuTabElement) {
		getRenameDialog().apply(menuTabElement.getText()) //
				.andThen(newName -> {
					menuTabElement.setText(newName);
					updateHtml();
					setSelectedVerticalTabElement(selectedElement);
				});
	}
	
	private void checkItemsVisibility() {
		boolean hasOthersToClose = false;
		boolean hasLeftItemsToClose = false;
		boolean hasRightItemsToClose = false;
		int index = tabElements.indexOf(menuTabElement);
		for (int i = 0; i < tabElements.size(); i++) {
			VerticalTabElement element = tabElements.get(i);
			
			if (!element.isStatic() && element != menuTabElement) {
				hasOthersToClose = true;
			
				if (i < index)
					hasLeftItemsToClose = true;
			}
			
			if (i > index)
				hasRightItemsToClose = true;
			
			if (hasOthersToClose && hasRightItemsToClose)
				break;
		}
		
		closeOtherItem.setEnabled(hasOthersToClose);
		closeLeftItem.setEnabled(hasLeftItemsToClose);
		closeRightItem.setEnabled(hasRightItemsToClose);
		closeAllItem.setEnabled(hasOthersToClose);
	}
	
	private void closeVerticalTabElement(VerticalTabElement element) {
		boolean selected = getSelectedElement() == element;
		int index = tabElements.indexOf(element);
		tabElements.remove(element);
		fireElementAddedOrRemoved(false, Collections.singletonList(element));
		updateHtml();
		
		if (!selected)
			markAsSelected(getSelectedElement());
		else {
			if (tabElements.size() == index)
				index--;
			if (index >= 0) {
				VerticalTabElement elementToSelect = tabElements.get(index);
				if (elementToSelect.isStatic())
					selectFirstTabElement();
				else
					setSelectedVerticalTabElement(elementToSelect);
			}
		}
		
		disposeElement(element);
	}
	
	public void selectFirstTabElement() {
		setSelectedVerticalTabElement(tabElements.get(0));
	}
	
	public void setSelectedVerticalTabElement(VerticalTabElement element) {		
		markAsSelected(element);
		if (selectedElement != element || this.alwaysFireElement) {
			VerticalTabElement previousElement = selectedElement;
			selectedElement = element;
			fireElementSelected(previousElement);
		}
		
		if (tabsWithReloadPending != null && tabsWithReloadPending.remove(element)) {
			Widget widget = element.getWidgetIfSupplied();
			if (widget instanceof ReloadableGmView)
				((ReloadableGmView) widget).reloadGmView();
		}
	}
	
	public VerticalTabElement getSelectedElement() {
		return selectedElement;
	}
	
	/**
	 * Returns the list of {@link VerticalTabElement}.
	 */
	public List<VerticalTabElement> getTabElements() {
		return tabElements;
	}
	
	/**
	 * Returns the {@link VerticalTabElement}, if any, which contains the given {@link Widget}.
	 */
	public VerticalTabElement getVerticalTabElementByWidget(Widget widget) {
		return tabElements.stream().filter(tabElement -> tabElement.getWidgetIfSupplied() == widget).findFirst().orElse(null);
	}
	
	private VerticalTabElement getVerticalTabElementByWidgetOrParent(Widget widget) {
		if (widget == null)
			return null;
		
		VerticalTabElement verticalTabElement = getVerticalTabElementByWidget(widget);
		if (verticalTabElement != null)
			return verticalTabElement;
		
		return getVerticalTabElementByWidgetOrParent(widget.getParent());
	}
	
	/**
	 * Returns the {@link VerticalTabElement}, if any, which contains the given supplier.
	 */
	public VerticalTabElement getVerticalTabElementByWidgetSupplier(Supplier<? extends Widget> widgetSupplier) {
		return tabElements.stream().filter(tabElement -> tabElement.getWidgetSupplier() == widgetSupplier).findFirst().orElse(null);
	}
	
	/**
	 * Returns the {@link VerticalTabElement}, if any, which is related to the given {@link WorkbenchActionContext}.
	 */
	public VerticalTabElement getVerticalTabElementByWorkbenchActionContext(WorkbenchActionContext<?> workbenchActionContext) {
		if (reuseWorkbenchActionContextTabElement && workbenchActionContextFolderTabElements != null && workbenchActionContext != null)
			return workbenchActionContextFolderTabElements.get(workbenchActionContext.getFolder());
		else
			return null;
	}
	
	/**
	 * Returns the {@link VerticalTabElement}, if any, which contains the given modelObject.
	 */
	public VerticalTabElement getVerticalTabElementByModelObject(Object modelObject) {
		if (modelObject == null)
			return null;
		
		return tabElements.stream().filter(tabElement -> tabElement.getModelObject() == modelObject).findFirst().orElse(null);
	}
	
	/**
	 * Refreshes the panel HTML, in case any element was changed manually.
	 */
	public void refresh() {
		updateHtml();
		
		if (selectedElement != null)
			markAsSelected(selectedElement);
	}
	
	public int getElementsSize() {
		return tabElements.size();
	}
	
	public void updateTabElementName(String newName, VerticalTabElement tabElement) {
		String stringRepresentation = getTabElementStringRepresentation(tabElement);
		Element element = DOM.getElementById(stringRepresentation);
		if (element == null)
			return;

		element.setInnerText(newName);
		tabElement.setText(SafeHtmlUtils.htmlEscape(newName));
		String newStringRepresentation = getTabElementStringRepresentation(tabElement);
		element.setId(newStringRepresentation);
		Element parentElement = DOM.getElementById("div" + stringRepresentation);
		if (parentElement != null)
			parentElement.setId("div" + newStringRepresentation);
		
		if (closableItems && !tabElement.isStatic()) {
			Element anchor = DOM.createAnchor();
			anchor.setId("anchor" + newStringRepresentation);
			anchor.setClassName(useHorizontalTabs ? "closeHorizontalIcon" : "closeIcon");
			element.appendChild(anchor);
		}
		
		updateHeight();
	}
	
	private void markAsSelected(VerticalTabElement element) {
		if (this.alwaysFireElement)
			return;
		
		String id = "div" + getTabElementStringRepresentation(element);
		NodeList<Element> tds = getElement().getElementsByTagName("div");
		for (int i = 0; i < tds.getLength(); i++) {
			Element td = tds.getItem(i);
			if (id.equals(td.getId()))
				td.addClassName("verticalTabSelected");
			else
				td.removeClassName("verticalTabSelected");
		}
	}
	
	private VerticalTabElement getElementByStringRepresentation(String stringRepresentation) {
		return tabElements.stream().filter(tabElement -> stringRepresentation.equals(getTabElementStringRepresentation(tabElement))).findFirst()
				.orElse(null);
	}
	
	private String getTabElementStringRepresentation(VerticalTabElement element) {
		if (element == null)
			return "";
		
		StringBuilder string = new StringBuilder();
		String s = element.getText().replaceAll("[^a-zA-Z0-9]", "");  //RVE - remove special characters
		string.append(s);
		if (element.getDescription() != null)
			string.append(".").append(element.getDescription());
		string.append(".").append(element.getId());
		return string.toString().replaceAll(" ", "").trim();
	}
	
	private void fireElementSelected(VerticalTabElement previousElement) {
		if (tabListeners != null)
			tabListeners.forEach(l -> l.onVerticalTabElementSelected(previousElement, selectedElement));
	}
	
	private void fireElementAddedOrRemoved(boolean added, List<VerticalTabElement> verticalTabElements) {
		if (tabListeners != null) {
			int size = tabElements.size();
			tabListeners.forEach(l -> l.onVerticalTabElementAddedOrRemoved(size, added, verticalTabElements));
		}
	}
	
	private void fireHeightChanged() {
		if (tabListeners != null)
			tabListeners.forEach(l -> l.onHeightChanged(currentHeight));
	}
	
	private void updateHtml() {
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<html><body>");
		if (!tabElements.isEmpty()) {
			if (useHorizontalTabs) {
				htmlString.append("<div class='horizontalTabsContainer'>");
				htmlString.append("<div class='horizontalTabsRow'>");
				tabElements.stream().filter(e -> e.isVisible() && e.isSystemVisible()).forEach(e -> updateHtml(htmlString, e));
				htmlString.append("</div>");
				htmlString.append("<br/></div>");
			} else {
				htmlString.append("<table class='verticalTabItems' cellpadding='0' cellspacing='0' border='0'>");
				tabElements.stream().filter(e -> e.isVisible() && e.isSystemVisible()).forEach(e -> updateHtml(htmlString, e));
				htmlString.append("</table>");
			}
		}
		htmlString.append("</body></html>");
		setHTML(htmlString.toString());
				
		updateHeight();
	}

	private void updateHeight() {
		NodeList<Element> nodes = this.getElement().getElementsByTagName("div");
		if (nodes == null)
			return;
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Element element = nodes.getItem(i);
			if (element.getClassName().contains("horizontalTabsContainer")) {
				int height = element.getClientHeight();
				if (currentHeight != height) {
					currentHeight = height;
					fireHeightChanged();
				}
				break;
			}
		}
	}
	
	private void updateHtml(StringBuilder htmlString, VerticalTabElement element) {
		String stringRepresentation = getTabElementStringRepresentation(element);
		String stringClassElementName = " ";
		boolean isSeparator = false;
		
		if (element.getModelObject() != null) {
			if (element.getModelObject() instanceof ModelAction)
				stringClassElementName = stringClassElementName + ((ModelAction) element.getModelObject()).getName();
			else if (element.getModelObject() instanceof DoubleStateAction) {
			    if (((DoubleStateAction) element.getModelObject()).isDefaultState())
			    	stringClassElementName = stringClassElementName  + " actionStateDefault";
			    else
			    	stringClassElementName = stringClassElementName  + " actionState";
			} else	if (element.getModelObject() instanceof SeparatorAction) {
				stringClassElementName = stringClassElementName  + " verticalTabSeparator";
				isSeparator = true;
			} else if (element.getDescription() != null) {
				stringClassElementName = stringClassElementName  + " "+element.getDescription();
			}
		}
		
		if (useHorizontalTabs) {
			htmlString.append("<div class='horizontalTabsBlock");
			if (element.isStatic())
				htmlString.append(" staticTabBlock");			
			htmlString.append("'>");
			
			htmlString.append("<div id='div").append(stringRepresentation).append("' class='verticalTabItem");
			htmlString.append(" horizontalTabItem");
			if (element.isStatic())
				htmlString.append(" staticTabItem");			
			if (element.isMarked())
				htmlString.append(" verticalTabMarked");			
			if (isSeparator)
				htmlString.append(" verticalTabSeparator");			
			htmlString.append("'>");
						
			ResourcePrototype icon = element.getIcon();
			htmlString.append("<div id='divBox").append(stringRepresentation).append("' class='verticalTabBackgroundBox").append(stringClassElementName).append("'");			
			if (element.getDescription() != null)
				htmlString.append(" title='").append(element.getDescription()).append("'");
			htmlString.append(">");
			if (icon != null && (element.isStatic() || displayIconsForNonStaticElements)) {
				String iconUrl = null;
				if (icon instanceof ImageResource)
					iconUrl = ((ImageResource) icon).getSafeUri().asString();
				else if (icon instanceof SVGResource)
					iconUrl = ((SVGResource) icon).getSafeUri().asString();
				htmlString.append("<a id='icon" + stringRepresentation + "' class='verticalTabElementIcon' style=\"background-image: url('" + iconUrl + "')	\" ></a>");
				htmlString.append("<div class='" + (element.isStatic() ? "tabItemNoLabel" : "tabItemLabel"));
			} else
				htmlString.append("<div class='tabItemLabelNoIcon");
			
			htmlString.append(" tabItemLabelHorizontal");
			htmlString.append("' id='").append(stringRepresentation).append("'");
			//if (element.getDescription() != null)
			//	htmlString.append(" title='").append(element.getDescription()).append("'");
			htmlString.append(">").append((element.getText() != null && !element.getText().equals("")) ? element.getText() : "");
			if (closableItems && !element.isStatic())
				htmlString.append("<a id='anchor").append(stringRepresentation).append("' class='closeHorizontalIcon'></a>");
			htmlString.append("</div>");

			if (element.isAdditionalaShow()) {
				htmlString.append("<div class='verticalTabItemFlowInfo'>");
				htmlString.append("		<span class='verticalTabItemFlowInfoText");
				if (element.getAdditionalClass() != null && !element.getAdditionalClass().isEmpty())
					htmlString.append(" " + element.getAdditionalClass());
				htmlString.append("'>").append(element.getAdditionalText()).append("</span>");			
				htmlString.append("</div>");
			}
			
			htmlString.append("</div>");
			htmlString.append("</div>");
						
			htmlString.append("</div>");
			return;
		}
		
		htmlString.append("<tr>");
		htmlString.append("<td align='right' class='gxtReset verticalTabItem'>");
		htmlString.append("<div id='div").append(stringRepresentation).append("' class='verticalTabItem");
		if (element.isStatic())
			htmlString.append(" staticTabItem");
		if (element.isMarked())
			htmlString.append(" verticalTabMarked");
		htmlString.append("'");
		if (element.getDescription() != null)
			htmlString.append(" title='").append(element.getDescription()).append("'");
		htmlString.append(">");
		
		ResourcePrototype icon = element.getIcon();
		if (icon != null && (element.isStatic() || displayIconsForNonStaticElements)) {
			String iconUrl = null;
			if (icon instanceof ImageResource)
				iconUrl = ((ImageResource) icon).getSafeUri().asString();
			else if (icon instanceof SVGResource)
				iconUrl = ((SVGResource) icon).getSafeUri().asString();
			htmlString.append("<a class='verticalTabElementIcon' style=\"background-image: url('" + iconUrl + "');\"></a>");
			htmlString.append("<div class='tabItemLabel");
		} else
			htmlString.append("<div class='tabItemLabelNoIcon");
		
		htmlString.append("' id='").append(stringRepresentation).append("'");
		//if (element.getDescription() != null)
		//	htmlString.append(" title='").append(element.getDescription()).append("'");
		htmlString.append(">").append(element.getText());
		if (closableItems && !element.isStatic())
			htmlString.append("<a id='anchor").append(stringRepresentation).append("' class='closeIcon'></a>");
		htmlString.append("</div>");
		
		if (element.isAdditionalaShow()) {
			htmlString.append("<div class='verticalTabItemFlowInfo'>");
			htmlString.append("		<span class='verticalTabItemFlowInfoText");
			if (element.getAdditionalClass() != null && !element.getAdditionalClass().isEmpty())
				htmlString.append(" " + element.getAdditionalClass());
			htmlString.append("'>").append(element.getAdditionalText()).append("</span>");			
			htmlString.append("</div>");
		}		
		
		htmlString.append("</div></td>");
		htmlString.append("<tr>");
	}
	
	private OpenerDialog getOpenerDialog() {
		if (openerDialog == null)
			openerDialog = new OpenerDialog();
		
		return openerDialog;
	}
	
	private RenameDialog getRenameDialog() {
		if (renameDialog == null)
			renameDialog = new RenameDialog();
		
		return renameDialog;
	}	
	
	private int getNotStaticElementsSize() {
		int counter = 0;
		for (VerticalTabElement element : tabElements) {
			if (!element.isStatic())
				counter++;
		}
		
		return counter;
	}
	
	public static interface VerticalTabListener {
		void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement);
		void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements);
		void onHeightChanged(int newHeight);
	}
	
	@Override
	public void disposeBean() throws Exception {
		clearElements();
		tabElements = null;
		
		listFolder.clear();
		//mapImageResourceFolder.clear();
		//mapDescriptionFolder.clear();
		
		if (tabListeners != null) {
			tabListeners.clear();
			tabListeners = null;
		}
		
		if (workbenchActionContextFolderTabElements != null) {
			workbenchActionContextFolderTabElements.clear();
			workbenchActionContextFolderTabElements = null;
		}
		
		if (tabsWithReloadPending != null) {
			tabsWithReloadPending.clear();
			tabsWithReloadPending = null;
		}
		
		dropTarget.release();
	}
	
    public void clearElements() {
		if (tabElements != null) {
			tabElements.forEach(el -> disposeElement(el));
			tabElements.clear();
		}    	
    }
	
	private void disposeElement(VerticalTabElement element) {
		if (reuseWorkbenchActionContextTabElement && workbenchActionContextFolderTabElements != null) {
			WorkbenchActionContext<?> workbenchActionContext = element.getWorkbenchActionContext();
			if (workbenchActionContext != null)
				workbenchActionContextFolderTabElements.remove(workbenchActionContext.getFolder());
		}
		
		Widget widget = element.getWidgetIfSupplied();
		if (widget instanceof DisposableBean) {
			try {
				((DisposableBean) widget).disposeBean();
			} catch (Exception e) {
				ErrorDialog.show("Error while disposing element.", e);
				e.printStackTrace();
			}
		}
	}

	private void updateElementsFromFolder () {
		//RVE - update state from folder and refresh element order
		List<VerticalTabElement> listElements = new ArrayList<>();
		listElements.addAll(tabElements);
		tabElements.clear();
		
		/*
		for (Folder folder : listFolder) {
			String elementName = folder.getName();
			for (VerticalTabElement element : listElements) {
				if (element.getName().equals(elementName)) {
					tabElements.add(element);
					updateElementFromFolder(element, folder);
					break;
				}
			}
		}
		*/
		
		listElements.forEach(el -> updateElementFromFolders(el));
			
		for (VerticalTabElement element : listElements) {	
			if (!tabElements.contains(element))
				tabElements.add(element);
			
			/*
			if (this.rootFolder != null) {
				//Only update visibility if the element was already visible
				element.setVisible(element.isVisible() ? listFolder.contains(element.getName()) : false);
			}
			*/
						
			//updateElement(element);
		}
		updateHtml();
	}
	
	private void updateFolderList(Folder rootFolder) {
		listFolder.clear();
		String folderParentName = rootFolder.getName();
		
		for (Folder subFolder : rootFolder.getSubFolders()) {
			if (subFolder.getName() == null)
				continue;
			
			if (subFolder.getSubFolders() != null && !subFolder.getSubFolders().isEmpty()) {
				folderParentName =  subFolder.getName();
				if (folderParentName.contains("$") && !folderParentName.equals(this.parentUseCase))
					listFolder.add(subFolder);
				if (folderParentName.equals(this.parentUseCase) || this.parentUseCase == null || this.parentUseCase.isEmpty())
					subFolder.getSubFolders().forEach(sf -> listFolder.add(sf));
			} else if (folderParentName.equals(this.parentUseCase) || this.parentUseCase == null || this.parentUseCase.isEmpty())
				listFolder.add(subFolder); 
		}		
	}
		
	private void updateElementFromFolders(VerticalTabElement element) {
        if (rootFolder == null || element == null ||  rootFolder.getSubFolders() == null)
            return;
		
        boolean elementIsVisible = false;
        boolean useCaseExists = false;
        String elementName = element.getName().replace("$", "");
        
		String folderParentName = rootFolder.getName();
		for (Folder subFolder : rootFolder.getSubFolders()) {
			if (subFolder.getName() == null)
				continue;
			
			if (subFolder.getSubFolders() != null && !subFolder.getSubFolders().isEmpty()) {
				folderParentName =  subFolder.getName();
				for (Folder subFolder2 : subFolder.getSubFolders()) {
					if (!folderParentName.equals(parentUseCase) && parentUseCase != null && !parentUseCase.isEmpty())
						continue;
					
					useCaseExists = true;
					if (!subFolder.getName().replace("$", "").equals(elementName) && !subFolder2.getName().replace("$", "").equals(elementName))
						continue;
					
					elementIsVisible = true;
					if (element.getModelObject() == null ) {
						updateElementFromFolder(element, subFolder2);
						continue;
					}
					
					if (element.getModelObject() instanceof DoubleStateAction && folderParentName.contains("$"))
						updateDoubleStateActionFromFolder((DoubleStateAction) element.getModelObject(),subFolder, subFolder2);
					else if (element.getModelObject() instanceof ModelAction)
						updateModelActionFromFolder((ModelAction) element.getModelObject(), subFolder2);
				}							
			} else if (folderParentName.equals(parentUseCase) || parentUseCase == null || parentUseCase.isEmpty()) {
				useCaseExists = true;
				if (subFolder.getName().replace("$", "").equals(elementName)) {
					elementIsVisible = true;
					if (element.getModelObject() == null)
						updateElementFromFolder(element, subFolder);
					else if (element.getModelObject() instanceof ModelAction)
			        	updateModelActionFromFolder((ModelAction) element.getModelObject(), subFolder);
				}
			}
		}
		
		if (element.isStatic() && element.isSystemConfigurable())
			element.setSystemVisible(!useCaseExists || (useCaseExists && elementIsVisible));
	}
	
	private PersistenceGmSession getWorkbenchSession() {
		if (workbenchSession != null)
			return workbenchSession;
		
		if (verticalTabManager != null)
			workbenchSession = verticalTabManager.getWorkbenchSession();
		
		return workbenchSession;
	}

	private void updateElementFromFolder(VerticalTabElement element, Folder folder) {
		String folderDisplayName = I18nTools.getDefault(folder.getDisplayName(), "").toString();
		if (folderDisplayName != null && !folderDisplayName.isEmpty())
		    element.setDescription(folderDisplayName);
		Resource resource = GMEIconUtil.getMediumImageFromIcon(folder.getIcon());
		PersistenceGmSession workbenchSession = getWorkbenchSession();
		if (resource != null && workbenchSession != null)
			element.setIcon(new GmImageResource(resource, workbenchSession.resources().url(resource).asString()));
	}
	
	private void updateModelActionFromFolder(ModelAction action, Folder subFolder) {
		String folderDisplayName = I18nTools.getDefault(subFolder.getDisplayName(), "").toString();
		if (folderDisplayName != null && !folderDisplayName.isEmpty())
			action.setTooltip(folderDisplayName);
		Resource resource = GMEIconUtil.getMediumImageFromIcon(subFolder.getIcon());
		PersistenceGmSession workbenchSession = getWorkbenchSession();
		if (resource != null && workbenchSession != null)
			action.setIcon(new GmImageResource(resource, workbenchSession.resources().url(resource).asString()));
		
		if (action instanceof ExchangeContentViewAction) {
			((ExchangeContentViewAction) action).setUseOptions(subFolder.getTags().contains(((ExchangeContentViewAction) action).getOptionTag()));
			((ExchangeContentViewAction) action).setUseCondensation(subFolder.getTags().contains(((ExchangeContentViewAction) action).getConensationTag()));
		}
	}

	private void updateDoubleStateActionFromFolder(DoubleStateAction action, Folder subFolder1, Folder subFolder2) {
		String folderDisplayName1 = I18nTools.getDefault(subFolder1.getDisplayName(), "").toString();
		if (folderDisplayName1 != null && !folderDisplayName1.isEmpty())
			action.setStateDescription1(folderDisplayName1);
		String folderDisplayName2 = I18nTools.getDefault(subFolder2.getDisplayName(), "").toString();
		if (folderDisplayName2 != null && !folderDisplayName2.isEmpty())
			action.setStateDescription2(folderDisplayName2);

		
		PersistenceGmSession workbenchSession = getWorkbenchSession();
		if (workbenchSession != null) {
			ImageResource imageResource = null;
			Resource resource = GMEIconUtil.getMediumImageFromIcon(subFolder1.getIcon());
			if (resource != null) {
				imageResource = new GmImageResource(resource, workbenchSession.resources().url(resource).asString());
				action.setStateIcon1(imageResource);
			}			
			
			resource = GMEIconUtil.getMediumImageFromIcon(subFolder2.getIcon());
			if (resource != null) {
				imageResource = new GmImageResource(resource, workbenchSession.resources().url(resource).asString());
				action.setStateIcon2(imageResource);
			}
		}
		
		action.updateState();
	}
	
	private VerticalTabElement getVerticalTabElement(Element target) {
		Element targetElement = Element.as(target);
		String id = targetElement.getId();
		if (id.startsWith("anchor"))
		    id = id.replaceFirst("anchor", "");
		if (id.startsWith("icon"))
		    id = id.replaceFirst("icon", "");
		id = id.replaceAll(" ", "").trim();
		
		return getElementByStringRepresentation(id);
	}
	
	private Timer getDragEnterTimer() {
		if (dragEnterTimer != null)
			return dragEnterTimer;
		
		dragEnterTimer = new Timer() {
			@Override
			public void run() {
				if (dragElement == null)
					return;
				
				VerticalTabElement element = getVerticalTabElement(dragElement);
				if (element != null)
					setSelectedVerticalTabElement(element);
			}
		};
		
		return dragEnterTimer;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void onUpdateAdditonalText(HasAdditionalText parent) {
		boolean update = false;
		for (VerticalTabElement element : tabElements) {
			if (element == null || element.getWidgetIfSupplied() == null)
				continue;
			
			if (element.getWidgetIfSupplied().equals(parent)) {
				update = true;
				element.setAdditionalaShow(parent.showAdditionalText());
				element.setAdditionalText(parent.getAdditionalText());
				element.setAdditionalClass(parent.getAdditionalClass());
			}
		}
		
		if (update)
			updateHtml();	
	}

}
