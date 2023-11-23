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
package com.braintribe.gwt.modeller.client.element;

import java.util.function.Supplier;

import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.GmModellerRenderer;
import com.braintribe.gwt.modeller.client.GmModellerTypeSource;
import com.braintribe.gwt.modeller.client.resources.ModellerModuleResources;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.graphics.Node;
import com.braintribe.model.processing.modellergraph.GmModellerMode;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.ui.FlowPanel;

public class NodeElement implements DragStartHandler, DragEnterHandler, DragOverHandler, DragLeaveHandler, DragHandler, DropHandler, DragEndHandler,
TouchStartHandler, TouchEndHandler, TouchMoveHandler, TouchCancelHandler,
MouseDownHandler, MouseUpHandler, MouseOverHandler, MouseOutHandler, ClickHandler, DoubleClickHandler,
ManipulationListener{
	
	private GmModeller modeller;
	private GmModellerRenderer renderer;
//	private GmModellerTypeSource typeSource;
//	private NodeSvg svg;
	private NodeDecoration decoration;
	private Node node;
//	private boolean selected = false;
	private ModelGraphConfigurationsNew config;
	Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	ImageElement img = Document.get().createImageElement();	
		
	@SuppressWarnings("unused")
	public NodeElement(Node node) {
		img.getStyle().setOpacity(0);
		img.getStyle().setDisplay(Display.NONE);
		getDecoration().getElement().appendChild(img);
		ensureHandlers();
		//adapt(node);
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
		getDecoration().setQuickAccessPanelProvider(quickAccessPanelProvider);
	}
	
	public void setConfig(ModelGraphConfigurationsNew config) {
		this.config = config;
		getDecoration().setConfig(config);
	}
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	public void setRenderer(GmModellerRenderer renderer) {
		this.renderer = renderer;
		getDecoration().setRenderer(renderer);
	}
	
	public void setTypeSource(GmModellerTypeSource typeSource) {
//		this.typeSource = typeSource;
		getDecoration().setTypeSource(typeSource);
	}
	
	public Node getNode() {
		return node;
	}
	
	public void adapt(Node node) {
		this.node = node;
//		getSvg().adapt(node);
		getDecoration().adapt(node);
	}
	
//	public NodeSvg getSvg() {
//		if(svg == null) {
//			svg = new NodeSvg();
//		}
//		return svg;
//	}
	
	public NodeDecoration getDecoration() {
		if(decoration == null) {
			decoration = new NodeDecoration();
//			decoration.setTypeSource(typeSource);
		}
		return decoration;
	}
	
	private void ensureHandlers() {
		getDecoration().ensureDragHandlers(this);
		getDecoration().ensureMouseHandlers(this);
	}
	
	public void reset() {
//		svg.reset();
		decoration.reset();
	}
	
	public void downplay() {
//		svg.downplay();
		decoration.downplay();
	}

	private void over() {
//		svg.over();
		decoration.over();
	}
	
	private void out() {
//		svg.out();
		decoration.out();
	}
	
	FlowPanel parent;
	
	public void show() {
		if(node.getColor().getAlpha() > 0) {
			if(parent != null)
				parent.add(decoration);
			out();
		}
	}
	
	public void hide() {
		parent = (FlowPanel) decoration.getParent();
		decoration.removeFromParent();
	}

	@Override
	public void onDrop(DropEvent event) {
		event.preventDefault();
//		String typeSignature = event.getData("Text");
		renderer.hideConnector();
		System.err.println("onDrop " + node.getTypeSignature());		
	}
	
	@Override
	public void onDragStart(DragStartEvent event) {
		GmType type = null;
		if(node.getTypeSignature().endsWith("---")) {
			String typeSig = node.getTypeSignature().substring(0, node.getTypeSignature().indexOf("---"));
			type = modeller.getType(typeSig);
		}
		else
			type = modeller.getType(node.getTypeSignature());
		if(type != null) {
			if(type.getDeclaringModel() == modeller.getModel()) {
				Element img = Document.get().createElement("img");
				renderer.setConnectionPossible(false);
				img.setAttribute("src", ModellerModuleResources.INSTANCE.blank().getSafeUri().asString());
				img.getStyle().setOpacity(0);
				event.setData("text/plain",node.getTypeSignature());
				event.getDataTransfer().setDragImage(img, 0, 0);		
				renderer.setConnectionSource(this);
				renderer.setConnectionTarget(null);
			}else {
				renderer.setConnectionPossible(false);
				renderer.setConnectionSource(null);
				renderer.setConnectionTarget(null);
				event.preventDefault();
			}
		}
	}

	@Override
	public void onDrag(DragEvent event) {
//		System.err.println("onDrag " + node.getTypeSignature());		
	}
	
	@Override
	public void onDragEnd(DragEndEvent event) {
//		System.err.println("onDragEnd " + node.getTypeSignature());
		renderer.onDragEnd(event);
	}
	
	@Override
	public void onDragEnter(DragEnterEvent event) {
//		System.err.println("onDragEnter " + node.getTypeSignature());
		renderer.setConnectionTarget(this);
		renderer.showConnectionChoices();
		over();
	}

	@Override
	public void onDragLeave(DragLeaveEvent event) {
//		System.err.println("onDragLeave " + node.getTypeSignature());
		GmType type = null;
		if(node.getTypeSignature().endsWith("---")) {
			String typeSig = node.getTypeSignature().substring(0, node.getTypeSignature().indexOf("---"));
			type = modeller.getType(typeSig);
		}else
			type = modeller.getType(node.getTypeSignature());
		renderer.setConnectionPossible(type.getDeclaringModel() == modeller.getModel());
		out();
	}

	@Override
	public void onDragOver(DragOverEvent event) {
//		System.err.println("onDragOver " + node.getTypeSignature());
		renderer.setConnectionTarget(this);
		over();
	}	
	
	@Override
	public void onTouchCancel(TouchCancelEvent event) {
//		System.err.println("onTouchCancel " + node.getTypeSignature());
	}

	@Override
	public void onTouchMove(TouchMoveEvent event) {
//		System.err.println("onTouchMove " + node.getTypeSignature());
	}

	@Override
	public void onTouchEnd(TouchEndEvent event) {
		System.err.println("onTouchEnd " + node.getTypeSignature());
	}

	@Override
	public void onTouchStart(TouchStartEvent event) {
		System.err.println("onTouchStart " + node.getTypeSignature());
	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
//		System.err.println("onDoubleClick " + node.getTypeSignature());
	}

	@Override
	public void onClick(ClickEvent event) {
		boolean fireDeferred = false;
		Object s = event.getSource();
		if(s == decoration.getTypeName()) {
			if((config.modellerMode == GmModellerMode.condensed && !config.currentFocusedType.equals(node.getTypeSignature())) || config.modellerMode == GmModellerMode.detailed) {
				System.err.println("focus");
				modeller.focus(node.getTypeSignature(), true);
				event.stopPropagation();
				fireDeferred = true;
			}
		}
//		else {
//			modeller.select(node.getTypeSignature());
//		}
		select();
		renderer.deselectNodes(this);
		renderer.showDetail(this, fireDeferred);
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
//		System.err.println("onMouseOut " + node.getTypeSignature());
		out();
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
//		System.err.println("onMouseOver " + node.getTypeSignature());
		over();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		System.err.println("onMouseUp " + node.getTypeSignature());
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
//		System.err.println("onMouseDown " + node.getTypeSignature());
	}
	
	public void select() {
		getDecoration().select();
	}
	
	public void deselect() {
		getDecoration().deselect();
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		//adapt(node);
	}

}
