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
package com.braintribe.gwt.modeller.client;

import java.util.Arrays;
import java.util.List;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.braintribe.gwt.modeller.client.action.GmModellerActions;
import com.braintribe.gwt.modeller.client.element.EdgeElement;
import com.braintribe.gwt.modeller.client.element.NodeElement;
import com.braintribe.gwt.modeller.client.element.PaginationElement;
import com.braintribe.model.processing.modellergraph.GmModellerMode;
import com.braintribe.model.processing.modellergraph.ModelGraphConfigurationsNew;
import com.braintribe.model.processing.modellergraph.common.Complex;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RequiresResize;

public class GmModellerPanel extends FocusPanel implements RequiresResize{
		
	private FlowPanel toolTipPanel;
	private FlowPanel mainPanel;
	private FlowPanel decorationPanel;
	
	private OMSVGDocument svgDocument;
	private OMSVGSVGElement svgPanel;	
	
	private OMSVGGElement nodeGroup;
	private OMSVGGElement edgeGroup;
	private OMSVGElement interactionGroup;
	
	private OMSVGSVGElement topSvgPanel;
	private OMSVGGElement decorationGroup;
	
	private GmModeller modeller;
	//private GmModellerActions actions;
	private ModelGraphConfigurationsNew configurations;
	
	public GmModellerPanel() {
		svgDocument = OMSVGParser.currentDocument();
		
//		add(getToolTipPanel());
		add(getMainPanel());
		
		addStyleName("gmModellerPanel");
		
		addFocusHandler(new FocusHandler() {			
			@Override
			public void onFocus(FocusEvent event) {
//				System.err.println("onFocus");
			}
		});
		
		addBlurHandler(new BlurHandler() {			
			@Override
			public void onBlur(BlurEvent event) {
//				System.err.println("onBlur");
			}
		});
		
//		addKeyDownHandler(new KeyDownHandler() {
//			
//			@Override
//			public void onKeyDown(KeyDownEvent event) {
//				if(event.isMetaKeyDown()) {
//					actions.perform(event);
//				}
//			}
//		});
		/*
		addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if(event.isMetaKeyDown()) {
					actions.perform(event);
				}
			}
		});*/
		
		/*
		new KeyNav(this) {
			@Override
			public void onKeyPress(NativeEvent evt) {
//				System.err.println("keyPress");
				if(evt.getMetaKey()) {
					actions.perform(evt);
				}				
			};
		};
		*/
	}
	
	public void setModeller(GmModeller modeller) {
		this.modeller = modeller;
	}
	
	@SuppressWarnings("unused")
	public void setActions(GmModellerActions actions) {
		//this.actions = actions;
	}
	
	public void setConfigurations(ModelGraphConfigurationsNew configurations) {
		this.configurations = configurations;
	}
	
	public void adaptSize(int size) {
		List<Style> styles = Arrays.asList(getMainPanel().getElement().getStyle(), getDecorationPanel().getElement().getStyle(), getSvgPanel().getElement().getStyle(), getTopSvgPanel().getElement().getStyle());
		styles.forEach(style -> {
			style.setWidth(size, Unit.PX);
			style.setHeight(size, Unit.PX);
		});
		
//		getEventPort().getWidth().getBaseVal().setValue(size);
//		getEventPort().getHeight().getBaseVal().setValue(size);
		
		getSvgPanel().setAttribute("height", size+"");
		getSvgPanel().setAttribute("width", size+"");
	}
	
	public FlowPanel getToolTipPanel() {
		if(toolTipPanel == null) {
			toolTipPanel = new FlowPanel();
			toolTipPanel.addStyleName("gmModellerToolTip");
		}
		return toolTipPanel;
	}
	
	public FlowPanel getMainPanel() {
		if(mainPanel == null) {
			mainPanel = new FlowPanel();
			mainPanel.addStyleName("gmModellerMainPanel");
			
			mainPanel.getElement().appendChild(getSvgPanel().getElement());
			mainPanel.add(getDecorationPanel());
			mainPanel.getElement().appendChild(getTopSvgPanel().getElement());
			mainPanel.add(getToolTipPanel());
		}
		return mainPanel;
	}
		
	public FlowPanel getDecorationPanel() {
		if(decorationPanel == null) {
			decorationPanel = new FlowPanel();
			decorationPanel.addStyleName("gmModellerDecorationPanel");
		}
		return decorationPanel;
	}
	
