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

import com.braintribe.gwt.geom.client.Rect;
import com.braintribe.gwt.processdesigner.client.ProcessDesigner;
import com.braintribe.gwt.processdesigner.client.ProcessDesignerMode;
import com.braintribe.gwt.processdesigner.client.event.EdgeKindChoice.ElementKind;
import com.braintribe.gwt.processdesigner.client.resources.ProcessDesignerResources;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processdefrep.NodeRepresentation;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

import tribefire.extension.process.model.deployment.Node;
import tribefire.extension.process.model.deployment.RestartNode;
import tribefire.extension.process.model.deployment.StandardNode;

public class NodeDivElement extends AbstractProcessSvgElement<NodeRepresentation>{

	FlowPanel wrapper;
	FlowPanel name;
	FlowPanel status;
	FlowPanel graceTimePeriod;
	Image icon;
	Image restartIcon;
	Image endIcon;
	
	private DecoupledInteractionElement decoupledInteractionElement;
	private ProcessDesigner designer;
	
	double startX = 0, startY = 0;
	
	public NodeDivElement(NodeRepresentation genericEntity) {
		super(genericEntity);		
	}
	
	public void setDecoupledInteractionElement(DecoupledInteractionElement decoupledInteractionElement) {
		this.decoupledInteractionElement = decoupledInteractionElement;
	}
	
	public void inject() {
		resizable(getWrapper().getElement().getAttribute("id"), this);
		draggable(getWrapper().getElement().getAttribute("id"), this);
	}
	
	public native void resizable(String id, Object that) /*-{
		var el = $wnd.$("#" + id);
		if(el.resizable){
			el.resizable({
				handles: "n, e, s, w, ne, se, sw, nw",
				containment: "parent", minWidth: 50, minHeight: 50,
				start: function(e,u){
					that.@com.braintribe.gwt.processdesigner.client.element.NodeDivElement::startResize(Ljava/lang/String;)('resize');
				},
				resize: function(e,u){
					that.@com.braintribe.gwt.processdesigner.client.element.NodeDivElement::resize(Ljava/lang/String;)('drag');
				},
				stop: function(e,u){
					that.@com.braintribe.gwt.processdesigner.client.element.NodeDivElement::stopResize(Ljava/lang/String;)('resize');
				} 
			});
		}
	}-*/;
	
	
	public native void draggable(String id, Object that) /*-{
		var el = $wnd.$("#" + id);
		if(el.draggable){
			el.draggable({
				start: function(e,u){
					that.@com.braintribe.gwt.processdesigner.client.element.NodeDivElement::startDrag(Ljava/lang/String;)('startDrag');
				},
				drag: function(e,u){
					that.@com.braintribe.gwt.processdesigner.client.element.NodeDivElement::drag(Ljava/lang/Object;Ljava/lang/Object;)(u.top, u.left);
				},
				stop: function(e,u){
					that.@com.braintribe.gwt.processdesigner.client.element.NodeDivElement::stopDrag(Ljava/lang/String;)('drag');
				}  			
			});
		}
	}-*/;
	
	public native void enableDrag(String id) /*-{
		var el = $wnd.$("#" + id);
		if(el.draggable){
			el.draggable('enable')			
		}
	}-*/;

	public native void enableResize(String id) /*-{
		var el = $wnd.$("#" + id);
		if(el.resizable){
			el.resizable('enable')			
		}
	}-*/;

	public native void disableDrag(String id) /*-{
		var el = $wnd.$("#" + id);
		if(el.draggable){
			el.draggable('disable')			
		}
	}-*/;
	
	public native void disableResize(String id) /*-{
		var el = $wnd.$("#" + id);
		if(el.resizable){
			el.resizable('disable')			
		}
	}-*/;
	
	@SuppressWarnings("unused")
	public void startResize(String method) {
		if(designer.getProcessDesignerConfiguration().getProcessDesignerMode() == ProcessDesignerMode.selecting) {
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().selectElement(this, false, true);
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().setIdle(true);
		}
	}
	
	@SuppressWarnings("unused")
	public void resize(String method) {
		parentEventHandler.renderRelatedElements(this);
	}
	
	@SuppressWarnings("unused")
	public void stopResize(String method) {		
		designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().setIdle(false);
		commit();
	}
	
	@SuppressWarnings("unused")
	public void startDrag(String method) {
		startY = getWrapper().getAbsoluteTop();
		startX = getWrapper().getAbsoluteLeft();
		if(designer.getProcessDesignerConfiguration().getProcessDesignerMode() == ProcessDesignerMode.selecting) {
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().selectElement(this, false, true);
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().setIdle(true);
		}
	}
	
	@SuppressWarnings("unused")
	public void drag(Object top, Object left) {
		parentEventHandler.renderRelatedElements(this);
		//parentEventHandler.renderSelection(getWrapper().getAbsoluteLeft() - startX, getWrapper().getAbsoluteTop() - startY);
	}
	
