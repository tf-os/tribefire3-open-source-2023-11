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
package com.braintribe.gwt.thumbnailpanel.client;

import java.util.Collections;
import java.util.List;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmMouseInteractionEvent;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.model.generic.path.ModelPath;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;

/**
 * View implementation for the {@link ThumbnailPanel}.
 */
public class ThumbnailListView extends ListView<ImageResourceModelData, ImageResourceModelData> {
	//private static final Logger logger = new Logger(ThumbnailListView.class);
	
	private ThumbnailPanel thumbnailPanel;
	private Timer windowChangedTimer;
	private Touch touchDown;
	private int downX;
	private int downY;
	private boolean isTouchMoving;
	private int scrollTop;
	private int scrollLeft;
	private Touch touchMove;
	private int clickCount;
	private long clickStartTime;
	private final long dblClickTimeOut = 250;
	private final int minMovingDistance = 10;

	//http://www.sencha.com/examples/#ExamplePlace:listview - Sencha listView example
	public ThumbnailListView(ThumbnailPanel thumbnailPanel, ListStore<ImageResourceModelData> store,
			ValueProvider<? super ImageResourceModelData, ImageResourceModelData> valueProvider,
			ListViewAppearance<ImageResourceModelData> appearance) {
		super(store, valueProvider, appearance);
		this.thumbnailPanel = thumbnailPanel;
		
		setSelectionModel(new ThumbnailListViewSelectionModel<ImageResourceModelData>(thumbnailPanel) {
			@Override
			protected boolean isInput(Element target) {
				//TODO: WHY was this try/catch added here??? Please avoid creating stuff like this!! At least, add a comment...
				try {
					return super.isInput(target);
				} catch(Exception ex) {
					return true;
				}
			}
		});
		
		setBorders(false);
		setLoadingIndicator("...loading images...");
		setStyleName("thumbnailPanelContainer");
		Style style = getElement().getStyle();
		style.setOverflowX(Overflow.HIDDEN);
		style.setOverflowY(Overflow.AUTO);
		setCell(new ThumbCustomImageListCell(thumbnailPanel));
		
		addDomHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
				selectNextVerticalItem(true, false, true);
				event.stopPropagation();
				event.preventDefault();
			}
			
			if (event.getNativeKeyCode() == KeyCodes.KEY_UP) {
				selectNextVerticalItem(false, false, true);
				event.stopPropagation();
				event.preventDefault();
			}
		}, KeyDownEvent.getType());
		
		addDomHandler(event -> fireWindowChanged(), ScrollEvent.getType());
		addDomHandler(event -> fireClickOrDoubleClick(false, new ThumbnailPanelMouseInteractionEvent(event, thumbnailPanel)), DoubleClickEvent.getType());
		
		addDomHandler(event -> {
			thumbnailPanel.clickX = event.getClientX();
			thumbnailPanel.clickY = event.getClientY();
			new Timer() {
				@Override
				public void run() {
					focus();
				}
			}.schedule(100); //We need this timer to set focus
		}, MouseDownEvent.getType());
		
		getSelectionModel().addSelectionChangedHandler(event -> {
			Scheduler.get().scheduleDeferred(() -> {
			//new Timer() {
			//	@Override
			//	public void run() {
					thumbnailPanel.selectedModel = null;
					List<ImageResourceModelData> selectedModels = event.getSelection();
					if (selectedModels != null && !selectedModels.isEmpty())
						thumbnailPanel.selectedModel = selectedModels.get(0);
					
					if (thumbnailPanel.selectedModel != null && thumbnailPanel.selectedModel.isGroupModel()) 
						fireClickGroupModel(thumbnailPanel.selectedModel);
					else {					
						if (thumbnailPanel.selectedModel != null)
							fireClickOrDoubleClick(true, new ThumbnailPanelSelectionEvent(event, thumbnailPanel));
					}
					thumbnailPanel.fireGmSelectionListeners();
					
					if (thumbnailPanel.unselectAfterClick)
						Scheduler.get().scheduleDeferred(getSelectionModel()::deselectAll);
			//	}
			//}.schedule(200); //We need this timer because sometimes the double click is not fired due to the operations here
			});		
		});
		
//		addListener(Events.Refresh, new Listener<ListViewEvent<ImageResourceModelData>>() {
//			public void handleEvent(ListViewEvent<ImageResourceModelData> be) {
//				updateModelsVisibility();
//			}
//		});
		
		addAttachHandler(event -> {
			if (event.isAttached())
				fireWindowChanged();
		}); 			
		
