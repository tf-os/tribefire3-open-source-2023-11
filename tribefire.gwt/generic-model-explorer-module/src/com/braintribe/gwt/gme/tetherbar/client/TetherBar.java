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
package com.braintribe.gwt.gme.tetherbar.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.gwt.gme.tetherbar.client.resources.LocalizedText;
import com.braintribe.gwt.gme.tetherbar.client.resources.TetherBarResources;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.AbstractImagePrototype.ImagePrototypeElement;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * Panel that will display {@link TetherBarElement}(s), in a BreadCrumb style.
 * @author michel.docouto
 *
 */
public class TetherBar extends HTML implements InitializableBean, DisposableBean {
	private static final Logger logger = new Logger(TetherBar.class);
	
	static {
		TetherBarResources.INSTANCE.css().ensureInjected();
	}
	
	private List<TetherBarElement> tetherBarElements;
	private Set<TetherBarListener> tetherBarListeners;
	private TetherBarElement selectedElement;
	private boolean clickable = true;
	private TetherBarElement tetherElementToMove;
	private ImagePrototypeElement moveActionImage;
	private int maxTetherBarElements = -1;
	private Menu menu;
	private Timer dragEnterTimer;
	private Element dragElement;
	private DropTarget dropTarget;
	
	public TetherBar() {
		tetherBarElements = new ArrayList<>();
		
		addClickHandler(event -> {
			if (!clickable)
				return;
			
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (!Element.is(target))
				return;
			
			Element targetElement = Element.as(target);
			TetherBarElement tetherBarElement = getElementByStringRepresentation(targetElement.getId());
			if (tetherBarElement != null)
				setSelectedTetherBarElement(targetElement.getId(), tetherBarElement);
		});
	}
	