	@SuppressWarnings("unused")
	public void stopDrag(String method) {
		designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().setIdle(false);
		commit();
	}
	
	public FlowPanel getWrapper() {
		if(wrapper == null) {
			wrapper = new FlowPanel();
			wrapper.addStyleName("nodeElement");
			Style style = wrapper.getElement().getStyle();
			style.setPosition(Position.ABSOLUTE);
			
			wrapper.addDomHandler(this, MouseOverEvent.getType());
			wrapper.addDomHandler(this, MouseOutEvent.getType());
			wrapper.addDomHandler(this, MouseDownEvent.getType());
			wrapper.addDomHandler(this, MouseMoveEvent.getType());
			wrapper.addDomHandler(this, MouseUpEvent.getType());
			wrapper.addDomHandler(this, ClickEvent.getType());
			
			wrapper.add(getIcon());
			wrapper.add(getName());
			wrapper.add(getStatus());
			wrapper.add(getGraceTimePeriod());
			
		}
		return wrapper;
	}
	
	public FlowPanel getName() {
		if(name == null) {
			name = new FlowPanel();
			name.addStyleName("nodeElement-name");
		}
		return name;
	}
	
	public FlowPanel getStatus() {
		if(status == null) {
			status = new FlowPanel();
			status.addStyleName("nodeElement-status");
		}
		return status;
	}
	
	public FlowPanel getGraceTimePeriod() {
		if(graceTimePeriod == null) {
			graceTimePeriod = new FlowPanel();
			graceTimePeriod.addStyleName("nodeElement-graceTimePeriod");
		}
		return graceTimePeriod;
	}
	
	public Image getIcon() {
		if(icon == null) {
			icon = new Image();
			icon.addStyleName("nodeElement-icon");
		}
		return icon;
	}

	@Override
	public FlowPanel getRepresentation() {
		return getWrapper();
	}

	@Override
	public void setX(double x) {
		Style style = getWrapper().getElement().getStyle();
		style.setLeft(x, Unit.PX);
		if(decoupledInteractionElement != null)
			decoupledInteractionElement.setX(getX() + getWidth() + 15);
	}

	@Override
	public double getX() {
		Style style = getWrapper().getElement().getStyle();
		return Double.parseDouble(style.getLeft().substring(0,style.getLeft().length()-2));
	}

	@Override
	public void setY(double y) {
		Style style = getWrapper().getElement().getStyle();
		style.setTop(y, Unit.PX);
		if(decoupledInteractionElement != null)
			decoupledInteractionElement.setY(getY() - 15);
	}

	@Override
	public double getY() {
		Style style = getWrapper().getElement().getStyle();
		return Double.parseDouble(style.getTop().substring(0,style.getTop().length()-2));
	}

	@Override
	public void setX2(double x2) {
		//NOP
	}

	@Override
	public double getX2() {
		return getX();
	}

	@Override
	public void setY2(double y2) {
		//NOP
	}

	@Override
	public double getY2() {
		return getY();
	}

	@Override
	public void setCenterX(double centerX) {
		//NOP
	}

	@Override
	public double getCenterX() {
		return getX()+(getWidth()/2);
	}

	@Override
	public void setCenterY(double centerY) {
		//NOP
	}

	@Override
	public double getCenterY() {
		return getY()+(getHeight()/2);
	}

	@Override
	public void setWidth(double width) {
		Style style = getWrapper().getElement().getStyle();
		style.setWidth(width, Unit.PX);
	}

	@Override
	public double getWidth() {
		Style style = getWrapper().getElement().getStyle();
		return Double.parseDouble(style.getWidth().substring(0,style.getWidth().length()-2));
	}

	@Override
	public void setHeight(double height) {
		Style style = getWrapper().getElement().getStyle();
		style.setHeight(height, Unit.PX);
	}

	@Override
	public double getHeight() {
		Style style = getWrapper().getElement().getStyle();
		return Double.parseDouble(style.getHeight().substring(0,style.getHeight().length()-2));
	}

	@Override
	public void handleSelectionChange() {
		if(getSelected()) {
			getWrapper().removeStyleName("hovered");
			getWrapper().addStyleName("selected");
		}else {
			getWrapper().removeStyleName("selected");
		}
	}

	@Override
	public void handleHoveringChange() {
		if(!getSelected()) {
			if(getHovered()) {
				getWrapper().addStyleName("hovered");
			}else {
				getWrapper().removeStyleName("hovered");
			}
		}else
			getWrapper().removeStyleName("hovered");
	}

	@Override
	public void handleActiveChange() {
		//NOOP
	}

