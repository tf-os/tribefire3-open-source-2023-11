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
package com.braintribe.gwt.gxt.gxtresources.components.client;

import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.Style.Side;
import com.sencha.gxt.core.client.util.Point;
import com.sencha.gxt.widget.core.client.event.XEvent;
import com.sencha.gxt.widget.core.client.tips.ToolTip;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

public class GmeToolTip extends ToolTip {
	
	  private Element hoverElement;
	  private boolean showOnHover = true;
	  private List<GmeToolTip> listChildTooltips = new ArrayList<>();
	  private int lastTop = 0;
	  private int lastLeft = 0;

	  public GmeToolTip(Widget parentWidget, Element hoverElement, ToolTipConfig config) {
		super(parentWidget, config);
		this.hoverElement = hoverElement;
		anchorEl.addClassName("gmeToolTipAnchor");
		getAppearance().applyAnchorDirectionStyle(anchorEl, Side.BOTTOM);		
	  }
		  	  
	  @Override
	  protected void onMouseMove(Event event) {
		    targetXY = event.<XEvent> cast().getXY();
            Point p = getTargetXY(0);
		  
		    updateToolTipPosition(p.getX(), p.getY());	    
		  
		    /*
		    targetXY = event.<XEvent> cast().getXY();
		    if (isAttached() && toolTipConfig.isTrackMouse()) {
		      Side origAnchor = toolTipConfig.getAnchor();
		      toolTipConfig.setAnchor(origAnchor);
		      if (constrainPosition) {
		        p = getElement().adjustForConstraints(p);
		      }
		      super.showAt(p.getX(), p.getY());
		    }
		    */
	  }	  
	  
	  @Override
	  protected void onTargetMouseOut(MouseOutEvent event) {
	    onTargetOut(event.getNativeEvent().<Event> cast());
	  }

	  @Override
	  protected void onTargetMouseOver(MouseOverEvent event) {
	    onTargetOver(event.getNativeEvent().<Event> cast());
	  }

	  @Override
	  protected void onTargetOut(Event ce) {
		if (hoverElement == null || !showOnHover)
			return;
		
        int mouseX = ce.getClientX();
	    int mouseY = ce.getClientY();
	    
	    boolean isLeftOk = (mouseX >= hoverElement.getAbsoluteLeft());
	    boolean isRightOk = (mouseX <= hoverElement.getAbsoluteRight());
	    boolean isTopOk = (mouseY >= hoverElement.getAbsoluteTop());
	    boolean isBottomOk = (mouseY <= hoverElement.getAbsoluteBottom());
	    
	    if (!isLeftOk || !isRightOk || !isTopOk || !isBottomOk) {
	    	hide();
	    	for (GmeToolTip childToolTip : listChildTooltips) 
	    		childToolTip.hide();
    	}		
	  }
	  
	  @Override
	  protected void onTargetOver(Event ce) {
		int mouseX = ce.getClientX();
		int mouseY = ce.getClientY();
		  
	    updateToolTipPosition(mouseX, mouseY);	    
	  }

	private void updateToolTipPosition(int mouseX, int mouseY) {
		if (disabled ) 
	      return;
	    
		if (hoverElement == null || !showOnHover)
			return;	    
	    	    
	    int iLeft = hoverElement.getAbsoluteLeft();
	    int iRight = hoverElement.getAbsoluteRight();
	    int iTop = hoverElement.getAbsoluteTop();
	    int iBottom = hoverElement.getAbsoluteBottom();
	    
	    boolean isLeftOk = (mouseX >= iLeft);
	    boolean isRightOk = (mouseX <= iRight);
	    boolean isTopOk = (mouseY >= iTop);
	    boolean isBottomOk = (mouseY <= iBottom);
	    
	    if (isLeftOk && isRightOk && isTopOk && isBottomOk) {
	    	//RVE - need get tip width and height - possible to get only when is showed...so 1st show out of the screen resolution 
	    	showAtElement(hoverElement);
	    	for (GmeToolTip childToolTip : listChildTooltips) 
	    		childToolTip.showAtElement(hoverElement);
    	}
	}

	  public void showAtHoverElement() {
		  this.showAtElement(hoverElement);
	  }
	  
	  public void showAtElement(Element element) {
		    if (element == null)
		    	element = hoverElement;
		    
		    if (element == null)
		    	return;
		  
	    	if (!isVisible()) {
	    		showAt(-100, -100);  //RVE needed to calculate OffsetWidth and OffsetHeight	    		
	    	}
	    	
	    	int ileft = element.getAbsoluteRight() - getOffsetWidth();
	    	int itop = element.getAbsoluteTop() - getOffsetHeight() - 8;	
	    	
	    	Integer offsetData = getData("offset");
	    	if (offsetData != null) {
	    		itop = itop - ((offsetData)*(getOffsetHeight()+1));
	    	}
	    	
	    	if ((Math.abs(lastLeft - ileft) > 5) || (Math.abs(lastTop - itop) > 5))	    	
	    		showAt( ileft, itop);	    			  
	  }
	  
	  @Override
	  public void showAt(int x, int y) {
		  super.showAt(x, y);
		  lastLeft = x;
		  lastTop = y;
	  }	  
	  
	  @Override
	  protected void syncAnchor() {
		    Anchor anchorPos, targetPos;
		    final int offsetX, offsetY;
		    int anchorOffset = toolTipConfig.getAnchorOffset();
		    switch (toolTipConfig.getAnchor()) {
		      case TOP:
		        anchorPos = Anchor.BOTTOM;
		        targetPos = Anchor.TOP_LEFT;
		        offsetX = 20 + anchorOffset;
		        offsetY = 2;
		        break;
		      case RIGHT:
		        anchorPos = Anchor.LEFT;
		        targetPos = Anchor.TOP_RIGHT;
		        offsetX = -2;
		        offsetY = 11 + anchorOffset;
		        break;
		      case BOTTOM:
		        anchorPos = Anchor.TOP;
		        targetPos = Anchor.BOTTOM_RIGHT;
		        //offsetX = 20 + anchorOffset;
		        offsetX = - 8 - anchorOffset;
		        offsetY = -2;
		        break;
		      default:
		        anchorPos = Anchor.RIGHT;
		        targetPos = Anchor.TOP_LEFT;
		        offsetX = 2;
		        offsetY = 11 + anchorOffset;
		        break;
		    }
		    anchorEl.alignTo(this.getElement(), new AnchorAlignment(anchorPos, targetPos, false), offsetX, offsetY);
		  }

	public boolean isShowOnHover() {
		return showOnHover;
	}

	public void setShowOnHover(boolean showOnHover) {
		this.showOnHover = showOnHover;
	}
	
	public void setHoverElement(Element hoverElement) {
		this.hoverElement = hoverElement;
	}

	public Element getHoverElement() {
		return this.hoverElement;
	}	
	
	public void addChildToolTip (GmeToolTip tooltip) {
		listChildTooltips.add(tooltip);
	}

	public void removeChildToolTip (GmeToolTip tooltip) {
		listChildTooltips.remove(tooltip);
	}
	
	public int getChildToolTipCount() {
		return listChildTooltips.size();
	}
}