	/**
	 * Configures whether the {@link TetherBarElement}s added to the {@link TetherBar} are clickable.
	 * Defaults to true.
	 */
	@Configurable
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}
	
	public void addTetherBarElement(TetherBarElement element) {
		insertTetherBarElement(tetherBarElements.size(), element);
	}
	
	/**
	 * Configures how many {@link TetherBarElement}s show in row
	 * Defaults to -1 (no limit)
	 */
	@Configurable
	public void setMaxTetherBarElements(int maxTetherBarElements) {
		this.maxTetherBarElements = maxTetherBarElements;
	}
	
	public void insertTetherBarElement(int index, TetherBarElement element) {
		List<TetherBarElement> removedElements = new ArrayList<>();
		while (tetherBarElements.size() > index)
			removedElements.add(tetherBarElements.remove(tetherBarElements.size() - 1));
		
		if (!removedElements.isEmpty())
			fireTetherBarElementsRemoved(removedElements);
		tetherBarElements.add(element);
		fireTetherBarElementAdded(element);
		updateHtml();
	}
	
	/**
	 * Removes all the elements from the TetherBar, from this index on.
	 */
	public void removeTetherBarElementsFrom(int index) {
		if (tetherBarElements.size() <= index)
			return;
		
		List<TetherBarElement> subList = tetherBarElements.subList(index, tetherBarElements.size());
		List<TetherBarElement> elementsToRemove = new ArrayList<>(subList);
		removeTetherBarElements(elementsToRemove);
	}
	
	public void removeTetherBarElements(List<TetherBarElement> elements) {
		tetherBarElements.removeAll(elements);
		fireTetherBarElementsRemoved(elements);
		updateHtml();
		
		disposeElements(elements);
	}
	
	public int getElementsSize() {
		return tetherBarElements.size();
	}
	
	public void clearTetherBarElements() {
		if (!tetherBarElements.isEmpty()) {
			fireTetherBarElementsRemoved(tetherBarElements);
			tetherBarElements.clear();
			updateHtml();
		}
	}
	
	public boolean selectLastTetherBarElement() {
		if (tetherBarElements.isEmpty())
			return false;
		
		setSelectedThetherBarElement(tetherBarElements.get(tetherBarElements.size() - 1));
		return true;
	}
	
	public void setSelectedThetherBarElement(TetherBarElement element) {
		setSelectedTetherBarElement(getTetherBarElementStringRepresentation(element), element);
	}
	
	@Override
	public void intializeBean() throws Exception {
		if (!clickable)
			return;
		
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
	
	private void setSelectedTetherBarElement(String id, TetherBarElement element) {
		NodeList<Element> divs = this.getElement().getElementsByTagName("div");
		for (int i = 0; i < divs.getLength(); i++) {
			Element div = divs.getItem(i);
			if (id.equals(div.getId()))
				div.addClassName("tetherSelectedElement");
			else
				div.removeClassName("tetherSelectedElement");
		}
		
		selectedElement = element;
		fireElementSelected();
	}
	
	private void addMoveActionImageToLastElement() {
		if (!clickable || tetherBarElements.size() <= 1)
			return;
		
		TetherBarElement element = tetherBarElements.get(tetherBarElements.size() - 1);
		
		String id = getTetherBarElementStringRepresentation(element);
		NodeList<Element> divs = this.getElement().getElementsByTagName("div");
		for (int i = 0; i < divs.getLength(); i++) {
			Element div = divs.getItem(i);
			if (id.equals(div.getId())) {
				div.appendChild(getMoveActionImage());
				tetherElementToMove = element;
				break;
			}
		}
	}
	
	public void addTetherBarListener(TetherBarListener tetherBarListener) {
		if (tetherBarListeners == null)
			tetherBarListeners = new LinkedHashSet<>();
		tetherBarListeners.add(tetherBarListener);
	}
	
	public void removeTetherBarListener(TetherBarListener tetherBarListener) {
		if (tetherBarListeners != null) {
			tetherBarListeners.remove(tetherBarListener);
			if (tetherBarListeners.isEmpty())
				tetherBarListeners = null;
		}
	}
	
	/**
	 * Returns the currently selected {@link TetherBarElement}.
	 */
	public TetherBarElement getSelectedElement() {
		return selectedElement;
	}
	
	/**
	 * Returns the index of the currently selected {@link TetherBarElement}.
	 */
	public int getSelectedElementIndex() {
		return tetherBarElements.indexOf(selectedElement);
	}
	
	/**
	 * Returns all {@link TetherBarElement}s.
	 */
	public List<TetherBarElement> getTetherBarElements() {
		return tetherBarElements;
	}
	
	/**
	 * Returns the index of the given element, or -1 if the element is not in the {@link TetherBar}.
	 */
	public int getIndexOfElement(TetherBarElement element) {
		return tetherBarElements.indexOf(element);
	}
	
	/**
	 * Returns the {@link TetherBarElement} at the given index, or null if there is no element at the given index.
	 */
	public TetherBarElement getElementAt(int index) {
		if (tetherBarElements.size() > index && index >= 0)
			return tetherBarElements.get(index);
		return null;
	}
	
	/**
	 * Checks if the {@link TetherBar} contains a {@link TetherBarElement} with the given {@link ModelPathElement}.
	 */
	public boolean containsElement(ModelPathElement modelPathElement) {
		return getTetherBarElementByModelPathElement(modelPathElement) != null;
	}
	
	/**
	 * Returns the {@link TetherBarElement} for the given {@link ModelPathElement}.
	 */
	public TetherBarElement getTetherBarElementByModelPathElement(ModelPathElement modelPathElement) {
		return getTetherBarElementByModelPathElement(modelPathElement, false);
	}
	
	/**
	 * Returns the {@link TetherBarElement} for the given {@link ModelPathElement}.
	 */
	public TetherBarElement getTetherBarElementByModelPathElement(ModelPathElement modelPathElement, boolean compareValueOnly) {
		return tetherBarElements.stream()
				.filter(element -> element.getModelPathElement() != null
						&& (compareValueOnly ? element.getModelPathElement().getValue().equals(modelPathElement.getValue())
								: element.getModelPathElement().equals(modelPathElement)))
				.findFirst().orElse(null);
	}
	
	private TetherBarElement getElementByStringRepresentation(String stringRepresentation) {
		String escapedStringRepresentation = SafeHtmlUtils.htmlEscape(stringRepresentation);
		for (TetherBarElement tetherBarElement : tetherBarElements) {
			if (escapedStringRepresentation.equals(getTetherBarElementStringRepresentation(tetherBarElement)))
				return tetherBarElement;
		}
		
		if (!escapedStringRepresentation.contains("."))
			return null;
		
		escapedStringRepresentation = escapedStringRepresentation.substring(0, escapedStringRepresentation.lastIndexOf("."));
		for (TetherBarElement tetherBarElement : tetherBarElements) {
			ModelPathElement modelPathElement = tetherBarElement.getModelPathElement();
			if (modelPathElement == null)
				continue;
			
			GenericModelType type = modelPathElement.getType();
			if (type.isCollection() && ((CollectionType) type).getCollectionKind().equals(CollectionKind.set)) {
				String tetherElementStringRepresentation = getTetherBarElementStringRepresentation(tetherBarElement);
				if (tetherElementStringRepresentation.contains(".") && escapedStringRepresentation
						.equals(tetherElementStringRepresentation.substring(0, tetherElementStringRepresentation.lastIndexOf(".")))) {
					return tetherBarElement;
				}
			}
		}
		
		return null;
	}
	
	public TetherBarElement getElementByView(GmContentView view) {
		return tetherBarElements.stream().filter(element -> element.getContentViewIfProvided() == view).findFirst().orElse(null);
	}
	
	private void updateHtml() {
		StringBuilder htmlString = new StringBuilder();
		htmlString.append("<html><body>");
		if (tetherBarElements.isEmpty()) {
			htmlString.append("</body></html>");
			setHTML(htmlString.toString());
			addMoveActionImageToLastElement();
			return;
		}
		
		htmlString.append("<ul class='gxtReset tetherElement");
		if (!clickable)
			htmlString.append(" tetherElementUnselectable");
		htmlString.append("'>");
		
		boolean addNextItemToMenu = false;
		boolean createNextItemAsMenu = false;
		boolean useMenu = (maxTetherBarElements > 0 && tetherBarElements.size() > maxTetherBarElements);
		if (useMenu && clickable)
			prepareMenu();			
		
		String  menuDescription = "";
		
		for (int i = 0; i < tetherBarElements.size()/* - 1*/; i++) {
			TetherBarElement element = tetherBarElements.get(i);
			String id = getTetherBarElementStringRepresentation(element);
			String description = element.getDescription();
			String name = element.getName();
			String className = "";
			
			if (i == 0)
				className = "tetherFirstElement";
			if (i == tetherBarElements.size()-1)
				className = className + " tetherLastElement";  //can be only one element, than have both class
			if (className.isEmpty())
				className = "tetherMiddleElement";
			
			boolean showAtMenu = false;
			boolean createTetherMenuItem = false;
			if (useMenu) {
				int val = (addNextItemToMenu) ? 1 : 2;
				showAtMenu = (i > 0 && i < ( tetherBarElements.size() - val));
				createTetherMenuItem = (i + 1 == tetherBarElements.size() - val) || createNextItemAsMenu;
				
				createNextItemAsMenu = false;					
				if (element == selectedElement) {
					showAtMenu = false;
					addNextItemToMenu = true;
					if (createTetherMenuItem) {
						createTetherMenuItem = false;
						createNextItemAsMenu = true;
					}
				} 
			}
			
			if (showAtMenu) {
				if (clickable) {
				    MenuItem menuItem;
					ImageResource icon =  new ImageResourcePrototype(name, TetherBarResources.INSTANCE.arrowTetherSvg().getSafeUri(), 0, 0, 24, 24, false, false);
					menuItem = new MenuItem();
					menuItem.setId(id);
					menuItem.setText(name);
					menuItem.setTitle(description);
					menuItem.setIcon(icon);
					menuItem.setData("item", element);
					menu.add(menuItem);
				}					
				
				if (!menuDescription.isEmpty() && !name.isEmpty())
					menuDescription = menuDescription + "&#10;";
				menuDescription = menuDescription + SafeHtmlUtils.htmlEscape(name);
				description = menuDescription;
				
				if (!createTetherMenuItem)
					continue;					
				
				name = "...";
				id = SafeHtmlUtils.htmlEscape("tetherBarElementGroup");
			} else 
				description = SafeHtmlUtils.htmlEscape(description);
			
			htmlString.append("<li class='").append(className).append("'><div id='").append(id).append("' class='").append(className);
			if (selectedElement == element)
				htmlString.append(" tetherSelectedElement");
			htmlString.append("'");				
			
			if (element.getDescription() != null) {
				//RVE - need differ for SafeHtmlUtils.htmlEscape - for group title - need line separator ("&#13;")
				//htmlString.append(" title='").append(SafeHtmlUtils.htmlEscape(description)).append("'");
				htmlString.append(" title='").append(description).append("'");
			}
			htmlString.append(">").append(SafeHtmlUtils.htmlEscape(name)).append("</div></li>");
		}
		
		htmlString.append("</ul>");
		htmlString.append("</body></html>");
		setHTML(htmlString.toString());
		
		addMoveActionImageToLastElement();
	}
	
	private void prepareMenu() {
		menu = new Menu();
		menu.addSelectionHandler(event -> {
			Item item = event.getSelectedItem();
			Object object = item.getData("item");
			if (object != null) {
				selectedElement = (TetherBarElement) object;
				fireElementSelected();
				updateHtml();
			}
		});
	}
	
	private String getTetherBarElementStringRepresentation(TetherBarElement element) {
		StringBuilder string = new StringBuilder();
		string.append(element.getName());
		if (element.getDescription() != null)
			string.append(".").append(element.getDescription());
		
		if (element.getModelPathElement() != null)
			string.append(".").append(element.getModelPathElement().hashCode());
		
		return SafeHtmlUtils.htmlEscape(string.toString());
	}
	
	private ImagePrototypeElement getMoveActionImage() {
		if (moveActionImage != null)
			return moveActionImage;
		
		moveActionImage = AbstractImagePrototype.create(TetherBarResources.INSTANCE.moveUp()).createElement();
		moveActionImage.setClassName(TetherBarResources.INSTANCE.css().moveTetherActionStyle());
		Event.sinkEvents(moveActionImage, Event.ONCLICK);
		Event.setEventListener(moveActionImage, event -> {
			ModelPath modelPath = tetherElementToMove.getModelPath();
			removeTetherBarElements(Collections.singletonList(tetherElementToMove));
			if (!tetherBarElements.isEmpty())
				setSelectedThetherBarElement(tetherBarElements.get(tetherBarElements.size() - 1));
			fireModelPathNavigation(modelPath);
		});
		moveActionImage.setTitle(LocalizedText.INSTANCE.moveToTab());
		
		return moveActionImage;
	}
	
	private void fireModelPathNavigation(ModelPath modelPath) {
		ModelPathNavigationListener listener = GMEUtil.getModelPathNavigationListener(getParent());
		if (listener != null)
			listener.onOpenModelPath(modelPath);
	}
	
	private void fireElementSelected() {
		if (tetherBarListeners != null)
			tetherBarListeners.forEach(l -> l.onTetherBarElementSelected(selectedElement));
	}
	
	private void fireTetherBarElementAdded(TetherBarElement element) {
		if (tetherBarListeners != null)
			tetherBarListeners.forEach(l -> l.onTetherBarElementAdded(element));
	}
	
	private void fireTetherBarElementsRemoved(List<TetherBarElement> elements) {
		if (tetherBarListeners != null)
			tetherBarListeners.forEach(l -> l.onTetherBarElementsRemoved(elements));
	}
	
	private Timer getDragEnterTimer() {
		if (dragEnterTimer != null)
			return dragEnterTimer;
		
		dragEnterTimer = new Timer() {
			@Override
			public void run() {
				if (dragElement == null)
					return;
				
				TetherBarElement element = getElementByStringRepresentation(dragElement.getId());
				if (element != null)
					setSelectedThetherBarElement(element);
			}
		};
		
		return dragEnterTimer;
	}
	
	public static interface TetherBarListener {
		void onTetherBarElementSelected(TetherBarElement tetherBarElement);
		void onTetherBarElementAdded(TetherBarElement tetherBarElementAdded);
		void onTetherBarElementsRemoved(List<TetherBarElement> tetherBarElementsRemoved);
	}
	
	@Override
	public void disposeBean() throws Exception {
		if (tetherBarListeners != null) {
			tetherBarListeners.clear();
			tetherBarListeners = null;
		}
		
		if (tetherBarElements != null) {
			disposeElements(tetherBarElements);
			tetherBarElements.clear();
		}
		
		if (dropTarget != null)
			dropTarget.release();
	}
	
	private void disposeElements(Collection<TetherBarElement> elements) {
		Scheduler.get().scheduleDeferred(() -> {
			try {
				for (TetherBarElement element : elements) {
					GmContentView view = element.getContentViewIfProvided();
					if (view instanceof DisposableBean)
						((DisposableBean) view).disposeBean();
				}
			} catch (Exception e) {
				logger.error("Error while disposing view.", e);
				e.printStackTrace();
			}
		});
	}

}
