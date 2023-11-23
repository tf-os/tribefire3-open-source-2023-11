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
package com.braintribe.gwt.processdesigner.client.action;

import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGImageElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.ImageResource;

public abstract class ProcessDesignerActionMenuElement extends OMSVGGElement implements ManipulationListener, GmSelectionListener, ClickHandler, MouseOutHandler, MouseOverHandler, MouseMoveHandler, MouseDownHandler, MouseUpHandler{
	
	protected int width = 70;
	protected int height = 65;
	private OMSVGRectElement rect;
	private ImageResource icon;
	private OMSVGImageElement iconElement;
	private String name;
	//private String tooltip;
	private OMSVGTextElement text;
	
	private String mouseDownStyle = "fill:white;stroke:#bebebe;stroke-width:1";
	private String mouseOverStyle = "fill:white;stroke:#cdcdcd;stroke-width:1";
	private String defaultStyle = "fill:white;stroke:#e3e3e3;stroke-width:0";
	private String activeStyle = "fill:#ffc97d;stroke:#e3e3e3;stroke-width:0";
	
	private boolean useIcon = true;
	private boolean enabled = true;
	private boolean active = false;
	
	public ProcessDesignerActionMenuElement() {		
		
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
		addDomHandler(this, MouseMoveEvent.getType());
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		
		if(enabled)
			setAttribute("cursor", "pointer");
		else
			removeAttribute("cursor");
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	/*public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}*/
	
	public void setIcon(ImageResource icon) {
		this.icon = icon;
	}
	
	public ImageResource getIcon() {
		return icon;
	}
	
	public void init(){
		getRect().setAttribute("x", getX()+"");
		getRect().setAttribute("y", getY()+"");
		
		if(useIcon){
			getIconElement().getHref().setBaseVal(icon.getSafeUri().asString()); //setAttribute("href", action.getIcon().getSafeUri().asString());
			getIconElement().setAttribute("width", icon.getWidth()+"");
			getIconElement().setAttribute("height", icon.getHeight()+"");
			getIconElement().setAttribute("x",((getX()+(width/2)) - (icon.getWidth() / 2))+"");
			getIconElement().setAttribute("y",((getY()+(height/2)) - (icon.getHeight() / 2))+"");
		}
		
		getText().setAttribute("x", (getX()+width/2)+"");
		getText().setAttribute("y", (getY()+height)-5+"");
		getText().getElement().setInnerText(name);
		
		appendChild(getRect());
		if(useIcon)
			appendChild(getIconElement());
		else
			appendChild(prepareIconElement());
		appendChild(getText());
		configure();
	}
	
	public OMSVGRectElement getRect() {
		if(rect == null){
			rect = new OMSVGRectElement(0, 0, width, height, 0, 0);
			rect.setAttribute("style", defaultStyle);
		}
		return rect;
	}
	
	public OMSVGImageElement getIconElement() {
		if(iconElement == null){
			iconElement = new OMSVGImageElement(0, 0, 0, 0, "");
			iconElement.setAttribute("style", "opacity:1.0");
		}
		return iconElement;
	}
	
	public OMSVGTextElement getText() {
		if(text == null){
			text = OMSVGParser.currentDocument().createSVGTextElement();
			text.setAttribute("text-anchor","middle");
			text.setAttribute("dominant-baseline","middle");
			text.setAttribute("font-family", "Open Sans");
//			text.setAttribute("font-weight", "bold");
			text.setAttribute("opacity", "1.0");
			text.setAttribute("fill", "black");
			text.setAttribute("font-size", "11px");	
		}
		return text;
	}
	
	public double getX(){
		try{
			return Double.parseDouble(getAttribute("x"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	public double getY(){
		try{
			return Double.parseDouble(getAttribute("y"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if(enabled)
			perform();
	}
	
	@Override
	public void onMouseOut(MouseOutEvent event) {
		if(enabled){
			if(active)
				getRect().setAttribute("style", activeStyle);
			else
				getRect().setAttribute("style", defaultStyle);
		}
	}
	
	@Override
	public void onMouseOver(MouseOverEvent event) {
		if(enabled){
			if(active)
				getRect().setAttribute("style", activeStyle);
			else
				getRect().setAttribute("style", mouseOverStyle);
		}
	}
	
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if(enabled){
			if(active)
				getRect().setAttribute("style", activeStyle);
			else
				getRect().setAttribute("style", mouseOverStyle);
		}
	}
	
	@Override
	public void onMouseDown(MouseDownEvent event) {
		if(enabled){
			if(active)
				getRect().setAttribute("style", activeStyle);
			else
				getRect().setAttribute("style", mouseDownStyle);
		}
	}
	
	@Override
	public void onMouseUp(MouseUpEvent event) {
		if(enabled){
			if(active)
				getRect().setAttribute("style", activeStyle);
			else
				getRect().setAttribute("style", mouseOverStyle);
		}
	}
	
	public void setActive(boolean active) {
		this.active = active;
		if(enabled){
			if(active)
				getRect().setAttribute("style", activeStyle);
			else
				getRect().setAttribute("style", defaultStyle);
		}		
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		if(!enabled){
			setAttribute("opacity", "0.1");
			removeAttribute("cursor");
		}
		else{
			setAttribute("opacity",  "1");
			setAttribute("cursor",  "pointer");
		}
	}
	
	public void setUseIcon(boolean useIcon) {
		this.useIcon = useIcon;
	}
	
	public void dispose(){
		handleDipose();
	}
	
	public abstract OMSVGGElement prepareIconElement();
	
	public abstract void handleDipose();
	
	public abstract void configure();
	
	public abstract void perform();
	
}
