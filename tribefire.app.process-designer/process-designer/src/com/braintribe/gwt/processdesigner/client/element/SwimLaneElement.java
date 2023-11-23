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
package com.braintribe.gwt.processdesigner.client.element;

import java.util.ArrayList;
import java.util.List;

import org.vectomatic.dom.svg.OMSVGCircleElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGPoint;
import org.vectomatic.dom.svg.OMSVGRect;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTextElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.SwimLaneRepresentation;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBox;
import com.sencha.gxt.widget.core.client.Dialog;

public class SwimLaneElement extends AbstractProcessSvgElement<SwimLaneRepresentation>{
	
	public final static String RESIZE_HANDLE_POSITION_PARAMTER = "handlePosition";
	
	private SwimLaneRepresentation swimLaneRepresentation;
	
	private OMSVGGElement group;
	//private OMSVGRectElement resizingRect;
	private OMSVGCircleElement leftTopResizeHandle;
	private OMSVGCircleElement middleTopResizeHandle;
	private OMSVGCircleElement rightTopResizeHandle;
	private OMSVGCircleElement leftMiddleResizeHandle;
	private OMSVGCircleElement rightMiddleResizeHandle;
	private OMSVGCircleElement leftBottomResizeHandle;
	private OMSVGCircleElement middleBottomResizeHandle;
	private OMSVGCircleElement rightBottomResizeHandle;
	
	//private OMSVGCircleElement currentSelectedHandle;
	
	private OMSVGRectElement swimLaneElement;
	
	private Dialog textBoxDialog;
	private TextBox textBox;
	private OMSVGTextElement textElement;
	
	private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
	
	private OMSVGSVGElement svg;
	
	public SwimLaneElement(SwimLaneRepresentation swimLaneRepresentation) {
		super(swimLaneRepresentation);
		this.swimLaneRepresentation = swimLaneRepresentation;
		
		group = new OMSVGGElement();

		
		if(swimLaneElement == null){
			swimLaneElement = new OMSVGRectElement(swimLaneRepresentation.getX().floatValue(),swimLaneRepresentation.getY().floatValue(), 
					swimLaneRepresentation.getWidth().floatValue(),swimLaneRepresentation.getHeight().floatValue(), 25, 25);
			getHandlers().add(swimLaneElement.addMouseMoveHandler(this));
			getHandlers().add(swimLaneElement.addMouseDownHandler(this));
			getHandlers().add(swimLaneElement.addMouseUpHandler(this));
			getHandlers().add(swimLaneElement.addMouseOverHandler(this));
			getHandlers().add(swimLaneElement.addMouseOutHandler(this));
			getHandlers().add(swimLaneElement.addClickHandler(this));
		}
		swimLaneElement.setAttribute("style", "fill:" + swimLaneRepresentation.getColor() + ";stroke:none");
		swimLaneElement.setAttribute("opacity", "0.1");
		
		group.appendChild(swimLaneElement);
		
		if(textElement == null){
			textElement = OMSVGParser.currentDocument().createSVGTextElement();
			textElement.setAttribute("text-anchor","left");
			textElement.setAttribute("dominant-baseline","hanging");
			textElement.setAttribute("font-family", "Open Sans");
			textElement.setAttribute("font-weight", "bold");
			textElement.setAttribute("opacity", "1.0");
			textElement.setAttribute("fill", "black");
			textElement.setAttribute("font-size", "12px");
			
			handlerRegistrations.add(textElement.addClickHandler(event -> {
				textElement.setAttribute("opacity", "0");
				getTextBoxDialog(SwimLaneElement.this.swimLaneRepresentation.getText(), textElement.getBBox());
			}));
		}
		textElement.getElement().setInnerHTML(swimLaneRepresentation.getText());
		textElement.setAttribute("x", (swimLaneRepresentation.getX() + 25)+"");
		textElement.setAttribute("y", (swimLaneRepresentation.getY() + 5)+"");
		
		group.appendChild(textElement);
		
		initHandles();
	}
	
	public SwimLaneRepresentation getSwimLaneRepresentation() {
		return swimLaneRepresentation;
	}
	
	public void setSvg(OMSVGSVGElement svg) {
		this.svg = svg;
	}
	
