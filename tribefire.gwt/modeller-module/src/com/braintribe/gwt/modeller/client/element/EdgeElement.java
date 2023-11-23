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

import com.braintribe.gwt.gme.propertypanel.client.field.QuickAccessDialog;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.modeller.client.GmModeller;
import com.braintribe.gwt.modeller.client.GmModellerRenderer;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellergraph.graphics.AggregationKind;
import com.braintribe.model.modellergraph.graphics.Edge;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
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
import com.google.gwt.user.client.Timer;

public class EdgeElement implements MouseMoveHandler, MouseDownHandler, MouseUpHandler, MouseOverHandler, MouseOutHandler, ClickHandler, DoubleClickHandler, ManipulationListener{

	private GmModeller modeller;
	private GmModellerRenderer renderer;
	//private ModelGraphConfigurationsNew config;
	//private PersistenceGmSession session;
	
	private EdgeSvg svg;
	private EdgeDecoration decoration;
	private Edge edge;
	
	private NodeElement fromNodeElement;
	private NodeElement toNodeElement;
	
	GmMetaModel model;
	QuickAccessDialog quickAccessDialog;
	Supplier<SpotlightPanel> quickAccessPanelProvider;
	
	Timer hightlightRequest;
	
	public EdgeElement(Edge edge, boolean inverse) {
		ensureHandlers();
		adapt(edge, inverse);
	}
	
	public void setSession(PersistenceGmSession session) {
		//this.session = session;
		getDecoration().setSession(session);
	}
	
	public void setConfig(ModelGraphConfigurationsNew config) {
		//this.config = config;
		getDecoration().setConfig(config);
	}
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
		getDecoration().setModeller(modeller);
	}
	
	public void setRenderer(GmModellerRenderer renderer) {
		this.renderer = renderer;
	}
	
	public void setQuickAccessPanelProvider(Supplier<SpotlightPanel> quickAccessPanelProvider) {
		this.quickAccessPanelProvider = quickAccessPanelProvider;
		getDecoration().setQuickAccessPanelProvider(quickAccessPanelProvider);
	}
	
	public void setModel(GmMetaModel model) {
		this.model = model;
		getDecoration().setModel(model);
	}
	
	public void setFromNodeElement(NodeElement fromNodeElement) {
		this.fromNodeElement = fromNodeElement;
	}
	
	public NodeElement getFromNodeElement() {
		return fromNodeElement;
	}
	
	public void setToNodeElement(NodeElement toNodeElement) {
		this.toNodeElement = toNodeElement;
	}
	
	public NodeElement getToNodeElement() {
		return toNodeElement;
	}
	
	public Edge getEdge() {
		return edge;
	}
	
	public EdgeSvg getSvg() {
		if(svg == null) {
			svg = new EdgeSvg();
		}
		return svg;
	}
	
	public EdgeDecoration getDecoration() { 
		if(decoration == null) {
			decoration = new EdgeDecoration();
		}
		return decoration;
	}
	
	public void adapt(Edge edge, boolean inverse) {
		this.edge = edge;
		getSvg().adapt(edge, inverse);
		if(fromNodeElement != null && toNodeElement != null)
			getDecoration().adapt(fromNodeElement, toNodeElement, edge);
	}	
	
	private void ensureHandlers() {
		getSvg().ensureMouseHandlers(this);
//		getDecoration().ensureMouseHandlers(this);
	}
	
	public void over() {
		if(hightlightRequest == null) {
			hightlightRequest = new Timer() {				
				@Override
				public void run() {
					renderer.highlightRelation(EdgeElement.this);
				}
			};
			hightlightRequest.schedule(250);
		}		
//		svg.over();
		if(this.edge.getStartAggregationKind() == AggregationKind.ordered_aggregation) {
			renderer.showTooltip("list of " + this.edge.getToNode().getText());
		}else if(this.edge.getStartAggregationKind() == AggregationKind.unordered_aggregation) {
			renderer.showTooltip("set of " + this.edge.getToNode().getText());
		}else if(this.edge.getEndAggregationKind() == AggregationKind.ordered_aggregation) {
			renderer.showTooltip("list of " + this.edge.getFromNode().getText());
		}else if(this.edge.getEndAggregationKind() == AggregationKind.unordered_aggregation) {
			renderer.showTooltip("set of " + this.edge.getFromNode().getText());
		}
		
	}

	public void out() {
		if(hightlightRequest != null) {
			hightlightRequest.cancel();
			hightlightRequest = null;
		}
		renderer.resetRelations();
		renderer.hideTooltip();
//		svg.out();
	}
	
	public void downplay() {
		svg.downplay();
		if(fromNodeElement != null)
			fromNodeElement.downplay();
		if(toNodeElement != null)
			toNodeElement.downplay();
	}
	
	public void reset() {
		svg.reset();
		if(fromNodeElement != null)
			fromNodeElement.reset();
		if(toNodeElement != null)
			toNodeElement.reset();
	}

	@Override
	public void onDoubleClick(DoubleClickEvent event) {
		//NOP
	}

	@Override
	public void onClick(ClickEvent event) {
		System.err.println("showDetail");
		modeller.detail(edge, true);
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		out();
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		over();
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		//NOP
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		//NOP
	}
	
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		over();
	}
	
	@Override
	public void noticeManipulation(Manipulation manipulation) {
		adapt(edge, false);
	}
	
}