	@Override
	public void initialize() {
		
		NodeRepresentation nodeRepresentation = (NodeRepresentation)getEntity();
		Node node2 = nodeRepresentation.getNode();
		boolean isDrainNode = false;
		
		if(node2 != null){
			
			if(node2.getName() != null)
				getName().getElement().setInnerText(node2.getName().value());
			
			if(node2.getState() != null){
				getStatus().getElement().setInnerText("(" + node2.getState().toString() + ")");
			}else{
				getIcon().setUrl(ProcessDesignerResources.INSTANCE.init().getSafeUri().asString());
			}
			
			if(node2 instanceof StandardNode){
				TimeSpan gracePeriod = ((StandardNode) node2).getGracePeriod();
				if(gracePeriod != null){
					String gp = gracePeriod.getValue() + getTimeUnit(gracePeriod.getUnit());
					getGraceTimePeriod().getElement().setInnerText(gp);
				}
				
				isDrainNode = designer.isDrainNode(node2);
				if(isDrainNode){
					getIcon().setUrl(ProcessDesignerResources.INSTANCE.end().getSafeUri().asString());
				}
			}
			
			if(node2 instanceof RestartNode){
				getIcon().setUrl(ProcessDesignerResources.INSTANCE.restart().getSafeUri().asString());
			}	
		}else{
			getIcon().setUrl(ProcessDesignerResources.INSTANCE.init().getSafeUri().asString());
		}
		
		getIcon().setVisible(!(getIcon().getUrl().isEmpty() || getIcon().getUrl() == null));
	}
	
	private String getTimeUnit(TimeUnit timeUnit){
		if(timeUnit != null){
			switch (timeUnit) {
			case day:
				return "day";
			case hour:
				return "hr";
			case microSecond:
				return "μs";
			case milliSecond:
				return "ms";
			case minute:
				return "min";
			case nanoSecond:
				return "ns";
			case planckTime:
				return "pt";
			case second:
				return "sec";
			default:
				return "sec";
			}
		}else
			return "";
	}

	@Override
	public boolean doesIntersect(Rect mouseRect) {		
		return new Rect(getX(), getY(), getWidth(), getHeight()).intersect(mouseRect) != null;
	}

	@Override
	public void commit() {
		NestedTransaction nestedTransaction = session.getTransaction().beginNestedTransaction();
		((NodeRepresentation)getEntity()).setX(getX());
		((NodeRepresentation)getEntity()).setY(getY());
		((NodeRepresentation)getEntity()).setWidth(getWidth());
		((NodeRepresentation)getEntity()).setHeight(getHeight());
		nestedTransaction.commit();
	}

	@Override
	public String getDescription() {
		NodeRepresentation nodeRepresentation = (NodeRepresentation)getEntity();
		Node node = nodeRepresentation.getNode();
		return "node " + (node.getState() != null ? node.getState().toString() : "INIT");
	}

	@Override
	public ElementKind getElementKind() {
		NodeRepresentation nodeRepresentation = genericEntity;
		
		Node node2 = nodeRepresentation.getNode();
		if(node2 instanceof StandardNode)
			return ElementKind.standardNode;
		else if(node2 instanceof RestartNode)
			return ElementKind.restartNode;
		
		return null;
	}

	@Override
	public boolean canBeConnected() {
		return true;
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//NOOP
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		designer.getProcessDesignerRenderer().focus();
		if(designer.getProcessDesignerConfiguration().getProcessDesignerMode() == ProcessDesignerMode.connecting) {
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().onMouseDown(event);
		}
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		//NOOP
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		designer.getProcessDesignerRenderer().focus();
		if(designer.getProcessDesignerConfiguration().getProcessDesignerMode() == ProcessDesignerMode.connecting) {
			if(designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().currentFocusedElement != null && designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().currentFocusedElement != this) {
				designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().onMouseMove(event);
			}
		}
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		designer.getProcessDesignerRenderer().focus();
		designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().hoverElement(this, true, event);
		if(designer.getProcessDesignerConfiguration().getProcessDesignerMode() == ProcessDesignerMode.connecting) {
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().onMouseOver(event);
		}
	}
	
	@Override
	public void onMouseOut(MouseOutEvent event) {
		//NOOP
	}

	@Override
	public void onClick(ClickEvent event) {
		if(!getSelected())
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().selectElement(this, event.isShiftKeyDown(), true);
		else
			designer.getProcessDesignerRenderer().getProcessDefintionElementEventHandler().deselectElement(this, true);		
	}
	
	public void enableInteractions() {
		enableResize(getWrapper().getElement().getAttribute("id"));
		enableDrag(getWrapper().getElement().getAttribute("id"));		
	}
	
	public void disableInteractions() {
		disableDrag(getWrapper().getElement().getAttribute("id"));
		disableResize(getWrapper().getElement().getAttribute("id"));
	}

	public void setDesigner(ProcessDesigner designer) {
		this.designer = designer;
	}

}