	@Override
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
		adapt();
	}
	
	public Dialog getTextBoxDialog(String text, OMSVGRect inputRect) {
		if(textBoxDialog == null){
			textBoxDialog = new Dialog();
			textBoxDialog.setHeaderVisible(false);
			textBoxDialog.setShadow(false);
			textBoxDialog.setClosable(false);
			textBoxDialog.setDraggable(false);
			textBoxDialog.setBodyBorder(false);
			textBoxDialog.setBorders(false);
			textBoxDialog.setBodyStyle("background: transparent");
			textBoxDialog.add(getTextBox());
			textBoxDialog.setFocusWidget(getTextBox());
			textBoxDialog.setWidth("100%");
			textBoxDialog.setHeight(12);
		}
		textBox.setValue(text);
		textBoxDialog.show();
		textBox.setHeight(inputRect.getHeight()+"px");
		textBox.setWidth(inputRect.getWidth()+"px");
//		textBoxDialog.setWidth((int)inputRect.getWidth());
//		textBox.setWidth(inputRect.getWidth()+"px");
//		textBoxDialog.setHeight((int)inputRect.getHeight());
//		textBox.setHeight(inputRect.getHeight()+"px");
		
		OMSVGPoint point = svg.createSVGPoint(inputRect.getX(), inputRect.getY());
		point = point.matrixTransform(svg.getScreenCTM());
		
		textBoxDialog.setPosition((int)point.getX(),(int)point.getY());
		return textBoxDialog;
	}
	
	public TextBox getTextBox() {
		if(textBox == null){
			textBox = new TextBox();
			Style style = textBox.getElement().getStyle();
			style.setFontWeight(FontWeight.BOLD);
			style.setFontSize(12, Unit.PX);
			style.setProperty("fontFamily", "Open Sans");
			style.setProperty("background", "none");
			style.setProperty("border", "none");
			
			textBox.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
						textBox.setFocus(false);
						textBoxDialog.hide();
						textElement.setAttribute("opacity", "1");
					}
				}
			});
			textBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					swimLaneRepresentation.setText(textBox.getValue());
					textElement.getElement().setInnerText(textBox.getValue());
					textBoxDialog.hide();
					textElement.setAttribute("opacity", "1");
				}
			});
			textBox.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					textBoxDialog.hide();
					textElement.setAttribute("opacity", "1");
				}
			});
		}
		return textBox;
	}
	
	boolean suppressAdaption = false;
	
	public void adapt(){
		if(!suppressAdaption){
			swimLaneElement.setAttribute("x", swimLaneRepresentation.getX().toString());
			swimLaneElement.setAttribute("y", swimLaneRepresentation.getY().toString());
			swimLaneElement.setAttribute("width", swimLaneRepresentation.getWidth().toString());
			swimLaneElement.setAttribute("height", swimLaneRepresentation.getHeight().toString());
			
			group.setAttribute("x", swimLaneRepresentation.getX().toString());
			group.setAttribute("y", swimLaneRepresentation.getY().toString());
			group.setAttribute("width", swimLaneRepresentation.getWidth().toString());
			group.setAttribute("height", swimLaneRepresentation.getHeight().toString());
			
			textElement.setAttribute("x", (swimLaneRepresentation.getX() + 25)+"");
			textElement.setAttribute("y", (swimLaneRepresentation.getY() + 5)+"");
			textElement.getElement().setInnerText(swimLaneRepresentation.getText());
			
			swimLaneElement.setAttribute("style", "fill:" + swimLaneRepresentation.getColor() + ";stroke:none");
			
			initHandles();
		}
	}
	
	@Override
	public void commit(){
		suppressAdaption = true;
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		swimLaneRepresentation.setX(getX());
		swimLaneRepresentation.setY(getY());
		swimLaneRepresentation.setWidth(getWidth());
		swimLaneRepresentation.setHeight(getHeight());
		nestedTransaction.commit();
		suppressAdaption = false;
	}
	
	@Override
	public void setX(double x){
		group.setAttribute("x", x+"");
		swimLaneElement.setAttribute("x", x+"");
		initHandles();
		textElement.setAttribute("x", (x + 25)+"");
	}
	
	@Override
	public double getX(){
		try{
			return Double.parseDouble(group.getAttribute("x"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public double getY(){
		try{
			return Double.parseDouble(group.getAttribute("y"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public double getWidth(){
		try{
			return Double.parseDouble(group.getAttribute("width"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public double getHeight(){
		try{
			return Double.parseDouble(group.getAttribute("height"));
		}catch(Exception ex){
			return 0;
		}
	}
	
	@Override
	public void setY(double y){
		group.setAttribute("y", y+"");
		swimLaneElement.setAttribute("y", y+"");
		initHandles();
		textElement.setAttribute("y", (y + 5)+"");
	}
	
	@Override
	public void setWidth(double width){
		group.setAttribute("width", width+"");
		swimLaneElement.setAttribute("width", width+"");
		initHandles();
	}
	
	@Override
	public void setHeight(double height){
		group.setAttribute("height", height+"");
		swimLaneElement.setAttribute("height", height+"");
		initHandles();
	}
	
	public void initHandles(){
		leftTopResizeHandle = initResizeHandle(leftTopResizeHandle, getX(), getY());
		leftTopResizeHandle.setId("leftTopResizeHandle");
		leftTopResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.topLeft.name());
		leftTopResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(leftTopResizeHandle);
		
		middleTopResizeHandle = initResizeHandle(middleTopResizeHandle,  getX()+(getWidth()/2), getY());
		middleTopResizeHandle.setId("middleTopResizeHandle");
		middleTopResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.topMiddle.name());
		middleTopResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(middleTopResizeHandle);
		
		rightTopResizeHandle = initResizeHandle(rightTopResizeHandle, getX()+getWidth(), getY());
		rightTopResizeHandle.setId("rightTopResizeHandle");
		rightTopResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.topRight.name());
		rightTopResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(rightTopResizeHandle);
		
		leftMiddleResizeHandle = initResizeHandle(leftMiddleResizeHandle, getX(), getY()+(getHeight()/2));
		leftMiddleResizeHandle.setId("leftMiddleResizeHandle");
		leftMiddleResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.middleLeft.name());
		leftMiddleResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(leftMiddleResizeHandle);
		
		rightMiddleResizeHandle = initResizeHandle(rightMiddleResizeHandle, getX()+getWidth(), getY()+(getHeight()/2));
		rightMiddleResizeHandle.setId("rightMiddleResizeHandle");
		rightMiddleResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.middleRight.name());
		rightMiddleResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(rightMiddleResizeHandle);
		
		leftBottomResizeHandle = initResizeHandle(leftBottomResizeHandle, getX(), getY()+getHeight());
		leftBottomResizeHandle.setId("leftBottomResizeHandle");
		leftBottomResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.bottomLeft.name());
		leftBottomResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(leftBottomResizeHandle);
		
		middleBottomResizeHandle = initResizeHandle(middleBottomResizeHandle, getX()+(getWidth()/2), getY()+getHeight());
		middleBottomResizeHandle.setId("middleBottomResizeHandle");
		middleBottomResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.bottomMiddle.name());
		middleBottomResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(middleBottomResizeHandle);
		
		rightBottomResizeHandle = initResizeHandle(rightBottomResizeHandle, getX()+getWidth(), getY()+getHeight());
		rightBottomResizeHandle.setId("rightBottomResizeHandle");
		rightBottomResizeHandle.setAttribute(RESIZE_HANDLE_POSITION_PARAMTER, ResizeHandlePosition.bottomRight.name());
		rightBottomResizeHandle.setAttribute("opacity", selected ? "1" :  "0");
		group.appendChild(rightBottomResizeHandle);
	}
	
	@Override
	public void dispose(){
		for(HandlerRegistration handlerRegistration : handlerRegistrations){
			handlerRegistration.removeHandler();
		}
		handlerRegistrations.clear();
	}
	
	private OMSVGCircleElement initResizeHandle(OMSVGCircleElement resizeHandle, double x, double y){
		if(resizeHandle == null){
			resizeHandle = new OMSVGCircleElement((float)x,(float)y, 5);
			resizeHandle.setAttribute("style", "fill:#9ed2ff;stroke:#40a7ff;stroke-width:2");
			
			final OMSVGCircleElement finalResizeHandle = resizeHandle;
			handlerRegistrations.add(resizeHandle.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					parentEventHandler.currentFocusedResizeHandle = finalResizeHandle;
					parentEventHandler.currentFocusedSwimLaneElement = SwimLaneElement.this;
					parentEventHandler.delegateOnMouseDown(event);
				}
			}));
			handlerRegistrations.add(resizeHandle.addMouseMoveHandler(new MouseMoveHandler() {
				@Override
				public void onMouseMove(MouseMoveEvent event) {
					parentEventHandler.delegateOnMouseMove(event);	
				}
			}));
		}else{		
			resizeHandle.setAttribute("cx", x+"");
			resizeHandle.setAttribute("cy", y+"");
		}		
		return resizeHandle;
	}

	@Override
	public OMSVGGElement getRepresentation() {
		return group;
	}

	@Override
	public void setX2(double x2) {
		//NOP
	}

	@Override
	public double getX2() {
		return 0;
	}

	@Override
	public void setY2(double y2) {
		//NOP
	}

	@Override
	public double getY2() {
		return 0;
	}

	@Override
	public void handleSelectionChange() {
		//NOP
	}

	@Override
	public void handleHoveringChange() {
		//NOP
	}

	@Override
	public void handleActiveChange() {
		//NOP
	}

	@Override
	public void initialize() {
		//NOP
	}

	@Override
	public boolean doesIntersect(Rect mouseRect) {
		return false;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOP
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		parentEventHandler.delegateOnMouseDown(event);
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		//NOP
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		//NOP
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		parentEventHandler.hoverElement(this, true, event);
	}

	@Override
	public void onClick(ClickEvent event) {
		//NOP
	}
	
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setCenterX(double centerX) {
		//NOP
	}

	@Override
	public double getCenterX() {
		return 0;
	}

	@Override
	public void setCenterY(double centerY) {
		//NOP
	}

	@Override
	public double getCenterY() {
		return 0;
	}
	
	@Override
	public ElementKind getElementKind() {
		return null;
	}
	
	@Override
	public boolean canBeConnected() {
		return false;
	}
	
}