	public OMSVGSVGElement getSvgPanel() {
		if(svgPanel == null) {
			svgPanel = svgDocument.createSVGSVGElement();
			svgPanel.getElement().setAttribute("class", "gmModellerSvg");		
			svgPanel.appendChild(getEdgeGroup());
			svgPanel.appendChild(getInteractionGroup());
			svgPanel.appendChild(getNodeGroup());	
			
			
			svgPanel.addDomHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					System.err.println("onClick svg");
					//modeller.resetSelection();
				}
			}, ClickEvent.getType());
		}
		return svgPanel;
	}
	
	public OMSVGSVGElement getTopSvgPanel() {
		if(topSvgPanel == null) {
			topSvgPanel = svgDocument.createSVGSVGElement();
			topSvgPanel.getElement().setAttribute("class", "gmModellerTopSvg");	
			topSvgPanel.appendChild(getDecorationGroup());
		}
		return topSvgPanel;
	}
	
	public OMSVGGElement getNodeGroup() {
		if(nodeGroup == null) {
			nodeGroup = svgDocument.createSVGGElement();
			nodeGroup.setAttribute("id", "nodes");
		}
		return nodeGroup;
	}
	
	public OMSVGGElement getEdgeGroup() {
		if(edgeGroup == null) {
			edgeGroup = svgDocument.createSVGGElement();
			edgeGroup.setAttribute("id", "edges");
		}
		return edgeGroup;
	}
	
	public OMSVGGElement getDecorationGroup() {
		if(decorationGroup == null) {
			decorationGroup = svgDocument.createSVGGElement();
			decorationGroup.setAttribute("id", "decoration");
		}
		return decorationGroup;
	}
	
	public OMSVGElement getInteractionGroup() {
		if(interactionGroup == null) {
			interactionGroup = svgDocument.createSVGGElement();
			interactionGroup.setAttribute("id", "interaction");
		}
		return interactionGroup;
	}
	
	@SuppressWarnings("unused")
	public void ensureView(List<Float> radii, double x) {
		/*
		for(Float f : radii) {
			OMSVGCircleElement r = svgDocument.createSVGCircleElement();
			r.setAttribute("style", "fill:none; stroke:red; stroke-width:2");
			r.setAttribute("r", f.toString());
			r.setAttribute("cx", x+"");
			r.setAttribute("cy", x+"");
			getDecorationGroup().appendChild(r);
		}
		*/		
	}

	public void ensureNode(NodeElement nodeElement) {
		boolean visible = nodeElement.getNode().getColor().getAlpha() > 0;
		if(visible) {
			if(nodeElement.getDecoration().getParent() == null)
				getDecorationPanel().add(nodeElement.getDecoration());
//			if(nodeElement.getSvg().getCircle().getParentNode() == null)
//				getNodeGroup().appendChild(nodeElement.getSvg().getCircle());	
		}else {
			if(nodeElement.getDecoration().getParent() != null)
				getDecorationPanel().remove(nodeElement.getDecoration());
//			if(nodeElement.getSvg().getCircle().getParentNode() != null)
//				getNodeGroup().removeChild(nodeElement.getSvg().getCircle());	
		}			
	}
	
	public boolean ensureEdge(EdgeElement edgeElement) {
		boolean visible = edgeElement.getEdge().getColor().getAlpha() > 0;
		if(visible) {
			if(edgeElement.getEdge().getColor().getAlpha() == 1 && configurations.modellerMode == GmModellerMode.detailed) {
				if(edgeElement.getDecoration().getParent() == null)
					getDecorationPanel().add(edgeElement.getDecoration());
			}else {
				if(edgeElement.getDecoration().getParent() != null)
					getDecorationPanel().remove(edgeElement.getDecoration());
			}
			
			if(edgeElement.getSvg().getG().getParentNode() == null)
				getEdgeGroup().appendChild(edgeElement.getSvg().getG());
			if(edgeElement.getSvg().getStartAggregation().getParentNode() == null)
				getDecorationGroup().appendChild(edgeElement.getSvg().getStartAggregation());
			if(edgeElement.getSvg().getEndAggregation().getParentNode() == null)
				getDecorationGroup().appendChild(edgeElement.getSvg().getEndAggregation());
			if(edgeElement.getSvg().getGeneralization().getParentNode() == null)
				getDecorationGroup().appendChild(edgeElement.getSvg().getGeneralization());
		}else {
			if(edgeElement.getDecoration().getParent() != null)
				getDecorationPanel().remove(edgeElement.getDecoration());
			
			if(edgeElement.getSvg().getG().getParentNode() != null)
				getEdgeGroup().removeChild(edgeElement.getSvg().getG());
			if(edgeElement.getSvg().getStartAggregation().getParentNode() != null)
				getDecorationGroup().removeChild(edgeElement.getSvg().getStartAggregation());
			if(edgeElement.getSvg().getEndAggregation().getParentNode() != null)
				getDecorationGroup().removeChild(edgeElement.getSvg().getEndAggregation());
			if(edgeElement.getSvg().getGeneralization().getParentNode() != null)
				getDecorationGroup().removeChild(edgeElement.getSvg().getGeneralization());
		}
		return visible;
	}
	
	public void showTooltip(String tooltip) {
		getToolTipPanel().getElement().setInnerText(tooltip);
	}
	
	public void hideTooltip() {
		getToolTipPanel().getElement().setInnerText("");
	}
	
	public void show(PaginationElement pe) {
		if(pe.getParentNode() == null)
			getInteractionGroup().appendChild(pe);
	}
	
	public void hide(PaginationElement pe) {
		if(pe.getParentNode() != null)
			getInteractionGroup().removeChild(pe);
	}
	
	@Override
	public void onResize() {
		//int width = getParent().getOffsetWidth();
		int height = getParent().getOffsetHeight();
		configurations.setViewPortDimension(new Complex(height, height));
		adaptSize(height);
		modeller.rerender();
	}
	
	@Override
	public void clear() {
		getEdgeGroup().getElement().setInnerHTML("");
		getInteractionGroup().getElement().setInnerHTML("");
		getNodeGroup().getElement().setInnerHTML("");
		getDecorationPanel().clear();
		getDecorationGroup().getElement().setInnerHTML("");
		getToolTipPanel().clear();
	}

}