//		addListener(Events.Collapse, new Listener<ListViewEvent<ImageResourceModelData>>() {
//			public void handleEvent(ListViewEvent<ImageResourceModelData> be) {
//				fireWindowChanged();
//			}
//		});			
//		addListener(Events.Expand, new Listener<ListViewEvent<ImageResourceModelData>>() {
//			public void handleEvent(ListViewEvent<ImageResourceModelData> be) {
//				fireWindowChanged();
//			}
//		});
		
		addDomHandler(event -> {
			Touch touch = event.getTouches().get(0);
			touchDown = touch;
			downX = touch.getScreenX();
			downY = touch.getScreenY();
			isTouchMoving = false;
//					NativeEvent ne = Document.get().createMouseOverEvent(0, touch.getScreenX(), touch.getScreenY(), touch.getClientX(), touch.getClientY(), 
//							false, false, false, false,  event.getTouches().length() > 1 ? NativeEvent.BUTTON_RIGHT :  NativeEvent.BUTTON_LEFT, event.getRelativeElement());
//					DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());
//					
//					ne = Document.get().createMouseDownEvent(0, touch.getScreenX(), touch.getScreenY(), touch.getClientX(), touch.getClientY(), 
//							false, false, false, false,  event.getTouches().length() > 1 ? NativeEvent.BUTTON_RIGHT :  NativeEvent.BUTTON_LEFT);
//					DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());					
			
			event.preventDefault();
			
			XElement element = getElement();
			scrollTop = element.getScrollTop();
			scrollLeft = element.getScrollLeft();
			
			//logger.debug("onTouchStart " + touch.getClientX() + " " + touch.getClientY() + " " + touch.getScreenX() + " " + touch.getScreenY() + " "
					//+ touch.getPageX() + " " + touch.getPageY());
			
		}, TouchStartEvent.getType());
		
		addDomHandler(event -> {
			Touch touch = isTouchMoving ? touchMove : touchDown;
			
//					NativeEvent ne = Document.get().createMouseUpEvent(0, touch.getScreenX(), touch.getScreenY(), touch.getClientX(), touch.getClientY(), 
//							false, false, false, false,  event.getTouches().length() > 1 ? NativeEvent.BUTTON_RIGHT :  NativeEvent.BUTTON_LEFT);
//					DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());
							
			if (!isTouchMoving) {
				clickCount++;
				//logger.debug("clickCount " + clickCount);
				boolean fireClick = true;
				
				long newTime = System.currentTimeMillis() - clickStartTime;
				clickStartTime = System.currentTimeMillis();
				
				if (newTime <= dblClickTimeOut) {
					clickStartTime = 0;
					fireClick = false;
				}
				
				for (Element e : getElements()) {
					Rect eRect = new Rect(e.getAbsoluteLeft(), e.getAbsoluteTop(), e.getOffsetWidth(), e.getOffsetHeight());
					Rect tRect = new Rect(touch.getScreenX(), touch.getScreenY(), 1, 1);
					if (eRect.intersect(tRect) != null) {
						//logger.debug("isect " + e.getString());
						int index = findElementIndex(e);
						getSelectionModel().select(index, false);
					}
				}
				
				fireClickOrDoubleClick(fireClick, new ThumbnailPanelMouseInteractionEvent(event, thumbnailPanel));
			}
			
//					ne = Document.get().createMouseOutEvent(0, touch.getScreenX(), touch.getScreenY(), touch.getClientX(), touch.getClientY(), 
//							false, false, false, false,  event.getTouches().length() > 1 ? NativeEvent.BUTTON_RIGHT :  NativeEvent.BUTTON_LEFT, event.getRelativeElement());
//					DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());
			
			//logger.debug("onTouchEnd " + touch.getClientX() + " " + touch.getClientY() + " " + touch.getScreenX() + " " + touch.getScreenY() + " "
				//	+ touch.getPageX() + " " + touch.getPageY() + " ");
			
			event.preventDefault();
		}, TouchEndEvent.getType());
		
		addDomHandler(event -> event.preventDefault(), TouchCancelEvent.getType());
		
		addDomHandler(event -> {
			Touch touch = event.getTouches().get(0);
			touchMove = touch;
			
			int xDist = touchMove.getScreenX() - downX;
			int yDist = touchMove.getScreenY() - downY;
			
//					logger.log(Level.SEVERE, "xDist " + xDist + " yDist " + yDist);
			
			isTouchMoving = Math.abs(xDist) >= minMovingDistance || Math.abs(yDist) >= minMovingDistance;
			
			if (isTouchMoving) {
//						NativeEvent ne = Document.get().createMouseOverEvent(0, touch.getScreenX(), touch.getScreenY(), touch.getClientX(), touch.getClientY(), 
//								false, false, false, false,  event.getTouches().length() > 1 ? NativeEvent.BUTTON_RIGHT :  NativeEvent.BUTTON_LEFT, event.getRelativeElement());
//						DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());
//						
//						ne = Document.get().createMouseMoveEvent(0, touch.getScreenX(), touch.getScreenY(), touch.getClientX(), touch.getClientY(), 
//								false, false, false, false,  event.getTouches().length() > 1 ? NativeEvent.BUTTON_RIGHT :  NativeEvent.BUTTON_LEFT);
//						DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());	
//						
//						ne = Document.get().createScrollEvent();
//						DomEvent.fireNativeEvent(ne, (HasHandlers) event.getSource(), event.getRelativeElement());
				
				getElement().setScrollLeft(scrollLeft - xDist);
				getElement().setScrollTop(scrollTop - yDist);
			}
			
			//logger.debug("onTouchMove " + touch.getClientX() + " " + touch.getClientY() + " " + touch.getScreenX() + " " + touch.getScreenY() + " "
				//	+ touch.getPageX() + " " + touch.getPageY() + " " + xDist + " " + yDist);
			
//					event.preventDefault();
		}, TouchMoveEvent.getType());
		
	}

	public boolean selectNextHorizontalItem(boolean useNext, boolean keepExisting, boolean useFocusItem) {
		ImageResourceModelData model = getSelectionModel().getSelectedItem();
		if (model == null)
			return false;
		
		int itemId = getStore().indexOf(model);
		
		if (useNext)
		    itemId = itemId + 1;
		else
		    itemId = itemId - 1;
		
		getSelectionModel().select(itemId, keepExisting); // .select(nextElement.getId(), false); //RVE maybe nextElement.getTabIndex()
		if (useFocusItem)
			focusItem(itemId);
		return true;		
	}
	
	public boolean selectNextVerticalItem(boolean useDown, boolean keepExisting, boolean useFocusItem) {
		ImageResourceModelData model = getSelectionModel().getSelectedItem();
		if (model == null) 
			return false;
		
		int itemId = getStore().indexOf(model);
		
		/*
		if (useDown)
		    itemId = itemId - 1;
		else
		    itemId = itemId + 1;
		    */
			
		XElement itemElement = getElement(itemId);
		if (itemId < 0 || itemElement == null) 
			return false;
			
		int iSearch = 0;
		
		while (iSearch < 3) {
			
			//RVE - 1st try find element from the center - Need when for example 1st line have 4 objects, 2nd line just 3 objects and are centered
			//RVE - 2nd try find element from the begin
			//RVE - 3nd try find element from the end
			int left;
			int newTop = itemElement.getOffsetTop() + Math.round(itemElement.getOffsetHeight()/2);
			
			if (iSearch == 0) 
				left = itemElement.getOffsetLeft() + Math.round(itemElement.getOffsetWidth()/2);		
			else if (iSearch == 1) 
				left = itemElement.getOffsetLeft() + 5;				
			else
				left = itemElement.getOffsetLeft() + itemElement.getOffsetWidth() - 5;		
			
			if (useDown)
				newTop = newTop + itemElement.getOffsetHeight();
			else
				newTop = newTop - itemElement.getOffsetHeight();
	
			
			if (useDown) { 
				Element nextElement = findNextSiblingElement(itemElement, left, newTop);
				if (nextElement != null) {
					int index = getElement().getChildIndex(nextElement);				
					getSelectionModel().select(index, keepExisting); // .select(nextElement.getId(), false); //RVE maybe nextElement.getTabIndex()
					if (useFocusItem)
						focusItem(index);
					return true;
				} 
			} else {
				Element nextElement = findPreviousElement(itemElement, left, newTop);
				if (nextElement != null) {
					int index = getElement().getChildIndex(nextElement);				
					getSelectionModel().select(index, keepExisting); // .select(nextElement.getId(), false); //RVE maybe nextElement.getTabIndex()
					if (useFocusItem)
						focusItem(index);
					return true;
				}			
			}
			iSearch++;
		}
		return false;
	}
	
	protected void fireClickGroupModel(ImageResourceModelData selectedModel) {
		// TODO Show only Models with selected group
		if (selectedModel == null || !selectedModel.isGroupModel() || selectedModel.getGroupName() == null || selectedModel.getGroupName().isEmpty()
				|| selectedModel.isBreakModel())
			return;

		boolean isVisibilityChanged = false;
		for (ImageResourceModelData model : getStore().getAll()) {
			if (model.isGroupModel()) 
				continue;

			boolean isModelVisible = selectedModel.getGroupName().equals(model.getGroupName());
			isVisibilityChanged = (isVisibilityChanged) || (model.isVisible() != isModelVisible);
			model.setVisible(isModelVisible);				
		}
		
		if (isVisibilityChanged)
			refresh();
	}
	
	private Element findNextSiblingElement(Element element, int left, int top) {
		if (element == null)
			return null;
		
		Element nextElement = element.getNextSiblingElement();
		if (nextElement == null)
			return null;
		
		int offLeft = nextElement.getOffsetLeft();
		int offTop = nextElement.getOffsetTop();
		int offRight = nextElement.getOffsetLeft() + nextElement.getOffsetWidth();
		int offBottom = nextElement.getOffsetTop() + nextElement.getOffsetHeight();
		
		if (left >= offLeft && left <= offRight	 && top >= offTop && top <= offBottom)
			return nextElement;
		else
			return findNextSiblingElement(nextElement, left, top);		
	}
	
	private Element findPreviousElement(Element element, int left, int top) {
		if (element == null)
			return null;
		
		Element nextElement = element.getPreviousSiblingElement();
		if (nextElement == null)
			return null;
		
		int offLeft = nextElement.getOffsetLeft();
		int offTop = nextElement.getOffsetTop();
		int offRight = nextElement.getOffsetLeft() + nextElement.getOffsetWidth();
		int offBottom = nextElement.getOffsetTop() + nextElement.getOffsetHeight();
		
		if (left >= offLeft && left <= offRight	 && top >= offTop && top <= offBottom)
			return nextElement;
		else
			return findPreviousElement(nextElement, left, top);		
	}
	
	private void fireWindowChanged() {
		if (windowChangedTimer == null) {
			windowChangedTimer = new Timer() {
				@Override
				public void run() {
					if (thumbnailPanel.gmViewportListeners != null)
						thumbnailPanel.gmViewportListeners.forEach(listener -> listener.onWindowChanged(thumbnailPanel));
				}
			};
		}
		
		windowChangedTimer.schedule(200);
	}
	
	private void fireClickOrDoubleClick(boolean click, GmMouseInteractionEvent event) {
		if (thumbnailPanel.gmInteractionListeners == null)
			return;
		
		for (GmInteractionListener listener : thumbnailPanel.gmInteractionListeners) {
			if (click)
				listener.onClick(event);
			else
				listener.onDblClick(event);
		}
		
		if (click)
			return;
		
		ModelPath model = thumbnailPanel.getFirstSelectedItem();
		if (model == null)
			return;
		
		ModelAction action = thumbnailPanel.actionManager.getWorkWithEntityAction(thumbnailPanel);
		if (action != null) {
			action.updateState(Collections.singletonList(Collections.singletonList(model)));
			action.perform(null);
		}
	}
	
	/*
	private XElement activeElement;
	private ImageResourceModelData activeModel;
	private Timer outTimer;
	private Timer overTimer;
	
	@Override
	protected void onMouseOut(Event ce) {
		if(overTimer != null) overTimer.cancel();
		
		outTimer = new Timer() {
			
			@Override
			public void run() {
				tryDeactivate(ce);
			}
		};
		
		outTimer.schedule(250);
		super.onMouseOut(ce);
	}
	
	private void tryDeactivate(Event ce) {
		if (activeElement != null) {
			if (!ce.<XEvent>cast().within(activeElement, true)) {		  
				thumbnailPanel.getPreviewUrl(activeModel, activeModel.getEntity(), PreviewType.STANDARD, new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						getStore().update(activeModel);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						
					}
			  });			  
			  activeElement = null;
			  activeModel = null;
		  }
		}
	}
	
	@Override
	protected void onMouseOver(Event ce) {
		if(outTimer != null) outTimer.cancel();	
		
		overTimer = new Timer() {
			
			@Override
			public void run() {
				Element target = ce.getTarget().<Element>cast();
			    target = findElement(target);
			    if (target != null) {
					int index = indexOf(target);
					if (index != -1) {				
						ImageResourceModelData model = getStore().get(index);
						if(model.getEntity() != null && activeModel != model) {
							
							tryDeactivate(ce);
							activeModel = model;
							activeElement = target.<XElement>cast();
							thumbnailPanel.getPreviewUrl(model, model.getEntity(), PreviewType.ACTIVE, new AsyncCallback<Void>() {
								
								@Override
								public void onSuccess(Void result) {
									getStore().update(model);
								}
								
								@Override
								public void onFailure(Throwable caught) {
									
								}
							});
						}
					}else
						tryDeactivate(ce);
			    }else
			    	tryDeactivate(ce);
			}
		};
		overTimer.schedule(250);
		
	    super.onMouseOver(ce);
	}
	*/
}
